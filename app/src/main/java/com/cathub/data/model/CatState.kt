package com.cathub.data.model

/**
 * Cat state machine — defines all possible states for a cat avatar.
 * Each state maps to a different animation and behaviour.
 */
enum class CatState {
    /** Night time / Do Not Disturb — eyes closed, breathing animation */
    SLEEPING,

    /** Morning / alarm — stretching, yawning */
    WAKING,

    /** Default state — sitting, looking around */
    IDLE,

    /** Moving around the screen */
    WALKING,

    /** Calendar event active — typing, reading, concentrating */
    WORKING,

    /** Free time / weekend — running, jumping, chasing */
    PLAYING,

    /** Meal times — munching animation */
    EATING,

    /** Birthday / special event — jumping, sparkles */
    EXCITED,

    /** Rainy day / cancelled event — ears down, rain drops */
    SAD,

    /** Hermes speaking — mouth animation, speech bubble */
    TALKING,

    /** Processing request — thought bubble with "..." */
    THINKING,

    /** Greeting / arrival — wave animation */
    WAVING,
}

/**
 * World state — ambient conditions that affect all cats and the background.
 */
data class WorldState(
    val timeOfDay: TimeOfDay,
    val weather: Weather,
    val season: Season,
    val specialEvent: SpecialEvent? = null,
)

enum class TimeOfDay {
    DAWN,       // 5-7am
    MORNING,    // 7-12pm
    AFTERNOON,  // 12-5pm
    EVENING,    // 5-8pm
    DUSK,       // 8-9pm
    NIGHT,      // 9pm-5am
}

enum class Weather {
    SUNNY,
    CLOUDY,
    RAINY,
    SNOWY,
    STORMY,
}

enum class Season {
    SPRING,
    SUMMER,
    AUTUMN,
    WINTER,
}

enum class SpecialEvent {
    BIRTHDAY,
    CHRISTMAS,
    NEW_YEAR,
    HALLOWEEN,
    EASTER,
    SCHOOL_HOLIDAY,
}

/**
 * Transition rules — maps context to cat state.
 */
object CatStateRules {

    /**
     * Determine cat state based on family member context.
     */
    fun determineState(
        member: FamilyMember,
        worldState: WorldState,
        hasActiveNotification: Boolean = false,
    ): CatState {
        // Special events override everything
        if (worldState.specialEvent == SpecialEvent.BIRTHDAY && member.mood == Mood.EXCITED) {
            return CatState.EXCITED
        }

        // Night time = sleeping
        if (worldState.timeOfDay == TimeOfDay.NIGHT) {
            return CatState.SLEEPING
        }

        // Morning transition
        if (worldState.timeOfDay == TimeOfDay.DAWN) {
            return CatState.WAKING
        }

        // Active calendar event
        if (member.currentActivity?.isOngoing == true) {
            return when (member.currentActivity.category) {
                ActivityCategory.WORK -> CatState.WORKING
                ActivityCategory.SCHOOL -> CatState.WORKING
                ActivityCategory.EATING -> CatState.EATING
                ActivityCategory.PLAYING -> CatState.PLAYING
                ActivityCategory.SLEEPING -> CatState.SLEEPING
                else -> CatState.IDLE
            }
        }

        // Rainy weather = sad
        if (worldState.weather == Weather.RAINY || worldState.weather == Weather.STORMY) {
            return CatState.SAD
        }

        // Weekend / free time = playing
        if (member.mood == Mood.HAPPY || member.mood == Mood.EXCITED) {
            return CatState.PLAYING
        }

        // Default
        return CatState.IDLE
    }
}
