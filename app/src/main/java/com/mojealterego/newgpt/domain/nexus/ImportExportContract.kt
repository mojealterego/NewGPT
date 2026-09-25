package com.mojealterego.newgpt.domain.nexus
data class ImportCheck(val compatible:Boolean,val warnings:List<String>,val errors:List<String>)
class ImportExportContract{
 fun validate(version:Int,current:Int,encrypted:Boolean):ImportCheck{
  val e=mutableListOf<String>();if(version>current)e+="FUTURE_VERSION";if(!encrypted)e+="UNENCRYPTED_ARCHIVE"
  return ImportCheck(e.isEmpty(),if(version<current)listOf("MIGRATION_REQUIRED")else emptyList(),e)
 }
}