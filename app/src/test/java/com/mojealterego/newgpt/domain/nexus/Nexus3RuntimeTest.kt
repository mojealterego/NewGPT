package com.mojealterego.newgpt.domain.nexus
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test

class Nexus3RuntimeTest{
 @Test fun contextCompiler_deduplicatesAndBudgets(){
  val c=ContextCompiler().compile(listOf(ContextFragment("a","hello"),ContextFragment("b","hello"),ContextFragment("c","world")),64,8)
  assertEquals(2,c.fragments.size)
 }
 @Test fun memoryLifecycle_blocksUntrustedExternal(){
  val e=MemoryLifecycleEngine{1000000L}
  val m=LifecycleMemory("1","x",0,1000000,1.0,.5,TrustLevel.EXTERNAL_CONTENT)
  assertEquals(MemoryAction.REQUIRES_CONFIRMATION,e.classify(m))
 }
 @Test fun evaluation_detectsRegression(){
  val e=AgentEvaluationEngine();val t=EvaluationCase("1","x",setOf("alpha","beta"))
  assertTrue(e.evaluate(t,"alpha beta").passed)
  assertTrue(e.regression(listOf(e.evaluate(t,"alpha")),listOf(e.evaluate(t,"alpha beta")))<0)
 }
 @Test fun privacy_privateMode_blocks_sensitive(){
  assertFalse(PrivacyPolicyEngine().allow(setOf("private_key"),"LOCAL",true))
 }
 @Test fun appFunctions_capability_is_api_gated(){
  val bridge=AppFunctionsBridge()
  if(android.os.Build.VERSION.SDK_INT<36) assertTrue(bridge.capabilities().isEmpty())
 }
 @Test fun modelRouter_failsOverAfterCooldown(){
  runBlocking{
   val router=ModelRouter3{1000L}
   router.register(ModelEndpoint("a","x","m",setOf(RouteMode.CLOUD),1,1))
   router.register(ModelEndpoint("b","y","m",setOf(RouteMode.CLOUD),2,1))
   router.reportFailure("a")
   assertEquals("b",router.choose(RouteMode.CLOUD)?.id)
  }
 }
}