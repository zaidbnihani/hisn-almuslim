package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.foundation.border
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.List
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.addPathNodes
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.data.AthkarData
import com.example.ui.MainViewModel
import com.example.ui.screens.*
import com.example.ui.theme.AthkarTheme
import com.example.ui.theme.CardSurface
import com.example.ui.theme.DarkCanvas
import com.example.ui.theme.GoldBright
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

val ShieldFilledVector: ImageVector = ImageVector.Builder(
    name = "ShieldFilled",
    defaultWidth = 24.dp,
    defaultHeight = 24.dp,
    viewportWidth = 24f,
    viewportHeight = 24f
).apply {
    addPath(
        fill = SolidColor(Color.White),
        pathData = addPathNodes("M12 1 C17 1 21 3 21 8 C21 16 15 21 12 23 C9 21 3 16 3 8 C3 3 7 1 12 1 Z")
    )
}.build()

val ShieldOutlinedVector: ImageVector = ImageVector.Builder(
    name = "ShieldOutlined",
    defaultWidth = 24.dp,
    defaultHeight = 24.dp,
    viewportWidth = 24f,
    viewportHeight = 24f
).apply {
    addPath(
        fill = SolidColor(Color.White),
        pathData = addPathNodes("M12 2 C16.5 2 19.5 3.8 19.5 8 C19.5 14.8 14.5 19.2 12 20.8 C9.5 19.2 4.5 14.8 4.5 8 C4.5 3.8 7.5 2 12 2 Z M12 4.5 C8.8 4.5 6.5 5.8 6.5 8 C6.5 13.2 10.2 17.2 12 18.5 C13.8 17.2 17.5 13.2 17.5 8 C17.5 5.8 15.2 4.5 12 4.5 Z")
    )
}.build()

val TasbihFilledVector: ImageVector = ImageVector.Builder(
    name = "TasbihFilled",
    defaultWidth = 24.dp,
    defaultHeight = 24.dp,
    viewportWidth = 24f,
    viewportHeight = 24f
).apply {
    addPath(
        fill = SolidColor(Color.White),
        pathData = addPathNodes("M12 2 C6.48 2 2 6.48 2 12 C2 15.3 3.6 18.2 6.1 20 L4.5 23 H7.5 L8.5 20.8 C9.6 21.6 10.8 22 12 22 C13.2 22 14.4 21.6 15.5 20.8 L16.5 23 H19.5 L17.9 20 C20.4 18.2 22 15.3 22 12 C22 6.48 17.52 2 12 2 Z M12 5 C15.86 5 19 8.14 19 12 C19 15.86 15.86 19 12 19 C8.14 19 5 15.86 5 12 C5 8.14 8.14 5 12 5 Z")
    )
}.build()

val TasbihOutlinedVector: ImageVector = ImageVector.Builder(
    name = "TasbihOutlined",
    defaultWidth = 24.dp,
    defaultHeight = 24.dp,
    viewportWidth = 24f,
    viewportHeight = 24f
).apply {
    addPath(
        fill = SolidColor(Color.White),
        pathData = addPathNodes("M12 2 C6.48 2 2 6.48 2 12 C2 17.52 6.48 22 12 22 C17.52 22 22 17.52 22 12 C22 6.48 17.52 2 12 2 Z M12 4 C16.42 4 20 7.58 20 12 C20 16.42 16.42 20 12 20 C7.58 20 4 16.42 4 12 C4 7.58 7.58 4 12 4 Z M12 7 C9.24 7 7 9.24 7 12 C7 14.76 9.24 17 12 17 C14.76 17 17 14.76 17 12 C17 9.24 14.76 7 12 7 Z")
    )
}.build()

sealed class Screen(
    val route: String,
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    object Home : Screen("home", "الأذكار", Icons.Filled.Home, Icons.Outlined.Home)
    object Tasbih : Screen("tasbih", "السبحة", TasbihFilledVector, TasbihOutlinedVector)
    object Prayer : Screen("prayer", "المواقيت", Icons.Filled.DateRange, Icons.Outlined.DateRange)
    object Fortress : Screen("fortress", "الحصن", ShieldFilledVector, ShieldOutlinedVector)
    object Convert : Screen("convert", "التاريخ", Icons.Filled.DateRange, Icons.Outlined.DateRange)
    object Settings : Screen("settings", "الإعدادات", Icons.Filled.Settings, Icons.Outlined.Settings)
}

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            AthkarTheme {
                val uiState by viewModel.uiState.collectAsState()
                val navController = rememberNavController()

                val activeAdhanName = uiState.activeAdhanPrayerName
                val currentMuezzin = remember(uiState.selectedMuezzinId) {
                    AthkarData.muezzinsList.find { it.id == uiState.selectedMuezzinId } ?: AthkarData.muezzinsList[0]
                }

                var adhanPlayer by remember { mutableStateOf<android.media.MediaPlayer?>(null) }

                LaunchedEffect(activeAdhanName, currentMuezzin) {
                    if (activeAdhanName != null) {
                        adhanPlayer?.release()
                        adhanPlayer = null
                        try {
                            val mp = android.media.MediaPlayer().apply {
                                setAudioAttributes(
                                    android.media.AudioAttributes.Builder()
                                        .setContentType(android.media.AudioAttributes.CONTENT_TYPE_MUSIC)
                                        .setUsage(android.media.AudioAttributes.USAGE_MEDIA)
                                        .build()
                                )
                                setDataSource(currentMuezzin.audioUrl)
                                setOnPreparedListener {
                                    it.start()
                                }
                                setOnCompletionListener {
                                    viewModel.stopAdhan()
                                }
                                setOnErrorListener { _, _, _ ->
                                    viewModel.stopAdhan()
                                    true
                                }
                                prepareAsync()
                            }
                            adhanPlayer = mp
                        } catch (e: Exception) {
                            viewModel.stopAdhan()
                        }
                    } else {
                        try {
                            adhanPlayer?.stop()
                        } catch (e: Exception) {}
                        adhanPlayer?.release()
                        adhanPlayer = null
                    }
                }

                DisposableEffect(Unit) {
                    onDispose {
                        try {
                            adhanPlayer?.stop()
                        } catch (e: Exception) {}
                        adhanPlayer?.release()
                        adhanPlayer = null
                    }
                }

                val items = listOf(
                    Screen.Home,
                    Screen.Tasbih,
                    Screen.Prayer,
                    Screen.Fortress,
                    Screen.Convert,
                    Screen.Settings
                )

                Box(modifier = Modifier.fillMaxSize()) {
                    Scaffold(
                        modifier = Modifier.fillMaxSize(),
                    containerColor = DarkCanvas,
                    bottomBar = {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .navigationBarsPadding()
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(32.dp),
                                color = CardSurface.copy(alpha = 0.92f),
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    androidx.compose.ui.graphics.Brush.linearGradient(
                                        colors = listOf(
                                            GoldBright.copy(alpha = 0.4f),
                                            GoldPrimary.copy(alpha = 0.15f),
                                            GoldBright.copy(alpha = 0.3f)
                                        )
                                    )
                                ),
                                shadowElevation = 8.dp
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 4.dp, vertical = 6.dp),
                                    horizontalArrangement = Arrangement.SpaceEvenly,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    val navBackStackEntry by navController.currentBackStackEntryAsState()
                                    val currentRoute = navBackStackEntry?.destination?.route

                                    items.forEach { screen ->
                                        val isSelected = currentRoute == screen.route

                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .clip(RoundedCornerShape(20.dp))
                                                .background(
                                                    if (isSelected) GoldPrimary.copy(alpha = 0.1f) else androidx.compose.ui.graphics.Color.Transparent
                                                )
                                                .clickable {
                                                    if (currentRoute != screen.route) {
                                                        navController.navigate(screen.route) {
                                                            popUpTo(navController.graph.findStartDestination().id) {
                                                                saveState = true
                                                            }
                                                            launchSingleTop = true
                                                            restoreState = true
                                                        }
                                                    }
                                                }
                                                .padding(vertical = 6.dp)
                                                .testTag("nav_item_${screen.route}"),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Column(
                                                horizontalAlignment = Alignment.CenterHorizontally,
                                                verticalArrangement = Arrangement.Center
                                            ) {
                                                Icon(
                                                    imageVector = if (isSelected) screen.selectedIcon else screen.unselectedIcon,
                                                    contentDescription = screen.title,
                                                    tint = if (isSelected) GoldBright else TextMuted,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                                Spacer(modifier = Modifier.height(2.dp))
                                                Text(
                                                    text = screen.title,
                                                    fontSize = 9.5.sp,
                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                    color = if (isSelected) GoldBright else TextMuted,
                                                    maxLines = 1
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                ) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = Screen.Home.route,
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        composable(Screen.Home.route) {
                            HomeScreen(
                                uiState = uiState,
                                onCategorySelected = { viewModel.selectCategory(it) },
                                onDecrementDhikr = { catId, dhikrId, maxCount ->
                                    viewModel.decrementDhikr(catId, dhikrId, maxCount)
                                }
                            )
                        }

                        composable(Screen.Tasbih.route) {
                            TasbihScreen(
                                uiState = uiState,
                                onIncrementTasbih = { viewModel.incrementTasbih() },
                                onResetTasbih = { viewModel.resetCurrentTasbih() },
                                onSelectPhrase = { viewModel.selectTasbihPhrase(it) }
                            )
                        }

                        composable(Screen.Prayer.route) {
                            PrayerTimesScreen(
                                uiState = uiState,
                                onCitySelected = { viewModel.selectCity(it) },
                                onMethodSelected = { viewModel.selectMethod(it) }
                            )
                        }

                        composable(Screen.Fortress.route) {
                            FortressScreen(
                                uiState = uiState,
                                onQueryChanged = { viewModel.updateFortressQuery(it) }
                            )
                        }

                        composable(Screen.Convert.route) {
                            DateConverterScreen(
                                uiState = uiState,
                                onDateChanged = { y, m, d -> viewModel.updateConvertDate(y, m, d) }
                            )
                        }

                        composable(Screen.Settings.route) {
                            SettingsScreen(
                                uiState = uiState,
                                onToggleSound = { viewModel.toggleSound() },
                                onToggleNotifications = { viewModel.toggleNotifications() },
                                onMethodSelected = { viewModel.selectMethod(it) },
                                onSetCustomLocation = { lat, lon, name -> viewModel.setCustomLocation(lat, lon, name) },
                                onResetAll = { viewModel.resetAllData() },
                                onSelectMuezzin = { viewModel.selectMuezzin(it) }
                            )
                        }
                    }
                }

                // Floating Adhan Popup Overlay
                if (activeAdhanName != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.88f))
                            .clickable(enabled = false) {},
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp)
                        ) {
                            com.example.ui.components.LanternLightSurround(
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Spacer(modifier = Modifier.height(16.dp))

                                Text(
                                    text = "الله أكبر • الله أكبر",
                                    fontSize = 26.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = GoldBright,
                                    textAlign = TextAlign.Center
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                Text(
                                    text = "حان الآن موعد صلاة $activeAdhanName",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary,
                                    textAlign = TextAlign.Center
                                )

                                Text(
                                    text = "حسب التوقيت المحلي لمدينة ${AthkarData.presetCities.find { it.key == uiState.selectedCityKey }?.name ?: ""}",
                                    fontSize = 13.sp,
                                    color = TextSecondary,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(top = 4.dp)
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                Text(
                                    text = "بصوت: ${currentMuezzin.name}",
                                    fontSize = 14.sp,
                                    color = GoldPrimary,
                                    textAlign = TextAlign.Center
                                )

                                Spacer(modifier = Modifier.height(28.dp))

                                Button(
                                    onClick = { viewModel.stopAdhan() },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = GoldPrimary,
                                        contentColor = DarkCanvas
                                    ),
                                    shape = RoundedCornerShape(24.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(54.dp)
                                ) {
                                    Text(
                                        text = "إيقاف الأذان",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }

                    // Mandatory Privacy Policy Consent Gate Overlay
                    if (uiState.showPrivacyGate) {
                        PrivacyConsentScreen(
                            versionString = uiState.privacyVersion,
                            policyUrl = uiState.privacyPolicyUrl,
                            onAccept = {
                                viewModel.acceptPrivacyPolicy()
                            },
                            onDecline = {
                                finishAffinity()
                            }
                        )
                    }
                }
            }
        }
    }
}
}
