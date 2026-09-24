package com.mojealterego.newgpt.data.local.gguf

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GgufNativeEngine @Inject constructor() {
    init {
        System.loadLibrary("newgpt_native")
    }

    private external fun loadModelNative(modelPath: String): Long
    private external fun formatChatNative(
        contextPtr: Long,
        roles: Array<String>,
        contents: Array<String>
    ): String?
    private external fun generateNative(contextPtr: Long, prompt: String, callback: TokenCallback)
    private external fun stopGenerationNative(contextPtr: Long)
    private external fun freeModelNative(contextPtr: Long)

    @Volatile
    private var contextPtr = 0L
    private var loadedPath: String? = null

    @Synchronized
    fun loadModel(path: String) {
        if (contextPtr != 0L && loadedPath == path) return
        if (contextPtr != 0L) {
            stopGenerationNative(contextPtr)
            freeModelNative(contextPtr)
        }
        contextPtr = loadModelNative(path)
        check(contextPtr != 0L) { "Nie można załadować modelu GGUF." }
        loadedPath = path
    }

    @Synchronized
    fun formatChat(messages: List<Pair<String, String>>): String? {
        check(contextPtr != 0L) { "Model GGUF nie został załadowany." }
        if (messages.isEmpty()) return null
        return formatChatNative(
            contextPtr,
            messages.map { it.first }.toTypedArray(),
            messages.map { it.second }.toTypedArray()
        )
    }

    fun generate(prompt: String): Flow<String> = callbackFlow {
        val context = contextPtr
        check(context != 0L) { "Model GGUF nie został załadowany." }

        val callback = object : TokenCallback {
            override fun onToken(token: String) {
                trySend(token)
            }

            override fun onComplete() {
                close()
            }
        }

        val generation: Job = launch(Dispatchers.IO) {
            generateNative(context, prompt, callback)
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
