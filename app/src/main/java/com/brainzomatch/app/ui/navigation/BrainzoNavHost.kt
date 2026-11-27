package com.brainzomatch.app.ui.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.brainzomatch.app.BrainzoApplication
import com.brainzomatch.app.core.audio.SoundEffect
import com.brainzomatch.app.games.blitz.QuickBrainEngine
import com.brainzomatch.app.games.difference.FindDifferenceEngine
import com.brainzomatch.app.games.hidden.HiddenObjectsEngine
import com.brainzomatch.app.games.jigsaw.JigsawEngine
import com.brainzomatch.app.games.math.MathBlastEngine
import com.brainzomatch.app.games.memory.MemoryMatchEngine
import com.brainzomatch.app.games.shapes.ColorShapeEngine
import com.brainzomatch.app.games.word.WordWizardEngine
import com.brainzomatch.app.ui.screens.home.HomeScreen
import com.brainzomatch.app.ui.screens.map.LevelMapScreen
import com.brainzomatch.app.ui.screens.parents.ParentDashboardScreen
import com.brainzomatch.app.ui.screens.profile.ProfileAvatarScreen
import com.brainzomatch.app.ui.screens.rewards.RewardsWardrobeScreen
import com.brainzomatch.app.ui.screens.vip.VipScreen
import com.brainzomatch.app.ui.theme.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

/**
 * Brainzo Match - Core Navigation Graph & Screen Architecture
 * File Path: app/src/main/java/com/brainzomatch/app/ui/navigation/BrainzoNavHost.kt
 */

sealed class Screen(val route: String, val title: String, val icon: ImageVector? = null) {
    object Home : Screen("home", "Home", Icons.Rounded.Home)
    object Games : Screen("games", "Games", Icons.Rounded.SportsEsports)
    object LevelMap : Screen("map", "Quests", Icons.Rounded.EmojiEvents)
    object Rewards : Screen("rewards", "Gifts", Icons.Rounded.CardGiftcard)
    object Parents : Screen("parents", "Parents", Icons.Rounded.Shield)
    object Profile : Screen("profile", "Profile", Icons.Rounded.AccountCircle)
    object Vip : Screen("vip", "VIP Pass")
    
    // Unified Game Screen Route with Game ID parameter
    object GameHost : Screen("game_host/{gameId}", "Game") {
        fun createRoute(gameId: String) = "game_host/$gameId"
    }

    // Direct game routes for deep-linking
    object GameMemory : Screen("game/memory", "Memory Match")
    object GameMath : Screen("game/math", "Math Blast")
    object GameDifference : Screen("game/difference", "Find The Difference")
    object GameJigsaw : Screen("game/jigsaw", "Jigsaw Puzzle")
    object GameShapes : Screen("game/shapes", "Color & Shape")
    object GameWord : Screen("game/word", "Word Wizard")
    object GameHidden : Screen("game/hidden", "Hidden Objects")
    object GameBlitz : Screen("game/blitz", "Quick Brain Blitz")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BrainzoMainApp() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Tab routes showing persistent bottom navigation bar
    val bottomBarRoutes = listOf(
        Screen.Home.route,
        Screen.Games.route,
        Screen.LevelMap.route,
        Screen.Rewards.route,
        Screen.Parents.route
    )
    val showBottomBar = currentRoute in bottomBarRoutes

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 8.dp
                ) {
                    val navItems = listOf(
                        Screen.Home,
                        Screen.Games,
                        Screen.LevelMap,
                        Screen.Rewards,
                        Screen.Parents
                    )
                    navItems.forEach { screen ->
                        val selected = currentRoute == screen.route
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { 
                                screen.icon?.let { 
                                    Icon(it, contentDescription = screen.title) 
                                } 
                            },
                            label = { 
                                Text(
                                    text = screen.title, 
                                    fontWeight = if (selected) FontWeight.Black else FontWeight.Bold,
                                    fontSize = 12.sp
                                ) 
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = BrainzoPurple,
                                selectedTextColor = BrainzoPurple,
                                indicatorColor = BrainzoSurfaceLow,
                                unselectedIconColor = BrainzoTextMuted,
                                unselectedTextColor = BrainzoTextMuted
                            )
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding),
            enterTransition = { fadeIn(animationSpec = tween(220)) },
            exitTransition = { fadeOut(animationSpec = tween(220)) }
        ) {
            // 1. HOME SCREEN
            composable(Screen.Home.route) {
                HomeScreen(
                    onNavigateToGame = { gameId ->
                        navController.navigate(Screen.GameHost.createRoute(gameId))
                    },
                    onNavigateToProfile = { navController.navigate(Screen.Profile.route) },
                    onNavigateToVip = { navController.navigate(Screen.Vip.route) },
                    onNavigateToMap = { navController.navigate(Screen.LevelMap.route) }
                )
            }

            // 2. GAMES SCREEN (All 8 Games Hub)
            composable(Screen.Games.route) {
                HomeScreen(
                    onNavigateToGame = { gameId ->
                        navController.navigate(Screen.GameHost.createRoute(gameId))
                    },
                    onNavigateToProfile = { navController.navigate(Screen.Profile.route) },
                    onNavigateToVip = { navController.navigate(Screen.Vip.route) },
                    onNavigateToMap = { navController.navigate(Screen.LevelMap.route) }
                )
            }

            // 3. LEVEL MAP & QUESTS SCREEN
            composable(Screen.LevelMap.route) {
                LevelMapScreen(
                    onLevelSelected = { lvl ->
                        // Maps level number to appropriate game
                        val targetGame = when (lvl % 8) {
                            1 -> "game_memory"
                            2 -> "game_math"
                            3 -> "game_difference"
                            4 -> "game_jigsaw"
                            5 -> "game_shapes"
                            6 -> "game_word"
                            7 -> "game_hidden"
                            else -> "game_blitz"
                        }
                        navController.navigate(Screen.GameHost.createRoute(targetGame))
                    },
                    onBack = { navController.popBackStack() }
                )
            }

            // 4. REWARDS & WARDROBE SCREEN
            composable(Screen.Rewards.route) {
                RewardsWardrobeScreen(
                    onBack = { navController.popBackStack() },
                    onGoVip = { navController.navigate(Screen.Vip.route) }
                )
            }

            // 5. PARENTS & CONTROLS DASHBOARD
            composable(Screen.Parents.route) {
                ParentDashboardScreen(
                    onBack = { navController.popBackStack() }
                )
            }

            // 6. PLAYER AVATARS & PROFILE SCREEN
            composable(Screen.Profile.route) {
                ProfileAvatarScreen(
                    onBack = { navController.popBackStack() },
                    onSaved = { navController.popBackStack() }
                )
            }

            // 7. VIP MEMBERSHIP SCREEN
            composable(Screen.Vip.route) {
                VipScreen(
                    onClose = { navController.popBackStack() }
                )
            }

            // 8. UNIFIED GAME HOST (DISPATCHES TO ALL 8 GAME ENGINES)
            composable(
                route = Screen.GameHost.route,
                arguments = listOf(navArgument("gameId") { type = NavType.StringType })
            ) { backStackEntry ->
                val gameId = backStackEntry.arguments?.getString("gameId") ?: "game_memory"
                val app = LocalContext.current.applicationContext as BrainzoApplication
                
                GameHostContainer(
                    gameId = gameId,
                    app = app,
                    onBack = { navController.popBackStack() },
                    onGoHome = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Home.route) { inclusive = false }
                            launchSingleTop = true
                        }
                    },
                    onOpenRewards = {
                        navController.navigate(Screen.Rewards.route)
                    }
                )
            }
        }
    }
}

/**
 * Dispatcher container mapping game IDs directly to actual stateful engines and UI components.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameHostContainer(
    gameId: String,
    app: BrainzoApplication,
    onBack: () -> Unit,
    onGoHome: () -> Unit,
    onOpenRewards: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val gameTitle = remember(gameId) {
        when (gameId) {
            "game_memory" -> "Game 1: Memory Match"
            "game_math" -> "Game 2: Math Blast"
            "game_difference" -> "Game 3: Find The Difference"
            "game_jigsaw" -> "Game 4: Jigsaw Quest"
            "game_shapes" -> "Game 5: Color & Shape Match"
            "game_word" -> "Game 6: Word Wizard"
            "game_hidden" -> "Game 7: Hidden Objects"
            "game_blitz" -> "Game 8: Quick Brain Blitz"
            else -> "Brainzo Game"
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = gameTitle,
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
                    IconButton(onClick = onGoHome) {
                        Icon(Icons.Rounded.Home, contentDescription = "Home", tint = BrainzoPurple)
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
            when (gameId) {
                // GAME 1: MEMORY MATCH
                "game_memory" -> {
                    val safariAnimals = remember {
                        listOf("lion", "elephant", "zebra", "giraffe", "monkey", "hippo", "panda", "tiger")
                    }
                    val engine = remember { MemoryMatchEngine(safariAnimals) }
                    LaunchedEffect(Unit) { engine.startNewGame() }
                    val state by engine.state.collectAsState()

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        GameHud(
                            score = state.score,
                            streak = state.streak,
                            stars = state.starsEarned,
                            indicator = "Pairs: ${state.pairsFound}/${state.totalPairs}"
                        )
                        Spacer(Modifier.height(16.dp))

                        // 4x3 Card Grid
                        Column(
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            val rows = state.cards.chunked(3)
                            rows.forEachIndexed { rowIdx, rowItems ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    rowItems.forEachIndexed { colIdx, card ->
                                        val cardIndex = rowIdx * 3 + colIdx
                                        Card(
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(90.dp),
                                            shape = RoundedCornerShape(16.dp),
                                            colors = CardDefaults.cardColors(
                                                containerColor = if (card.isFaceUp || card.isMatched) BrainzoSurfaceLow else BrainzoPurple
                                            ),
                                            onClick = {
                                                coroutineScope.launch {
                                                    engine.onCardSelected(
                                                        index = cardIndex,
                                                        onMatch = {
                                                            app.audioManager.playSound(SoundEffect.MATCH_CORRECT)
                                                        },
                                                        onFail = {
                                                            app.audioManager.playSound(SoundEffect.WRONG_TRY_AGAIN)
                                                        }
                                                    )
                                                }
                                            }
                                        ) {
                                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                                if (card.isFaceUp || card.isMatched) {
                                                    val emoji = when (card.identifier) {
                                                        "lion" -> "🦁"
                                                        "elephant" -> "🐘"
                                                        "zebra" -> "🦓"
                                                        "giraffe" -> "🦒"
                                                        "monkey" -> "🐒"
                                                        "hippo" -> "🦛"
                                                        "panda" -> "🐼"
                                                        else -> "🐯"
                                                    }
                                                    Text(emoji, fontSize = 34.sp)
                                                } else {
                                                    Text("🧠", fontSize = 28.sp)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        if (state.isGameOver) {
                            Spacer(Modifier.height(24.dp))
                            VictoryCard(
                                score = state.score,
                                stars = state.starsEarned,
                                onReplay = { engine.startNewGame() },
                                onRewards = onOpenRewards
                            )
                        }
                    }
                }

                // GAME 2: MATH BLAST
                "game_math" -> {
                    val engine = remember { MathBlastEngine() }
                    val state by engine.state.collectAsState()

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        GameHud(score = state.score, streak = state.comboStreak, stars = 3, indicator = "Speed Rocket")
                        Spacer(Modifier.height(16.dp))

                        // Rocket race progress bar
                        Text("🚀 Player Speed: ${state.playerProgressPercent}%", fontWeight = FontWeight.Bold, color = BrainzoSky)
                        LinearProgressIndicator(
                            progress = state.playerProgressPercent / 100f,
                            modifier = Modifier.fillMaxWidth().height(12.dp),
                            color = BrainzoSky
                        )
                        Spacer(Modifier.height(24.dp))

                        // Math Question Card
                        Surface(
                            shape = RoundedCornerShape(24.dp),
                            color = MaterialTheme.colorScheme.surface,
                            shadowElevation = 2.dp,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "${state.currentQuestion.factorA} ${state.currentQuestion.operation} ${state.currentQuestion.factorB} = ?",
                                    fontSize = 36.sp,
                                    fontWeight = FontWeight.Black,
                                    color = BrainzoTextDark
                                )
                            }
                        }
                        Spacer(Modifier.height(20.dp))

                        // 4 Math Options
                        state.currentQuestion.options.chunked(2).forEach { rowOptions ->
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                rowOptions.forEach { opt ->
                                    Button(
                                        onClick = {
                                            val correct = engine.submitAnswer(opt)
                                            if (correct) {
                                                app.audioManager.playSound(SoundEffect.MATCH_CORRECT)
                                                coroutineScope.launch {
                                                    app.repository.addReward(50, 1)
                                                }
                                            } else {
                                                app.audioManager.playSound(SoundEffect.WRONG_TRY_AGAIN)
                                            }
                                        },
                                        modifier = Modifier.weight(1f).height(64.dp),
                                        shape = RoundedCornerShape(16.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = BrainzoPurple)
                                    ) {
                                        Text(opt.toString(), fontSize = 24.sp, fontWeight = FontWeight.Black)
                                    }
                                }
                            }
                            Spacer(Modifier.height(12.dp))
                        }
                    }
                }

                // GAME 3: FIND THE DIFFERENCE
                "game_difference" -> {
                    val engine = remember { FindDifferenceEngine(app.repository, app.audioManager, coroutineScope) }
                    LaunchedEffect(Unit) { engine.startLevel(1) }
                    val state by engine.state.collectAsState()

                    Column(
                        modifier = Modifier.fillMaxSize().padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        GameHud(score = state.score, streak = state.streak, stars = state.starsEarned, indicator = "Found: ${state.foundDifferences.size}/${state.totalDifferences}")
                        Spacer(Modifier.height(16.dp))

                        // Dual visual panels
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Surface(
                                shape = RoundedCornerShape(18.dp),
                                color = MaterialTheme.colorScheme.surface,
                                modifier = Modifier.weight(1f).height(180.dp),
                                shadowElevation = 2.dp
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text("🌲🦊🍄\nOriginal", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                            Surface(
                                shape = RoundedCornerShape(18.dp),
                                color = MaterialTheme.colorScheme.surface,
                                modifier = Modifier.weight(1f).height(180.dp),
                                shadowElevation = 2.dp
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text("🌲🦊🍄❓\nSpot Deltas", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                        Spacer(Modifier.height(16.dp))
                        Button(
                            onClick = { engine.useSherlockHint() },
                            colors = ButtonDefaults.buttonColors(containerColor = BrainzoGold),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Text("🔍 Use Sherlock Hint (${state.hintsRemaining} Left)", color = BrainzoTextDark, fontWeight = FontWeight.Black)
                        }
                    }
                }

                // GAME 4: JIGSAW PUZZLE
                "game_jigsaw" -> {
                    val engine = remember { JigsawEngine(app.repository, app.audioManager, coroutineScope) }
                    LaunchedEffect(Unit) { engine.startLevel(1) }
                    val state by engine.state.collectAsState()

                    Column(
                        modifier = Modifier.fillMaxSize().padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        GameHud(score = state.score, streak = 1, stars = 3, indicator = "Pieces: ${state.placedCount}/${state.totalPieces}")
                        Spacer(Modifier.height(16.dp))

                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = MaterialTheme.colorScheme.surface,
                            modifier = Modifier.fillMaxWidth().height(220.dp),
                            shadowElevation = 2.dp
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text("🧩 Jigsaw Canvas 3×3 Grid\nDrag & Snap Pieces", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        Spacer(Modifier.height(16.dp))
                        Button(
                            onClick = { engine.autoSnapOnePiece() },
                            colors = ButtonDefaults.buttonColors(containerColor = BrainzoMint),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Text("Auto-Snap Next Piece", fontWeight = FontWeight.Black)
                        }
                    }
                }

                // GAME 5: COLOR & SHAPE MATCH
                "game_shapes" -> {
                    val engine = remember { ColorShapeEngine(app.repository, app.audioManager, coroutineScope) }
                    LaunchedEffect(Unit) { engine.startLevel(5) }
                    val state by engine.state.collectAsState()

                    Column(
                        modifier = Modifier.fillMaxSize().padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        GameHud(score = state.score, streak = state.streak, stars = state.starsEarned, indicator = "Portals: ${state.portals.count { it.isCompleted }}/${state.portals.size}")
                        Spacer(Modifier.height(16.dp))

                        // Target Portals
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            state.portals.forEach { portal ->
                                Surface(
                                    shape = RoundedCornerShape(16.dp),
                                    color = if (portal.isCompleted) BrainzoMint.copy(alpha = 0.2f) else BrainzoSurfaceLow,
                                    modifier = Modifier.weight(1f).height(90.dp)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(6.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Text(portal.targetShape.iconEmoji, fontSize = 28.sp)
                                        Text(
                                            if (portal.isCompleted) "MATCHED" else portal.targetColor.colorName,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                        Spacer(Modifier.height(20.dp))

                        // Available Pieces
                        Text("Available Pieces to Drop:", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Spacer(Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            state.availablePieces.filter { !it.isMatched }.take(4).forEach { piece ->
                                Button(
                                    onClick = {
                                        val firstIncomplete = state.portals.firstOrNull { !it.isCompleted }
                                        if (firstIncomplete != null) {
                                            engine.onDropPieceIntoPortal(piece.id, firstIncomplete.id)
                                        }
                                    },
                                    shape = RoundedCornerShape(14.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = BrainzoPurple)
                                ) {
                                    Text("${piece.shape.iconEmoji} ${piece.color.colorName}", fontSize = 12.sp)
                                }
                            }
                        }
                        Spacer(Modifier.height(16.dp))
                        Button(
                            onClick = { engine.useHint() },
                            colors = ButtonDefaults.buttonColors(containerColor = BrainzoGold),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Text("💡 Use Hint (${state.hintsRemaining} Left)", color = BrainzoTextDark, fontWeight = FontWeight.Black)
                        }
                    }
                }

                // GAME 6: WORD WIZARD
                "game_word" -> {
                    val engine = remember { WordWizardEngine(app.repository, app.audioManager, coroutineScope) }
                    LaunchedEffect(Unit) { engine.startLevel(11) }
                    val state by engine.state.collectAsState()

                    Column(
                        modifier = Modifier.fillMaxSize().padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        GameHud(score = state.score, streak = 1, stars = state.starsEarned, indicator = state.category)
                        Spacer(Modifier.height(16.dp))

                        Text(state.clueImageEmoji, fontSize = 64.sp)
                        Spacer(Modifier.height(12.dp))

                        // Word Slots
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            state.wordSlots.forEach { ch ->
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (ch != null) BrainzoSurfaceLow else MaterialTheme.colorScheme.surface,
                                    shadowElevation = 2.dp,
                                    modifier = Modifier.size(48.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = ch?.toString() ?: "_",
                                            fontSize = 24.sp,
                                            fontWeight = FontWeight.Black,
                                            color = BrainzoPurple
                                        )
                                    }
                                }
                            }
                        }
                        Spacer(Modifier.height(24.dp))

                        // Letter Rack
                        Text("Pick a Letter:", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Spacer(Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            state.rackLetters.forEach { letter ->
                                Button(
                                    onClick = { engine.onLetterBlockTapped(letter) },
                                    shape = RoundedCornerShape(14.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = BrainzoSky)
                                ) {
                                    Text(letter.toString(), fontSize = 20.sp, fontWeight = FontWeight.Black)
                                }
                            }
                        }
                        Spacer(Modifier.height(16.dp))
                        Button(
                            onClick = { engine.useHint() },
                            colors = ButtonDefaults.buttonColors(containerColor = BrainzoGold),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Text("🪄 Wizard Hint (${state.hintsLeft} Left)", color = BrainzoTextDark, fontWeight = FontWeight.Black)
                        }
                    }
                }

                // GAME 7: HIDDEN OBJECTS
                "game_hidden" -> {
                    val engine = remember { HiddenObjectsEngine(app.repository, app.audioManager, coroutineScope) }
                    LaunchedEffect(Unit) { engine.startLevel(7) }
                    val state by engine.state.collectAsState()

                    Column(
                        modifier = Modifier.fillMaxSize().padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        GameHud(score = state.score, streak = 1, stars = state.starsEarned, indicator = "Found: ${state.foundCount}/${state.totalCount}")
                        Spacer(Modifier.height(16.dp))

                        Surface(
                            shape = RoundedCornerShape(24.dp),
                            color = MaterialTheme.colorScheme.surface,
                            shadowElevation = 2.dp,
                            modifier = Modifier.fillMaxWidth().height(220.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text("🌳 Treehouse Library\nTap to discover hidden objects", fontWeight = FontWeight.Bold)
                            }
                        }
                        Spacer(Modifier.height(16.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            state.itemsToFind.forEach { item ->
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (item.isFound) BrainzoMint.copy(alpha = 0.2f) else BrainzoSurfaceLow,
                                    modifier = Modifier.padding(4.dp)
                                ) {
                                    Text(
                                        "${item.iconEmoji} ${item.name}",
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                        Spacer(Modifier.height(16.dp))
                        Button(
                            onClick = { engine.triggerRadarHint() },
                            colors = ButtonDefaults.buttonColors(containerColor = BrainzoPurple),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Text("📡 Radar Scan Hint (${state.hintsRemaining} Left)", fontWeight = FontWeight.Black)
                        }
                    }
                }

                // GAME 8: QUICK BRAIN BLITZ
                "game_blitz" -> {
                    val engine = remember { QuickBrainEngine(app.repository, app.audioManager, coroutineScope) }
                    LaunchedEffect(Unit) { engine.startRound() }
                    val state by engine.state.collectAsState()

                    Column(
                        modifier = Modifier.fillMaxSize().padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        GameHud(score = state.score, streak = state.streak, stars = 3, indicator = "Round ${state.roundNumber}/${state.totalRounds}")
                        Spacer(Modifier.height(16.dp))

                        Text("⏱️ ${state.secondsLeft}s", fontSize = 34.sp, fontWeight = FontWeight.Black, color = BrainzoOrange)
                        Spacer(Modifier.height(8.dp))
                        Text(state.currentQuestion.questionPrompt, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Spacer(Modifier.height(16.dp))

                        // Matrix 3x3
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            state.currentQuestion.matrixGrid.chunked(3).forEach { row ->
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    row.forEach { symbol ->
                                        Surface(
                                            shape = RoundedCornerShape(12.dp),
                                            color = MaterialTheme.colorScheme.surface,
                                            modifier = Modifier.size(54.dp),
                                            shadowElevation = 1.dp
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Text(symbol, fontSize = 24.sp)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        Spacer(Modifier.height(20.dp))

                        // Options
                        state.currentQuestion.options.forEach { opt ->
                            Button(
                                onClick = { engine.submitAnswer(opt.id) },
                                modifier = Modifier.fillMaxWidth().height(48.dp).padding(vertical = 2.dp),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = BrainzoPurple)
                            ) {
                                Text("${opt.id}: ${opt.label} ${opt.symbol}", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun GameHud(score: Int, streak: Int, stars: Int, indicator: String) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 2.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("⭐ Score: $score", fontWeight = FontWeight.Black, fontSize = 14.sp)
            Text("🔥 ${streak}x Combo", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = BrainzoOrange)
            Text(indicator, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = BrainzoPurple)
        }
    }
}

@Composable
fun VictoryCard(score: Int, stars: Int, onReplay: () -> Unit, onRewards: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 4.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("🎉 LEVEL COMPLETE!", fontWeight = FontWeight.Black, fontSize = 20.sp, color = BrainzoPurple)
            Spacer(Modifier.height(8.dp))
            Text("⭐⭐⭐ ($stars / 3 Stars Earned)", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Text("Final Score: $score", fontSize = 14.sp)
            Spacer(Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(
                    onClick = onReplay,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BrainzoPurple)
                ) {
                    Text("PLAY AGAIN", fontWeight = FontWeight.Black)
                }
                Button(
                    onClick = onRewards,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BrainzoMint)
                ) {
                    Text("GET REWARDS", fontWeight = FontWeight.Black)
                }
            }
        }
    }
}
