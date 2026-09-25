package com.mojealterego.newgpt.domain.nexus

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.File
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Serializable
data class BitemporalRecord(
    val id: String,
    val key: String,
    val value: String,
    val validFrom: Long,
    val validTo: Long? = null,
    val txFrom: Long,
    val txTo: Long? = null,
    val version: Int = 1,
    val supersedesId: String? = null
)

@Serializable
private data class BitemporalSnapshot(val records: List<BitemporalRecord> = emptyList())

@Singleton
class BitemporalMemoryStore @Inject constructor(
    @ApplicationContext context: Context
) {
    private val file = File(context.filesDir, "nexus/bitemporal.json")
    private val json = Json { ignoreUnknownKeys = true }

    private fun load(): MutableList<BitemporalRecord> =
        if (!file.exists()) mutableListOf()
        else runCatching {
            json.decodeFromString<BitemporalSnapshot>(file.readText()).records.toMutableList()
        }.getOrDefault(mutableListOf())

    private fun save(records: List<BitemporalRecord>) {
        file.parentFile?.mkdirs()
        file.writeText(json.encodeToString(BitemporalSnapshot(records.takeLast(10000))))
    }

    suspend fun remember(
        key: String,
        value: String,
        validFrom: Long = System.currentTimeMillis(),
        validTo: Long? = null,
        transactionTime: Long = System.currentTimeMillis()
    ): String = withContext(Dispatchers.IO) {
        require(key.trim().isNotEmpty())
        val id = "bt:" + UUID.randomUUID()
        load().also {
            it += BitemporalRecord(id, key.trim(), value.trim(), validFrom, validTo, transactionTime)
            save(it)
        }
        id
    }

    suspend fun correct(
        key: String,
        value: String,
        validFrom: Long,
        validTo: Long? = null,
        transactionTime: Long = System.currentTimeMillis()
    ): String = withContext(Dispatchers.IO) {
        val records = load()
        val active = records.filter { it.key == key && it.txTo == null }
        val newId = "bt:" + UUID.randomUUID()
        val updated = records.map { old ->
            if (old.key == key && old.txTo == null) old.copy(txTo = transactionTime) else old
        }.toMutableList()
        updated += BitemporalRecord(
            id = newId,
            key = key.trim(),
            value = value.trim(),
            validFrom = validFrom,
            validTo = validTo,
            txFrom = transactionTime,
            version = (active.maxOfOrNull { it.version } ?: 0) + 1,
            supersedesId = active.maxByOrNull { it.version }?.id
        )
        save(updated)
        newId
    }

    suspend fun current(key: String): BitemporalRecord? = withContext(Dispatchers.IO) {
        load().asSequence()
            .filter { it.key == key && it.txTo == null }
            .filter { it.validTo == null || it.validTo > System.currentTimeMillis() }
            .maxByOrNull { it.version }
    }

    suspend fun asOf(key: String, validAt: Long, transactionAt: Long): BitemporalRecord? =
        withContext(Dispatchers.IO) {
            load().asSequence()
                .filter { it.key == key }
                .filter { it.validFrom <= validAt && (it.validTo == null || validAt < it.validTo) }
                .filter { it.txFrom <= transactionAt && (it.txTo == null || transactionAt < it.txTo) }
                .maxByOrNull { it.version }
        }

    suspend fun pointInTime(transactionAt: Long): List<BitemporalRecord> =
        withContext(Dispatchers.IO) {
            load().filter { it.txFrom <= transactionAt && (it.txTo == null || transactionAt < it.txTo) }
        }

    suspend fun clear() = withContext(Dispatchers.IO) { if (file.exists()) file.delete() }
}
