package com.brainzomatch.app.ui.screens.map

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Star
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
import com.brainzomatch.app.core.data.local.entities.GameProgressEntity
import com.brainzomatch.app.ui.theme.*

/**
 * Level node model representing a step on the quest map.
 */
data class QuestLevelNode(
    val levelNumber: Int,
    val title: String,
    val gameId: String,
    val gameName: String,
    val iconEmoji: String,
    val starRating: Int = 0,
    val isUnlocked: Boolean = false,
    val isCurrent: Boolean = false
)

/**
 * LevelMapScreen — Production Jetpack Compose Quest & Progression Map.
 * File Path: app/src/main/java/com/brainzomatch/app/ui/screens/map/LevelMapScreen.kt
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LevelMapScreen(
    onLevelSelected: (Int) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val app = remember(context) { context.applicationContext as BrainzoApplication }

    val allProgress by app.repository.allProgress.collectAsState(initial = emptyList())
    val profileState by app.repository.playerProfile.collectAsState(initial = null)

    // Calculate maximum unlocked level across all game progress entities
    val maxUnlockedLevel = remember(allProgress) {
        if (allProgress.isEmpty()) 14
        else allProgress.maxOfOrNull { it.highestLevelUnlocked } ?: 14
    }

    val totalStars = profileState?.stars ?: 86
    val currentCoins = profileState?.coins ?: 1450

    // Construct 20 level nodes across 3 distinct fantasy islands
    val questLevels = remember(maxUnlockedLevel) {
        (1..20).map { lvl ->
            val (gameId, gameName, emoji) = when (lvl % 8) {
                1 -> Triple("game_memory", "Memory Match", "🦁")
                2 -> Triple("game_math", "Math Blast", "🚀")
                3 -> Triple("game_difference", "Find Difference", "🔍")
                4 -> Triple("game_jigsaw", "Jigsaw Quest", "🧩")
                5 -> Triple("game_shapes", "Color & Shape", "🎨")
                6 -> Triple("game_word", "Word Wizard", "🦉")
                7 -> Triple("game_hidden", "Hidden Objects", "🗝️")
                else -> Triple("game_blitz", "Brain Blitz", "⚡")
            }

            val unlocked = lvl <= maxUnlockedLevel
            val starsEarned = when {
                lvl < maxUnlockedLevel -> 3
                lvl == maxUnlockedLevel -> 2
                else -> 0
            }

            QuestLevelNode(
                levelNumber = lvl,
                title = "Level $lvl",
                gameId = gameId,
                gameName = gameName,
                iconEmoji = emoji,
                starRating = starsEarned,
                isUnlocked = unlocked,
                isCurrent = (lvl == maxUnlockedLevel)
            )
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Quests & World Map",
                        fontWeight = FontWeight.Black,
                        fontSize = 18.sp,
                        color = BrainzoTextDark
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Rounded.ArrowBack, contentDescription = "Back", tint = BrainzoPurple)
                    }
                },
                actions = {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = BrainzoSurfaceLow,
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("⭐ $totalStars", fontWeight = FontWeight.Black, fontSize = 12.sp, color = BrainzoGold)
                            Spacer(Modifier.width(6.dp))
                            Text("🪙 $currentCoins", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = BrainzoTextDark)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BrainzoBgLight)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(BrainzoBgLight)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Milestone Island Header Banner
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = BrainzoPurple,
                shadowElevation = 3.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("WORLD 1: SAVANNAH KINGDOM", color = BrainzoGold, fontWeight = FontWeight.Black, fontSize = 12.sp)
                        Spacer(Modifier.height(4.dp))
                        Text("Level $maxUnlockedLevel / 20 In Progress", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Text("Next Mega Mystery Chest in 2 levels!", color = BrainzoPurpleLight, fontSize = 12.sp)
                    }
                    Surface(
                        shape = CircleShape,
                        color = BrainzoSurfaceLow,
                        modifier = Modifier.size(52.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text("🗺️", fontSize = 28.sp)
                        }
                    }
                }
            }

            // Path Winding Wavy Quest Levels
            Text("Quest Levels Roadmap", fontWeight = FontWeight.Black, fontSize = 16.sp, color = BrainzoTextDark)

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                questLevels.forEachIndexed { index, node ->
                    // Alternate alignment (left, center, right) to simulate an adventure trail
                    val horizontalAlignment = when (index % 3) {
                        0 -> Alignment.Start
                        1 -> Alignment.CenterHorizontally
                        else -> Alignment.End
                    }

                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = when (index % 3) {
                            0 -> Alignment.CenterStart
                            1 -> Alignment.Center
                            else -> Alignment.CenterEnd
                        }
                    ) {
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = when {
                                node.isCurrent -> BrainzoSurfaceLow
                                node.isUnlocked -> Color.White
                                else -> Color(0xFFF3F4F6)
                            },
                            border = BorderStroke(
                                width = if (node.isCurrent) 2.5.dp else 1.dp,
                                color = if (node.isCurrent) BrainzoPurple else if (node.isUnlocked) BrainzoSurfaceDim else Color.Transparent
                            ),
                            shadowElevation = if (node.isUnlocked) 2.dp else 0.dp,
                            modifier = Modifier
                                .width(220.dp)
                                .clickable(enabled = node.isUnlocked) {
                                    onLevelSelected(node.levelNumber)
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(46.dp)
                                        .clip(CircleShape)
                                        .background(
                                            when {
                                                node.isCurrent -> BrainzoPurple
                                                node.isUnlocked -> BrainzoSkyLight
                                                else -> Color(0xFFE5E7EB)
                                            }
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (node.isUnlocked) {
                                        Text(node.iconEmoji, fontSize = 22.sp)
                                    } else {
                                        Icon(Icons.Rounded.Lock, contentDescription = "Locked", tint = BrainzoTextMuted)
                                    }
                                }

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = node.title,
                                        fontWeight = FontWeight.Black,
                                        fontSize = 14.sp,
                                        color = if (node.isUnlocked) BrainzoTextDark else BrainzoTextMuted
                                    )
                                    Text(
                                        text = node.gameName,
                                        fontSize = 11.sp,
                                        color = if (node.isCurrent) BrainzoPurple else BrainzoTextMuted,
                                        fontWeight = if (node.isCurrent) FontWeight.Bold else FontWeight.Normal,
                                        maxLines = 1
                                    )

                                    if (node.isUnlocked) {
                                        Row(
                                            modifier = Modifier.padding(top = 2.dp),
                                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                                        ) {
                                            repeat(3) { starIdx ->
                                                Icon(
                                                    imageVector = Icons.Rounded.Star,
                                                    contentDescription = null,
                                                    tint = if (starIdx < node.starRating) BrainzoGold else BrainzoSurfaceDim,
                                                    modifier = Modifier.size(13.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}
