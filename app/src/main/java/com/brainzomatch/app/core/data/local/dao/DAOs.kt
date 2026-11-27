package com.brainzomatch.app.core.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.brainzomatch.app.core.data.local.entities.GameProgressEntity
import com.brainzomatch.app.core.data.local.entities.ParentalControlsEntity
import com.brainzomatch.app.core.data.local.entities.PlayerProfileEntity
import kotlinx.coroutines.flow.Flow

/**
 * PlayerProfileDao provides database access methods for player profile, currency, and wardrobe.
 * File Path: app/src/main/java/com/brainzomatch/app/core/data/local/dao/PlayerProfileDao.kt
 */
@Dao
interface PlayerProfileDao {
    @Query("SELECT * FROM player_profile WHERE id = 'primary_kid'")
    fun getPlayer(): Flow<PlayerProfileEntity?>

    @Query("SELECT * FROM player_profile WHERE id = 'primary_kid'")
    suspend fun getPlayerSnapshot(): PlayerProfileEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun savePlayer(profile: PlayerProfileEntity)

    @Query("UPDATE player_profile SET coins = coins + :earnedCoins, stars = stars + :earnedStars WHERE id = 'primary_kid'")
    suspend fun addCurrency(earnedCoins: Int, earnedStars: Int)

    @Query("UPDATE player_profile SET coins = coins - :cost WHERE id = 'primary_kid' AND coins >= :cost")
    suspend fun deductCoins(cost: Int): Int

    @Query("UPDATE player_profile SET avatar_id = :avatarId, aura_color_hex = :auraHex WHERE id = 'primary_kid'")
    suspend fun updateAvatar(avatarId: String, auraHex: String)

    @Query("UPDATE player_profile SET equipped_hat_id = :hatId WHERE id = 'primary_kid'")
    suspend fun equipHat(hatId: String)
}

/**
 * GameProgressDao provides database access methods for tracking all 8 game engines.
 * File Path: app/src/main/java/com/brainzomatch/app/core/data/local/dao/GameProgressDao.kt
 */
@Dao
interface GameProgressDao {
    @Query("SELECT * FROM game_progress")
    fun getAllProgressFlow(): Flow<List<GameProgressEntity>>

    @Query("SELECT * FROM game_progress WHERE game_mode_id = :gameId")
    suspend fun getProgressForGame(gameId: String): GameProgressEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveProgress(progress: GameProgressEntity)

    @Query("""
        UPDATE game_progress 
        SET highest_level_unlocked = MAX(highest_level_unlocked, :completedLevel + 1),
            total_stars_earned = total_stars_earned + :newStars,
            times_played = times_played + 1,
            last_played_timestamp = :timestamp
        WHERE game_mode_id = :gameId
    """)
    suspend fun recordLevelCompletion(
        gameId: String,
        completedLevel: Int,
        newStars: Int,
        timestamp: Long = System.currentTimeMillis()
    )
}

/**
 * ParentalControlsDao provides database access methods for parent locks, screen time, and sensory audio settings.
 * File Path: app/src/main/java/com/brainzomatch/app/core/data/local/dao/ParentalControlsDao.kt
 */
@Dao
interface ParentalControlsDao {
    @Query("SELECT * FROM parental_controls WHERE id = 1")
    fun getControlsFlow(): Flow<ParentalControlsEntity?>

    @Query("SELECT * FROM parental_controls WHERE id = 1")
    suspend fun getControlsSnapshot(): ParentalControlsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveControls(controls: ParentalControlsEntity)

    @Query("UPDATE parental_controls SET minutes_played_today = minutes_played_today + :additionalMinutes WHERE id = 1")
    suspend fun incrementScreenTime(additionalMinutes: Int)

    @Query("UPDATE parental_controls SET daily_limit_minutes = :newLimit WHERE id = 1")
    suspend fun updateDailyLimit(newLimit: Int)

    @Query("UPDATE parental_controls SET music_volume = :music, sfx_volume = :sfx, haptics_enabled = :haptic WHERE id = 1")
    suspend fun updateSensorySettings(music: Int, sfx: Int, haptic: Boolean)
}
