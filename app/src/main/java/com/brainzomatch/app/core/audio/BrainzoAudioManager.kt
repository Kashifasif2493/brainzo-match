package com.brainzomatch.app.core.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.SoundPool
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log

/**
 * Sound effect enumerations matching the exact resource IDs in res/raw/
 */
enum class SoundEffect {
    TAP_BUTTON,
    CARD_FLIP,
    MATCH_CORRECT,
    WRONG_TRY_AGAIN,
    LEVEL_VICTORY,
    COIN_REWARD,
    TIMER_WARNING,
    PUZZLE_SNAP,
    WORD_LETTER_PLACE,
    STAR_BURST
}

/**
 * BrainzoAudioManager — Sensory audio engine managing low-latency SoundPool playback,
 * looping background MediaPlayer music, and synchronized tactile haptic vibrations.
 * File Path: app/src/main/java/com/brainzomatch/app/core/audio/BrainzoAudioManager.kt
 */
class BrainzoAudioManager(private val context: Context) {

    companion object {
        private const val TAG = "BrainzoAudioManager"
    }

    private var soundPool: SoundPool? = null
    private val soundMap = mutableMapOf<SoundEffect, Int>()

    private var bgmPlayer: MediaPlayer? = null
    private var isBgmPlaying: Boolean = false

    private var masterMusicVolume: Float = 0.75f
    private var masterSfxVolume: Float = 0.90f
    private var isHapticsEnabled: Boolean = true

    private val vibrator: Vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
        vibratorManager.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    }

    init {
        initSoundPool()
    }

    private fun initSoundPool() {
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        soundPool = SoundPool.Builder()
            .setMaxStreams(8)
            .setAudioAttributes(audioAttributes)
            .build()

        loadSounds()
    }

    private fun loadSounds() {
        val pool = soundPool ?: return

        fun loadResource(resName: String): Int {
            val resId = context.resources.getIdentifier(resName, "raw", context.packageName)
            return if (resId != 0) {
                try {
                    pool.load(context, resId, 1)
                } catch (e: Exception) {
                    Log.w(TAG, "Failed loading sound resource: $resName", e)
                    -1
                }
            } else {
                Log.w(TAG, "Audio resource not found in res/raw/: $resName")
                -1
            }
        }

        soundMap[SoundEffect.TAP_BUTTON] = loadResource("button_click")
        soundMap[SoundEffect.CARD_FLIP] = loadResource("card_flip")
        soundMap[SoundEffect.MATCH_CORRECT] = loadResource("correct_answer")
        soundMap[SoundEffect.WRONG_TRY_AGAIN] = loadResource("wrong_answer")
        soundMap[SoundEffect.LEVEL_VICTORY] = loadResource("victory")
        soundMap[SoundEffect.COIN_REWARD] = loadResource("coin_collect")
        soundMap[SoundEffect.TIMER_WARNING] = loadResource("timer_tick")
        soundMap[SoundEffect.PUZZLE_SNAP] = loadResource("puzzle_snap")
        soundMap[SoundEffect.WORD_LETTER_PLACE] = loadResource("puzzle_snap")
        soundMap[SoundEffect.STAR_BURST] = loadResource("reward_unlock")
    }

    fun playSound(effect: SoundEffect) {
        val soundId = soundMap[effect] ?: return
        if (soundId > 0) {
            soundPool?.play(soundId, masterSfxVolume, masterSfxVolume, 1, 0, 1.0f)
        }

        if (isHapticsEnabled) {
            triggerHaptic(effect)
        }
    }

    fun startBackgroundMusic() {
        if (bgmPlayer == null) {
            val resId = context.resources.getIdentifier("brainzo_background", "raw", context.packageName)
            if (resId != 0) {
                try {
                    bgmPlayer = MediaPlayer.create(context, resId)?.apply {
                        isLooping = true
                        setVolume(masterMusicVolume, masterMusicVolume)
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "Error creating background music MediaPlayer", e)
                }
            } else {
                Log.w(TAG, "brainzo_background raw audio asset not found; skipping BGM creation.")
            }
        }

        if (!isBgmPlaying && bgmPlayer != null) {
            try {
                bgmPlayer?.start()
                isBgmPlaying = true
                Log.d(TAG, "BGM playback started.")
            } catch (e: Exception) {
                Log.e(TAG, "Error starting background music", e)
            }
        }
    }

    fun pauseBackgroundMusic() {
        if (isBgmPlaying && bgmPlayer != null) {
            try {
                bgmPlayer?.pause()
                isBgmPlaying = false
                Log.d(TAG, "BGM paused.")
            } catch (e: Exception) {
                Log.e(TAG, "Error pausing background music", e)
            }
        }
    }

    fun resumeBackgroundMusic() {
        if (!isBgmPlaying && bgmPlayer != null) {
            try {
                bgmPlayer?.start()
                isBgmPlaying = true
                Log.d(TAG, "BGM resumed.")
            } catch (e: Exception) {
                Log.e(TAG, "Error resuming background music", e)
            }
        }
    }

    fun stopBackgroundMusic() {
        if (bgmPlayer != null) {
            try {
                bgmPlayer?.stop()
                isBgmPlaying = false
                Log.d(TAG, "BGM stopped.")
            } catch (e: Exception) {
                Log.e(TAG, "Error stopping background music", e)
            }
        }
    }

    /** Music volume from 0.0 to 1.0 (used by the Parent Dashboard slider). */
    fun setMusicVolume(volume: Float) {
        masterMusicVolume = volume.coerceIn(0f, 1f)
        bgmPlayer?.setVolume(masterMusicVolume, masterMusicVolume)
    }

    /** Sound-effects volume from 0.0 to 1.0 (used by the Parent Dashboard slider). */
    fun setSfxVolume(volume: Float) {
        masterSfxVolume = volume.coerceIn(0f, 1f)
    }

    fun updateSettings(musicVolPercent: Int, sfxVolPercent: Int, haptics: Boolean) {
        masterMusicVolume = (musicVolPercent / 100f).coerceIn(0f, 1f)
        masterSfxVolume = (sfxVolPercent / 100f).coerceIn(0f, 1f)
        isHapticsEnabled = haptics

        bgmPlayer?.setVolume(masterMusicVolume, masterMusicVolume)
    }

    private fun triggerHaptic(effect: SoundEffect) {
        if (!vibrator.hasVibrator()) return

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            when (effect) {
                SoundEffect.TAP_BUTTON -> {
                    vibrator.vibrate(VibrationEffect.createOneShot(15, VibrationEffect.DEFAULT_AMPLITUDE))
                }
                SoundEffect.MATCH_CORRECT, SoundEffect.PUZZLE_SNAP, SoundEffect.WORD_LETTER_PLACE -> {
                    vibrator.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 30, 40, 40), -1))
                }
                SoundEffect.WRONG_TRY_AGAIN -> {
                    vibrator.vibrate(VibrationEffect.createOneShot(60, 180))
                }
                SoundEffect.LEVEL_VICTORY, SoundEffect.STAR_BURST -> {
                    vibrator.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 50, 60, 50, 60, 100), -1))
                }
                else -> {
                    vibrator.vibrate(VibrationEffect.createOneShot(20, VibrationEffect.DEFAULT_AMPLITUDE))
                }
            }
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(30)
        }
    }

    fun release() {
        soundPool?.release()
        soundPool = null
        bgmPlayer?.stop()
        bgmPlayer?.release()
        bgmPlayer = null
        isBgmPlaying = false
    }
}
