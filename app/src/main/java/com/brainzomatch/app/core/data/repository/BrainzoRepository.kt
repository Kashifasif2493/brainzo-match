package com.brainzomatch.app.core.data.repository

import com.brainzomatch.app.core.data.local.BrainzoDatabase
import com.brainzomatch.app.core.data.local.entities.GameProgressEntity
import com.brainzomatch.app.core.data.local.entities.ParentalControlsEntity
import com.brainzomatch.app.core.data.local.entities.PlayerProfileEntity
import kotlinx.coroutines.flow.Flow

/**
 * BrainzoRepository — Repository managing reactive data flow across Room Database v2,
 * player avatar customization, game progress across all 8 games, and parental controls.
 * File Path: app/src/main/java/com/brainzomatch/app/core/data/repository/BrainzoRepository.kt
 */
class BrainzoRepository(private val db: BrainzoDatabase) {

    // ==========================================
    // 1. Player Profile & Economy
    // ==========================================
    val playerProfile: Flow<PlayerProfileEntity?> = db.playerProfileDao().getPlayer()

    suspend fun getPlayerSnapshot(): PlayerProfileEntity? {
        return db.playerProfileDao().getPlayerSnapshot()
    }

    suspend fun savePlayer(profile: PlayerProfileEntity) {
        db.playerProfileDao().savePlayer(profile)
    }

    suspend fun addReward(coins: Int, stars: Int) {
        db.playerProfileDao().addCurrency(coins, stars)
    }

    suspend fun deductCoins(cost: Int): Boolean {
        return db.playerProfileDao().deductCoins(cost) > 0
    }

    suspend fun updateAvatar(avatarId: String, auraHex: String) {
        db.playerProfileDao().updateAvatar(avatarId, auraHex)
    }

    suspend fun equipHat(hatId: String) {
        db.playerProfileDao().equipHat(hatId)
    }

    // ==========================================
    // 2. Game Progression (All 8 Game Engines)
    // ==========================================
    val allProgress: Flow<List<GameProgressEntity>> = db.gameProgressDao().getAllProgressFlow()

    suspend fun getProgressForGame(gameId: String): GameProgressEntity? {
        return db.gameProgressDao().getProgressForGame(gameId)
    }

    suspend fun recordCompletion(gameId: String, completedLevel: Int, starsEarned: Int) {
        db.gameProgressDao().recordLevelCompletion(gameId, completedLevel, starsEarned)
    }

    suspend fun saveProgress(progress: GameProgressEntity) {
        db.gameProgressDao().saveProgress(progress)
    }

    // ==========================================
    // 3. Parental Controls & Sensory Settings
    // ==========================================
    val parentalControls: Flow<ParentalControlsEntity?> = db.parentalControlsDao().getControlsFlow()

    suspend fun getControlsSnapshot(): ParentalControlsEntity? {
        return db.parentalControlsDao().getControlsSnapshot()
    }

    suspend fun saveParentalControls(controls: ParentalControlsEntity) {
        db.parentalControlsDao().saveControls(controls)
    }

    suspend fun incrementScreenTime(minutes: Int) {
        db.parentalControlsDao().incrementScreenTime(minutes)
    }

    suspend fun updateDailyLimit(minutes: Int) {
        db.parentalControlsDao().updateDailyLimit(minutes)
    }

    suspend fun saveSensorySettings(musicVolume: Int, sfxVolume: Int, hapticsEnabled: Boolean) {
        db.parentalControlsDao().updateSensorySettings(musicVolume, sfxVolume, hapticsEnabled)
    }

    // ==========================================
    // 4. First-launch defaults
    // ==========================================
    /**
     * Creates the single player profile, parental-controls row and one progress row per game
     * when they do not exist yet. Without these rows, reward and progress UPDATE queries
     * would silently affect nothing. Safe to call on every app start.
     */
    suspend fun ensureDefaults() {
        if (getPlayerSnapshot() == null) savePlayer(PlayerProfileEntity())
        if (getControlsSnapshot() == null) saveParentalControls(ParentalControlsEntity())
        listOf(
            "game_memory", "game_math", "game_difference", "game_jigsaw",
            "game_shapes", "game_word", "game_hidden", "game_blitz"
        ).forEach { id ->
            if (getProgressForGame(id) == null) saveProgress(GameProgressEntity(gameModeId = id))
        }
    }
}
