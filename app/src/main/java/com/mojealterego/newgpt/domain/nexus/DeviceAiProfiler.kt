package com.mojealterego.newgpt.domain.nexus
import android.app.ActivityManager
import android.content.Context
import android.os.Build
data class DeviceAiProfile(val androidApi:Int,val abi:String,val ramMb:Long,val lowRam:Boolean,val availableRamMb:Long,val recommendedMode:RouteMode,val localContextLimit:Int)
class DeviceAiProfiler{
 fun profile(context:Context):DeviceAiProfile{
  val am=context.getSystemService(ActivityManager::class.java);val mi=ActivityManager.MemoryInfo();am.getMemoryInfo(mi);val ram=mi.totalMem/(1024*1024);val low=am.isLowRamDevice
  val mode=if(low||ram<4096)RouteMode.CLOUD else RouteMode.HYBRID
  return DeviceAiProfile(Build.VERSION.SDK_INT,Build.SUPPORTED_ABIS.firstOrNull()?:"unknown",ram,low,mi.availMem/(1024*1024),mode,when{ram>=12288->32768;ram>=8192->16384;else->8192})
 }
}