package com.mojealterego.newgpt.domain.integration

import android.content.Context
import androidx.appfunctions.AppFunctionData
import androidx.appfunctions.AppFunctionManager
import androidx.appfunctions.AppFunctionSearchSpec
import androidx.appfunctions.ExecuteAppFunctionRequest
import androidx.appfunctions.metadata.AppFunctionBooleanTypeMetadata
import androidx.appfunctions.metadata.AppFunctionDoubleTypeMetadata
import androidx.appfunctions.metadata.AppFunctionFloatTypeMetadata
import androidx.appfunctions.metadata.AppFunctionIntTypeMetadata
import androidx.appfunctions.metadata.AppFunctionLongTypeMetadata
import androidx.appfunctions.metadata.AppFunctionMetadata
import androidx.appfunctions.metadata.AppFunctionStringTypeMetadata
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

data class DiscoveredAppFunction(
    val id: String,
    val packageName: String,
    val description: String,
    val parameters: List<String>
)

data class AppFunctionExecution(
    val functionId: String,
    val packageName: String,
    val output: String
)

/** Android 16+ bridge for discovering and invoking on-device AppFunctions. */
@Singleton
class AndroidAppFunctionsBridge @Inject constructor(
    @ApplicationContext private val context: Context
) {
    suspend fun discover(query: String? = null): List<DiscoveredAppFunction> = withContext(Dispatchers.IO) {
        val manager = AppFunctionManager.getInstance(context) ?: return@withContext emptyList()
        val metadata = manager.searchAppFunctions(AppFunctionSearchSpec.Builder().build())
        metadata.asSequence()
            .filter { query.isNullOrBlank() || it.description.contains(query, true) || it.id.contains(query, true) || it.packageName.contains(query, true) }
            .map { metadataItem ->
                DiscoveredAppFunction(
                    id = metadataItem.id,
                    packageName = metadataItem.packageName,
                    description = metadataItem.description,
                    parameters = metadataItem.parameters.map { parameter -> parameter.name }
                )
            }
            .toList()
    }

    suspend fun execute(metadata: AppFunctionMetadata, arguments: Map<String, String>): AppFunctionExecution = withContext(Dispatchers.IO) {
        val manager = AppFunctionManager.getInstance(context)
            ?: error("Android AppFunctions are not supported on this device.")
        val builder = AppFunctionData.Builder(metadata.parameters, metadata.components)
        metadata.parameters.forEach { parameter ->
            val value = arguments[parameter.name] ?: return@forEach
            when (parameter.dataType) {
                is AppFunctionStringTypeMetadata -> builder.setString(parameter.name, value)
                is AppFunctionBooleanTypeMetadata -> builder.setBoolean(parameter.name, value.toBooleanStrictOrNull() ?: error("Invalid boolean: " + parameter.name))
                is AppFunctionIntTypeMetadata -> builder.setInt(parameter.name, value.toIntOrNull() ?: error("Invalid int: " + parameter.name))
                is AppFunctionLongTypeMetadata -> builder.setLong(parameter.name, value.toLongOrNull() ?: error("Invalid long: " + parameter.name))
                is AppFunctionFloatTypeMetadata -> builder.setFloat(parameter.name, value.toFloatOrNull() ?: error("Invalid float: " + parameter.name))
                is AppFunctionDoubleTypeMetadata -> builder.setDouble(parameter.name, value.toDoubleOrNull() ?: error("Invalid double: " + parameter.name))
                else -> error("Unsupported AppFunction parameter type for " + parameter.name)
            }
        }
        val request = ExecuteAppFunctionRequest(
            targetPackageName = metadata.packageName,
            functionIdentifier = metadata.id,
            functionParameters = builder.build()
        )
        val response = manager.executeAppFunction(request)
        AppFunctionExecution(metadata.id, metadata.packageName, response.toString())
    }
}