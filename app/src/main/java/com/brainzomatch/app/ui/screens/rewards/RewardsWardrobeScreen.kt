package com.brainzomatch.app.ui.screens.rewards

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
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
import com.brainzomatch.app.ui.theme.*
import kotlinx.coroutines.launch

/**
 * Wardrobe item data model.
 */
data class WardrobeItem(
    val id: String,
    val name: String,
    val type: String,
    val costCoins: Int,
    val iconEmoji: String,
    val isVipOnly: Boolean = false
)

/**
 * RewardsWardrobeScreen — Production Jetpack Compose Dressing Room & Economy Shop.
 * File Path: app/src/main/java/com/brainzomatch/app/ui/screens/rewards/RewardsWardrobeScreen.kt
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RewardsWardrobeScreen(
    onBack: () -> Unit,
    onGoVip: () -> Unit
) {
    val context = LocalContext.current
    val app = remember(context) { context.applicationContext as BrainzoApplication }
    val coroutineScope = rememberCoroutineScope()

    val profileState by app.repository.playerProfile.collectAsState(initial = null)

    val currentCoins = profileState?.coins ?: 1450
    val equippedHatId = profileState?.equippedHatId ?: "item_crown"
    val unlockedItemIds = profileState?.unlockedItemIds ?: listOf("item_crown", "item_space")
    val isVipMember = profileState?.isVipMember ?: false

    var selectedTab by remember { mutableStateOf("all") }

    val wardrobeCatalog = remember {
        listOf(
            WardrobeItem("item_crown", "Golden Crown", "Rare Headwear", 0, "👑"),
            WardrobeItem("item_space", "Space Helmet", "Cosmic Gear", 0, "🪖"),
            WardrobeItem("item_propeller", "Propeller Cap", "Silly Fun", 300, "🧢"),
            WardrobeItem("item_shades", "Pixel Shades", "Meme Cool", 200, "🕶️"),
            WardrobeItem("item_cape", "Royal Cape", "Legendary Gear", 500, "🦸"),
            WardrobeItem("item_neon", "Galactic Neon", "VIP Mythic Tint", 0, "✨", isVipOnly = true)
        )
    }

    val filteredItems = remember(selectedTab, wardrobeCatalog) {
        when (selectedTab) {
            "hats" -> wardrobeCatalog.filter { it.type.contains("Headwear", ignoreCase = true) || it.type.contains("Gear", ignoreCase = true) || it.type.contains("Fun", ignoreCase = true) }
            "glasses" -> wardrobeCatalog.filter { it.type.contains("Cool", ignoreCase = true) }
            "capes" -> wardrobeCatalog.filter { it.type.contains("Legendary", ignoreCase = true) }
            else -> wardrobeCatalog
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Brainzo Dressing Room",
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
                        color = BrainzoGoldLight,
                        modifier = Modifier.padding(end = 12.dp)
                    ) {
                        Text(
                            "🪙 $currentCoins",
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                            fontWeight = FontWeight.Black,
                            fontSize = 13.sp,
                            color = BrainzoTextDark
                        )
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
            // Mascot 360 Pedestal Stage Card
            Surface(
                shape = RoundedCornerShape(28.dp),
                color = Color.White,
                shadowElevation = 2.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("360° STAGE", fontSize = 11.sp, fontWeight = FontWeight.Black, color = BrainzoPurple)
                    Spacer(Modifier.height(12.dp))
                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .clip(CircleShape)
                            .background(BrainzoSurfaceLow)
                            .border(3.dp, BrainzoPurpleLight, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        val equippedItem = wardrobeCatalog.firstOrNull { it.id == equippedHatId }
                        val itemEmoji = equippedItem?.iconEmoji ?: "👑"
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(itemEmoji, fontSize = 34.sp)
                            Text("🧠", fontSize = 38.sp)
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    val equippedTitle = wardrobeCatalog.firstOrNull { it.id == equippedHatId }?.name ?: "Golden Crown"
                    Text("Brainzo equipped with $equippedTitle", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = BrainzoTextDark)
                }
            }

            // Category Filter Tabs
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                val tabs = listOf(
                    Pair("all", "✨ All Items"),
                    Pair("hats", "🧢 Hats"),
                    Pair("glasses", "🕶️ Glasses"),
                    Pair("capes", "🦸 Capes")
                )
                tabs.forEach { (key, label) ->
                    val isSelected = selectedTab == key
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = if (isSelected) BrainzoPurple else Color.White,
                        border = BorderStroke(1.dp, if (isSelected) BrainzoPurple else BrainzoSurfaceDim),
                        modifier = Modifier.clickable { selectedTab = key }
                    ) {
                        Text(
                            text = label,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            color = if (isSelected) Color.White else BrainzoTextDark,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            // Wardrobe Catalog Grid
            filteredItems.chunked(2).forEach { row ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    row.forEach { item ->
                        val isEquipped = item.id == equippedHatId
                        val isUnlocked = item.id in unlockedItemIds || (item.isVipOnly && isVipMember)

                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = Color.White,
                            shadowElevation = 1.dp,
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(80.dp)
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(BrainzoSurfaceLow),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(item.iconEmoji, fontSize = 38.sp)
                                }
                                Spacer(Modifier.height(8.dp))
                                Text(item.name, fontWeight = FontWeight.Black, fontSize = 14.sp, color = BrainzoTextDark)
                                Text(item.type, fontSize = 11.sp, color = BrainzoTextMuted)
                                Spacer(Modifier.height(10.dp))

                                when {
                                    isEquipped -> {
                                        Button(
                                            onClick = { },
                                            enabled = false,
                                            modifier = Modifier.fillMaxWidth().height(38.dp),
                                            shape = RoundedCornerShape(12.dp),
                                            colors = ButtonDefaults.buttonColors(
                                                disabledContainerColor = BrainzoPurple,
                                                disabledContentColor = Color.White
                                            )
                                        ) {
                                            Text("EQUIPPED", fontSize = 11.sp, fontWeight = FontWeight.Black)
                                        }
                                    }
                                    isUnlocked -> {
                                        OutlinedButton(
                                            onClick = {
                                                coroutineScope.launch {
                                                    app.audioManager.playSound(SoundEffect.PUZZLE_SNAP)
                                                    app.repository.equipHat(item.id)
                                                }
                                            },
                                            modifier = Modifier.fillMaxWidth().height(38.dp),
                                            shape = RoundedCornerShape(12.dp)
                                        ) {
                                            Text("EQUIP", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = BrainzoPurple)
                                        }
                                    }
                                    item.isVipOnly -> {
                                        Button(
                                            onClick = onGoVip,
                                            modifier = Modifier.fillMaxWidth().height(38.dp),
                                            shape = RoundedCornerShape(12.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = BrainzoGold)
                                        ) {
                                            Text("VIP PASS", fontSize = 11.sp, fontWeight = FontWeight.Black, color = BrainzoTextDark)
                                        }
                                    }
                                    else -> {
                                        val canAfford = currentCoins >= item.costCoins
                                        Button(
                                            onClick = {
                                                if (canAfford) {
                                                    coroutineScope.launch {
                                                        val deducted = app.repository.deductCoins(item.costCoins)
                                                        if (deducted) {
                                                            app.audioManager.playSound(SoundEffect.REWARD_UNLOCK)
                                                            val updatedList = unlockedItemIds + item.id
                                                            val current = profileState
                                                            if (current != null) {
                                                                app.repository.savePlayer(
                                                                    current.copy(
                                                                        unlockedItemIds = updatedList,
                                                                        equippedHatId = item.id
                                                                    )
                                                                )
                                                            }
                                                        }
                                                    }
                                                }
                                            },
                                            enabled = canAfford,
                                            modifier = Modifier.fillMaxWidth().height(38.dp),
                                            shape = RoundedCornerShape(12.dp),
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = BrainzoGoldLight,
                                                contentColor = BrainzoTextDark
                                            )
                                        ) {
                                            Text("🪙 ${item.costCoins} GET", fontSize = 11.sp, fontWeight = FontWeight.Black)
                                        }
                                    }
                                }
                            }
                        }
                    }
                    if (row.size == 1) {
                        Spacer(Modifier.weight(1f))
                    }
                }
            }
        }
    }
}
