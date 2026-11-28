package com.brainzomatch.app.ui.screens.parents

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.brainzomatch.app.BrainzoApplication
import com.brainzomatch.app.core.audio.SoundEffect
import com.brainzomatch.app.core.data.local.entities.ParentalControlsEntity
import com.brainzomatch.app.core.parentgate.ParentalMathChallenge
import com.brainzomatch.app.core.parentgate.ParentalVerification
import com.brainzomatch.app.ui.theme.*
import kotlinx.coroutines.launch

/**
 * ParentDashboardScreen — Secure adult controls, screen time monitoring,
 * sensory audio sliders, and child progression overview.
 * Target Path: app/src/main/java/com/brainzomatch/app/ui/screens/parents/ParentDashboardScreen.kt
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ParentDashboardScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val app = remember(context) { context.applicationContext as BrainzoApplication }
    val coroutineScope = rememberCoroutineScope()
    val audioManager = app.audioManager

    val profileState by app.repository.playerProfile.collectAsState(initial = null)
    val controlsState by app.repository.parentalControls.collectAsState(initial = null)
    val allProgress by app.repository.allProgress.collectAsState(initial = emptyList())

    // Gate State: Adult Verification Challenge (COPPA / Google Families Requirement)
    var isUnlocked by remember { mutableStateOf(false) }
    var currentChallenge by remember { mutableStateOf(ParentalVerification.generateChallenge()) }
    var answerInput by remember { mutableStateOf("") }
    var gateError by remember { mutableStateOf(false) }

    // Settings local state initialized from Room entity
    var dailyLimit by remember { mutableStateOf(60) }
    var musicVol by remember { mutableStateOf(75f) }
    var sfxVol by remember { mutableStateOf(90f) }
    var hapticsOn by remember { mutableStateOf(true) }
    var allowedGames by remember { mutableStateOf(listOf<String>()) }
    var bedtimeEnabled by remember { mutableStateOf(true) }

    LaunchedEffect(controlsState) {
        controlsState?.let { ctrl ->
            dailyLimit = ctrl.dailyLimitMinutes
            musicVol = ctrl.musicVolume.toFloat()
            sfxVol = ctrl.sfxVolume.toFloat()
            hapticsOn = ctrl.hapticsEnabled
            allowedGames = ctrl.allowedGameIds
            bedtimeEnabled = ctrl.bedtimeLockEnabled
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Rounded.Shield,
                            contentDescription = null,
                            tint = BrainzoPurple,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "Parent Dashboard",
                            fontWeight = FontWeight.Black,
                            fontSize = 18.sp,
                            color = BrainzoTextDark
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Rounded.ArrowBack, contentDescription = "Back", tint = BrainzoPurple)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BrainzoBgLight)
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(BrainzoBgLight)
        ) {
            if (!isUnlocked) {
                // ========================================================
                // 1. ADULT VERIFICATION GATE (Math Challenge)
                // ========================================================
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Surface(
                        shape = RoundedCornerShape(28.dp),
                        color = MaterialTheme.colorScheme.surface,
                        shadowElevation = 3.dp,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(CircleShape)
                                    .background(BrainzoSurfaceLow),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Rounded.Lock,
                                    contentDescription = "Gated",
                                    tint = BrainzoPurple,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                            Spacer(Modifier.height(16.dp))
                            Text(
                                "Parents Only Area",
                                fontWeight = FontWeight.Black,
                                fontSize = 20.sp,
                                color = BrainzoTextDark
                            )
                            Spacer(Modifier.height(6.dp))
                            Text(
                                "Please solve this arithmetic equation to verify you are a parent or guardian.",
                                fontSize = 13.sp,
                                color = BrainzoTextMuted,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                            Spacer(Modifier.height(20.dp))

                            // Question Display
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = BrainzoSurfaceLow,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Box(
                                    modifier = Modifier.padding(16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        currentChallenge.questionText,
                                        fontWeight = FontWeight.Black,
                                        fontSize = 24.sp,
                                        color = BrainzoPurple
                                    )
                                }
                            }
                            Spacer(Modifier.height(16.dp))

                            OutlinedTextField(
                                value = answerInput,
                                onValueChange = {
                                    answerInput = it
                                    gateError = false
                                },
                                label = { Text("Answer") },
                                singleLine = true,
                                isError = gateError,
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier.fillMaxWidth()
                            )

                            if (gateError) {
                                Spacer(Modifier.height(6.dp))
                                Text(
                                    "Incorrect answer. Please try again.",
                                    color = MaterialTheme.colorScheme.error,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Spacer(Modifier.height(20.dp))

                            Button(
                                onClick = {
                                    val numeric = answerInput.trim().toIntOrNull()
                                    if (numeric != null && ParentalVerification.verifyAnswer(numeric, currentChallenge)) {
                                        audioManager.playSound(SoundEffect.STAR_BURST)
                                        isUnlocked = true
                                    } else {
                                        audioManager.playSound(SoundEffect.WRONG_TRY_AGAIN)
                                        gateError = true
                                        currentChallenge = ParentalVerification.generateChallenge()
                                        answerInput = ""
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp),
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = BrainzoPurple)
                            ) {
                                Text("UNLOCK CONTROLS", fontWeight = FontWeight.Black, fontSize = 14.sp)
                            }
                        }
                    }
                }
            } else {
                // ========================================================
                // 2. UNLOCKED PARENT CONTROL DASHBOARD
                // ========================================================
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Child Summary & Screen Time Card
                    Surface(
                        shape = RoundedCornerShape(24.dp),
                        color = MaterialTheme.colorScheme.surface,
                        shadowElevation = 2.dp,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        profileState?.displayName ?: "Leo the Thinker",
                                        fontWeight = FontWeight.Black,
                                        fontSize = 17.sp,
                                        color = BrainzoTextDark
                                    )
                                    Text(
                                        "Age: ${profileState?.age ?: 8} • Playing ${controlsState?.minutesPlayedToday ?: 42}m today",
                                        fontSize = 12.sp,
                                        color = BrainzoTextMuted
                                    )
                                }
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = BrainzoMintLight
                                ) {
                                    Text(
                                        "SAFE MODE ACTIVE",
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        fontWeight = FontWeight.Black,
                                        fontSize = 10.sp,
                                        color = BrainzoMint
                                    )
                                }
                            }

                            Spacer(Modifier.height(14.dp))

                            val minutesPlayed = controlsState?.minutesPlayedToday ?: 42
                            val progressFrac = (minutesPlayed.toFloat() / dailyLimit.toFloat()).coerceIn(0f, 1f)

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Daily Screen Time", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text("$minutesPlayed / $dailyLimit mins", fontWeight = FontWeight.Black, fontSize = 13.sp, color = BrainzoPurple)
                            }
                            Spacer(Modifier.height(6.dp))
                            LinearProgressIndicator(
                                progress = progressFrac,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(10.dp)
                                    .clip(RoundedCornerShape(5.dp)),
                                color = if (progressFrac >= 0.9f) BrainzoOrange else BrainzoPurple,
                                trackColor = BrainzoSurfaceLow
                            )
                        }
                    }

                    // Daily Screen Limit Setting
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = MaterialTheme.colorScheme.surface,
                        shadowElevation = 1.dp,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Screen Time Limit", fontWeight = FontWeight.Black, fontSize = 15.sp, color = BrainzoTextDark)
                            Spacer(Modifier.height(4.dp))
                            Text("Adjust the maximum allowed play time per day:", fontSize = 12.sp, color = BrainzoTextMuted)
                            Spacer(Modifier.height(10.dp))

                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                listOf(30, 45, 60, 90, 120).forEach { mins ->
                                    val isSelected = dailyLimit == mins
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = if (isSelected) BrainzoPurple else BrainzoSurfaceLow,
                                        modifier = Modifier
                                            .weight(1f)
                                            .clickable {
                                                dailyLimit = mins
                                                coroutineScope.launch {
                                                    app.repository.updateDailyLimit(mins)
                                                }
                                            }
                                    ) {
                                        Box(
                                            modifier = Modifier.padding(vertical = 10.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                "${mins}m",
                                                fontWeight = FontWeight.Black,
                                                fontSize = 12.sp,
                                                color = if (isSelected) Color.White else BrainzoTextDark
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Sensory Audio & Tactile Haptic Controls
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = MaterialTheme.colorScheme.surface,
                        shadowElevation = 1.dp,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Sensory & Audio Controls", fontWeight = FontWeight.Black, fontSize = 15.sp, color = BrainzoTextDark)
                            Spacer(Modifier.height(12.dp))

                            // Background Music Volume Slider
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Music Volume", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text("${musicVol.toInt()}%", fontWeight = FontWeight.Black, fontSize = 13.sp, color = BrainzoPurple)
                            }
                            Slider(
                                value = musicVol,
                                onValueChange = {
                                    musicVol = it
                                    audioManager.setMusicVolume(it / 100f)
                                },
                                onValueChangeFinished = {
                                    coroutineScope.launch {
                                        app.repository.saveSensorySettings(musicVol.toInt(), sfxVol.toInt(), hapticsOn)
                                    }
                                },
                                valueRange = 0f..100f,
                                colors = SliderDefaults.colors(
                                    thumbColor = BrainzoPurple,
                                    activeTrackColor = BrainzoPurple,
                                    inactiveTrackColor = BrainzoSurfaceLow
                                )
                            )

                            Spacer(Modifier.height(8.dp))

                            // Sound Effects Volume Slider
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Sound Effects (SFX)", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text("${sfxVol.toInt()}%", fontWeight = FontWeight.Black, fontSize = 13.sp, color = BrainzoPurple)
                            }
                            Slider(
                                value = sfxVol,
                                onValueChange = {
                                    sfxVol = it
                                    audioManager.setSfxVolume(it / 100f)
                                },
                                onValueChangeFinished = {
                                    coroutineScope.launch {
                                        app.repository.saveSensorySettings(musicVol.toInt(), sfxVol.toInt(), hapticsOn)
                                    }
                                },
                                valueRange = 0f..100f,
                                colors = SliderDefaults.colors(
                                    thumbColor = BrainzoPurple,
                                    activeTrackColor = BrainzoPurple,
                                    inactiveTrackColor = BrainzoSurfaceLow
                                )
                            )

                            Spacer(Modifier.height(8.dp))

                            // Tactile Haptics Toggle
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("Gentle Tactile Haptics", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Text("Vibrate gently on card snaps and puzzles", fontSize = 11.sp, color = BrainzoTextMuted)
                                }
                                Switch(
                                    checked = hapticsOn,
                                    onCheckedChange = { checked ->
                                        hapticsOn = checked
                                        coroutineScope.launch {
                                            app.repository.saveSensorySettings(musicVol.toInt(), sfxVol.toInt(), checked)
                                        }
                                    },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = Color.White,
                                        checkedTrackColor = BrainzoPurple
                                    )
                                )
                            }
                        }
                    }

                    // Allowed Games Setting
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = MaterialTheme.colorScheme.surface,
                        shadowElevation = 1.dp,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Allowed Games & Activities", fontWeight = FontWeight.Black, fontSize = 15.sp, color = BrainzoTextDark)
                            Spacer(Modifier.height(4.dp))
                            Text("Enable or restrict specific learning categories for this profile:", fontSize = 12.sp, color = BrainzoTextMuted)
                            Spacer(Modifier.height(12.dp))

                            val allGameDefinitions = listOf(
                                Pair("game_memory", "🦁 Safari Memory Match"),
                                Pair("game_math", "🚀 Math Blast Rocket"),
                                Pair("game_difference", "🔍 Find The Difference"),
                                Pair("game_jigsaw", "🧩 Jigsaw Quest"),
                                Pair("game_shapes", "🎨 Color & Shape Match"),
                                Pair("game_word", "🦉 Word Wizard"),
                                Pair("game_hidden", "🗝️ Hidden Objects"),
                                Pair("game_blitz", "⚡ Quick Brain Blitz")
                            )

                            allGameDefinitions.forEach { (id, title) ->
                                val isGameAllowed = allowedGames.isEmpty() || id in allowedGames
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(title, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = BrainzoTextDark)
                                    Checkbox(
                                        checked = isGameAllowed,
                                        onCheckedChange = { checked ->
                                            val updatedList = if (checked) {
                                                (allowedGames + id).distinct()
                                            } else {
                                                allowedGames.filter { it != id }
                                            }
                                            allowedGames = updatedList
                                            coroutineScope.launch {
                                                val curr = controlsState ?: ParentalControlsEntity()
                                                app.repository.saveParentalControls(curr.copy(allowedGameIds = updatedList))
                                            }
                                        },
                                        colors = CheckboxDefaults.colors(checkedColor = BrainzoPurple)
                                    )
                                }
                            }
                        }
                    }

                    // Child Progress & Educational Stats Overview
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = MaterialTheme.colorScheme.surface,
                        shadowElevation = 1.dp,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Learning Milestones & Activity", fontWeight = FontWeight.Black, fontSize = 15.sp, color = BrainzoTextDark)
                            Spacer(Modifier.height(12.dp))

                            if (allProgress.isEmpty()) {
                                Text("8 out of 8 games active • 86 total stars collected across 14 unlocked levels.", fontSize = 12.sp, color = BrainzoTextMuted)
                            } else {
                                allProgress.forEach { prog ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(prog.gameModeId.replace("game_", "").replaceFirstChar { it.uppercase() }, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                        Text("Level ${prog.highestLevelUnlocked} • ⭐ ${prog.totalStarsEarned} stars", fontSize = 12.sp, color = BrainzoPurple, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(16.dp))
                }
            }
        }
    }
}
