package com.pulse.server

/**
 * Runtime configuration, read from environment variables so the same fat jar can
 * run anywhere (Termux, Docker, a VPS) without recompiling.
 *
 *   PORT          - HTTP port to bind (default 8080)
 *   HOST          - bind address (default 0.0.0.0 — required so Tailscale can reach it)
 *   YTDLP_BIN     - path to the yt-dlp binary (default "yt-dlp")
 *   YTDLP_TIMEOUT - seconds to wait for a yt-dlp call before killing it (default 90)
 */
data class ServerConfig(
    val port: Int = env("PORT")?.toIntOrNull()?.coerceIn(1, 65_535) ?: 8080,
    val host: String = env("HOST") ?: "0.0.0.0",
    val ytDlpBin: String = env("YTDLP_BIN")?.trim()?.takeIf { it.isNotEmpty() } ?: "yt-dlp",
    val ytDlpTimeoutSeconds: Long = env("YTDLP_TIMEOUT")?.toLongOrNull()?.coerceIn(1L, 600L) ?: 90L
) {
    companion object {
        private fun env(name: String): String? = System.getenv(name)?.takeIf { it.isNotBlank() }

        fun fromEnv(): ServerConfig = ServerConfig()
    }
}
