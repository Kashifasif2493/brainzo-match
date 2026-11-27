package com.brainzomatch.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import com.brainzomatch.app.core.audio.BrainzoAudioManager
import com.brainzomatch.app.ui.navigation.BrainzoMainApp
import com.brainzomatch.app.ui.theme.BrainzoMatchTheme

/**
 * MainActivity — Entry point for Brainzo Match.
 * Target SDK: Android 16 (API 36) | Min SDK: 24
 * Manages lifecycle hooks for background music using the single application-scoped
 * BrainzoAudioManager instance and sets the Jetpack Compose surface.
 */
class MainActivity : ComponentActivity() {

    private lateinit var audioManager: BrainzoAudioManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Reuse the single application-scoped BrainzoAudioManager instance
        val app = application as BrainzoApplication
        audioManager = app.audioManager

        // Edge-to-edge layout configuration
        WindowCompat.setDecorFitsSystemWindows(window, true)

        setContent {
            BrainzoMatchTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    BrainzoMainApp()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Start or resume looping background music when returning to foreground
        audioManager.resumeBackgroundMusic()
    }

    override fun onPause() {
        super.onPause()
        // Pause background music when the app goes into the background
        audioManager.pauseBackgroundMusic()
    }

    override fun onDestroy() {
        super.onDestroy()
        // Only pause when finishing; do NOT call audioManager.release() on configuration
        // changes or Activity recreation so the application-scoped SoundPool & MediaPlayer
        // remain intact across recreations.
        if (isFinishing && !isChangingConfigurations) {
            audioManager.pauseBackgroundMusic()
        }
    }
}
