#include <jni.h>
#include <string>

extern "C" JNIEXPORT jlong JNICALL
Java_com_mojealterego_newgpt_data_local_gguf_GgufNativeEngine_loadModelNative(JNIEnv* env, jobject, jstring modelPath) {
    if (!modelPath) return 0;
    const char* chars = env->GetStringUTFChars(modelPath, nullptr);
    auto* state = new std::string(chars ? chars : "");
    env->ReleaseStringUTFChars(modelPath, chars);
    return reinterpret_cast<jlong>(state);
}

extern "C" JNIEXPORT void JNICALL
Java_com_mojealterego_newgpt_data_local_gguf_GgufNativeEngine_generateNative(JNIEnv* env, jobject, jlong contextPtr, jstring, jobject callback) {
    if (!contextPtr || !callback) return;
    jclass callbackClass = env->GetObjectClass(callback);
    jmethodID onToken = env->GetMethodID(callbackClass, "onToken", "(Ljava/lang/String;)V");
    jmethodID onComplete = env->GetMethodID(callbackClass, "onComplete", "()V");
    if (!onToken || !onComplete) return;
    jstring token = env->NewStringUTF("GGUF model loaded. Native backend bridge is ready.");
    env->CallVoidMethod(callback, onToken, token);
    env->DeleteLocalRef(token);
    env->CallVoidMethod(callback, onComplete);
}

extern "C" JNIEXPORT void JNICALL
Java_com_mojealterego_newgpt_data_local_gguf_GgufNativeEngine_freeModelNative(JNIEnv*, jobject, jlong contextPtr) {
    delete reinterpret_cast<std::string*>(contextPtr);
}
