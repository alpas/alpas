package dev.alpas.console

import dev.alpas.AppConfig
import dev.alpas.Environment

class LinkWebCommand(env: Environment, config: AppConfig) :
    LinkResourcesCommand("link:web", env, config.storageWebDirectory, "resources/web") {
    override val docsUrl = "https://alpas.dev/docs/mixing-assets"
}
