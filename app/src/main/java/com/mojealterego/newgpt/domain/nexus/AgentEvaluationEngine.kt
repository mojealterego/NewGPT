package com.mojealterego.newgpt.domain.nexus
data class EvaluationCase(val id:String,val input:String,val expectedTerms:Set<String>)
data class EvaluationResult(val caseId:String,val score:Double,val passed:Boolean,val notes:String)
class AgentEvaluationEngine{
 fun evaluate(t:EvaluationCase,out:String):EvaluationResult{val n=out.lowercase();val h=t.expectedTerms.count{n.contains(it.lowercase())};val s=if(t.expectedTerms.isEmpty())1.0 else h.toDouble()/t.expectedTerms.size;return EvaluationResult(t.id,s,s>=.8,"Matched " + h + "/" + t.expectedTerms.size + " expected terms")}
 fun regression(current:List<EvaluationResult>,baseline:List<EvaluationResult>)=if(current.isEmpty()||baseline.isEmpty())0.0 else current.map{it.score}.average()-baseline.map{it.score}.average()
}