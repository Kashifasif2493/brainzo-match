package com.brainzomatch.app

import android.app.Application
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import com.brainzomatch.app.core.ads.AdMobFamiliesManager
import com.brainzomatch.app.core.audio.BrainzoAudioManager
import com.brainzomatch.app.core.billing.BillingManager
import com.brainzomatch.app.core.data.local.BrainzoDatabase
import com.brainzomatch.app.core.data.repository.BrainzoRepository

/**
 * BrainzoApplication — Application class managing global singleton lifecycles:
 * - Room Database v2 local offline persistence
 * - Audio & tactile haptic manager (SoundPool + MediaPlayer)
 * - Google AdMob Families & COPPA child-directed configuration
 * - Google Play Billing v6 client
 */
class BrainzoApplication : Application() {

    companion object {
        private const val TAG = "BrainzoApplication"
        lateinit var instance: BrainzoApplication
            private set
    }

    lateinit var database: BrainzoDatabase
        private set

    lateinit var repository: BrainzoRepository
        private set

    lateinit var audioManager: BrainzoAudioManager
        private set

    lateinit var billingManager: BillingManager
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this

        Log.d(TAG, "Initializing Brainzo Match core systems...")

        // 1. Initialize persistent Room Database (v2 schema)
        database = BrainzoDatabase.getInstance(this)
        repository = BrainzoRepository(database)
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch { repository.ensureDefaults() }

        // 2. Initialize single application-scoped sensory audio & haptic manager.
        // Background music is started/resumed in MainActivity.onResume() so it
        // respects foreground/background lifecycle transitions cleanly.
        audioManager = BrainzoAudioManager(this)

        // 3. Initialize Google AdMob with strict Google Play Families Policy & COPPA compliance
        AdMobFamiliesManager.initialize(this)

        // 4. Initialize Google Play Billing v6 Manager for Brainzo VIP Pass
        billingManager = BillingManager(this) { isVipPurchased ->
            Log.d(TAG, "VIP subscription status updated: $isVipPurchased")
        }
        billingManager.startBillingConnection()

        Log.d(TAG, "Brainzo Match core initialized successfully.")
    }

    override fun onTerminate() {
        super.onTerminate()
        audioManager.release()
        billingManager.destroy()
    }
}
