package com.mojealterego.newgpt.domain.nexus
import kotlin.math.exp
data class LifecycleMemory(val id:String,val text:String,val createdAt:Long,val lastUsedAt:Long,val importance:Double,val confidence:Double,val trust:TrustLevel,val expiresAt:Long?=null,val archived:Boolean=false,val supersedesId:String?=null)
enum class MemoryAction{KEEP,CONSOLIDATE,DECAY,ARCHIVE,REQUIRES_CONFIRMATION}
class MemoryLifecycleEngine(private val now:()->Long={System.currentTimeMillis()}){
 fun classify(m:LifecycleMemory):MemoryAction{
  if(m.archived)return MemoryAction.ARCHIVE
  if(m.expiresAt!=null&&m.expiresAt<=now())return MemoryAction.ARCHIVE
  if(m.trust==TrustLevel.EXTERNAL_CONTENT&&m.confidence<.8)return MemoryAction.REQUIRES_CONFIRMATION
  val age=((now()-m.lastUsedAt).coerceAtLeast(0)/86400000.0);val s=m.importance*m.confidence*exp(-age/30.0)
  return when{ s>=.65->MemoryAction.KEEP;s>=.35->MemoryAction.CONSOLIDATE;s>=.10->MemoryAction.DECAY;else->MemoryAction.ARCHIVE}
 }
 fun decayScore(m:LifecycleMemory):Double{val age=((now()-m.lastUsedAt).coerceAtLeast(0)/86400000.0);return m.importance*m.confidence*exp(-age/30.0)}
}