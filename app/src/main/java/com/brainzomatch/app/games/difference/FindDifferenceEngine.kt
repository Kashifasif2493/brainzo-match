package com.brainzomatch.app.games.difference

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
 * Geometric bounding region for a single hidden difference target.
 * Normalized coordinates (0.0 to 1.0) for resolution-independent cross-device touch detection.
 */
data class DifferenceRegion(
    val id: String,
    val name: String,
    val normalizedX: Float, // Center X [0.0f .. 1.0f]
    val normalizedY: Float, // Center Y [0.0f .. 1.0f]
    val hitRadius: Float = 0.08f, // Hit threshold radius
    val clueHintText: String = "Look near the center"
)

/**
 * State representation for Find The Difference.
 */
data class FindDifferenceState(
    val level: Int = 1,
    val sceneTitle: String = "Forest Picnic Mystery",
    val foundDifferences: Set<String> = emptySet(),
    val totalDifferences: Int = 5,
    val score: Int = 0,
    val streak: Int = 0,
    val starsEarned: Int = 3,
    val hintsRemaining: Int = 3,
    val highlightedHintRegion: DifferenceRegion? = null,
    val isLevelComplete: Boolean = false
)

/**
 * FindDifferenceEngine — Complete game engine for Game 3: Find The Difference.
 * Implements normalized coordinate hit detection, touch tolerance calculation,
 * Sherlock Hint reveal radar, and Room database progress tracking.
 * File Path: app/src/main/java/com/brainzomatch/app/games/difference/FindDifferenceEngine.kt
 */
class FindDifferenceEngine(
    private val repository: BrainzoRepository? = null,
    private val audioManager: BrainzoAudioManager? = null,
    private val scope: CoroutineScope? = null
) {
    companion object {
        const val GAME_ID = "game_difference"
        private const val SCORE_PER_DIFFERENCE = 150
        private const val COMBO_BONUS = 30
    }

    private val _state = MutableStateFlow(FindDifferenceState())
    val state: StateFlow<FindDifferenceState> = _state.asStateFlow()

    private var activeDifferencesList: List<DifferenceRegion> = emptyList()

    init {
        startLevel(1)
    }

    /**
     * Initializes a level scene with predefined difference coordinate regions.
     */
    fun startLevel(level: Int) {
        activeDifferencesList = when (level) {
            1 -> listOf(
                DifferenceRegion("diff_sun", "Golden Sun Ray", 0.85f, 0.15f, 0.09f, "High in the sunny sky"),
                DifferenceRegion("diff_fox_tail", "Fox Tail Tip", 0.32f, 0.68f, 0.08f, "Look at the playful fox"),
                DifferenceRegion("diff_mushroom", "Red Mushroom Spot", 0.64f, 0.76f, 0.08f, "Down near the grass patches"),
                DifferenceRegion("diff_bird", "Blue Finch", 0.22f, 0.30f, 0.08f, "Resting on a branch"),
                DifferenceRegion("diff_apple", "Missing Picnic Apple", 0.52f, 0.58f, 0.08f, "On the red picnic basket")
            )
            2 -> listOf(
                DifferenceRegion("diff_castle_flag", "Castle Flag", 0.48f, 0.18f, 0.09f, "At the highest royal tower"),
                DifferenceRegion("diff_dragon_scale", "Sparkling Scale", 0.72f, 0.55f, 0.08f, "On the friendly dragon's back"),
                DifferenceRegion("diff_shield_crest", "Knight's Star Emblem", 0.25f, 0.62f, 0.08f, "On the shining shield"),
                DifferenceRegion("diff_cloud_shape", "Cat-shaped Cloud", 0.18f, 0.22f, 0.08f, "Drifting in the clouds"),
                DifferenceRegion("diff_flower_petal", "Rainbow Petal", 0.82f, 0.85f, 0.08f, "At the castle garden corner")
            )
            else -> listOf(
                DifferenceRegion("diff_rocket_flame", "Rocket Engine Flame", 0.50f, 0.82f, 0.09f, "Under the launch booster"),
                DifferenceRegion("diff_planet_ring", "Saturn Ring Gap", 0.80f, 0.25f, 0.08f, "Orbiting the giant planet"),
                DifferenceRegion("diff_astronaut_badge", "Mission Star Badge", 0.35f, 0.48f, 0.08f, "On the spacesuit chest"),
                DifferenceRegion("diff_alien_antenna", "Glowing Antenna", 0.68f, 0.65f, 0.08f, "Above the friendly green visitor"),
                DifferenceRegion("diff_star_cluster", "Big Dipper Constellation", 0.15f, 0.18f, 0.08f, "In deep dark space")
            )
        }

        val sceneName = when (level) {
            1 -> "Forest Picnic Mystery"
            2 -> "Royal Kingdom Fair"
            else -> "Cosmic Galaxy Voyage"
        }

        _state.value = FindDifferenceState(
            level = level,
            sceneTitle = sceneName,
            foundDifferences = emptySet(),
            totalDifferences = activeDifferencesList.size,
            score = 0,
            streak = 0,
            starsEarned = 3,
            hintsRemaining = 3,
            highlightedHintRegion = null,
            isLevelComplete = false
        )
    }

    /**
     * Checks if a player's touch at normalized (tapX, tapY) matches any unfound difference.
     * Normalized coordinates range from 0.0f (left/top) to 1.0f (right/bottom).
     */
    fun onCanvasTapped(tapX: Float, tapY: Float): Boolean {
        val currentState = _state.value
        if (currentState.isLevelComplete) return false

        // Check distance to all unfound target regions
        val hitDifference = activeDifferencesList.firstOrNull { region ->
            if (region.id in currentState.foundDifferences) return@firstOrNull false

            val dx = tapX - region.normalizedX
            val dy = tapY - region.normalizedY
            val distance = sqrt(dx.pow(2) + dy.pow(2))
            distance <= region.hitRadius
        }

        if (hitDifference != null) {
            // Difference found!
            audioManager?.playSound(SoundEffect.MATCH_CORRECT)
            val updatedFound = currentState.foundDifferences + hitDifference.id
            val newStreak = currentState.streak + 1
            val gainedScore = SCORE_PER_DIFFERENCE + (newStreak * COMBO_BONUS)
            val isComplete = updatedFound.size >= currentState.totalDifferences

            _state.update { state ->
                state.copy(
                    foundDifferences = updatedFound,
                    streak = newStreak,
                    score = state.score + gainedScore,
                    highlightedHintRegion = null, // Clear hint when found
                    isLevelComplete = isComplete
                )
            }

            if (isComplete) {
                onLevelWon()
            }
            return true
        } else {
            // Missed tap
            audioManager?.playSound(SoundEffect.WRONG_TRY_AGAIN)
            _state.update { state ->
                state.copy(streak = 0)
            }
            return false
        }
    }

    /**
     * Reveals the location of one unfound difference using a Sherlock Hint.
     */
    fun useSherlockHint(): DifferenceRegion? {
        val currentState = _state.value
        if (currentState.hintsRemaining <= 0 || currentState.isLevelComplete) return null

        val unfound = activeDifferencesList.firstOrNull { it.id !in currentState.foundDifferences } ?: return null

        audioManager?.playSound(SoundEffect.STAR_BURST)
        _state.update { state ->
            state.copy(
                hintsRemaining = state.hintsRemaining - 1,
                highlightedHintRegion = unfound
            )
        }
        return unfound
    }

    private fun onLevelWon() {
        audioManager?.playSound(SoundEffect.LEVEL_VICTORY)
        scope?.launch {
            repository?.recordCompletion(
                gameId = GAME_ID,
                completedLevel = _state.value.level,
                starsEarned = _state.value.starsEarned
            )
            repository?.addReward(coins = 50, stars = 3)
        }
    }
}
