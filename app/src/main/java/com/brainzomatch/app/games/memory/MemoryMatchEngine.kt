package com.brainzomatch.app.games.memory

import com.brainzomatch.app.core.audio.BrainzoAudioManager
import com.brainzomatch.app.core.audio.SoundEffect
import com.brainzomatch.app.core.data.repository.BrainzoRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Data model representing a single card in the Memory Match grid.
 */
data class MemoryCard(
    val id: Int,
    val identifier: String,
    val iconEmoji: String,
    val isFaceUp: Boolean = false,
    val isMatched: Boolean = false,
    val isEvaluating: Boolean = false
)

/**
 * Reactive state model for MemoryMatchEngine.
 */
data class MemoryGameState(
    val level: Int = 1,
    val cards: List<MemoryCard> = emptyList(),
    val totalPairs: Int = 6,
    val pairsFound: Int = 0,
    val score: Int = 0,
    val streak: Int = 0,
    val starsEarned: Int = 3,
    val movesCount: Int = 0,
    val isBusyEvaluating: Boolean = false,
    val isGameOver: Boolean = false,
    val elapsedTimeSeconds: Int = 0
)

/**
 * MemoryMatchEngine — Complete reactive game engine for Game 1: Safari Memory Match.
 * Supports adaptive grid layouts, combo streak multipliers, sound triggers, and Room DB progress persistence.
 * File Path: app/src/main/java/com/brainzomatch/app/games/memory/MemoryMatchEngine.kt
 */
class MemoryMatchEngine(
    private val animalIdentifiers: List<String> = listOf(
        "lion", "elephant", "zebra", "giraffe", "monkey", "hippo", "panda", "tiger"
    ),
    private val repository: BrainzoRepository? = null,
    private val audioManager: BrainzoAudioManager? = null,
    private val scope: CoroutineScope? = null
) {
    companion object {
        const val GAME_ID = "game_memory"
        private const val BASE_PAIR_SCORE = 100
        private const val COMBO_BONUS = 25
    }

    private val _state = MutableStateFlow(MemoryGameState())
    val state: StateFlow<MemoryGameState> = _state.asStateFlow()

    private var firstFlippedIndex: Int? = null

    init {
        startNewGame(level = 1)
    }

    /**
     * Resolves animal identifier to friendly visual emoji.
     */
    private fun getEmojiForAnimal(id: String): String {
        return when (id) {
            "lion" -> "🦁"
            "elephant" -> "🐘"
            "zebra" -> "🦓"
            "giraffe" -> "🦒"
            "monkey" -> "🐒"
            "hippo" -> "🦛"
            "panda" -> "🐼"
            "tiger" -> "🐯"
            "koala" -> "🐨"
            "bear" -> "🐻"
            else -> "🐾"
        }
    }

    /**
     * Starts a new game or restarts the current level.
     */
    fun startNewGame(level: Int = _state.value.level) {
        val pairsCount = when {
            level <= 1 -> 4  // 8 cards (2x4)
            level <= 3 -> 6  // 12 cards (3x4)
            else -> 8        // 16 cards (4x4)
        }

        val selectedPool = animalIdentifiers.shuffled().take(pairsCount)
        val deck = mutableListOf<MemoryCard>()

        var cardId = 0
        selectedPool.forEach { animal ->
            val emoji = getEmojiForAnimal(animal)
            // Two cards per pair
            deck.add(MemoryCard(id = cardId++, identifier = animal, iconEmoji = emoji))
            deck.add(MemoryCard(id = cardId++, identifier = animal, iconEmoji = emoji))
        }
        deck.shuffle()

        firstFlippedIndex = null
        _state.value = MemoryGameState(
            level = level,
            cards = deck,
            totalPairs = pairsCount,
            pairsFound = 0,
            score = 0,
            streak = 0,
            starsEarned = 3,
            movesCount = 0,
            isBusyEvaluating = false,
            isGameOver = false,
            elapsedTimeSeconds = 0
        )
    }

    /**
     * Handles card click selection.
     */
    fun onCardSelected(
        index: Int,
        onMatch: (() -> Unit)? = null,
        onFail: (() -> Unit)? = null
    ) {
        val currentState = _state.value
        if (currentState.isBusyEvaluating || currentState.isGameOver) return

        val cards = currentState.cards
        if (index !in cards.indices) return
        val selectedCard = cards[index]

        // Ignore already face-up or matched cards
        if (selectedCard.isFaceUp || selectedCard.isMatched) return

        // Audio tap
        audioManager?.playSound(SoundEffect.CARD_FLIP)

        val firstIdx = firstFlippedIndex
        if (firstIdx == null) {
            // First card flipped in current turn
            firstFlippedIndex = index
            _state.update { state ->
                val updatedCards = state.cards.toMutableList()
                updatedCards[index] = selectedCard.copy(isFaceUp = true)
                state.copy(cards = updatedCards)
            }
        } else {
            if (firstIdx == index) return // Same card tap protection

            // Second card flipped
            val firstCard = cards[firstIdx]
            val secondCard = selectedCard

            val isMatch = firstCard.identifier == secondCard.identifier

            // Reveal second card immediately
            _state.update { state ->
                val updatedCards = state.cards.toMutableList()
                updatedCards[index] = secondCard.copy(isFaceUp = true)
                state.copy(
                    cards = updatedCards,
                    movesCount = state.movesCount + 1,
                    isBusyEvaluating = true
                )
            }

            if (scope != null) {
                scope.launch {
                    processEvaluation(firstIdx, index, isMatch, onMatch, onFail)
                }
            } else {
                // Synchronous fallback evaluation if no scope passed
                executeEvaluationImmediate(firstIdx, index, isMatch, onMatch, onFail)
            }
        }
    }

    private suspend fun processEvaluation(
        firstIdx: Int,
        secondIdx: Int,
        isMatch: Boolean,
        onMatch: (() -> Unit)?,
        onFail: (() -> Unit)?
    ) {
        if (isMatch) {
            delay(280) // Visual recognition pause
            handleMatchSuccess(firstIdx, secondIdx)
            onMatch?.invoke()
            audioManager?.playSound(SoundEffect.MATCH_CORRECT)
        } else {
            delay(750) // Inspection pause before flip back
            handleMismatch(firstIdx, secondIdx)
            onFail?.invoke()
            audioManager?.playSound(SoundEffect.WRONG_TRY_AGAIN)
        }
        firstFlippedIndex = null
    }

    private fun executeEvaluationImmediate(
        firstIdx: Int,
        secondIdx: Int,
        isMatch: Boolean,
        onMatch: (() -> Unit)?,
        onFail: (() -> Unit)?
    ) {
        if (isMatch) {
            handleMatchSuccess(firstIdx, secondIdx)
            onMatch?.invoke()
        } else {
            handleMismatch(firstIdx, secondIdx)
            onFail?.invoke()
        }
        firstFlippedIndex = null
    }

    private fun handleMatchSuccess(firstIdx: Int, secondIdx: Int) {
        _state.update { state ->
            val updatedCards = state.cards.toMutableList()
            updatedCards[firstIdx] = updatedCards[firstIdx].copy(isMatched = true, isFaceUp = true)
            updatedCards[secondIdx] = updatedCards[secondIdx].copy(isMatched = true, isFaceUp = true)

            val newPairsFound = state.pairsFound + 1
            val newStreak = state.streak + 1
            val addedScore = BASE_PAIR_SCORE + (newStreak * COMBO_BONUS)
            val isComplete = newPairsFound >= state.totalPairs

            // Calculate star rating based on moves taken
            val parMoves = state.totalPairs * 2
            val calculatedStars = when {
                state.movesCount <= parMoves -> 3
                state.movesCount <= parMoves + 4 -> 2
                else -> 1
            }

            state.copy(
                cards = updatedCards,
                pairsFound = newPairsFound,
                streak = newStreak,
                score = state.score + addedScore,
                starsEarned = calculatedStars,
                isBusyEvaluating = false,
                isGameOver = isComplete
            )
        }

        if (_state.value.isGameOver) {
            onGameCompleted()
        }
    }

    private fun handleMismatch(firstIdx: Int, secondIdx: Int) {
        _state.update { state ->
            val updatedCards = state.cards.toMutableList()
            updatedCards[firstIdx] = updatedCards[firstIdx].copy(isFaceUp = false)
            updatedCards[secondIdx] = updatedCards[secondIdx].copy(isFaceUp = false)

            state.copy(
                cards = updatedCards,
                streak = 0, // Reset combo
                isBusyEvaluating = false
            )
        }
    }

    private fun onGameCompleted() {
        audioManager?.playSound(SoundEffect.LEVEL_VICTORY)
        scope?.launch {
            repository?.recordCompletion(
                gameId = GAME_ID,
                completedLevel = _state.value.level,
                starsEarned = _state.value.starsEarned
            )
            val coinReward = 30 + (_state.value.starsEarned * 15)
            repository?.addReward(coins = coinReward, stars = _state.value.starsEarned)
        }
    }
}
