package com.mojealterego.newgpt.domain.nexus
enum class WorkflowStepKind{THINK,MODEL,TOOL,AGENT,CONDITION,TRANSFORM,HUMAN_APPROVAL}
data class WorkflowStep(val id:String,val kind:WorkflowStepKind,val input:String,val dependsOn:Set<String> = emptySet(),val requiresApproval:Boolean=false)
data class WorkflowDefinition(val id:String,val name:String,val steps:List<WorkflowStep>,val maxSteps:Int=64)
class WorkflowEngine{
 fun validate(w:WorkflowDefinition):List<String>{
  val e=mutableListOf<String>();if(w.steps.isEmpty())e+="EMPTY_WORKFLOW";if(w.steps.size>w.maxSteps)e+="STEP_LIMIT"
  val ids=w.steps.map{it.id};if(ids.size!=ids.toSet().size)e+="DUPLICATE_STEP_ID";val known=ids.toSet()
  w.steps.forEach{if(!known.containsAll(it.dependsOn))e+="UNKNOWN_DEPENDENCY"}
  return e
 }
}