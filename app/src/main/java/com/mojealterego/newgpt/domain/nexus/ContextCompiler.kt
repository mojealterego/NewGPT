package com.mojealterego.newgpt.domain.nexus
data class ContextFragment(val source:String,val text:String,val trust:TrustLevel=TrustLevel.UNVERIFIED,val provenance:String?=null,val priority:Int=100)
enum class TrustLevel{USER_CONFIRMED,VERIFIED_SOURCE,MODEL_INFERENCE,UNVERIFIED,EXTERNAL_CONTENT}
data class CompiledContext(val text:String,val fragments:List<ContextFragment>,val estimatedTokens:Int,val warnings:List<String>)
class ContextCompiler{
 fun compile(fragments:List<ContextFragment>,maxTokens:Int,reservedTokens:Int=512):CompiledContext{
  val budget=(maxTokens-reservedTokens).coerceAtLeast(128);val seen=LinkedHashMap<String,ContextFragment>();var used=0
  fragments.sortedWith(compareBy<ContextFragment>{it.priority}.thenByDescending{it.trust.ordinal}).forEach{seen.putIfAbsent(it.text.trim().lowercase(),it)}
  val selected=seen.values.filter{val c=(it.text.length/4).coerceAtLeast(1);if(used+c>budget)false else{used+=c;true}}
  val warnings=buildList{if(selected.any{it.trust==TrustLevel.EXTERNAL_CONTENT})add("EXTERNAL_CONTENT_PRESENT");if(selected.any{it.trust==TrustLevel.UNVERIFIED})add("UNVERIFIED_CONTEXT_PRESENT")}
  val body=selected.joinToString("\n\n"){ "[SOURCE=" + it.source + "; TRUST=" + it.trust + "; PROVENANCE=" + (it.provenance ?: "none") + "]\n" + it.text }
  return CompiledContext(body,selected,used,warnings)
 }
}