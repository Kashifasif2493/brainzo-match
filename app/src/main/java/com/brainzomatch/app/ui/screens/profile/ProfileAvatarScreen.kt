package com.brainzomatch.app.ui.screens.profile

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Lock
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
import com.brainzomatch.app.core.data.local.entities.PlayerProfileEntity
import com.brainzomatch.app.ui.theme.*
import kotlinx.coroutines.launch

/**
 * Avatar option representation.
 */
data class AvatarOption(
    val id: String,
    val name: String,
    val emoji: String,
    val isLocked: Boolean = false,
    val lockRequirement: String = ""
)

/**
 * ProfileAvatarScreen — Production Jetpack Compose Profile Screen.
 * File Path: app/src/main/java/com/brainzomatch/app/ui/screens/profile/ProfileAvatarScreen.kt
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileAvatarScreen(
    onBack: () -> Unit,
    onSaved: () -> Unit
) {
    val context = LocalContext.current
    val app = remember(context) { context.applicationContext as BrainzoApplication }
    val coroutineScope = rememberCoroutineScope()

    val profileState by app.repository.playerProfile.collectAsState(initial = null)

    var gamerName by remember { mutableStateOf("Leo the Thinker") }
    var selectedAvatarId by remember { mutableStateOf("avatar_gamer_boy") }
    var selectedAuraHex by remember { mutableStateOf("#7C3AED") }

    LaunchedEffect(profileState) {
        profileState?.let { profile ->
            gamerName = profile.displayName
            selectedAvatarId = profile.avatarId
            selectedAuraHex = profile.auraColorHex
        }
    }

    val avatars = remember {
        listOf(
            AvatarOption("avatar_gamer_boy", "Gamer Boy", "🎧"),
            AvatarOption("avatar_cyber_girl", "Cyber Girl", "🥽"),
            AvatarOption("avatar_astronaut", "Astronaut", "👨‍🚀"),
            AvatarOption("avatar_dino_kid", "Dino Kid", "🦖"),
            AvatarOption("avatar_wizard_kid", "Wizard Kid", "🧙‍♂️"),
            AvatarOption("avatar_superhero", "Superhero", "🦸‍♀️"),
            AvatarOption("avatar_scientist", "Scientist", "🔬"),
            AvatarOption("avatar_robot", "Robot Buddy", "🤖"),
            AvatarOption("avatar_panda", "Panda Kid", "🐼"),
            AvatarOption("avatar_diver", "Ocean Diver", "🤿"),
            AvatarOption("avatar_ninja", "Ninja Kid", "🥷", isLocked = true, lockRequirement = "LVL 10"),
            AvatarOption("avatar_alien", "Alien Pal", "👾", isLocked = true, lockRequirement = "500 ⭐")
        )
    }

    val auras = remember {
        listOf(
            Pair("Lilac", Color(0xFF7C3AED)),
            Pair("Sky", Color(0xFF0EA5E9)),
            Pair("Solar", Color(0xFFF59E0B)),
            Pair("Mint", Color(0xFF10B981)),
            Pair("Coral", Color(0xFFF43F5E))
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Choose Your Brain Champion!",
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
            // Selected Champion Card
            Surface(
                shape = RoundedCornerShape(26.dp),
                color = Color.White,
                shadowElevation = 3.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    val auraParsedColor = try {
                        Color(android.graphics.Color.parseColor(selectedAuraHex))
                    } catch (e: Exception) {
                        BrainzoPurple
                    }

                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(auraParsedColor.copy(alpha = 0.2f))
                            .border(3.dp, auraParsedColor, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        val activeEmoji = avatars.firstOrNull { it.id == selectedAvatarId }?.emoji ?: "🎧"
                        Text(activeEmoji, fontSize = 36.sp)
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        OutlinedTextField(
                            value = gamerName,
                            onValueChange = { gamerName = it },
                            label = { Text("Gamer Tag") },
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(Modifier.height(4.dp))
                        val coins = profileState?.coins ?: 1450
                        val stars = profileState?.stars ?: 86
                        Text(
                            "🪙 $coins Coins  •  ⭐ $stars Stars  •  Age 8",
                            fontSize = 12.sp,
                            color = BrainzoTextMuted
                        )
                    }
                }
            }

            // Aura Color Picker
            Text("Aura Mood Color", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = BrainzoTextDark)
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                auras.forEach { (name, color) ->
                    val colorHex = String.format("#%06X", (0xFFFFFF and color.value.toInt()))
                    val isSelected = selectedAuraHex.equals(colorHex, ignoreCase = true)
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = if (isSelected) color else color.copy(alpha = 0.15f),
                        modifier = Modifier.clickable { selectedAuraHex = colorHex }
                    ) {
                        Text(
                            text = name,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            color = if (isSelected) Color.White else BrainzoTextDark,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            // 12 Avatar Grid
            Text("Avatars Gallery (10/12 Unlocked)", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = BrainzoTextDark)
            avatars.chunked(3).forEach { row ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    row.forEach { avatar ->
                        val isSelected = avatar.id == selectedAvatarId
                        Surface(
                            shape = RoundedCornerShape(18.dp),
                            color = Color.White,
                            border = BorderStroke(
                                2.dp,
                                if (isSelected) BrainzoPurple else Color.Transparent
                            ),
                            shadowElevation = 1.dp,
                            modifier = Modifier
                                .weight(1f)
                                .clickable(enabled = !avatar.isLocked) {
                                    selectedAvatarId = avatar.id
                                }
                        ) {
                            Column(
                                modifier = Modifier.padding(10.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(CircleShape)
                                        .background(if (avatar.isLocked) Color(0xFFF3F4F6) else BrainzoSurfaceLow),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (avatar.isLocked) {
                                        Icon(Icons.Rounded.Lock, contentDescription = "Locked", tint = BrainzoTextMuted)
                                    } else {
                                        Text(avatar.emoji, fontSize = 24.sp)
                                    }
                                }
                                Spacer(Modifier.height(6.dp))
                                Text(
                                    avatar.name,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    color = BrainzoTextDark
                                )
                                Text(
                                    if (avatar.isLocked) avatar.lockRequirement else if (isSelected) "EQUIPPED" else "Unlocked",
                                    fontSize = 10.sp,
                                    color = if (isSelected) BrainzoPurple else BrainzoTextMuted,
                                    fontWeight = if (isSelected) FontWeight.Black else FontWeight.Normal
                                )
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
            Button(
                onClick = {
                    coroutineScope.launch {
                        val current = profileState ?: PlayerProfileEntity()
                        app.repository.savePlayer(
                            current.copy(
                                displayName = gamerName,
                                avatarId = selectedAvatarId,
                                auraColorHex = selectedAuraHex
                            )
                        )
                        onSaved()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(containerColor = BrainzoMint)
            ) {
                Icon(Icons.Rounded.Check, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("SAVE & CONTINUE PLAYING", fontWeight = FontWeight.Black, fontSize = 16.sp)
            }
        }
    }
}
