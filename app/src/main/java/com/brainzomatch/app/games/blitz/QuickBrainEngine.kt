package com.brainzomatch.app.games.blitz

import com.brainzomatch.app.core.audio.BrainzoAudioManager
import com.brainzomatch.app.core.audio.SoundEffect
import com.brainzomatch.app.core.data.repository.BrainzoRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.random.Random

/**
 * Multiple choice option for blitz challenge question.
 */
data class BlitzOption(
    val id: String,
    val label: String,
    val symbol: String
)

/**
 * Blitz matrix question pattern.
 */
data class BlitzQuestion(
    val questionPrompt: String,
    val matrixGrid: List<String>,
    val options: List<BlitzOption>,
    val correctOptionId: String
)

/**
 * State container for Game 8: Quick Brain Blitz.
 */
data class QuickBrainState(
    val roundNumber: Int = 1,
    val totalRounds: Int = 5,
    val secondsLeft: Int = 15,
    val currentQuestion: BlitzQuestion,
    val score: Int = 0,
    val streak: Int = 0,
    val starsEarned: Int = 3,
    val isTimerActive: Boolean = false,
    val isRoundComplete: Boolean = false,
    val isGameOver: Boolean = false,
    val lastAnswerCorrect: Boolean? = null
)

/**
 * QuickBrainEngine — Complete game engine for Game 8: Quick Brain Blitz.
 * Rapid matrix pattern completion, 15-second ticking timer, streak bonuses,
 * SoundPool audio warnings, and Room database progress persistence.
 * File Path: app/src/main/java/com/brainzomatch/app/games/blitz/QuickBrainEngine.kt
 */
class QuickBrainEngine(
    private val repository: BrainzoRepository? = null,
    private val audioManager: BrainzoAudioManager? = null,
    private val scope: CoroutineScope? = null
) {
    companion object {
        const val GAME_ID = "game_blitz"
        private const val BASE_POINTS = 100
        private const val TIME_BONUS_MULTIPLIER = 10
        private const val STREAK_BONUS = 25
        private const val ROUND_DURATION_SECONDS = 15
    }

    private var timerJob: Job? = null

    private val _state = MutableStateFlow(
        QuickBrainState(
            currentQuestion = generateMatrixQuestion(1)
        )
    )
    val state: StateFlow<QuickBrainState> = _state.asStateFlow()

    init {
        startRound(1)
    }

    /**
     * Starts a speed round and launches 15-second countdown timer.
     */
    fun startRound(round: Int = 1) {
        timerJob?.cancel()

        val question = generateMatrixQuestion(round)
        _state.value = QuickBrainState(
            roundNumber = round,
            totalRounds = 5,
            secondsLeft = ROUND_DURATION_SECONDS,
            currentQuestion = question,
            score = if (round == 1) 0 else _state.value.score,
            streak = if (round == 1) 0 else _state.value.streak,
            starsEarned = 3,
            isTimerActive = true,
            isRoundComplete = false,
            isGameOver = false,
            lastAnswerCorrect = null
        )

        startTimerCountdown()
    }

    private fun startTimerCountdown() {
        if (scope == null) return

        timerJob = scope.launch {
            while (_state.value.secondsLeft > 0 && !_state.value.isRoundComplete && !_state.value.isGameOver) {
                delay(1000L)
                val newSeconds = _state.value.secondsLeft - 1

                if (newSeconds in 1..4) {
                    audioManager?.playSound(SoundEffect.TIMER_WARNING)
                }

                _state.update { it.copy(secondsLeft = newSeconds) }

                if (newSeconds <= 0) {
                    onTimeExpired()
                    break
                }
            }
        }
    }

    /**
     * Evaluates player's selected multiple-choice answer option.
     */
    fun submitAnswer(selectedOptionId: String): Boolean {
        val currentState = _state.value
        if (currentState.isRoundComplete || currentState.isGameOver) return false

        timerJob?.cancel()
        val isCorrect = selectedOptionId == currentState.currentQuestion.correctOptionId

        if (isCorrect) {
            audioManager?.playSound(SoundEffect.MATCH_CORRECT)
            val newStreak = currentState.streak + 1
            val gainedPoints = BASE_POINTS + (currentState.secondsLeft * TIME_BONUS_MULTIPLIER) + (newStreak * STREAK_BONUS)
            val isFinalRound = currentState.roundNumber >= currentState.totalRounds
            val newScore = currentState.score + gainedPoints

            _state.update { state ->
                state.copy(
                    score = newScore,
                    streak = newStreak,
                    isRoundComplete = true,
                    isGameOver = isFinalRound,
                    lastAnswerCorrect = true
                )
            }

            if (isFinalRound) {
                handleVictory()
            } else {
                // Auto proceed to next round after short delay
                scope?.launch {
                    delay(800L)
                    startRound(currentState.roundNumber + 1)
                }
            }
            return true
        } else {
            audioManager?.playSound(SoundEffect.WRONG_TRY_AGAIN)
            _state.update { state ->
                state.copy(
                    streak = 0,
                    lastAnswerCorrect = false
                )
            }
            return false
        }
    }

    private fun onTimeExpired() {
        audioManager?.playSound(SoundEffect.WRONG_TRY_AGAIN)
        val currentState = _state.value
        val isFinal = currentState.roundNumber >= currentState.totalRounds

        _state.update { state ->
            state.copy(
                streak = 0,
                isRoundComplete = true,
                isGameOver = isFinal,
                lastAnswerCorrect = false
            )
        }

        if (!isFinal) {
            scope?.launch {
                delay(1000L)
                startRound(currentState.roundNumber + 1)
            }
        } else {
            handleVictory()
        }
    }

    private fun handleVictory() {
        audioManager?.playSound(SoundEffect.LEVEL_VICTORY)
        scope?.launch {
            repository?.recordCompletion(
                gameId = GAME_ID,
                completedLevel = _state.value.roundNumber,
                starsEarned = _state.value.starsEarned
            )
            repository?.addReward(coins = 70, stars = 3)
        }
    }

    /**
     * Generates a 3x3 pattern matrix where the 9th cell is missing ("?").
     */
    private fun generateMatrixQuestion(round: Int): BlitzQuestion {
        val patternTypes = listOf("shapes", "numbers", "emojis")
        val pattern = patternTypes[(round - 1) % patternTypes.size]

        when (pattern) {
            "numbers" -> {
                val start = Random.nextInt(1, 10)
                val step = Random.nextInt(2, 5)
                val sequence = (0..8).map { (start + it * step).toString() }
                val correct = sequence[8]

                val matrix = sequence.subList(0, 8) + "?"
                val options = listOf(
                    BlitzOption("A", correct, "🔢"),
                    BlitzOption("B", (correct.toInt() + step).toString(), "🔢"),
                    BlitzOption("C", (correct.toInt() - 1).toString(), "🔢"),
                    BlitzOption("D", (correct.toInt() + 2).toString(), "🔢")
                ).shuffled()

                return BlitzQuestion(
                    questionPrompt = "What number completes the arithmetic grid?",
                    matrixGrid = matrix,
                    options = options,
                    correctOptionId = options.first { it.label == correct }.id
                )
            }
            "shapes" -> {
                val shapes = listOf("🔺", "🟦", "🟢")
                val grid = mutableListOf<String>()
                for (r in 0..2) {
                    for (c in 0..2) {
                        grid.add(shapes[(r + c) % shapes.size])
                    }
                }
                val correct = grid[8]
                val matrix = grid.subList(0, 8) + "?"

                val options = listOf(
                    BlitzOption("A", "🔺", "🔺"),
                    BlitzOption("B", "🟦", "🟦"),
                    BlitzOption("C", "🟢", "🟢"),
                    BlitzOption("D", "⭐", "⭐")
                ).shuffled()

                return BlitzQuestion(
                    questionPrompt = "Which shape logically fits the bottom-right corner?",
                    matrixGrid = matrix,
                    options = options,
                    correctOptionId = options.first { it.label == correct }.id
                )
            }
            else -> {
                val animals = listOf("🦁", "🐯", "🐼")
                val grid = mutableListOf<String>()
                for (r in 0..2) {
                    for (c in 0..2) {
                        grid.add(animals[(r + c) % animals.size])
                    }
                }
                val correct = grid[8]
                val matrix = grid.subList(0, 8) + "?"

                val options = listOf(
                    BlitzOption("A", "🦁", "🦁"),
                    BlitzOption("B", "🐯", "🐯"),
                    BlitzOption("C", "🐼", "🐼"),
                    BlitzOption("D", "🦊", "🦊")
                ).shuffled()

                return BlitzQuestion(
                    questionPrompt = "Find the missing friendly creature to complete the row!",
                    matrixGrid = matrix,
                    options = options,
                    correctOptionId = options.first { it.label == correct }.id
                )
            }
        }
    }
}
