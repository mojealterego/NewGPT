package com.mojealterego.newgpt.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpHeaders
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WebAccessService @Inject constructor(private val client: HttpClient) {
    private val urlPattern = Regex("""https?://[^s<>]+""", RegexOption.IGNORE_CASE)

    suspend fun fetchUrlFromPrompt(prompt: String): String? {
        val url = urlPattern.find(prompt)?.value?.trimEnd('.', ',', ')', ']', '}', ';') ?: return null
        return fetch(url)
    }

    suspend fun fetch(url: String): String {
        require(url.startsWith("https://") || url.startsWith("http://")) { "Dozwolone są tylko adresy HTTP(S)." }
        val html = client.get(url) {
            header(HttpHeaders.UserAgent, "NewGPT-MojeAlterego/1.0")
        }.bodyAsText()
        return html
            .replace(Regex("(?is)<script.*?</script>"), " ")
            .replace(Regex("(?is)<style.*?</style>"), " ")
            .replace(Regex("(?is)<[^>]+>"), " ")
            .replace(Regex("&nbsp;"), " ")
            .replace(Regex("&amp;"), "&")
            .replace(Regex("\s+"), " ")
            .trim()
            .take(12000)
    }
}
