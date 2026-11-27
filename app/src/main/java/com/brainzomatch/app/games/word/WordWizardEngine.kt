package com.brainzomatch.app.games.word

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
 * Vocabulary puzzle definition representation.
 */
data class WordPuzzle(
    val id: Int,
    val word: String,
    val clueEmoji: String,
    val category: String,
    val clueHintText: String
)

/**
 * Reactive state container for Game 6: Word Wizard.
 */
data class WordWizardState(
    val level: Int = 1,
    val targetWord: String = "TIGER",
    val clueImageEmoji: String = "🐯",
    val category: String = "Jungle Animals",
    val wordSlots: List<Char?> = listOf(null, null, null, null, null),
    val rackLetters: List<Char> = emptyList(),
    val filledIndex: Int = 0,
    val score: Int = 0,
    val starsEarned: Int = 3,
    val mistakesCount: Int = 0,
    val hintsLeft: Int = 3,
    val isWordCompleted: Boolean = false
)

/**
 * WordWizardEngine — Complete game engine for Game 6: Word Wizard.
 * Manages phonics letter spelling, word slot population, backspace/correction,
 * letter rack shuffling, wizard hints, and Room database progress persistence.
 * File Path: app/src/main/java/com/brainzomatch/app/games/word/WordWizardEngine.kt
 */
class WordWizardEngine(
    private val repository: BrainzoRepository? = null,
    private val audioManager: BrainzoAudioManager? = null,
    private val scope: CoroutineScope? = null
) {
    companion object {
        const val GAME_ID = "game_word"
        private const val SCORE_PER_CORRECT_WORD = 200
        private const val SCORE_PER_LETTER = 25
    }

    private val _state = MutableStateFlow(WordWizardState())
    val state: StateFlow<WordWizardState> = _state.asStateFlow()

    private val wordDictionary = listOf(
        WordPuzzle(1, "LION", "🦁", "Wild Animals", "King of the jungle"),
        WordPuzzle(2, "BEAR", "🐻", "Forest Friends", "Loves honey"),
        WordPuzzle(3, "ZEBRA", "🦓", "Safari Animals", "Black and white stripes"),
        WordPuzzle(4, "APPLE", "🍎", "Delicious Fruits", "Crisp red healthy snack"),
        WordPuzzle(5, "ROCKET", "🚀", "Space Voyage", "Flies to the stars"),
        WordPuzzle(6, "CASTLE", "🏰", "Royal Kingdom", "Home to kings and queens"),
        WordPuzzle(7, "PENGUIN", "🐧", "Polar World", "Waddles on ice"),
        WordPuzzle(8, "DOLPHIN", "🐬", "Ocean Life", "Friendly swimmer in the sea")
    )

    init {
        startLevel(1)
    }

    /**
     * Initializes a level puzzle and populates letter rack tiles.
     */
    fun startLevel(level: Int) {
        val puzzleIndex = (level - 1) % wordDictionary.size
        val puzzle = wordDictionary[puzzleIndex]
        val wordUpper = puzzle.word.uppercase()

        // Generate letter tiles rack with target letters plus 3 random distractor consonants/vowels
        val alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
        val distractors = (1..3).map { alphabet.random() }
        val allLetters = (wordUpper.toList() + distractors).shuffled()

        _state.value = WordWizardState(
            level = level,
            targetWord = wordUpper,
            clueImageEmoji = puzzle.clueEmoji,
            category = puzzle.category,
            wordSlots = List(wordUpper.length) { null },
            rackLetters = allLetters,
            filledIndex = 0,
            score = 0,
            starsEarned = 3,
            mistakesCount = 0,
            hintsLeft = 3,
            isWordCompleted = false
        )
    }

    /**
     * Handles player tapping a letter tile block from the rack.
     */
    fun onLetterBlockTapped(letter: Char): Boolean {
        val currentState = _state.value
        if (currentState.isWordCompleted) return false

        val nextSlot = currentState.wordSlots.indexOfFirst { it == null }
        if (nextSlot == -1) return false // All slots filled

        val expectedChar = currentState.targetWord[nextSlot]

        if (letter == expectedChar) {
            // Correct letter placement
            audioManager?.playSound(SoundEffect.PUZZLE_SNAP)

            val updatedSlots = currentState.wordSlots.toMutableList()
            updatedSlots[nextSlot] = letter

            // Remove placed letter instance from rack
            val updatedRack = currentState.rackLetters.toMutableList()
            updatedRack.remove(letter)

            val isComplete = updatedSlots.none { it == null }
            val addedScore = SCORE_PER_LETTER + (if (isComplete) SCORE_PER_CORRECT_WORD else 0)

            _state.update { state ->
                state.copy(
                    wordSlots = updatedSlots,
                    rackLetters = updatedRack,
                    filledIndex = nextSlot + 1,
                    score = state.score + addedScore,
                    isWordCompleted = isComplete
                )
            }

            if (isComplete) {
                handleVictory()
            }
            return true
        } else {
            // Incorrect letter tapped
            audioManager?.playSound(SoundEffect.WRONG_TRY_AGAIN)
            val newMistakes = currentState.mistakesCount + 1
            val calculatedStars = when {
                newMistakes <= 1 -> 3
                newMistakes <= 3 -> 2
                else -> 1
            }

            _state.update { state ->
                state.copy(
                    mistakesCount = newMistakes,
                    starsEarned = calculatedStars
                )
            }
            return false
        }
    }

    /**
     * Erases the last entered letter from the word slots (Backspace).
     */
    fun onBackspace(): Boolean {
        val currentState = _state.value
        if (currentState.isWordCompleted) return false

        val lastFilledSlot = currentState.wordSlots.indexOfLast { it != null }
        if (lastFilledSlot == -1) return false

        val removedChar = currentState.wordSlots[lastFilledSlot] ?: return false

        val updatedSlots = currentState.wordSlots.toMutableList()
        updatedSlots[lastFilledSlot] = null

        val updatedRack = currentState.rackLetters + removedChar

        _state.update { state ->
            state.copy(
                wordSlots = updatedSlots,
                rackLetters = updatedRack,
                filledIndex = lastFilledSlot
            )
        }
        return true
    }

    /**
     * Auto-populates the next expected letter into the active slot (Wizard Hint).
     */
    fun useHint(): Boolean {
        val currentState = _state.value
        if (currentState.hintsLeft <= 0 || currentState.isWordCompleted) return false

        val nextSlot = currentState.wordSlots.indexOfFirst { it == null }
        if (nextSlot == -1) return false

        val expectedChar = currentState.targetWord[nextSlot]
        audioManager?.playSound(SoundEffect.STAR_BURST)

        val updatedSlots = currentState.wordSlots.toMutableList()
        updatedSlots[nextSlot] = expectedChar

        val updatedRack = currentState.rackLetters.toMutableList()
        updatedRack.remove(expectedChar)

        val isComplete = updatedSlots.none { it == null }
        val addedScore = SCORE_PER_LETTER + (if (isComplete) SCORE_PER_CORRECT_WORD else 0)

        _state.update { state ->
            state.copy(
                wordSlots = updatedSlots,
                rackLetters = updatedRack,
                filledIndex = nextSlot + 1,
                hintsLeft = state.hintsLeft - 1,
                score = state.score + addedScore,
                isWordCompleted = isComplete
            )
        }

        if (isComplete) {
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
                starsEarned = _state.value.starsEarned
            )
            repository?.addReward(coins = 60, stars = _state.value.starsEarned)
        }
    }
}
