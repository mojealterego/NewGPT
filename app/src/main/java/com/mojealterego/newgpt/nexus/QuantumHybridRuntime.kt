package com.mojealterego.newgpt.nexus

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

data class QuantumConfig(
    val baseUrl: String = "https://quantum.cloud.ibm.com/api/v1",
    val bearerToken: String,
    val serviceCrn: String,
    val backend: String,
    val apiVersion: String = "2026-04-15"
)

data class QuantumJobReceipt(
    val httpStatus: Int,
    val jobId: String?,
    val rawBody: String,
    val submittedToQpu: Boolean
)

class IbmQuantumRuntime(private val config: QuantumConfig) {
    suspend fun submitSampler(openQasm3: String, shots: Int = 256): QuantumJobReceipt =
        withContext(Dispatchers.IO) {
            require(config.bearerToken.isNotBlank()) { "IBM Quantum bearer token is required" }
            require(config.serviceCrn.isNotBlank()) { "IBM Quantum Service-CRN is required" }
            require(config.backend.isNotBlank()) { "IBM Quantum backend is required" }
            require(openQasm3.isNotBlank()) { "OpenQASM 3 circuit is required" }
            require(shots in 1..1_000_000) { "shots out of range" }

            val connection = (URL(config.baseUrl.trimEnd('/') + "/jobs").openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                doOutput = true
                connectTimeout = 10_000
                readTimeout = 60_000
                setRequestProperty("Accept", "application/json")
                setRequestProperty("Content-Type", "application/json")
                setRequestProperty("Authorization", "Bearer " + config.bearerToken)
                setRequestProperty("Service-CRN", config.serviceCrn)
                setRequestProperty("IBM-API-Version", config.apiVersion)
            }

            val body = JSONObject()
                .put("program_id", "sampler")
                .put("backend", config.backend)
                .put("params", JSONObject()
                    .put("pubs", listOf(listOf(openQasm3)))
                    .put("options", JSONObject())
                    .put("version", 2)
                    .put("shots", shots))
                .toString()

            connection.outputStream.use { it.write(body.toByteArray(Charsets.UTF_8)) }
            val status = connection.responseCode
            val stream = if (status in 200..299) connection.inputStream else connection.errorStream
            val response = stream?.bufferedReader()?.use { it.readText() }.orEmpty()
            val id = runCatching { JSONObject(response).optString("id").ifBlank { null } }.getOrNull()
            QuantumJobReceipt(status, id, response.take(200_000), submittedToQpu = status in 200..299)
        }

    suspend fun job(jobId: String): String = get("/jobs/" + jobId.encodePathSegment())

    suspend fun result(jobId: String): String = get("/jobs/" + jobId.encodePathSegment())

    private suspend fun get(path: String): String = withContext(Dispatchers.IO) {
        val connection = (URL(config.baseUrl.trimEnd('/') + path).openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = 10_000
            readTimeout = 60_000
            setRequestProperty("Accept", "application/json")
            setRequestProperty("Authorization", "Bearer " + config.bearerToken)
            setRequestProperty("Service-CRN", config.serviceCrn)
            setRequestProperty("IBM-API-Version", config.apiVersion)
        }
        val status = connection.responseCode
        val stream = if (status in 200..299) connection.inputStream else connection.errorStream
        val body = stream?.bufferedReader()?.use { it.readText() }.orEmpty()
        if (status !in 200..299) error("IBM Quantum HTTP $status: " + body.take(4000))
        body
    }
}

private fun String.encodePathSegment(): String =
    java.net.URLEncoder.encode(this, Charsets.UTF_8.name()).replace("+", "%20")
