package com.brainzomatch.app.ui.screens.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.brainzomatch.app.BrainzoApplication
import com.brainzomatch.app.core.data.local.entities.GameProgressEntity
import com.brainzomatch.app.ui.theme.BrainzoBgLight
import com.brainzomatch.app.ui.theme.BrainzoGold
import com.brainzomatch.app.ui.theme.BrainzoGoldLight
import com.brainzomatch.app.ui.theme.BrainzoMint
import com.brainzomatch.app.ui.theme.BrainzoMintLight
import com.brainzomatch.app.ui.theme.BrainzoOrange
import com.brainzomatch.app.ui.theme.BrainzoOrangeLight
import com.brainzomatch.app.ui.theme.BrainzoPurple
import com.brainzomatch.app.ui.theme.BrainzoPurpleDark
import com.brainzomatch.app.ui.theme.BrainzoSky
import com.brainzomatch.app.ui.theme.BrainzoSkyLight
import com.brainzomatch.app.ui.theme.BrainzoSurface
import com.brainzomatch.app.ui.theme.BrainzoSurfaceLow
import com.brainzomatch.app.ui.theme.BrainzoTextDark
import com.brainzomatch.app.ui.theme.BrainzoTextMuted

/**
 * Brainzo Match - Home Dashboard & Games Hub.
 * File Path: app/src/main/java/com/brainzomatch/app/ui/screens/home/HomeScreen.kt
 *
 * Shows the player's coins / stars / streak, a VIP banner, a "continue your quest"
 * card and a grid of all 8 mini games with live progress from Room.
 */
private data class GameTile(
    val id: String,
    val title: String,
    val tagline: String,
    val emoji: String,
    val accent: Color,
    val accentLight: Color
)

private val gameTiles = listOf(
    GameTile("game_memory", "Memory Match", "Flip & find pairs", "🧠", BrainzoPurple, BrainzoSurfaceLow),
    GameTile("game_math", "Math Blast", "Race with numbers", "🚀", BrainzoSky, BrainzoSkyLight),
    GameTile("game_difference", "Spot the Difference", "Sharpen your eyes", "🔍", BrainzoOrange, BrainzoOrangeLight),
    GameTile("game_jigsaw", "Jigsaw Quest", "Snap the pieces", "🧩", BrainzoMint, BrainzoMintLight),
    GameTile("game_shapes", "Color & Shape", "Match by rules", "🔷", BrainzoPurple, BrainzoSurfaceLow),
    GameTile("game_word", "Word Wizard", "Spell the magic", "🪄", BrainzoGold, BrainzoGoldLight),
    GameTile("game_hidden", "Hidden Objects", "Find them all", "🌳", BrainzoMint, BrainzoMintLight),
    GameTile("game_blitz", "Quick Brain Blitz", "Think fast", "⚡", BrainzoOrange, BrainzoOrangeLight)
)

@Composable
fun HomeScreen(
    onNavigateToGame: (String) -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToVip: () -> Unit,
    onNavigateToMap: () -> Unit
) {
    val app = LocalContext.current.applicationContext as BrainzoApplication
    val player by app.repository.playerProfile.collectAsState(initial = null)
    val progressList by app.repository.allProgress.collectAsState(initial = emptyList())
    val progressById = remember(progressList) { progressList.associateBy { it.gameModeId } }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(BrainzoBgLight),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // ---------- Header ----------
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Hello, ${player?.displayName ?: "Little Thinker"}! 👋",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black,
                        color = BrainzoTextDark
                    )
                    Text(
                        text = "Ready to train your brain today?",
                        fontSize = 14.sp,
                        color = BrainzoTextMuted
                    )
                }
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(BrainzoSurfaceLow)
                        .clickable(role = Role.Button, onClick = onNavigateToProfile),
                    contentAlignment = Alignment.Center
                ) {
                    Text("🙂", fontSize = 26.sp)
                }
            }
        }

        // ---------- Stats ----------
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatChip("🪙", "${player?.coins ?: 0}", "Coins", BrainzoGoldLight, Modifier.weight(1f))
                StatChip("⭐", "${player?.stars ?: 0}", "Stars", BrainzoSkyLight, Modifier.weight(1f))
                StatChip("🔥", "${player?.streakDays ?: 0}", "Day streak", BrainzoOrangeLight, Modifier.weight(1f))
            }
        }

        // ---------- Quest card ----------
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onNavigateToMap),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent)
            ) {
                Row(
                    modifier = Modifier
                        .background(Brush.linearGradient(listOf(BrainzoPurple, BrainzoPurpleDark)))
                        .padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Continue your quest", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Black)
                        Spacer(Modifier.height(4.dp))
                        Text("Unlock new levels and earn stars", color = Color.White.copy(alpha = 0.85f), fontSize = 13.sp)
                    }
                    Text("🗺️", fontSize = 44.sp)
                }
            }
        }

        // ---------- VIP banner (hidden for VIP members) ----------
        if (player?.isVipMember != true) {
            item {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(onClick = onNavigateToVip),
                    shape = RoundedCornerShape(20.dp),
                    color = BrainzoGoldLight,
                    border = BorderStroke(1.dp, BrainzoGold.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("👑", fontSize = 30.sp)
                        Spacer(Modifier.size(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Brainzo VIP Pass", fontWeight = FontWeight.Black, color = BrainzoTextDark)
                            Text("No ads, bonus rewards and exclusive outfits", fontSize = 12.sp, color = BrainzoTextMuted)
                        }
                        Text("View", fontWeight = FontWeight.Black, color = BrainzoOrange)
                    }
                }
            }
        }

        // ---------- Games ----------
        item {
            Text("All Games", fontSize = 20.sp, fontWeight = FontWeight.Black, color = BrainzoTextDark)
        }
        gameTiles.chunked(2).forEach { rowTiles ->
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    rowTiles.forEach { tile ->
                        GameCard(
                            tile = tile,
                            progress = progressById[tile.id],
                            onClick = { onNavigateToGame(tile.id) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    if (rowTiles.size == 1) Spacer(Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun StatChip(emoji: String, value: String, label: String, bg: Color, modifier: Modifier = Modifier) {
    Surface(modifier = modifier, shape = RoundedCornerShape(18.dp), color = bg) {
        Column(
            modifier = Modifier.padding(vertical = 12.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(emoji, fontSize = 22.sp)
            Text(value, fontWeight = FontWeight.Black, fontSize = 18.sp, color = BrainzoTextDark)
            Text(label, fontSize = 11.sp, color = BrainzoTextMuted)
        }
    }
}

@Composable
private fun GameCard(
    tile: GameTile,
    progress: GameProgressEntity?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.clickable(role = Role.Button, onClick = onClick),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = BrainzoSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(tile.accentLight),
                contentAlignment = Alignment.Center
            ) {
                Text(tile.emoji, fontSize = 32.sp)
            }
            Spacer(Modifier.height(10.dp))
            Text(
                tile.title,
                fontWeight = FontWeight.Black,
                fontSize = 14.sp,
                color = BrainzoTextDark,
                textAlign = TextAlign.Center,
                maxLines = 1
            )
            Text(
                tile.tagline,
                fontSize = 11.sp,
                color = BrainzoTextMuted,
                textAlign = TextAlign.Center,
                maxLines = 1
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = if (progress == null || progress.timesPlayed == 0) {
                    "New!"
                } else {
                    "Level ${progress.highestLevelUnlocked}  •  ⭐ ${progress.totalStarsEarned}"
                },
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = tile.accent
            )
        }
    }
}
