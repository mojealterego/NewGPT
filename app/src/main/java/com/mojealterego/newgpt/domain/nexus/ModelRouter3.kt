package com.mojealterego.newgpt.domain.nexus
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
enum class RouteMode { LOCAL, CLOUD, HYBRID, PRIVATE }
data class ModelEndpoint(val id:String,val provider:String,val model:String,val modes:Set<RouteMode>,val priority:Int=100,val maxFailures:Int=3)
data class EndpointHealth(val failures:Int=0,val cooldownUntilMs:Long=0L)
class ModelRouter3(private val clock:()->Long={System.currentTimeMillis()}){
 private val lock=Mutex(); private val health=mutableMapOf<String,EndpointHealth>(); private val endpoints=mutableListOf<ModelEndpoint>()
 suspend fun register(e:ModelEndpoint)=lock.withLock{endpoints.removeAll{it.id==e.id};endpoints+=e;endpoints.sortBy{it.priority}}
 suspend fun choose(mode:RouteMode,now:Long=clock())=lock.withLock{endpoints.firstOrNull{mode in it.modes&&(health[it.id]?.cooldownUntilMs?:0L)<=now}}
 suspend fun reportSuccess(id:String)=lock.withLock{health[id]=EndpointHealth()}
 suspend fun reportFailure(id:String,now:Long=clock())=lock.withLock{val e=endpoints.firstOrNull{it.id==id}?:return@withLock;val old=health[id]?:EndpointHealth();val n=old.failures+1;health[id]=if(n>=e.maxFailures)EndpointHealth(n,now+30000)else EndpointHealth(n,old.cooldownUntilMs)}
 suspend fun snapshotHealth()=lock.withLock{health.toMap()}
}