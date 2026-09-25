package com.mojealterego.newgpt.data.remote

import com.mojealterego.newgpt.domain.cognitive.ToolCallGuard
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpHeaders
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WebAccessService @Inject constructor(
    private val client: HttpClient,
    private val guard: ToolCallGuard
) {
    private val urlPattern = Regex("""https?://[^\s<>]+""", RegexOption.IGNORE_CASE)

    suspend fun fetchUrlFromPrompt(prompt: String): String? {
        val url = urlPattern.find(prompt)?.value?.trimEnd('.', ',', ')', ']', '}', ';') ?: return null
        return fetch(url)
    }

    suspend fun fetch(url: String): String {
        val safeUrl = guard.validateHttpUrl(url).getOrThrow()
        val html = client.get(safeUrl) {
            header(HttpHeaders.UserAgent, "NewGPT-MojeAlterego/2.0")
        }.bodyAsText()

        return html
            .take(2_000_000)
            .replace(Regex("(?is)<script.*?</script>"), " ")
            .replace(Regex("(?is)<style.*?</style>"), " ")
            .replace(Regex("(?is)<[^>]+>"), " ")
            .replace(Regex("&nbsp;"), " ")
            .replace(Regex("&amp;"), "&")
            .replace(Regex("""\s+"""), " ")
            .trim()
            .take(20_000)
    }
}
