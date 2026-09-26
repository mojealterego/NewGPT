package com.mojealterego.newgpt.domain.assistant

data class PaulaVideoAsset(
    val id: String,
    val resourceName: String,
    val loop: Boolean = true,
    val muted: Boolean = true,
    val description: String = ""
)

object PaulaVideoAssets {
    val default = PaulaVideoAsset(
        id = "paula-default",
        resourceName = "paula_default",
        description = "Placeholder for the video file supplied by the user."
    )
}
