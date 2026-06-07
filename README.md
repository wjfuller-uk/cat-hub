# 🐱 Cat Hub

**Your family, as cats, on the wall, that talk back.**

Cat Hub is a family command centre for Fire tablets. Each family member is represented by a pixel-art cat that reacts to their schedule, location, weather, and voice commands. Powered by [Hermes](https://github.com/nousresearch/hermes-agent) for AI.

## What It Does

- **Family as cats** — each person is a unique pixel-art cat with dynamic states
- **Always-on display** — dedicated launcher, kiosk mode, always visible
- **Voice control** — "Jarvis, what's Imogen doing today?" → her cat wakes up and tells you
- **Smart reactions** — cats sleep at night, work during school hours, celebrate birthdays
- **Proactive alerts** — "School run in 30 minutes" → cat starts getting ready
- **Location aware** — knows who's home and who's out
- **Weather reactive** — cats react to rain, sun, snow

## Architecture

```
[Fire Tablet - Cat Hub App]
    ├── CatView (Jetpack Compose + Canvas)
    ├── Dashboard (time, weather, schedule)
    ├── VoiceController (Porcupine + TTS)
    └── HermesBridge (WebSocket to relay)

[Hermes Relay Server]
    ├── android_relay.py (WebSocket bridge)
    ├── android_voice.py (voice pipeline)
    └── hermes -z (LLM brain)

[External Services]
    ├── Google Calendar
    ├── Weather API
    └── DeepSeek LLM
```

## Tech Stack

- **Kotlin** + **Jetpack Compose** — modern Android UI
- **Porcupine v1.9.5** — wake word detection ("Jarvis")
- **Hermes Relay** — WebSocket bridge to AI brain
- **Google Calendar API** — schedule data
- **OpenWeather API** — weather data
- **Room DB** — local cache

## Project Structure

```
cat-hub/
├── docs/
│   ├── PRD.md                    # Product Requirements Document
│   └── plans/                    # Implementation plans
├── app/
│   └── src/
│       └── main/
│           ├── java/com/cathub/
│           │   ├── CatHubApp.kt          # Application class
│           │   ├── MainActivity.kt       # Launcher activity
│           │   ├── ui/
│           │   │   ├── cats/             # Cat rendering & animations
│           │   │   ├── dashboard/        # Dashboard widgets
│           │   │   ├── world/            # Background & ambient
│           │   │   └── theme/            # Colours, typography
│           │   ├── data/
│           │   │   ├── model/            # Data classes
│           │   │   ├── calendar/         # Google Calendar integration
│           │   │   ├── weather/          # Weather API
│           │   │   └── location/         # GPS via relay
│           │   ├── voice/
│           │   │   ├── PorcupineWakeWord.kt
│           │   │   ├── TtsEngine.kt
│           │   │   └── VoiceController.kt
│           │   ├── hermes/
│           │   │   ├── RelayClient.kt    # WebSocket connection
│           │   │   └── HermesBridge.kt   # Voice + commands
│           │   └── kiosk/
│           │       ├── CatHubLauncher.kt # Launcher replacement
│           │       └── KioskMode.kt      # Immersive mode
│           ├── res/
│           │   ├── drawable/             # Cat pixel art assets
│           │   ├── raw/                  # Porcupine model
│           │   └── values/               # Strings, colours
│           └── AndroidManifest.xml
├── build.gradle.kts
├── settings.gradle.kts
├── README.md
├── .gitignore
└── LICENSE
```

## Getting Started

### Prerequisites

- Android Studio (latest)
- Fire HD 10 tablet (or any Android device for dev)
- Hermes relay server running
- Picovoice account (for Porcupine wake word)

### Build

```bash
# Clone
git clone https://github.com/tbf-marketing/cat-hub.git
cd cat-hub

# Build
./gradlew assembleDebug

# Install
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

### Configure

1. Set relay server URL in `local.properties`:
   ```
   RELAY_URL=ws://your-server:8766
   ```

2. Set Picovoice API key in `local.properties`:
   ```
   PORCUPINE_KEY=your-api-key
   ```

3. Grant permissions:
   - `SYSTEM_ALERT_WINDOW` — for cat overlay
   - `RECORD_AUDIO` — for wake word
   - `ACCESS_FINE_LOCATION` — for location awareness

## Development Phases

| Phase | Goal | Timeline |
|-------|------|----------|
| 1. Foundation | Basic cat launcher + voice | Week 1-2 |
| 2. Dynamic States | Calendar + time + weather | Week 3-4 |
| 3. Location & Context | GPS + face recognition | Week 5-6 |
| 4. Polish & Delight | Animations + sounds + messaging | Week 7-8 |

## Related Projects

- **[hermes-voice](https://github.com/tbf-marketing/hermes-voice)** — Original TV companion app (prototype)
- **[Hermes Agent](https://github.com/nousresearch/hermes-agent)** — AI assistant framework

## License

MIT

---

*Built with ❤️ for the family.*
