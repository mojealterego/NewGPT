package com.mojealterego.newgpt.data.local.gguf

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GgufNativeEngine @Inject constructor() {
    @Volatile
    private var nativeLoaded = false

    @Synchronized
    private fun ensureNativeLoaded() {
        if (nativeLoaded) return
        try {
            System.loadLibrary("newgpt_native")
            nativeLoaded = true
        } catch (error: UnsatisfiedLinkError) {
            throw IllegalStateException(
                "Lokalny silnik GGUF nie może zostać uruchomiony na tym urządzeniu.",
                error
            )
        }
    }

    private external fun loadModelNative(modelPath: String, gpuLayers: Int): Long
    private external fun formatChatNative(contextPtr: Long, roles: Array<String>, contents: Array<String>): String?
    private external fun generateNative(
        contextPtr: Long,
        prompt: String,
        callback: TokenCallback,
        contextSize: Int,
        maxTokens: Int,
        temperature: Float,
        topP: Float,
        threads: Int
    )
    private external fun stopGenerationNative(contextPtr: Long)
    private external fun freeModelNative(contextPtr: Long)

    @Volatile private var contextPtr = 0L
    private var loadedPath: String? = null
    private var loadedGpuLayers = Int.MIN_VALUE

    @Synchronized
    fun loadModel(path: String, gpuLayers: Int) {
        ensureNativeLoaded()
        if (contextPtr != 0L && loadedPath == path && loadedGpuLayers == gpuLayers) return
        if (contextPtr != 0L) {
            stopGenerationNative(contextPtr)
            freeModelNative(contextPtr)
        }
        contextPtr = loadModelNative(path, gpuLayers.coerceIn(0, 128))
        check(contextPtr != 0L) { "Nie można załadować modelu GGUF." }
        loadedPath = path
        loadedGpuLayers = gpuLayers
    }

    @Synchronized
    fun formatChat(messages: List<Pair<String, String>>): String? {
        ensureNativeLoaded()
        check(contextPtr != 0L) { "Model GGUF nie został załadowany." }
        if (messages.isEmpty()) return null
        return formatChatNative(
            contextPtr,
            messages.map { it.first }.toTypedArray(),
            messages.map { it.second }.toTypedArray()
        )
    }

    fun generate(
        prompt: String,
        contextSize: Int,
        maxTokens: Int,
        temperature: Float,
        topP: Float,
        threads: Int
    ): Flow<String> = callbackFlow {
        ensureNativeLoaded()
        val context = contextPtr
        check(context != 0L) { "Model GGUF nie został załadowany." }
        val callback = object : TokenCallback {
            override fun onToken(token: String) { trySend(token) }
            override fun onComplete() { close() }
        }
        val generation: Job = launch(Dispatchers.IO) {
            generateNative(
                context, prompt, callback,
                contextSize.coerceIn(1024, 32768),
                maxTokens.coerceIn(64, 8192),
                temperature.coerceIn(0f, 2f),
                topP.coerceIn(0.05f, 1f),
                threads.coerceIn(1, 32)
            )
        }
        awaitClose {
            stopGenerationNative(context)
            generation.cancel()
        }
    }

    private interface TokenCallback {
        fun onToken(token: String)
        fun onComplete()
    }
}
