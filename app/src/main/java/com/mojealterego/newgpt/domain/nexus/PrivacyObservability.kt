package com.mojealterego.newgpt.domain.nexus
data class DataFlowEvent(val operation:String,val destination:String,val dataClasses:Set<String>,val allowed:Boolean,val timestamp:Long=System.currentTimeMillis())
class PrivacyPolicyEngine{
 private val blocked=setOf("private_key","password","keystore_secret")
 fun allow(dataClasses:Set<String>,destination:String,privateMode:Boolean)=!privateMode||(dataClasses intersect blocked).isEmpty()&&destination=="LOCAL"
}
data class AiMetric(val operation:String,val latencyMs:Long,val success:Boolean,val tokens:Int=0,val source:String="unknown")
class ObservabilityCore{
 private val events=ArrayDeque<AiMetric>()
 @Synchronized fun record(m:AiMetric){if(events.size>=1000)events.removeFirst();events.addLast(m)}
 @Synchronized fun snapshot():List<AiMetric> = events.toList()
 @Synchronized fun p95Latency():Long{if(events.isEmpty())return 0;val x=events.map{it.latencyMs}.sorted();return x[((x.size-1)*95)/100]}
}