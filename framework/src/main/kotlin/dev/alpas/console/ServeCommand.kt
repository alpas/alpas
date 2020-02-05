package dev.alpas.console

import dev.alpas.AppConfig
import dev.alpas.asBlue

class ServeCommand(config: AppConfig) :
    Command(help = "Serve your app at ${"https://localhost:${config.appPort}".asBlue()}", name = "serve")
