package dev.alpas.console

import dev.alpas.AppConfig

class ServeCommand(config: AppConfig) :
    Command(help = "Serve your app at https://localhost:${config.appPort}", name = "serve")
