# Product Requirements Document
# Cat Hub — Family Command Centre

**Version:** 1.0
**Date:** 2026-06-06
**Author:** Will + Hermes
**Status:** Draft

---

## 1. Vision

A Fire HD 10 tablet mounted in the kitchen/living room that shows your family as living cat characters. Each cat represents a family member — they sleep when you're asleep, work when you're at work, celebrate when it's your birthday, and come alive when you talk to them.

It's a **family command centre** disguised as a virtual pet game. The cats are the interface. Hermes is the brain.

**In one sentence:** *"Your family, as cats, on the wall, that talk back."*

## 2. Problem Statement

Current smart displays (Echo Show, Google Nest Hub) are:
- **Impersonal** — show generic clocks, weather, ads
- **Disconnected** — don't know who's looking at them
- **Passive** — wait to be asked, don't proactively help
- **Ugly** — no personality, no warmth, no joy

Families need a shared screen that:
- Shows who's home and who's out
- Highlights today's schedule for each person
- Proactively alerts ("school run in 30 minutes")
- Responds to voice naturally
- Feels alive, not like a dashboard

## 3. Target Users

| User | Role | Needs |
|------|------|-------|
| Will | Primary user, parent | See family status at glance, voice control, reminders |
| Imogen | Child | See her schedule, fun interactions, feel represented |
| Future family members | Expandable | Easy to add new cats |

## 4. Core Concept

### The Cats

Each family member is represented by a **pixel-art cat** with:
- **Unique appearance** — colour, pattern, accessories (glasses, hat, scarf)
- **Personality traits** — derived from real schedule/behaviour
- **Dynamic states** — tied to real-world data (calendar, time, location, weather)
- **Interactions** — respond to touch, voice, and each other

### The World

The cats live in a **shared environment** that changes with:
- **Time of day** — dawn, day, dusk, night (background lighting)
- **Weather** — sunny, rainy, cloudy, snowy (ambient effects)
- **Seasons** — spring blossoms, summer sun, autumn leaves, winter snow
- **Special events** — birthdays, holidays, school holidays

### The Brain (Hermes)

Hermes powers:
- **Voice commands** — "What's Imogen doing today?"
- **Proactive alerts** — "School run in 30 minutes"
- **Data fusion** — combining calendar + location + weather into cat behaviours
- **Notifications** — messages appear as speech bubbles above the relevant cat

## 5. Feature Specification

### 5.1 Cat Avatars

**Description:** Each family member has a unique pixel-art cat.

**Requirements:**
- Pixel art style (16x16 or 32x32 base, scaled up)
- Customisable: colour, pattern, eyes, accessories
- Smooth animations: idle, sleeping, walking, sitting, playing, eating
- Each cat has a name tag below
- Tap a cat to see their detail card (schedule, location, mood)

**States:**
| State | Trigger | Animation |
|-------|---------|-----------|
| Sleeping | Night time / Do Not Disturb | Eyes closed, breathing animation |
| Waking | Morning / alarm | Stretching, yawning |
| Working | Calendar event active | Typing, reading, concentrating |
| Playing | Free time / weekend | Running, jumping, chasing |
| Eating | Meal times | Munching animation |
| Excited | Birthday / special event | Jumping, sparkles |
| Sad | Rainy day / cancelled event | Ears down, rain drops |
| Talking | Hermes speaking | Mouth animation, speech bubble |
| Waving | Greeting / arrival | Wave animation |
| Thinking | Processing request | Thought bubble with "..." |

### 5.2 Family Dashboard

**Description:** Always-visible overview of the family.

**Requirements:**
- Current time and date (large, readable from distance)
- Weather widget with icon and temperature
- Today's schedule for each family member (next 3 events)
- Who's home / who's out (location-based)
- Upcoming reminders and alerts
- Ambient background that changes with time/weather

### 5.3 Voice Control (Hermes Integration)

**Description:** Talk to the cats via Hermes.

**Requirements:**
- Wake word: "Jarvis" (Porcupine v1.9.5)
- "What's Imogen doing today?" → Imogen's cat wakes up, shows schedule
- "Tell everyone dinner's ready" → all cats look up, notification sent
- "What's the weather?" → cat looks at sky, weather shown
- "Set a reminder for 3pm" → cat nods, reminder created
- Natural conversation via Hermes LLM
- TTS response played through tablet speaker
- Visual feedback: speech bubbles above the talking cat

### 5.4 Notifications & Alerts

**Description:** Proactive notifications displayed as cat interactions.

**Requirements:**
- Calendar reminders → relevant cat gets alert animation
- School run reminder (30 min before) → Imogen's cat starts getting ready
- Dinner time → all cats gather
- Weather alerts → cats react to weather changes
- Custom family messages → speech bubble above recipient's cat
- Notification history (swipe up to see recent)

### 5.5 Calendar Integration

**Description:** Each cat's schedule comes from Google Calendar.

**Requirements:**
- One Google Calendar per family member (or colour-coded events)
- Sync every 15 minutes
- Show today's events on the cat's detail card
- Upcoming events shown on main screen
- All-day events vs timed events
- School schedule for Imogen
- Work schedule for Will

### 5.6 Location Awareness

**Description:** Know who's home and who's out.

**Requirements:**
- Phone GPS via Hermes relay (existing infrastructure)
- "Home" zone defined by geofence
- Cat state updates when family member arrives/leaves
- Privacy: location only shared with family, stored locally
- Fallback: manual "I'm home" / "I'm out" via voice or tap

### 5.7 Face Recognition (Phase 2)

**Description:** Know who's looking at the screen.

**Requirements:**
- Front camera + Android FaceDetector (on-device, no cloud)
- Personalised greetings: "Good morning, Will!"
- Show relevant info for the person looking
- Privacy: faces not stored, processed in real-time only
- Opt-in per family member

### 5.8 Kiosk Mode

**Description:** Tablet is always-on, dedicated to Cat Hub.

**Requirements:**
- Replace default launcher
- Immersive mode (hide status bar, nav bar)
- Screen timeout: never (or wake-on-motion)
- Auto-start on boot
- No other apps accessible (unless admin gesture)
- Battery optimisation: dim screen when no activity

## 6. Technical Architecture

### 6.1 System Overview

```
┌─────────────────────────────────────────────────────────┐
│                    Fire HD 10 Tablet                     │
│  ┌─────────────────────────────────────────────────────┐ │
│  │                  Cat Hub App                         │ │
│  │  ┌──────────┐  ┌──────────┐  ┌──────────────────┐  │ │
│  │  │ CatView  │  │Dashboard │  │ VoiceController  │  │ │
│  │  │(Compose) │  │  Widget  │  │ (Porcupine+TTS)  │  │ │
│  │  └────┬─────┘  └────┬─────┘  └────────┬─────────┘  │ │
│  │       │              │                  │            │ │
│  │  ┌────┴──────────────┴──────────────────┴─────────┐ │ │
│  │  │              CatHubViewModel                    │ │ │
│  │  │  (State management, data fusion, animations)   │ │ │
│  │  └────┬──────────────┬──────────────────┬─────────┘ │ │
│  │       │              │                  │            │ │
│  │  ┌────┴─────┐  ┌────┴─────┐  ┌────────┴─────────┐ │ │
│  │  │ Calendar │  │ Location │  │  HermesBridge    │ │ │
│  │  │ Provider │  │ Provider │  │  (WebSocket)     │ │ │
│  │  └──────────┘  └──────────┘  └──────────────────┘ │ │
│  └─────────────────────────────────────────────────────┘ │
│                           │                              │
│                    WebSocket (ws://)                      │
│                           │                              │
└───────────────────────────┼──────────────────────────────┘
                            │
                            ▼
┌───────────────────────────────────────────────────────────┐
│                   Hermes Relay Server                      │
│                   (37.120.187.94:8766)                     │
│  ┌──────────────┐  ┌──────────────┐  ┌────────────────┐  │
│  │ android_     │  │ android_     │  │  Hermes CLI    │  │
│  │ relay.py     │  │ voice.py     │  │  (LLM brain)   │  │
│  └──────────────┘  └──────────────┘  └────────────────┘  │
└───────────────────────────────────────────────────────────┘
                            │
                            ▼
┌───────────────────────────────────────────────────────────┐
│                   External Services                        │
│  ┌──────────────┐  ┌──────────────┐  ┌────────────────┐  │
│  │ Google       │  │ Weather API  │  │  DeepSeek LLM  │  │
│  │ Calendar     │  │ (OpenWeather)│  │                │  │
│  └──────────────┘  └──────────────┘  └────────────────┘  │
└───────────────────────────────────────────────────────────┘
```

### 6.2 Tech Stack

| Layer | Technology | Rationale |
|-------|-----------|-----------|
| Language | Kotlin | Android native, existing codebase |
| UI | Jetpack Compose | Modern declarative UI, smooth animations |
| Animations | Compose Animation + Canvas | Full control over pixel art rendering |
| State management | ViewModel + StateFlow | Reactive, lifecycle-aware |
| DI | Hilt | Standard Android DI |
| Networking | OkHttp + WebSocket | Existing relay integration |
| Calendar | Google Calendar API | Already integrated in Hermes |
| Location | FusedLocationProvider + relay | Phone GPS via existing relay |
| Wake word | Porcupine v1.9.5 | Already proven in hermes-voice |
| TTS | Android TTS + edge-tts (relay) | Existing voice pipeline |
| Storage | Room DB | Local cache for settings, history |
| Kiosk | Device Admin + Launcher | System-level kiosk mode |

### 6.3 Data Models

```kotlin
// Core family member
data class FamilyMember(
    val id: String,                    // unique ID
    val name: String,                  // "Will", "Imogen"
    val displayName: String,           // "Dad", "Immy"
    val catProfile: CatProfile,        // visual appearance
    val calendarId: String,            // Google Calendar ID
    val deviceId: String?,             // phone device ID for location
    val isHome: Boolean,               // location-based
    val currentActivity: Activity?,    // derived from calendar
    val mood: Mood,                    // derived from context
)

// Cat visual appearance
data class CatProfile(
    val baseColor: Color,              // primary fur colour
    val pattern: CatPattern,           // solid, striped, spotted, tuxedo
    val eyeColor: Color,
    val accessories: List<Accessory>,  // glasses, hat, scarf, bow
    val size: CatSize,                 // small (child), medium, large (adult)
)

// Cat state machine
enum class CatState {
    SLEEPING,      // night / DND
    WAKING,        // morning transition
    IDLE,          // default, sitting
    WALKING,       // moving around
    WORKING,       // calendar event active
    PLAYING,       // free time
    EATING,        // meal times
    EXCITED,       // special events
    SAD,           // bad weather / cancelled
    TALKING,       // Hermes speaking
    THINKING,      // processing
    WAVING,        // greeting
}

// Activity from calendar
data class Activity(
    val title: String,
    val startTime: Instant,
    val endTime: Instant?,
    val isAllDay: Boolean,
    val category: ActivityCategory,    // work, school, personal, family
)

// Ambient world state
data class WorldState(
    val timeOfDay: TimeOfDay,          // dawn, day, dusk, night
    val weather: Weather,              // sunny, cloudy, rainy, snowy
    val season: Season,                // spring, summer, autumn, winter
    val specialEvent: SpecialEvent?,   // birthday, holiday
)
```

### 6.4 Screen Layout

```
┌─────────────────────────────────────────────────────────────┐
│  ┌─────────┐                               ┌─────────────┐ │
│  │ 14:32   │      ☀️ 18°C                 │  🎂 Will's  │ │
│  │ Sat 6 Jun│                               │  Birthday!  │ │
│  └─────────┘                               └─────────────┘ │
│                                                             │
│    ┌───────┐         ┌───────┐         ┌───────┐          │
│    │ ╱╲   │         │ ╱╲   │         │ ╱╲   │          │
│    │(°°)  │         │(°°)  │         │(°°)  │          │
│    │ > <  │         │ > <  │         │ > <  │          │
│    │  ║   │         │  ║   │         │  ║   │          │
│    └──║───┘         └──║───┘         └──║───┘          │
│       ║                ║                ║                │
│     Will             Imogen           [Pet]             │
│    Working           School           Sleeping          │
│                                                             │
│  ┌─────────────────────────────────────────────────────┐ │
│  │ 📅 Today:                                           │ │
│  │  Will: Team standup (14:00), Pick up Imogen (15:30) │ │
│  │  Imogen: Maths (09:00), Art (11:00), Swimming (14:00)│ │
│  └─────────────────────────────────────────────────────┘ │
│                                                             │
│  💬 "Dinner in 30 minutes!" — Hermes                      │
│                                                             │
│  ┌─────────────────────────────────────────────────────┐ │
│  │ 🎤 [Tap to talk] or say "Jarvis..."                 │ │
│  └─────────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────────┘
```

## 7. Phased Delivery

### Phase 1: Foundation (Week 1-2)
**Goal:** Basic cat launcher with static avatars and Hermes voice.

- [ ] Project setup (Kotlin, Compose, Hilt, Room)
- [ ] Cat pixel art assets (base + animations for 3 states)
- [ ] FamilyMember data model + CatProfile
- [ ] CatView composable with basic animations (idle, sleeping, talking)
- [ ] Dashboard layout (time, weather, schedule)
- [ ] Hermes relay WebSocket integration
- [ ] Voice control (Porcupine wake word + TTS response)
- [ ] Kiosk mode (launcher replacement, immersive)
- [ ] Settings screen (family member management)

**Deliverable:** A working launcher that shows 2-3 cat avatars, responds to "Jarvis", and displays basic schedule info.

### Phase 2: Dynamic States (Week 3-4)
**Goal:** Cats react to real-world data.

- [ ] Google Calendar integration (per-person sync)
- [ ] Time-of-day state transitions (sleeping → waking → working → idle)
- [ ] Weather API integration
- [ ] Ambient background (time/weather/season)
- [ ] Cat detail card (tap to see full schedule)
- [ ] Notification routing (alerts above relevant cat)
- [ ] Proactive alerts ("school run in 30 min")

**Deliverable:** Cats move between states based on calendar and time. Weather affects the background.

### Phase 3: Location & Context (Week 5-6)
**Goal:** Know who's home, react to arrivals.

- [ ] Phone GPS via relay (location polling)
- [ ] Home zone geofence
- [ ] Arrival/departure cat animations
- [ ] Face recognition (Android FaceDetector)
- [ ] Personalised greetings
- [ ] Cat-to-cat interactions (grooming, playing together)
- [ ] Special event handling (birthdays, holidays)

**Deliverable:** Cats know who's home. Face recognition triggers personalised greetings.

### Phase 4: Polish & Delight (Week 7-8)
**Goal:** Make it feel alive.

- [ ] Advanced animations (seasonal, weather reactions)
- [ ] Sound effects (purrs, meows, ambient)
- [ ] Family messaging ("tell everyone dinner's ready")
- [ ] Activity history ("what did we do today?")
- [ ] Widget support (Android widgets for quick info)
- [ ] Backup/restore (family data, settings)
- [ ] Performance optimisation (battery, memory)

**Deliverable:** A polished, delightful family command centre.

## 8. Open Questions

| Question | Options | Decision |
|----------|---------|----------|
| Pixel art style | 16x16 vs 32x32 vs vector | TBD — 32x32 likely best balance |
| Cat design | Commission vs generate vs DIY | TBD — could use AI generation + manual refinement |
| Calendar approach | One shared calendar vs per-person | TBD — per-person is cleaner but more setup |
| Location accuracy | GPS polling vs geofence events | TBD — geofence is more battery-efficient |
| Pet cats | Just family or also real pets? | TBD — could add as "bonus" characters |
| Multiple tablets | Same view everywhere or different? | TBD — could sync state across devices |
| Voice personality | Generic vs per-cat voice | TBD — per-cat would be delightful but complex |

## 9. Success Metrics

| Metric | Target | How to Measure |
|--------|--------|----------------|
| Daily usage | Family looks at it 5+ times/day | Screen-on events |
| Voice interactions | 3+ voice commands/day | Hermes relay logs |
| Calendar accuracy | 95% of events shown correctly | Manual review |
| Uptime | 99%+ (always on) | Heartbeat monitoring |
| Response latency | <2s voice-to-response | Relay timestamps |
| Battery life | 8+ hours unplugged | Battery monitoring |
| Family approval | "It's cool" from Imogen | User feedback |

## 10. Risks & Mitigations

| Risk | Impact | Mitigation |
|------|--------|------------|
| Fire tablet performance | Slow animations, lag | Profile early, optimise Compose, use Canvas for heavy rendering |
| Battery drain from always-on | Tablet dies daily | Dim screen when idle, motion wake, efficient animations |
| Calendar sync failures | Stale schedule data | Cache locally, retry with backoff, show "last updated" time |
| Face recognition false positives | Wrong greeting | Confidence threshold, fallback to generic greeting |
| Family members don't like their cat | Rejection | Customisable avatars, let每个人 choose their cat |
| Porcupine accuracy on tablet mic | Missed wake words | Test early, tune sensitivity, fallback to tap-to-talk |
| Relay server downtime | No voice/location | Local fallback for basic functions, queue messages |

## 11. Non-Goals (v1)

- ❌ Multi-household support (just one family, one tablet)
- ❌ Social features (no sharing cats with friends)
- ❌ Gamification (no points, levels, achievements)
- ❌ In-app purchases (no cat accessories store)
- ❌ iOS support (Android only, Fire tablet target)
- ❌ Cloud sync (local-first, privacy-focused)
- ❌ Custom wake word (stick with "Jarvis")

## 12. Appendix

### A. Existing Infrastructure
- **Relay Server:** `ws://100.111.44.87:8766` (Hermes VPS)
- **Hermes CLI:** `hermes -z` one-shot mode for LLM
- **Voice Pipeline:** Whisper STT + edge-tts (in relay)
- **Phone GPS:** Available via `android_location` tool
- **Calendar:** Google Calendar via Hermes tools

### B. Reference Projects
- **hermes-voice** — Existing cat overlay + Porcupine + relay integration
- **WallPanel** — Launcher replacement pattern
- **TARS** — Vision integration reference

### C. Cat Art Inspiration
- Neko Atsume (cat collection game)
- Stardew Valley (pixel art pets)
- Animal Crossing (village + character interactions)
- Nyan Cat (classic pixel cat)

---

*This PRD is a living document. Update as decisions are made.*
