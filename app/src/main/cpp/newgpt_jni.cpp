#include "llama.h"
#include <jni.h>
#include <algorithm>
#include <string>
#include <vector>

struct NativeContext {
    llama_model* model = nullptr;
    llama_context* context = nullptr;
};

static void notifyToken(JNIEnv* env, jobject callback, const std::string& token) {
    if (token.empty()) return;
    jclass clazz = env->GetObjectClass(callback);
    jmethodID method = env->GetMethodID(clazz, "onToken", "(Ljava/lang/String;)V");
    if (!method) return;
    jstring value = env->NewStringUTF(token.c_str());
    env->CallVoidMethod(callback, method, value);
    env->DeleteLocalRef(value);
    env->DeleteLocalRef(clazz);
}

static void notifyComplete(JNIEnv* env, jobject callback) {
    jclass clazz = env->GetObjectClass(callback);
    jmethodID method = env->GetMethodID(clazz, "onComplete", "()V");
    if (method) env->CallVoidMethod(callback, method);
    env->DeleteLocalRef(clazz);
}

extern "C" JNIEXPORT jlong JNICALL
Java_com_mojealterego_newgpt_data_local_gguf_GgufNativeEngine_loadModelNative(
        JNIEnv* env, jobject, jstring modelPath) {
    if (!modelPath) return 0;

    const char* path = env->GetStringUTFChars(modelPath, nullptr);
    if (!path) return 0;

    ggml_backend_load_all();

    llama_model_params modelParams = llama_model_default_params();
    modelParams.n_gpu_layers = 0;

    llama_model* model = llama_model_load_from_file(path, modelParams);
    env->ReleaseStringUTFChars(modelPath, path);

    if (!model) return 0;

    auto* native = new NativeContext();
    native->model = model;
    return reinterpret_cast<jlong>(native);
}

extern "C" JNIEXPORT void JNICALL
Java_com_mojealterego_newgpt_data_local_gguf_GgufNativeEngine_generateNative(
        JNIEnv* env, jobject, jlong contextPtr, jstring prompt, jobject callback) {
    if (!contextPtr || !prompt || !callback) return;

    auto* native = reinterpret_cast<NativeContext*>(contextPtr);
    if (!native->model) return;

    const char* promptChars = env->GetStringUTFChars(prompt, nullptr);
    if (!promptChars) return;
    std::string promptText(promptChars);
    env->ReleaseStringUTFChars(prompt, promptChars);

    const llama_vocab* vocab = llama_model_get_vocab(native->model);
    const int nPrompt = -llama_tokenize(
        vocab, promptText.c_str(), promptText.size(), nullptr, 0, true, true);

    if (nPrompt <= 0) {
        notifyComplete(env, callback);
        return;
    }

    std::vector<llama_token> promptTokens(static_cast<size_t>(std::min(nPrompt, 3072)));
    const int tokenCount = llama_tokenize(
        vocab, promptText.c_str(), promptText.size(),
        promptTokens.data(), promptTokens.size(), true, true);

    if (tokenCount < 0) {
        notifyComplete(env, callback);
        return;
    }
    promptTokens.resize(static_cast<size_t>(tokenCount));

    llama_context_params contextParams = llama_context_default_params();
    contextParams.n_ctx = 4096;
    contextParams.n_batch = static_cast<uint32_t>(std::min<size_t>(promptTokens.size(), 4096));

    if (native->context) {
        llama_free(native->context);
        native->context = nullptr;
    }

    native->context = llama_init_from_model(native->model, contextParams);
    if (!native->context) {
        notifyComplete(env, callback);
        return;
    }

    llama_sampler_chain_params samplerParams = llama_sampler_chain_default_params();
    llama_sampler* sampler = llama_sampler_chain_init(samplerParams);
    llama_sampler_chain_add(sampler, llama_sampler_init_temp(0.7f));
    llama_sampler_chain_add(sampler, llama_sampler_init_top_p(0.9f, 1));
    llama_sampler_chain_add(sampler, llama_sampler_init_dist(1234));

    llama_batch batch = llama_batch_get_one(promptTokens.data(), promptTokens.size());
    const int maxTokens = 512;

    for (int generated = 0; generated < maxTokens; ++generated) {
        if (llama_decode(native->context, batch) != 0) break;

        llama_token token = llama_sampler_sample(sampler, native->context, -1);
        if (llama_vocab_is_eog(vocab, token)) break;

        char buffer[512];
        const int written = llama_token_to_piece(vocab, token, buffer, sizeof(buffer), 0, true);
        if (written > 0) notifyToken(env, callback, std::string(buffer, written));

        batch = llama_batch_get_one(&token, 1);
    }

    llama_sampler_free(sampler);
    notifyComplete(env, callback);
}

extern "C" JNIEXPORT void JNICALL
Java_com_mojealterego_newgpt_data_local_gguf_GgufNativeEngine_freeModelNative(
        JNIEnv*, jobject, jlong contextPtr) {
    if (!contextPtr) return;
    auto* native = reinterpret_cast<NativeContext*>(contextPtr);
    if (native->context) llama_free(native->context);
    if (native->model) llama_model_free(native->model);
    delete native;
}
