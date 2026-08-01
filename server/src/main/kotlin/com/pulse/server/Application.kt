package com.pulse.server

import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty

fun main() {
    val config = ServerConfig.fromEnv()
    val client = YtDlpClient(config)

    println("pulse-server starting on http://${config.host}:${config.port} (yt-dlp: ${config.ytDlpBin})")

    embeddedServer(Netty, port = config.port, host = config.host) {
        configureJson()
        configureErrors()
        configureRoutes(client)
    }.start(wait = true)
}
