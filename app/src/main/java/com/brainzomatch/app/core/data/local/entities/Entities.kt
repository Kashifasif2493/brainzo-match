package com.brainzomatch.app.core.data.local.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Player Profile Entity storing gamer tag, level, currency, avatar selection, and wardrobe.
 * File Path: app/src/main/java/com/brainzomatch/app/core/data/local/entities/PlayerProfileEntity.kt
 */
@Entity(tableName = "player_profile")
data class PlayerProfileEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String = "primary_kid",

    @ColumnInfo(name = "display_name")
    val displayName: String = "Little Thinker",

    @ColumnInfo(name = "age")
    val age: Int = 8,

    @ColumnInfo(name = "avatar_id")
    val avatarId: String = "avatar_gamer_boy",

    @ColumnInfo(name = "aura_color_hex")
    val auraColorHex: String = "#7C3AED",

    @ColumnInfo(name = "coins")
    val coins: Int = 0,

    @ColumnInfo(name = "stars")
    val stars: Int = 0,

    @ColumnInfo(name = "streak_days")
    val streakDays: Int = 0,

    @ColumnInfo(name = "is_vip_member")
    val isVipMember: Boolean = false,

    @ColumnInfo(name = "unlocked_avatar_ids", defaultValue = "[]")
    val unlockedAvatarIds: List<String> = listOf("avatar_gamer_boy", "avatar_cyber_girl", "avatar_astronaut"),

    @ColumnInfo(name = "equipped_hat_id", defaultValue = "'item_crown'")
    val equippedHatId: String = "item_crown",

    @ColumnInfo(name = "equipped_accessory_id", defaultValue = "''")
    val equippedAccessoryId: String = "",

    @ColumnInfo(name = "unlocked_item_ids", defaultValue = "[]")
    val unlockedItemIds: List<String> = listOf("item_crown", "item_space"),

    @ColumnInfo(name = "total_playtime_seconds", defaultValue = "0")
    val totalPlaytimeSeconds: Long = 0L,

    @ColumnInfo(name = "last_login_timestamp", defaultValue = "0")
    val lastLoginTimestamp: Long = System.currentTimeMillis()
)

/**
 * Game Progress Entity tracking level milestones, accuracy, and star ratings across all 8 game engines.
 * File Path: app/src/main/java/com/brainzomatch/app/core/data/local/entities/GameProgressEntity.kt
 */
@Entity(tableName = "game_progress")
data class GameProgressEntity(
    @PrimaryKey
    @ColumnInfo(name = "game_mode_id")
    val gameModeId: String, // e.g., "game_memory", "game_math", "game_difference"

    @ColumnInfo(name = "highest_level_unlocked")
    val highestLevelUnlocked: Int = 1,

    @ColumnInfo(name = "total_stars_earned")
    val totalStarsEarned: Int = 0,

    @ColumnInfo(name = "high_score")
    val highScore: Int = 0,

    @ColumnInfo(name = "accuracy_percentage")
    val accuracyPercentage: Int = 100,

    @ColumnInfo(name = "times_played")
    val timesPlayed: Int = 0,

    @ColumnInfo(name = "level_star_map_json", defaultValue = "'{}'")
    val levelStarMapJson: String = "{}", // e.g. "{\"1\":3, \"2\":3, \"3\":2}"

    @ColumnInfo(name = "last_played_timestamp", defaultValue = "0")
    val lastPlayedTimestamp: Long = System.currentTimeMillis()
)

/**
 * Parental Controls Entity storing adult PIN hash, daily screen-time limits, and sensory preferences.
 * File Path: app/src/main/java/com/brainzomatch/app/core/data/local/entities/ParentalControlsEntity.kt
 */
@Entity(tableName = "parental_controls")
data class ParentalControlsEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: Int = 1,

    @ColumnInfo(name = "pin_hash")
    val pinHash: String = "03ac674216f3e15c761ee1a5e255f067953623c8b388b4459e13f978d7c846f4", // SHA-256 of default PIN 1234 (parents should change it)

    @ColumnInfo(name = "daily_limit_minutes")
    val dailyLimitMinutes: Int = 60,

    @ColumnInfo(name = "minutes_played_today")
    val minutesPlayedToday: Int = 0,

    @ColumnInfo(name = "bedtime_lock_enabled")
    val bedtimeLockEnabled: Boolean = true,

    @ColumnInfo(name = "bedtime_start_hour")
    val bedtimeStartHour: Int = 20, // 8:00 PM

    @ColumnInfo(name = "bedtime_end_hour")
    val bedtimeEndHour: Int = 7, // 7:00 AM

    @ColumnInfo(name = "allowed_game_ids", defaultValue = "[]")
    val allowedGameIds: List<String> = listOf(
        "game_memory", "game_math", "game_difference", "game_jigsaw",
        "game_shapes", "game_word", "game_hidden", "game_blitz"
    ),

    @ColumnInfo(name = "music_volume", defaultValue = "75")
    val musicVolume: Int = 75, // 0 - 100

    @ColumnInfo(name = "sfx_volume", defaultValue = "90")
    val sfxVolume: Int = 90, // 0 - 100

    @ColumnInfo(name = "haptics_enabled", defaultValue = "1")
    val hapticsEnabled: Boolean = true,

    @ColumnInfo(name = "parent_email", defaultValue = "''")
    val parentEmail: String = ""
)
