package com.brainzomatch.app.ui.screens.vip

import android.app.Activity
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.brainzomatch.app.BrainzoApplication
import com.brainzomatch.app.core.audio.SoundEffect
import com.brainzomatch.app.core.parentgate.ParentalMathChallenge
import com.brainzomatch.app.core.parentgate.ParentalVerification
import com.brainzomatch.app.ui.theme.*
import kotlinx.coroutines.launch

/**
 * VipScreen — Brainzo VIP Pass subscription screen with Google Play Billing v6,
 * COPPA adult parent verification gate prior to initiating purchases, and realistic purchase states.
 * Target Path: app/src/main/java/com/brainzomatch/app/ui/screens/vip/VipScreen.kt
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VipScreen(
    onClose: () -> Unit
) {
    val context = LocalContext.current
    val app = remember(context) { context.applicationContext as BrainzoApplication }
    val coroutineScope = rememberCoroutineScope()
    val audioManager = app.audioManager
    val billingManager = app.billingManager

    val profileState by app.repository.playerProfile.collectAsState(initial = null)
    val isAlreadyVip = profileState?.isVipMember ?: false

    // State for adult verification gate dialog prior to initiating billing
    var showParentGateDialog by remember { mutableStateOf(false) }
    var currentChallenge by remember { mutableStateOf(ParentalVerification.generateChallenge()) }
    var answerInput by remember { mutableStateOf("") }
    var gateError by remember { mutableStateOf(false) }
    var pendingBillingAction by remember { mutableStateOf<(() -> Unit)?>(null) }

    // Billing runtime status message (informative, never claiming fake success)
    var billingStatusMessage by remember { mutableStateOf<String?>(null) }
    var isBillingLoading by remember { mutableStateOf(false) }

    val vipBenefits = remember {
        listOf(
            Triple("👑", "Unlimited Golden Wardrobe", "Exclusive mythic suits, crowns, capes, and cosmic neon glows."),
            Triple("🚀", "All 8 Games Unlocked Instantly", "Instant unlimited access to all levels without cooldowns."),
            Triple("🚫", "100% Ad-Free Learning", "Guaranteed uninterrupted child experience with zero promotional ads."),
            Triple("⭐", "2x Daily Star Multiplier", "Double star rewards for every brain puzzle completed."),
            Triple("👨‍👩‍👧", "Detailed Parent Progress Analytics", "In-depth speed, cognitive accuracy, and developmental metrics.")
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Brainzo VIP Pass",
                        fontWeight = FontWeight.Black,
                        fontSize = 18.sp,
                        color = BrainzoTextDark
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onClose) {
                        Icon(Icons.Rounded.Close, contentDescription = "Close", tint = BrainzoPurple)
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            // Adults verify before restoring subscriptions
                            pendingBillingAction = {
                                isBillingLoading = true
                                billingStatusMessage = "Checking Google Play subscription history..."
                                coroutineScope.launch {
                                    billingManager.queryPurchases()
                                    isBillingLoading = false
                                    billingStatusMessage = "Subscription restore check complete."
                                }
                            }
                            currentChallenge = ParentalVerification.generateChallenge()
                            answerInput = ""
                            gateError = false
                            showParentGateDialog = true
                        }
                    ) {
                        Text("Restore", fontWeight = FontWeight.Bold, color = BrainzoPurple, fontSize = 13.sp)
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
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Gold Gradient VIP Hero Header
            Surface(
                shape = RoundedCornerShape(26.dp),
                color = BrainzoGold,
                shadowElevation = 3.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.25f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("👑", fontSize = 38.sp)
                    }
                    Spacer(Modifier.height(10.dp))
                    Text(
                        "UNLOCK BRAINZO VIP",
                        fontWeight = FontWeight.Black,
                        fontSize = 20.sp,
                        color = BrainzoTextDark
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "The Ultimate Kid's Brain Training & Puzzle Experience",
                        fontSize = 12.sp,
                        color = BrainzoTextDark.copy(alpha = 0.85f),
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(12.dp))

                    if (isAlreadyVip) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = BrainzoMint
                        ) {
                            Text(
                                "ACTIVE SUBSCRIBER",
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                color = Color.White,
                                fontWeight = FontWeight.Black,
                                fontSize = 11.sp
                            )
                        }
                    } else {
                        Text(
                            "$4.99 / Month • Cancel Anytime",
                            fontWeight = FontWeight.Black,
                            fontSize = 14.sp,
                            color = BrainzoPurpleDark
                        )
                    }
                }
            }

            // Benefits Checklist
            Surface(
                shape = RoundedCornerShape(22.dp),
                color = Color.White,
                shadowElevation = 2.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        "All Premium Benefits Included:",
                        fontWeight = FontWeight.Black,
                        fontSize = 15.sp,
                        color = BrainzoTextDark
                    )

                    vipBenefits.forEach { (emoji, title, desc) ->
                        Row(
                            verticalAlignment = Alignment.Top,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(BrainzoSurfaceLow),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(emoji, fontSize = 18.sp)
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(title, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = BrainzoTextDark)
                                Text(desc, fontSize = 11.sp, color = BrainzoTextMuted, lineHeight = 15.sp)
                            }
                        }
                    }
                }
            }

            // Billing Status / Feedback Banner
            if (billingStatusMessage != null) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = BrainzoSurfaceLow,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = billingStatusMessage.orEmpty(),
                        modifier = Modifier.padding(12.dp),
                        fontSize = 12.sp,
                        color = BrainzoTextDark,
                        textAlign = TextAlign.Center
                    )
                }
            }

            // Subscribe Button
            Button(
                onClick = {
                    if (isAlreadyVip) {
                        billingStatusMessage = "You are already a Brainzo VIP subscriber!"
                        return@Button
                    }
                    // Adult Verification required prior to initiating purchase
                    pendingBillingAction = {
                        val activity = context as? Activity
                        if (activity != null) {
                            isBillingLoading = true
                            billingStatusMessage = "Connecting to Google Play Billing..."
                            billingManager.launchSubscriptionPurchase(activity, com.brainzomatch.app.core.billing.BillingManager.PRODUCT_VIP_MONTHLY)
                            isBillingLoading = false
                        } else {
                            billingStatusMessage = "Unable to launch Google Play on non-activity context."
                        }
                    }
                    currentChallenge = ParentalVerification.generateChallenge()
                    answerInput = ""
                    gateError = false
                    showParentGateDialog = true
                },
                enabled = !isBillingLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isAlreadyVip) BrainzoMint else BrainzoPurple,
                    contentColor = Color.White
                )
            ) {
                if (isBillingLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("Connecting...", fontWeight = FontWeight.Black, fontSize = 15.sp)
                } else if (isAlreadyVip) {
                    Icon(Icons.Rounded.Check, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("VIP MEMBERSHIP ACTIVE", fontWeight = FontWeight.Black, fontSize = 15.sp)
                } else {
                    Icon(Icons.Rounded.Lock, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("SUBSCRIBE NOW • $4.99/MO", fontWeight = FontWeight.Black, fontSize = 15.sp)
                }
            }

            Text(
                "Subscription auto-renews monthly unless canceled in Google Play Store subscriptions at least 24 hours before the end of the current period. Family Sharing supported.",
                fontSize = 10.sp,
                color = BrainzoTextMuted,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 8.dp)
            )

            Spacer(Modifier.height(16.dp))
        }
    }

    // ========================================================
    // PARENT GATE VERIFICATION DIALOG (COPPA Mandatory)
    // ========================================================
    if (showParentGateDialog) {
        AlertDialog(
            onDismissRequest = { showParentGateDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.Shield, contentDescription = null, tint = BrainzoPurple)
                    Spacer(Modifier.width(8.dp))
                    Text("Parents Only", fontWeight = FontWeight.Black, fontSize = 18.sp)
                }
            },
            text = {
                Column {
                    Text(
                        "Please solve this math equation to authorize billing actions:",
                        fontSize = 13.sp,
                        color = BrainzoTextMuted
                    )
                    Spacer(Modifier.height(12.dp))
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = BrainzoSurfaceLow,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier.padding(12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                currentChallenge.questionText,
                                fontWeight = FontWeight.Black,
                                fontSize = 20.sp,
                                color = BrainzoPurple
                            )
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(
                        value = answerInput,
                        onValueChange = {
                            answerInput = it
                            gateError = false
                        },
                        label = { Text("Answer") },
                        singleLine = true,
                        isError = gateError,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (gateError) {
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "Incorrect. Please try again.",
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val numeric = answerInput.trim().toIntOrNull()
                        if (numeric != null && ParentalVerification.verifyAnswer(numeric, currentChallenge)) {
                            audioManager.playSound(SoundEffect.STAR_BURST)
                            showParentGateDialog = false
                            pendingBillingAction?.invoke()
                            pendingBillingAction = null
                        } else {
                            audioManager.playSound(SoundEffect.WRONG_TRY_AGAIN)
                            gateError = true
                            currentChallenge = ParentalVerification.generateChallenge()
                            answerInput = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BrainzoPurple),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("VERIFY & CONTINUE", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            },
            dismissButton = {
                TextButton(onClick = { showParentGateDialog = false }) {
                    Text("Cancel", color = BrainzoTextMuted, fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}
