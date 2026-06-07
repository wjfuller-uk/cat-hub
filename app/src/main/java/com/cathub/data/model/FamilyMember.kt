package com.cathub.data.model

import androidx.compose.ui.graphics.Color

/**
 * Family member data for Cat Hub.
 * Each person is represented by a unique cat with distinct appearance.
 */
data class FamilyMember(
    val id: String,
    val name: String,
    val displayName: String,
    val role: FamilyRole,
    val catProfile: CatProfile,
    val calendarId: String?,        // Google Calendar ID
    val deviceId: String?,          // Phone device ID for location tracking
    val isHome: Boolean = true,     // Location-based
    val currentActivity: Activity? = null,
    val mood: Mood = Mood.NEUTRAL,
)

enum class FamilyRole {
    DAD,
    MUM,
    CHILD,
    PET,        // For actual pets (future)
}

/**
 * Cat visual appearance — defines how each family member's cat looks.
 */
data class CatProfile(
    val baseColor: Color,           // Primary fur colour
    val pattern: CatPattern = CatPattern.SOLID,
    val eyeColor: Color = Color(0xFF4CAF50),  // Default green eyes
    val accessories: List<Accessory> = emptyList(),
    val size: CatSize = CatSize.MEDIUM,
    val spriteSheet: String,        // Path to sprite sheet drawable
)

enum class CatPattern {
    SOLID,          // Single colour
    STRIPED,        // Tabby stripes
    SPOTTED,        // Spots
    TUXEDO,         // Black and white
    CALICO,         // Multi-colour
    SIAMESE,        // Light body, dark points
}

enum class CatSize {
    SMALL,          // Child cat (Imogen)
    MEDIUM,         // Adult cat (default)
    LARGE,          // Larger adult (optional)
}

enum class Accessory {
    GLASSES,
    HAT,
    SCARF,
    BOW,
    BELL,
    COLLAR,
}

/**
 * Activity derived from calendar or context.
 */
data class Activity(
    val title: String,
    val category: ActivityCategory,
    val isOngoing: Boolean = false,
)

enum class ActivityCategory {
    WORK,
    SCHOOL,
    PERSONAL,
    FAMILY,
    SLEEPING,
    EATING,
    PLAYING,
}

/**
 * Mood derived from context (weather, schedule, time).
 */
enum class Mood {
    HAPPY,
    NEUTRAL,
    TIRED,
    EXCITED,
    SAD,
    BUSY,
}

/**
 * Predefined family members for the Fuller family.
 */
object FullerFamily {

    val WILL = FamilyMember(
        id = "will",
        name = "Will",
        displayName = "Dad",
        role = FamilyRole.DAD,
        catProfile = CatProfile(
            baseColor = Color(0xFFFF9800),  // Orange
            pattern = CatPattern.STRIPED,
            eyeColor = Color(0xFF2196F3),   // Blue eyes
            accessories = listOf(Accessory.GLASSES),
            size = CatSize.LARGE,
            spriteSheet = "drawable/cats/will_cat",
        ),
        calendarId = null,  // Set during onboarding
        deviceId = null,    // Set during onboarding
    )

    val LUCY = FamilyMember(
        id = "lucy",
        name = "Lucy",
        displayName = "Mum",
        role = FamilyRole.MUM,
        catProfile = CatProfile(
            baseColor = Color(0xFFFFFFFF),  // White
            pattern = CatPattern.SOLID,
            eyeColor = Color(0xFF4CAF50),   // Green eyes
            accessories = listOf(Accessory.BOW),
            size = CatSize.MEDIUM,
            spriteSheet = "drawable/cats/lucy_cat",
        ),
        calendarId = null,
        deviceId = null,
    )

    val IMOGEN = FamilyMember(
        id = "imogen",
        name = "Imogen",
        displayName = "Immy",
        role = FamilyRole.CHILD,
        catProfile = CatProfile(
            baseColor = Color(0xFF9E9E9E),  // Grey
            pattern = CatPattern.SOLID,
            eyeColor = Color(0xFFFFEB3B),   // Yellow eyes
            accessories = listOf(Accessory.SCARF),
            size = CatSize.SMALL,
            spriteSheet = "drawable/cats/imogen_cat",
        ),
        calendarId = null,
        deviceId = null,
    )

    /**
     * All family members in display order.
     */
    val ALL = listOf(WILL, LUCY, IMOGEN)
}
