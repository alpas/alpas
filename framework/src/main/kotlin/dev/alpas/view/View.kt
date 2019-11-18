package dev.alpas.view

import com.mitchellbosecke.pebble.extension.AbstractExtension

open class View(val name: String, val args: Map<String, Any?>? = null)
open class ViewExtension : AbstractExtension()
