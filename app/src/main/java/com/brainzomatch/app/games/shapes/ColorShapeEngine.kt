package com.brainzomatch.app.games.shapes

import com.brainzomatch.app.core.audio.BrainzoAudioManager
import com.brainzomatch.app.core.audio.SoundEffect
import com.brainzomatch.app.core.data.repository.BrainzoRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Geometric shape types with child-friendly emoji icons.
 */
enum class ShapeType(val displayName: String, val iconEmoji: String) {
    CIRCLE("Circle", "🔵"),
    SQUARE("Square", "🟦"),
    TRIANGLE("Triangle", "🔺"),
    STAR("Star", "⭐"),
    HEART("Heart", "❤️"),
    DIAMOND("Diamond", "💎"),
    HEXAGON("Hexagon", "⬡")
}

/**
 * Primary color classifications for shape puzzles.
 */
enum class ShapeColor(val colorName: String, val hexCode: String) {
    RED("Ruby Red", "#EF4444"),
    BLUE("Ocean Blue", "#3B82F6"),
    GREEN("Emerald Green", "#10B981"),
    YELLOW("Sun Yellow", "#F59E0B"),
    PURPLE("Royal Purple", "#8B5CF6"),
    ORANGE("Tangerine Orange", "#F97316")
}

/**
 * Movable geometric piece item.
 */
data class ColorShapePiece(
    val id: Int,
    val shape: ShapeType,
    val color: ShapeColor,
    val isMatched: Boolean = false,
    val isHighlighted: Boolean = false
)

/**
 * Target portal receptacle where pieces must be sorted.
 */
data class ShapePortal(
    val id: Int,
    val targetShape: ShapeType,
    val targetColor: ShapeColor,
    val isCompleted: Boolean = false
)

/**
 * State container for Game 5: Color & Shape Match.
 */
data class ColorShapeState(
    val level: Int = 1,
    val portals: List<ShapePortal> = emptyList(),
    val availablePieces: List<ColorShapePiece> = emptyList(),
    val totalMatchesRequired: Int = 4,
    val currentMatches: Int = 0,
    val score: Int = 0,
    val streak: Int = 0,
    val starsEarned: Int = 3,
    val hintsRemaining: Int = 3,
    val isLevelCompleted: Boolean = false,
    val activeHintPieceId: Int? = null
)

/**
 * ColorShapeEngine — Complete game engine for Game 5: Color & Shape Match.
 * Supports multi-attribute matching (shape geometry + color hue), portal completion,
 * combo scoring, and persistent Room database progress recording.
 * File Path: app/src/main/java/com/brainzomatch/app/games/shapes/ColorShapeEngine.kt
 */
class ColorShapeEngine(
    private val repository: BrainzoRepository? = null,
    private val audioManager: BrainzoAudioManager? = null,
    private val scope: CoroutineScope? = null
) {
    companion object {
        const val GAME_ID = "game_shapes"
        private const val BASE_MATCH_SCORE = 120
        private const val COMBO_BONUS = 30
        private const val COMPLETION_BONUS = 250
    }

    private val _state = MutableStateFlow(ColorShapeState())
    val state: StateFlow<ColorShapeState> = _state.asStateFlow()

    init {
        startLevel(1)
    }

    /**
     * Initializes a level with difficulty-scaled portals and piece sets.
     */
    fun startLevel(level: Int) {
        val portalCount = when {
            level <= 2 -> 3
            level <= 5 -> 4
            else -> 6
        }

        val allShapes = ShapeType.values().toList().shuffled()
        val allColors = ShapeColor.values().toList().shuffled()

        val generatedPortals = mutableListOf<ShapePortal>()
        val generatedPieces = mutableListOf<ColorShapePiece>()

        for (i in 0 until portalCount) {
            val targetShape = allShapes[i % allShapes.size]
            val targetColor = allColors[i % allColors.size]

            generatedPortals.add(
                ShapePortal(
                    id = i,
                    targetShape = targetShape,
                    targetColor = targetColor,
                    isCompleted = false
                )
            )

            // Correct matching piece
            generatedPieces.add(
                ColorShapePiece(
                    id = i,
                    shape = targetShape,
                    color = targetColor,
                    isMatched = false
                )
            )
        }

        // Add 1 or 2 distractor pieces on higher levels (levels >= 3)
        if (level >= 3) {
            val distractorShape = allShapes.last()
            val distractorColor = allColors.first()
            generatedPieces.add(
                ColorShapePiece(
                    id = portalCount,
                    shape = distractorShape,
                    color = distractorColor,
                    isMatched = false
                )
            )
        }

        _state.value = ColorShapeState(
            level = level,
            portals = generatedPortals,
            availablePieces = generatedPieces.shuffled(),
            totalMatchesRequired = portalCount,
            currentMatches = 0,
            score = 0,
            streak = 0,
            starsEarned = 3,
            hintsRemaining = 3,
            isLevelCompleted = false,
            activeHintPieceId = null
        )
    }

    /**
     * Evaluates dropping a piece onto a specific portal.
     * @param pieceId The ID of the dragged shape piece
     * @param portalId The ID of the target destination portal
     * Returns true if both shape and color match the portal specifications.
     */
    fun onDropPieceIntoPortal(pieceId: Int, portalId: Int): Boolean {
        val currentState = _state.value
        if (currentState.isLevelCompleted) return false

        val piece = currentState.availablePieces.firstOrNull { it.id == pieceId } ?: return false
        val portal = currentState.portals.firstOrNull { it.id == portalId } ?: return false

        if (portal.isCompleted || piece.isMatched) return false

        val isExactMatch = (piece.shape == portal.targetShape && piece.color == portal.targetColor)

        if (isExactMatch) {
            audioManager?.playSound(SoundEffect.MATCH_CORRECT)
            val updatedPortals = currentState.portals.map {
                if (it.id == portalId) it.copy(isCompleted = true) else it
            }
            val updatedPieces = currentState.availablePieces.map {
                if (it.id == pieceId) it.copy(isMatched = true, isHighlighted = false) else it
            }

            val newMatches = currentState.currentMatches + 1
            val newStreak = currentState.streak + 1
            val gainedPoints = BASE_MATCH_SCORE + (newStreak * COMBO_BONUS)
            val isDone = newMatches >= currentState.totalMatchesRequired
            val finalScore = currentState.score + gainedPoints + (if (isDone) COMPLETION_BONUS else 0)

            _state.update { state ->
                state.copy(
                    portals = updatedPortals,
                    availablePieces = updatedPieces,
                    currentMatches = newMatches,
                    streak = newStreak,
                    score = finalScore,
                    isLevelCompleted = isDone,
                    activeHintPieceId = null
                )
            }

            if (isDone) {
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
     * Highlights a valid piece-portal match for the player as a hint.
     */
    fun useHint(): Boolean {
        val currentState = _state.value
        if (currentState.hintsRemaining <= 0 || currentState.isLevelCompleted) return false

        val openPortal = currentState.portals.firstOrNull { !it.isCompleted } ?: return false
        val matchingPiece = currentState.availablePieces.firstOrNull { piece ->
            !piece.isMatched && piece.shape == openPortal.targetShape && piece.color == openPortal.targetColor
        } ?: return false

        audioManager?.playSound(SoundEffect.STAR_BURST)
        _state.update { state ->
            val updatedPieces = state.availablePieces.map {
                if (it.id == matchingPiece.id) it.copy(isHighlighted = true) else it.copy(isHighlighted = false)
            }
            state.copy(
                hintsRemaining = state.hintsRemaining - 1,
                availablePieces = updatedPieces,
                activeHintPieceId = matchingPiece.id
            )
        }
        return true
    }

    private fun handleVictory() {
        audioManager?.playSound(SoundEffect.LEVEL_VICTORY)
        scope?.launch {
            repository?.recordCompletion(
                gameId = GAME_ID,
                completedLevel = _state.value.level,
                starsEarned = _state.value.starsEarned
            )
            repository?.addReward(coins = 55, stars = 3)
        }
    }
}
