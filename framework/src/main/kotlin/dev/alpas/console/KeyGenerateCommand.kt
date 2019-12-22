package dev.alpas.console

import dev.alpas.base64Encoded
import dev.alpas.printAsSuccess
import dev.alpas.secureRandomString

class KeyGenerateCommand : Command(name = "key:generate", help = "Generate a base64 encoded API key.") {
    override fun run() {
        success("base64:${secureRandomString(32).base64Encoded()}")
    }
}
