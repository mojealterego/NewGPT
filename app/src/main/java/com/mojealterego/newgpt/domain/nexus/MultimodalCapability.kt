package com.mojealterego.newgpt.domain.nexus
enum class MediaKind{TEXT,IMAGE,PDF,DOCUMENT,AUDIO,VIDEO,URL,MODEL}
data class MediaInput(val id:String,val kind:MediaKind,val mime:String,val name:String,val sizeBytes:Long)
data class MediaAnalysis(val id:String,val kind:MediaKind,val summary:String,val extractedText:String?=null)
class MultimodalCapability{fun accepts(kind:MediaKind)=kind in MediaKind.entries}