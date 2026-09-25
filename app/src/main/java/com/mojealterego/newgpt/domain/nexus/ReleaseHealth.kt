package com.mojealterego.newgpt.domain.nexus
data class ReleaseGate(val name:String,val passed:Boolean,val details:String)
class ReleaseHealthGate{fun ready(gates:List<ReleaseGate>)=gates.isNotEmpty()&&gates.all{it.passed}}