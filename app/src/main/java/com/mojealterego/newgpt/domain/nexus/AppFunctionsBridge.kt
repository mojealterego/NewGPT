package com.mojealterego.newgpt.domain.nexus
import android.os.Build
class AppFunctionsBridge{
 fun supported()=Build.VERSION.SDK_INT>=36
 fun capabilities():Set<String>=if(supported())setOf("chat","summarize","retrieve_memory","compile_context","run_agent")else emptySet()
}