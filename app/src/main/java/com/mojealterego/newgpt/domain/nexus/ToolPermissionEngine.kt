package com.mojealterego.newgpt.domain.nexus
enum class ToolRisk{READ,WRITE,EXTERNAL,PRIVILEGED}
data class ToolGrant(val toolId:String,val risk:ToolRisk,val enabled:Boolean=true,val requiresConfirmation:Boolean=true)
data class ToolDecision(val allowed:Boolean,val requiresConfirmation:Boolean,val reason:String)
class ToolPermissionEngine{
 fun decide(g:ToolGrant,privateMode:Boolean):ToolDecision{
  if(!g.enabled)return ToolDecision(false,false,"DISABLED")
  if(privateMode&&g.risk==ToolRisk.EXTERNAL)return ToolDecision(false,false,"PRIVATE_MODE")
  return ToolDecision(true,g.requiresConfirmation||g.risk.ordinal>=ToolRisk.WRITE.ordinal,"POLICY")
 }
}