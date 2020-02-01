package dev.alpas.http

import dev.alpas.validation.SharedDataBag

data class RenderContext(
    val call: HttpCall,
    val sharedDataBag: SharedDataBag,
    val viewName: String? = null
)
