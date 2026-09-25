package com.mojealterego.newgpt.nexus

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.security.MessageDigest

data class SubstrateDesign(
    val materialFamily: String,
    val transistorTopology: String,
    val targetEnergyPerOp: Double,
    val targetFrequencyGHz: Double,
    val thermalLimitC: Double,
    val rationale: String
)

data class FabricationRequest(
    val requestId: String,
    val design: SubstrateDesign,
    val authorizationToken: String
)

data class FabricationReceipt(
    val httpStatus: Int,
    val accepted: Boolean,
    val body: String
)

class PhysicalSubstrateRuntime(
    private val endpoint: String,
    private val allowedHost: String
) {
    fun designFromTelemetry(
        compute: DeviceComputeSnapshot,
        targetEnergyPerOp: Double,
        targetFrequencyGHz: Double
    ): SubstrateDesign {
        val thermal = compute.batteryTemperatureC ?: 25.0
        val material = when {
            targetEnergyPerOp < 0.5 -> "graphene-or-nanotube-candidate"
            targetFrequencyGHz > 5.0 -> "advanced-nanowire-candidate"
            else -> "silicon-optimized"
        }
        val topology = if (compute.cpuThreads >= 8) "heterogeneous-manycore" else "heterogeneous-efficient-core"
        return SubstrateDesign(
            materialFamily = material,
            transistorTopology = topology,
            targetEnergyPerOp = targetEnergyPerOp.coerceAtLeast(0.000001),
            targetFrequencyGHz = targetFrequencyGHz.coerceAtLeast(0.1),
            thermalLimitC = (thermal + 20.0).coerceAtLeast(45.0),
            rationale = "Derived from real Android device telemetry; requires physical lab validation before fabrication."
        )
    }

    suspend fun submitAfterHumanApproval(request: FabricationRequest): FabricationReceipt =
        withContext(Dispatchers.IO) {
            require(request.authorizationToken.isNotBlank()) { "Explicit fabrication authorization is required" }
            val url = URL(endpoint)
            require(url.protocol == "https") { "Fabrication endpoint must use HTTPS" }
            require(url.host == allowedHost) { "Fabrication host is not allowlisted" }

            val payload = JSONObject()
                .put("requestId", request.requestId)
                .put("design", JSONObject()
                    .put("materialFamily", request.design.materialFamily)
                    .put("transistorTopology", request.design.transistorTopology)
                    .put("targetEnergyPerOp", request.design.targetEnergyPerOp)
                    .put("targetFrequencyGHz", request.design.targetFrequencyGHz)
                    .put("thermalLimitC", request.design.thermalLimitC)
                    .put("rationale", request.design.rationale))
                .put("approvalDigest", sha256(request.authorizationToken))
                .toString()

            val connection = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                doOutput = true
                connectTimeout = 10_000
                readTimeout = 30_000
                setRequestProperty("Content-Type", "application/json")
                setRequestProperty("Authorization", "Bearer " + request.authorizationToken)
            }
            connection.outputStream.use { it.write(payload.toByteArray(Charsets.UTF_8)) }
            val status = connection.responseCode
            val stream = if (status in 200..299) connection.inputStream else connection.errorStream
            val body = stream?.bufferedReader()?.use { it.readText() }.orEmpty()
            FabricationReceipt(status, status in 200..299, body.take(50_000))
        }

    private fun sha256(value: String): String =
        MessageDigest.getInstance("SHA-256")
            .digest(value.toByteArray(Charsets.UTF_8))
            .joinToString("") { "%02x".format(it) }
}
