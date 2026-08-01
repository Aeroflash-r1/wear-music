# pulse-server

Self-hosted media backend for the **Pulse** Wear OS app. Runs on your own machine —
most conveniently an old Android phone in **Termux** — and is reachable from your
watch over **Tailscale**. It uses **yt-dlp** as the YouTube/Music engine, so there
are no flaky third-party API instances to depend on.

## What it does

| Endpoint                 | Purpose                                        |
|--------------------------|------------------------------------------------|
| `GET /health`            | Health check, server + yt-dlp version, cache   |
| `GET /api/search?q=..`   | Search songs (`filter=music_songs`), albums (`music_albums`), channels |
| `GET /api/streams/{id}`  | Best audio stream URL for a video id           |
| `GET /api/track/{id}`    | Track metadata + direct stream URL             |
| `GET /api/playlist/{id}` | Playlist / album tracks                        |
| `GET /api/channel/{id}`  | Artist info + top tracks                       |
| `GET /api/trending`      | Trending feed                                  |

Results are cached in memory (search 15 min, streams/tracks 60 min, trending 30 min)
so YouTube isn't hammered and the watch gets instant repeat responses.

## Build

Requires JDK 17+.

```bash
cd server
./gradlew shadowJar
# fat jar: server/build/libs/pulse-server-all.jar
```

If you don't have a Gradle wrapper, use any Gradle ≥ 8.5:
`gradle shadowJar` (or `gradle-9.3.1/bin/gradle shadowJar`).

## Run on your phone (Termux + Tailscale)

### 1. Install Termux dependencies

```bash
pkg update && pkg upgrade
pkg install openjdk-17 python ffmpeg deno
python3 -m pip install -U "yt-dlp[default]"
```

`ffmpeg` is optional but recommended (helps yt-dlp fallback formats).
`deno` helps yt-dlp solve YouTube's JS challenges.

### 2. Install Tailscale

```bash
pkg install tailscale
# Start the daemon in userspace-networking mode (no root needed):
tailscaled --tun=userspace-networking &
tailscale up
# Note the tailnet IP:
tailscale ip -4
```

### 3. Copy and run the server

```bash
# copy pulse-server-all.jar to the phone (e.g. scp from your computer, or
# download it), then:
cd ~
java -jar pulse-server-all.jar
```

Or use the bundled launcher (copy `run-server.sh` **next to** the jar, in the
same directory):

```bash
chmod +x run-server.sh
./run-server.sh
```

The server binds `0.0.0.0:8080` by default. Configure via env vars:

```bash
PORT=8080 YTDLP_BIN=yt-dlp YTDLP_TIMEOUT=90 java -jar pulse-server-all.jar
```

### 4. Verify

From any device on your tailnet:

```bash
curl http://<phone-tailnet-ip>:8080/health
```

## Connect the Pulse app

1. Open **Settings → Backend → Server URL** in the app.
2. Enter `http://<phone-tailnet-ip>:8080` (the URL from `tailscale ip -4`).
3. Tap **Connection Test** — it should report Connected.
4. Search, play, browse — everything now flows through your own server.

## Troubleshooting

- **Search returns nothing / 404**: make sure `yt-dlp` is installed and works:
  `yt-dlp --version`. YouTube sometimes rate-limits; wait and retry.
- **Server unreachable from watch**: both devices must be on the same tailnet and
  the phone's Tailscale must be up (`tailscale status`).
- **Slow first search**: yt-dlp resolves each result; subsequent identical queries
  are served from cache.
