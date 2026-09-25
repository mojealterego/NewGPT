package com.mojealterego.newgpt.domain.cognitive

import com.mojealterego.newgpt.domain.agent.AgentTool
import java.net.URI
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ToolCallGuard @Inject constructor(
    private val policy: PolicyEngine
) {
    fun check(
        tool: AgentTool,
        approval: Boolean = false,
        trustedContext: Boolean = false
    ): PolicyDecision = policy.evaluate(tool, approval, trustedContext)

    fun validateHttpUrl(raw: String): Result<String> = runCatching {
        val uri = URI(raw.trim())
        require(uri.scheme.equals("https", true) || uri.scheme.equals("http", true)) { "Unsupported scheme" }
        val host = uri.host?.lowercase() ?: error("Missing host")
        require(host != "localhost" && host != "127.0.0.1" && host != "::1") { "Local host blocked" }
        require(!host.endsWith(".localhost")) { "Local host blocked" }
        require(!host.startsWith("169.254.")) { "Link-local host blocked" }
        require(!host.startsWith("10.") && !host.startsWith("192.168.")) { "Private host blocked" }
        val second = host.split(".").getOrNull(1)
        require(!(host.startsWith("172.") && second?.toIntOrNull()?.let { it in 16..31 } == true)) {
            "Private host blocked"
        }
        uri.toString()
    }
}
