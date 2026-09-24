package com.mojealterego.newgpt.data.remote

import android.content.Context
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.statement.bodyAsChannel
import io.ktor.http.HttpHeaders
import io.ktor.utils.io.readAvailable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HuggingFaceModelService @Inject constructor(
    private val context: Context,
    private val client: HttpClient
) {
    suspend fun download(
        repoId: String,
        fileName: String,
        revision: String,
        token: String?,
        onProgress: (Long, Long?) -> Unit
    ): File = withContext(Dispatchers.IO) {
        require(repoId.matches(Regex("[A-Za-z0-9_.-]+/[A-Za-z0-9_.-]+"))) { "Nieprawidłowe repo_id Hugging Face." }
        require(fileName.isNotBlank()) { "Podaj nazwę pliku." }
        val safeName = fileName.substringAfterLast('/').replace(Regex("[^A-Za-z0-9._-]"), "_")
        val destination = File(File(context.filesDir, "models").apply { mkdirs() }, safeName)
        val revisionName = revision.ifBlank { "main" }
        val url = "https://huggingface.co/" + repoId + "/resolve/" + revisionName + "/" + fileName
        val response = client.get(url) {
            if (!token.isNullOrBlank()) header(HttpHeaders.Authorization, "Bearer " + token)
        }
        check(response.status.value in 200..399) { "Hugging Face HTTP " + response.status.value }
        val total = response.headers[HttpHeaders.ContentLength]?.toLongOrNull()
        var written = 0L
        response.bodyAsChannel().let { channel ->
            destination.outputStream().buffered().use { output ->
                val buffer = ByteArray(64 * 1024)
                while (!channel.isClosedForRead) {
                    val n = channel.readAvailable(buffer, 0, buffer.size)
                    if (n <= 0) continue
                    output.write(buffer, 0, n)
                    written += n
                    onProgress(written, total)
                }
            }
        }
        destination
    }
}
