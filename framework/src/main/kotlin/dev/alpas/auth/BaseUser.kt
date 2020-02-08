package dev.alpas.auth

import dev.alpas.ozone.Ozone

interface BaseUser<E : BaseUser<E>> : Ozone<E>, Authenticatable {
    override var id: Long
    override var password: String
    override var email: String?
    var name: String?
}
