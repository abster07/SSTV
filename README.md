# 📺 StreamVault — Android Smart TV IPTV App

A production-grade **Android TV / Smart TV** live streaming app built with **Kotlin + Jetpack Compose**, powered by the [iptv-org public API](https://iptv-org.github.io/api/).

---

## ✨ Features

### 🎬 Streaming Engine
| Feature | Implementation |
|---|---|
| HLS streams | Media3 ExoPlayer `HlsMediaSource` |
| DASH streams | Media3 ExoPlayer `DashMediaSource` |
| Progressive | Media3 `ProgressiveMediaSource` |
| **FFmpeg audio** | `media3-decoder-ffmpeg` — MP2, AC3, EAC3, Opus, Vorbis, Flac |
| Hardware decode | `DefaultRenderersFactory` GPU acceleration |
| Per-stream headers | Custom `OkHttp` interceptors for `Referer` + `User-Agent` |
| Auto stream detection | URL-based format sniffing (`.m3u8`, `.mpd`, progressive) |

### 📡 API Integration (iptv-org)
| Endpoint | Usage |
|---|---|
| `/channels.json` | Full channel metadata, country, categories |
| `/streams.json` | HLS/DASH/progressive stream URLs per channel |
| `/logos.json` | High-res channel logos (SVG/PNG/WebP) |
| `/categories.json` | 30+ categories with emoji icons |
| `/countries.json` | 200+ countries with flags |
| `/feeds.json` | Multi-feed per channel (HD, SD, regional) |
| `/blocklist.json` | DMCA/NSFW auto-filtering |
| `/guides.json` | EPG program guide metadata |

### 🎨 UI / UX
- **Cinematic dark TV UI** — optimised for 10-foot viewing distance
- Animated **collapsible side navigation** that expands on focus
- **Featured carousel** — top 10 channels with backdrop blur effect
- **Category chips** — 30+ categories with emoji icons + color coding
- **Country filter row** — browse 200+ countries with flag emoji
- **6-column responsive channel grid**
- **Animated focus rings** with glow effect + spring scale physics
- Pulsing **LIVE badge** and quality badges (1080p / 720p / 480p)
- Smooth page transitions (fade + slide)
- **Auto-hide player controls** (5 second timer)
- Fullscreen **cinematic player** with gradient overlays
- **Stream picker panel** — slide-in panel listing all available feeds
- **Quality selector** — inline radio list

### ⚙️ Customisation
| Option | Values |
|---|---|
| **Theme** | Dark · AMOLED · Midnight Blue · Forest |
| **Accent color** | Cyan · Orange · Pink · Green · Gold |
| **Stream quality** | Auto · 1080p · 720p · 480p · 360p |
| **Buffer size** | 2s · 5s · 10s · 15s · 30s |
| **Audio language** | 15 languages — English, French, Arabic, Chinese… |
| **Decoder** | Hardware GPU · Software · FFmpeg-prefer |
| **Subtitles** | On / Off |
| **Content filter** | NSFW toggle |
| **Parental PIN** | 4-digit PIN with virtual keypad |
| **Auto-play next** | On / Off |
| **EPG Guide** | On / Off |
| **Clock display** | On / Off |

### 🌍 Multi-language UI
String resources provided in:
- 🇬🇧 English (base)
- 🇫🇷 French (`values-fr`)
- 🇩🇪 German (`values-de`)
- 🇪🇸 Spanish (`values-es`)
- 🇸🇦 Arabic (`values-ar`) — RTL ready

### 💾 Local Persistence
- **Room** — Favorites database + watch history (50 entries)
- **DataStore Preferences** — all settings survive app restarts
- Channel favorites toggle with instant UI feedback

---

## 🏗️ Architecture

```
StreamVault
├── data/
│   ├── api/          Retrofit IptvApiService + Room Database/DAOs
│   ├── model/        DTOs, Domain models, Room entities, Settings
│   └── repository/   ChannelRepository, SettingsRepository
├── di/               Hilt AppModule (network, DB, singletons)
├── player/           StreamPlayerManager (ExoPlayer + FFmpeg)
└── ui/
    ├── components/   FocusableCard, ChannelCard, CategoryChip,
    │                 SearchBar, LiveBadge, QualityBadge, NavItem…
    ├── screens/
    │   ├── home/     HomeScreen + HomeViewModel
    │   ├── player/   PlayerScreen + PlayerViewModel
    │   └── settings/ SettingsScreen + SettingsViewModel
    ├── theme/        Colors, Dimens, TextStyles, CategoryIcons
    └── AppNavigation.kt  NavHost + SideNavigation + FavoritesScreen
```

**Stack:** MVVM + Repository pattern · StateFlow · Hilt DI · Coroutines

---

## 🚀 Getting Started

### Prerequisites
- Android Studio Hedgehog or newer
- Android SDK 34
- JDK 17

### Build

```bash
git clone https://github.com/abhinav0014/StreamVault.git
cd StreamVault
./gradlew assembleDebug
```

### Install on TV / Emulator

```bash
adb connect <tv-ip>:5555
adb install app/build/outputs/apk/debug/app-debug.apk
```

### TV Emulator Setup
- AVD: **Android TV (1080p)** or **Google TV**
- API Level: 26+

---

## 📦 Dependencies

```toml
# Core
androidx.core-ktx         1.13.1
Jetpack Compose BOM       2024.05.00
androidx.tv:tv-material   1.0.0-alpha11

# Player
media3-exoplayer          1.3.1
media3-exoplayer-hls      1.3.1
media3-exoplayer-dash     1.3.1
media3-decoder-ffmpeg     1.3.1   ← FFmpeg audio codec extension

# Network
Retrofit                  2.11.0
OkHttp                    4.12.0
Gson                      2.10.1

# DI
Hilt                      2.51.1

# Local storage
Room                      2.6.1
DataStore Preferences     1.1.1

# Images
Coil                      2.6.0

# Navigation
Navigation Compose        2.7.7
```

---

## 🎮 TV Remote Navigation

| Button | Action |
|---|---|
| D-Pad | Focus navigation |
| Centre / OK | Select / Play |
| Back | Back / Close menu |
| Menu | Show controls (in player) |

Focus management uses Compose's built-in `FocusRequester` + `onFocusChanged` with spring-physics scale animations. Every interactive element has clear focus ring feedback with the selected accent colour.

---

## 🔧 FFmpeg Audio Codec Support

StreamVault uses the `media3-decoder-ffmpeg` extension to handle non-standard audio codecs common in IPTV streams:

| Codec | Standard ExoPlayer | With FFmpeg |
|---|---|---|
| MP2 | ❌ | ✅ |
| AC3 (Dolby) | Limited | ✅ |
| E-AC3 | Limited | ✅ |
| Opus | ✅ | ✅ (prefer) |
| Vorbis | ✅ | ✅ (prefer) |
| FLAC | ✅ | ✅ (prefer) |
| PCM | ✅ | ✅ |

The FFmpeg renderer mode is set to `EXTENSION_RENDERER_MODE_PREFER`, meaning FFmpeg is tried first for all supported codecs, falling back to the platform decoder.

---

## 📝 Licence
MIT — free to fork, modify, and distribute.
Data provided by [iptv-org](https://github.com/iptv-org/iptv) under MIT licence.
