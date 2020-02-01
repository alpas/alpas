package dev.alpas.console

import dev.alpas.Environment
import dev.alpas.RESOURCES_DIRS

class LinkTemplatesCommand(env: Environment) :
    LinkResourcesCommand("link:templates", env, env.storagePath(*RESOURCES_DIRS, "templates"), "resources/templates") {
    override val docsUrl = "https://alpas.dev/docs/pebble-templates"
}
