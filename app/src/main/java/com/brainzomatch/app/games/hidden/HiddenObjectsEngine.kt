package com.brainzomatch.app.games.hidden

import com.brainzomatch.app.core.audio.BrainzoAudioManager
import com.brainzomatch.app.core.audio.SoundEffect
import com.brainzomatch.app.core.data.repository.BrainzoRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.math.pow
import kotlin.math.sqrt

/**
 * Representation of a single hidden object embedded in a scene.
 */
data class HiddenObjectItem(
    val id: String,
    val name: String,
    val iconEmoji: String,
    val normalizedX: Float, // Center X [0.0f .. 1.0f]
    val normalizedY: Float, // Center Y [0.0f .. 1.0f]
    val hitRadius: Float = 0.08f,
    val isFound: Boolean = false,
    val isRadarHighlighted: Boolean = false
)

/**
 * State container for Game 7: Hidden Objects.
 */
data class HiddenObjectsState(
    val level: Int = 1,
    val sceneTheme: String = "Enchanted Treehouse Library",
    val itemsToFind: List<HiddenObjectItem> = emptyList(),
    val totalCount: Int = 5,
    val foundCount: Int = 0,
    val score: Int = 0,
    val streak: Int = 0,
    val starsEarned: Int = 3,
    val hintsRemaining: Int = 3,
    val activeRadarTarget: HiddenObjectItem? = null,
    val isLevelCompleted: Boolean = false
)

/**
 * HiddenObjectsEngine — Complete game engine for Game 7: Hidden Objects.
 * Implements normalized 2D coordinate hit-testing, radar scanning hints,
 * combo multipliers, and Room DB achievement tracking.
 * File Path: app/src/main/java/com/brainzomatch/app/games/hidden/HiddenObjectsEngine.kt
 */
class HiddenObjectsEngine(
    private val repository: BrainzoRepository? = null,
    private val audioManager: BrainzoAudioManager? = null,
    private val scope: CoroutineScope? = null
) {
    companion object {
        const val GAME_ID = "game_hidden"
        private const val SCORE_PER_ITEM = 140
        private const val STREAK_BONUS = 35
        private const val COMPLETION_BONUS = 250
    }

    private val _state = MutableStateFlow(HiddenObjectsState())
    val state: StateFlow<HiddenObjectsState> = _state.asStateFlow()

    init {
        startLevel(1)
    }

    /**
     * Initializes a level scene with predefined hidden item coordinates.
     */
    fun startLevel(level: Int) {
        val itemsList = when (level) {
            1 -> listOf(
                HiddenObjectItem("item_book", "Magic Spellbook", "📖", 0.28f, 0.35f, 0.08f),
                HiddenObjectItem("item_key", "Golden Key", "🗝️", 0.74f, 0.62f, 0.08f),
                HiddenObjectItem("item_telescope", "Brass Telescope", "🔭", 0.52f, 0.22f, 0.09f),
                HiddenObjectItem("item_feather", "Quill Feather", "🪶", 0.82f, 0.44f, 0.08f),
                HiddenObjectItem("item_magnifier", "Detective Magnifier", "🔍", 0.18f, 0.78f, 0.08f)
            )
            2 -> listOf(
                HiddenObjectItem("item_compass", "Pirate Compass", "🧭", 0.33f, 0.25f, 0.08f),
                HiddenObjectItem("item_gem", "Blue Sapphire", "💎", 0.65f, 0.80f, 0.08f),
                HiddenObjectItem("item_map", "Treasure Scroll", "🗺️", 0.45f, 0.55f, 0.08f),
                HiddenObjectItem("item_lantern", "Vintage Lantern", "🏮", 0.80f, 0.30f, 0.08f),
                HiddenObjectItem("item_potion", "Glowing Elixir", "🧪", 0.15f, 0.50f, 0.08f)
            )
            else -> listOf(
                HiddenObjectItem("item_crystal", "Star Crystal", "🔮", 0.50f, 0.40f, 0.08f),
                HiddenObjectItem("item_hourglass", "Sand Timer", "⏳", 0.30f, 0.70f, 0.08f),
                HiddenObjectItem("item_crown", "Royal Diadem", "👑", 0.70f, 0.20f, 0.08f),
                HiddenObjectItem("item_shield", "Silver Shield", "🛡️", 0.85f, 0.65f, 0.08f),
                HiddenObjectItem("item_candle", "Mystic Candle", "🕯️", 0.20f, 0.30f, 0.08f)
            )
        }

        val sceneName = when (level) {
            1 -> "Enchanted Treehouse Library"
            2 -> "Sunken Galleon Treasury"
            else -> "Secret Crystal Caverns"
        }

        _state.value = HiddenObjectsState(
            level = level,
            sceneTheme = sceneName,
            itemsToFind = itemsList,
            totalCount = itemsList.size,
            foundCount = 0,
            score = 0,
            streak = 0,
            starsEarned = 3,
            hintsRemaining = 3,
            activeRadarTarget = null,
            isLevelCompleted = false
        )
    }

    /**
     * Evaluates a tap touch coordinate (0.0 to 1.0) on the scene image.
     */
    fun onSceneTapped(tapX: Float, tapY: Float): Boolean {
        val currentState = _state.value
        if (currentState.isLevelCompleted) return false

        // Check distance to all unfound target regions
        val hitItem = currentState.itemsToFind.firstOrNull { item ->
            if (item.isFound) return@firstOrNull false

            val dx = tapX - item.normalizedX
            val dy = tapY - item.normalizedY
            val distance = sqrt(dx.pow(2) + dy.pow(2))
            distance <= item.hitRadius
        }

        if (hitItem != null) {
            audioManager?.playSound(SoundEffect.MATCH_CORRECT)
            val updatedItems = currentState.itemsToFind.map {
                if (it.id == hitItem.id) it.copy(isFound = true, isRadarHighlighted = false) else it
            }

            val newFoundCount = currentState.foundCount + 1
            val newStreak = currentState.streak + 1
            val gainedPoints = SCORE_PER_ITEM + (newStreak * STREAK_BONUS)
            val isComplete = newFoundCount >= currentState.totalCount
            val finalScore = currentState.score + gainedPoints + (if (isComplete) COMPLETION_BONUS else 0)

            _state.update { state ->
                state.copy(
                    itemsToFind = updatedItems,
                    foundCount = newFoundCount,
                    streak = newStreak,
                    score = finalScore,
                    activeRadarTarget = null,
                    isLevelCompleted = isComplete
                )
            }

            if (isComplete) {
                handleVictory()
            }
            return true
        } else {
            audioManager?.playSound(SoundEffect.WRONG_TRY_AGAIN)
            _state.update { state ->
                state.copy(streak = 0)
            }
            return false
        }
    }

    /**
     * Triggers radar scan hint to highlight one unfound item.
     */
    fun triggerRadarHint(): HiddenObjectItem? {
        val currentState = _state.value
        if (currentState.hintsRemaining <= 0 || currentState.isLevelCompleted) return null

        val targetItem = currentState.itemsToFind.firstOrNull { !it.isFound } ?: return null

        audioManager?.playSound(SoundEffect.STAR_BURST)
        val updatedItems = currentState.itemsToFind.map {
            if (it.id == targetItem.id) it.copy(isRadarHighlighted = true) else it
        }

        _state.update { state ->
            state.copy(
                hintsRemaining = state.hintsRemaining - 1,
                itemsToFind = updatedItems,
                activeRadarTarget = targetItem
            )
        }
        return targetItem
    }

    private fun handleVictory() {
        audioManager?.playSound(SoundEffect.LEVEL_VICTORY)
        scope?.launch {
            repository?.recordCompletion(
                gameId = GAME_ID,
                completedLevel = _state.value.level,
                starsEarned = _state.value.starsEarned
            )
            repository?.addReward(coins = 65, stars = 3)
        }
    }
}
