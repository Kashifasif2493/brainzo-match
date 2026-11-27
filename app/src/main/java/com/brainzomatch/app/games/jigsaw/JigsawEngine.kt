package com.brainzomatch.app.games.jigsaw

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
 * Representation of a single jigsaw tile piece.
 */
data class JigsawPiece(
    val id: Int,
    val correctRow: Int,
    val correctCol: Int,
    val displayIcon: String,
    val isSnapped: Boolean = false,
    val currentX: Float = 0f,
    val currentY: Float = 0f
)

/**
 * State container for Jigsaw Quest.
 */
data class JigsawState(
    val level: Int = 1,
    val gridSize: Int = 3, // 3x3 = 9 pieces
    val themeTitle: String = "Jungle Kingdom",
    val pieces: List<JigsawPiece> = emptyList(),
    val totalPieces: Int = 9,
    val placedCount: Int = 0,
    val score: Int = 0,
    val hintsLeft: Int = 3,
    val isCompleted: Boolean = false
)

/**
 * JigsawEngine — Complete game engine for Game 4: Jigsaw Puzzle Quest.
 * Manages piece coordinate snapping, distance tolerance checking, tray pieces management,
 * auto-snap assistance, and Room DB progress persistence.
 * File Path: app/src/main/java/com/brainzomatch/app/games/jigsaw/JigsawEngine.kt
 */
class JigsawEngine(
    private val repository: BrainzoRepository? = null,
    private val audioManager: BrainzoAudioManager? = null,
    private val scope: CoroutineScope? = null
) {
    companion object {
        const val GAME_ID = "game_jigsaw"
        private const val SNAP_DISTANCE_THRESHOLD = 0.14f // Within 14% of slot center = auto snap
        private const val SCORE_PER_PIECE = 40
        private const val COMPLETION_BONUS = 200
    }

    private val _state = MutableStateFlow(JigsawState())
    val state: StateFlow<JigsawState> = _state.asStateFlow()

    init {
        startLevel(1)
    }

    /**
     * Initializes a jigsaw level with a 3×3 grid (9 pieces).
     */
    fun startLevel(level: Int) {
        val size = 3
        val total = size * size

        val themeIcons = when (level) {
            1 -> listOf("🌴", "🦁", "🐒", "🌿", "🦜", "🌺", "🐘", "🍌", "💧") // Jungle
            2 -> listOf("🏰", "👑", "🦄", "🛡️", "⚔️", "💎", "🐉", "🎺", "🌟") // Castle
            else -> listOf("🚀", "🪐", "⭐", "🛸", "🌙", "☄️", "👾", "🛰️", "✨") // Space
        }

        val themeName = when (level) {
            1 -> "Jungle Kingdom"
            2 -> "Royal Castle Quest"
            else -> "Deep Space Odyssey"
        }

        val generatedPieces = mutableListOf<JigsawPiece>()
        var pieceId = 0

        for (row in 0 until size) {
            for (col in 0 until size) {
                generatedPieces.add(
                    JigsawPiece(
                        id = pieceId,
                        correctRow = row,
                        correctCol = col,
                        displayIcon = themeIcons[pieceId % themeIcons.size],
                        isSnapped = false,
                        currentX = 0f,
                        currentY = 0f
                    )
                )
                pieceId++
            }
        }

        _state.value = JigsawState(
            level = level,
            gridSize = size,
            themeTitle = themeName,
            pieces = generatedPieces.shuffled(),
            totalPieces = total,
            placedCount = 0,
            score = 0,
            hintsLeft = 3,
            isCompleted = false
        )
    }

    /**
     * Evaluates a piece release at target coordinate slot.
     * @param pieceId ID of dragged piece
     * @param targetRow Grid row dropped over [0..2]
     * @param targetCol Grid col dropped over [0..2]
     * Returns true if piece snaps into the correct location.
     */
    fun onDropPiece(pieceId: Int, targetRow: Int, targetCol: Int): Boolean {
        val currentState = _state.value
        if (currentState.isCompleted) return false

        val piece = currentState.pieces.firstOrNull { it.id == pieceId } ?: return false
        if (piece.isSnapped) return true

        val isCorrectSlot = (piece.correctRow == targetRow && piece.correctCol == targetCol)

        if (isCorrectSlot) {
            audioManager?.playSound(SoundEffect.PUZZLE_SNAP)
            val updatedPieces = currentState.pieces.map {
                if (it.id == pieceId) it.copy(isSnapped = true) else it
            }
            val newPlaced = currentState.placedCount + 1
            val isDone = newPlaced >= currentState.totalPieces
            val addedScore = SCORE_PER_PIECE + (if (isDone) COMPLETION_BONUS else 0)

            _state.update { state ->
                state.copy(
                    pieces = updatedPieces,
                    placedCount = newPlaced,
                    score = state.score + addedScore,
                    isCompleted = isDone
                )
            }

            if (isDone) {
                handleVictory()
            }
            return true
        } else {
            audioManager?.playSound(SoundEffect.WRONG_TRY_AGAIN)
            return false
        }
    }

    /**
     * Coordinate-based drag evaluation using normalized grid space.
     * Slot centers for 3x3: col = (col + 0.5) / 3, row = (row + 0.5) / 3.
     */
    fun onReleasePieceAtNormalizedCoords(pieceId: Int, dropX: Float, dropY: Float): Boolean {
        val size = _state.value.gridSize
        val targetCol = (dropX * size).toInt().coerceIn(0, size - 1)
        val targetRow = (dropY * size).toInt().coerceIn(0, size - 1)

        val targetCenterX = (targetCol + 0.5f) / size
        val targetCenterY = (targetRow + 0.5f) / size

        val dist = sqrt((dropX - targetCenterX).pow(2) + (dropY - targetCenterY).pow(2))
        return if (dist <= SNAP_DISTANCE_THRESHOLD) {
            onDropPiece(pieceId, targetRow, targetCol)
        } else {
            false
        }
    }

    /**
     * Auto-snaps one unsnapped piece to its correct slot (Hint feature).
     */
    fun autoSnapOnePiece(): Boolean {
        val currentState = _state.value
        if (currentState.isCompleted) return false

        val unsnappedPiece = currentState.pieces.firstOrNull { !it.isSnapped } ?: return false

        audioManager?.playSound(SoundEffect.PUZZLE_SNAP)
        val updatedPieces = currentState.pieces.map {
            if (it.id == unsnappedPiece.id) it.copy(isSnapped = true) else it
        }
        val newPlaced = currentState.placedCount + 1
        val isDone = newPlaced >= currentState.totalPieces

        _state.update { state ->
            state.copy(
                pieces = updatedPieces,
                placedCount = newPlaced,
                score = state.score + SCORE_PER_PIECE,
                hintsLeft = (state.hintsLeft - 1).coerceAtLeast(0),
                isCompleted = isDone
            )
        }

        if (isDone) {
            handleVictory()
        }
        return true
    }

    private fun handleVictory() {
        audioManager?.playSound(SoundEffect.LEVEL_VICTORY)
        scope?.launch {
            repository?.recordCompletion(
                gameId = GAME_ID,
                completedLevel = _state.value.level,
                starsEarned = 3
            )
            repository?.addReward(coins = 60, stars = 3)
        }
    }
}
