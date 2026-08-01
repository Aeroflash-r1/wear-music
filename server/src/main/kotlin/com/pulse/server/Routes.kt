package com.pulse.server

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.response.respond
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

fun Application.configureJson() {
    install(ContentNegotiation) {
        json(Json {
            ignoreUnknownKeys = true
            prettyPrint = false
        })
    }
}

fun Application.configureErrors() {
    install(StatusPages) {
        exception<Throwable> { call, cause ->
            call.respond(HttpStatusCode.InternalServerError, ApiError(cause.message ?: "Internal error"))
        }
    }
}

fun Application.configureRoutes(client: YtDlpClient) {
    routing {
        get("/") {
            call.respond(mapOf("name" to "pulse-server", "status" to "ok"))
        }

        get("/health") {
            call.respond(client.health())
        }

        get("/api/search") {
            val q = call.request.queryParameters["q"]
            if (q.isNullOrBlank()) {
                return@get call.respond(HttpStatusCode.BadRequest, ApiError("Missing query parameter 'q'"))
            }
            val filter = call.request.queryParameters["filter"]
            val items = client.search(q, filter)
            // 200 + empty list (not 404) so the app can show "No results" instead of
            // treating a legitimately-empty search as a backend failure.
            call.respond(mapOf("items" to items))
        }

        get("/api/streams/{id}") {
            val id = call.parameters["id"] ?: return@get call.respond(HttpStatusCode.BadRequest, ApiError("Missing id"))
            val stream = client.getAudioStream(id)
            if (stream == null) {
                call.respond(HttpStatusCode.NotFound, ApiError("Could not resolve audio stream for $id"))
            } else {
                call.respond(stream)
            }
        }

        get("/api/track/{id}") {
            val id = call.parameters["id"] ?: return@get call.respond(HttpStatusCode.BadRequest, ApiError("Missing id"))
            val track = client.getTrack(id)
            if (track == null) {
                call.respond(HttpStatusCode.NotFound, ApiError("Could not resolve track $id"))
            } else {
                call.respond(track)
            }
        }

        get("/api/playlist/{id}") {
            val id = call.parameters["id"] ?: return@get call.respond(HttpStatusCode.BadRequest, ApiError("Missing id"))
            val playlist = client.getPlaylist(id)
            if (playlist == null) {
                call.respond(HttpStatusCode.NotFound, ApiError("Could not resolve playlist $id"))
            } else {
                call.respond(playlist)
            }
        }

        get("/api/channel/{id}") {
            val id = call.parameters["id"] ?: return@get call.respond(HttpStatusCode.BadRequest, ApiError("Missing id"))
            val artist = client.getArtist(id)
            if (artist == null) {
                call.respond(HttpStatusCode.NotFound, ApiError("Could not resolve channel $id"))
            } else {
                call.respond(artist)
            }
        }

        get("/api/trending") {
            val items = client.getTrending()
            call.respond(mapOf("items" to items))
        }
    }
}
