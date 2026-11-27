package com.brainzomatch.app.games.math

import com.brainzomatch.app.core.audio.BrainzoAudioManager
import com.brainzomatch.app.core.audio.SoundEffect
import com.brainzomatch.app.core.data.repository.BrainzoRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.random.Random

/**
 * Arithmetic problem representation with operand parameters, operator, and multiple-choice options.
 */
data class MathQuestion(
    val factorA: Int,
    val factorB: Int,
    val operation: String,
    val correctAnswer: Int,
    val options: List<Int>,
    val displayPrompt: String
)

/**
 * State container for Math Blast speed-math game.
 */
data class MathBlastState(
    val level: Int = 1,
    val currentQuestion: MathQuestion,
    val score: Int = 0,
    val comboStreak: Int = 0,
    val questionsAnswered: Int = 0,
    val totalQuestionsForLevel: Int = 10,
    val playerProgressPercent: Int = 0,
    val rivalProgressPercent: Int = 0,
    val isLevelWon: Boolean = false,
    val isGameOver: Boolean = false,
    val lastAnswerWasCorrect: Boolean? = null
)

/**
 * MathBlastEngine — Complete game engine for Game 2: Math Blast Rocket Race.
 * Generates age-calibrated arithmetic puzzles (+, -, ×), manages rocket progress against rival,
 * calculates score bonuses, and updates Room DB.
 * File Path: app/src/main/java/com/brainzomatch/app/games/math/MathBlastEngine.kt
 */
class MathBlastEngine(
    private val repository: BrainzoRepository? = null,
    private val audioManager: BrainzoAudioManager? = null,
    private val scope: CoroutineScope? = null
) {
    companion object {
        const val GAME_ID = "game_math"
        private const val BASE_POINTS_PER_QUESTION = 50
        private const val STREAK_MULTIPLIER_BONUS = 20
        private const val TARGET_QUESTIONS_TO_WIN = 10
    }

    private val _state = MutableStateFlow(
        MathBlastState(
            currentQuestion = generateQuestionForLevel(1)
        )
    )
    val state: StateFlow<MathBlastState> = _state.asStateFlow()

    init {
        startLevel(1)
    }

    fun startLevel(level: Int) {
        val firstQuestion = generateQuestionForLevel(level)
        _state.value = MathBlastState(
            level = level,
            currentQuestion = firstQuestion,
            score = 0,
            comboStreak = 0,
            questionsAnswered = 0,
            totalQuestionsForLevel = TARGET_QUESTIONS_TO_WIN,
            playerProgressPercent = 0,
            rivalProgressPercent = 0,
            isLevelWon = false,
            isGameOver = false,
            lastAnswerWasCorrect = null
        )
    }

    /**
     * Submits player's answer selection and advances rocket race.
     * Returns true if answer is correct.
     */
    fun submitAnswer(selectedAnswer: Int): Boolean {
        val currentState = _state.value
        if (currentState.isGameOver) return false

        val isCorrect = selectedAnswer == currentState.currentQuestion.correctAnswer

        if (isCorrect) {
            audioManager?.playSound(SoundEffect.MATCH_CORRECT)
            val newStreak = currentState.comboStreak + 1
            val gainedPoints = BASE_POINTS_PER_QUESTION + (newStreak * STREAK_MULTIPLIER_BONUS)
            val answeredCount = currentState.questionsAnswered + 1

            // Rocket distance increments by 10% per correct answer
            val newPlayerProgress = (answeredCount * (100 / TARGET_QUESTIONS_TO_WIN)).coerceAtMost(100)
            val isWon = newPlayerProgress >= 100

            val nextQuestion = if (!isWon) generateQuestionForLevel(currentState.level) else currentState.currentQuestion

            _state.update { state ->
                state.copy(
                    score = state.score + gainedPoints,
                    comboStreak = newStreak,
                    questionsAnswered = answeredCount,
                    playerProgressPercent = newPlayerProgress,
                    isLevelWon = isWon,
                    isGameOver = isWon,
                    currentQuestion = nextQuestion,
                    lastAnswerWasCorrect = true
                )
            }

            if (isWon) {
                handleVictory()
            }
        } else {
            audioManager?.playSound(SoundEffect.WRONG_TRY_AGAIN)
            // Competitor gains slight progress on mistake
            _state.update { state ->
                val rivalAdvance = (state.rivalProgressPercent + 15).coerceAtMost(100)
                state.copy(
                    comboStreak = 0,
                    rivalProgressPercent = rivalAdvance,
                    lastAnswerWasCorrect = false
                )
            }
        }

        return isCorrect
    }

    private fun handleVictory() {
        audioManager?.playSound(SoundEffect.LEVEL_VICTORY)
        scope?.launch {
            repository?.recordCompletion(
                gameId = GAME_ID,
                completedLevel = _state.value.level,
                starsEarned = 3
            )
            repository?.addReward(coins = 75, stars = 3)
        }
    }

    /**
     * Calibrates math difficulty to age appropriateness (Levels 1–3: Addition/Subtraction up to 20;
     * Levels 4+: Multiplication tables 2–9).
     */
    private fun generateQuestionForLevel(level: Int): MathQuestion {
        val op = when {
            level <= 2 -> if (Random.nextBoolean()) "+" else "-"
            level == 3 -> listOf("+", "-", "+").random()
            else -> listOf("+", "-", "×").random()
        }

        var numA: Int
        var numB: Int
        var answer: Int

        when (op) {
            "+" -> {
                val maxRange = if (level <= 2) 12 else 25
                numA = Random.nextInt(2, maxRange)
                numB = Random.nextInt(2, maxRange)
                answer = numA + numB
            }
            "-" -> {
                val maxRange = if (level <= 2) 15 else 30
                val high = Random.nextInt(6, maxRange)
                val low = Random.nextInt(2, high)
                numA = high
                numB = low
                answer = numA - numB
            }
            "×" -> {
                numA = Random.nextInt(2, 9)
                numB = Random.nextInt(2, 9)
                answer = numA * numB
            }
            else -> {
                numA = 5
                numB = 5
                answer = 10
            }
        }

        // Generate 3 plausible incorrect distractor options
        val distractors = mutableSetOf<Int>()
        distractors.add(answer)

        while (distractors.size < 4) {
            val offset = listOf(-2, -1, 1, 2, 3, 5, 10).random()
            val candidate = answer + offset
            if (candidate >= 0 && candidate != answer) {
                distractors.add(candidate)
            }
        }

        val optionsList = distractors.toList().shuffled()
        val prompt = "$numA $op $numB = ?"

        return MathQuestion(
            factorA = numA,
            factorB = numB,
            operation = op,
            correctAnswer = answer,
            options = optionsList,
            displayPrompt = prompt
        )
    }
}
