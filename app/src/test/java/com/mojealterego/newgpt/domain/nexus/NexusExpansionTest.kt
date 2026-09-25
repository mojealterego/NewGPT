package com.mojealterego.newgpt.domain.nexus
import org.junit.Assert.*
import org.junit.Test
class NexusExpansionTest{
 @Test fun workflowRejectsUnknownDependency(){
  val w=WorkflowDefinition("w","x",listOf(WorkflowStep("a",WorkflowStepKind.MODEL,"",setOf("missing"))))
  assertTrue(WorkflowEngine().validate(w).isNotEmpty())
 }
 @Test fun privateModeRejectsExternalTool(){
  val d=ToolPermissionEngine().decide(ToolGrant("web",ToolRisk.EXTERNAL),true)
  assertFalse(d.allowed)
 }
 @Test fun releaseGateNeedsAllChecks(){assertFalse(ReleaseHealthGate().ready(listOf(ReleaseGate("ci",true,""),ReleaseGate("security",false,""))))}
 @Test fun multimodalAcceptsVideo(){assertTrue(MultimodalCapability().accepts(MediaKind.VIDEO))}
}