package dev.alpas.http

// todo: Change the name of this class, I don't like it
data class RequestError(val attribute: String, val value: Any? = null, val message: String)
