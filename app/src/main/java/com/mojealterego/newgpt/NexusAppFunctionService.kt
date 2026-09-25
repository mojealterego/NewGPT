package com.mojealterego.newgpt
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.appfunctions.AppFunction
import androidx.appfunctions.AppFunctionService
import androidx.appfunctions.AppFunctionServiceEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@RequiresApi(Build.VERSION_CODES.BAKLAVA)
@AppFunctionServiceEntryPoint(serviceName="NexusAppFunctionService",appFunctionXmlFileName="nexus_app_function_service")
abstract class BaseNexusAppFunctionService:AppFunctionService(){
 @AppFunction(isDescribedByKDoc=true)
 suspend fun getNewGptCapabilities():String=withContext(Dispatchers.Default){
  "NewGPT: local/cloud/hybrid AI, memory, RAG, agents, context compilation, MCP governance and evaluation."
 }
 @AppFunction(isDescribedByKDoc=true)
 suspend fun classifyNewGptIntent(query:String):String=withContext(Dispatchers.Default){
  when{query.isBlank()->"EMPTY";query.contains("agent",true)->"AGENT";query.contains("memory",true)->"MEMORY";query.contains("model",true)||query.contains("AI",true)->"AI";else->"CHAT"}
 }
}