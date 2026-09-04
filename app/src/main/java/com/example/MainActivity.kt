package com.example

import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.width
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Radio
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.List
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Radio
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.addPathNodes
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
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
import com.example.update.UpdateCheckerEffect
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
    object Radio : Screen("quran_radio", "إذاعة", Icons.Filled.Radio, Icons.Outlined.Radio)
    object Settings : Screen("settings", "الإعدادات", Icons.Filled.Settings, Icons.Outlined.Settings)
}

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()
    private var textToSpeech: android.speech.tts.TextToSpeech? = null

    private fun speakText(text: String) {
        if (textToSpeech == null) {
            textToSpeech = android.speech.tts.TextToSpeech(this) { status ->
                if (status == android.speech.tts.TextToSpeech.SUCCESS) {
                    textToSpeech?.language = java.util.Locale("ar")
                    textToSpeech?.speak(text, android.speech.tts.TextToSpeech.QUEUE_FLUSH, null, null)
                }
            }
        } else {
            textToSpeech?.speak(text, android.speech.tts.TextToSpeech.QUEUE_FLUSH, null, null)
        }
    }

    override fun onDestroy() {
        try {
            textToSpeech?.stop()
            textToSpeech?.shutdown()
        } catch (_: Exception) {}
        super.onDestroy()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            AthkarTheme {
                val uiState by viewModel.uiState.collectAsState()
                val navController = rememberNavController()
                
                // Firebase Realtime Global Announcement Dialog
                val activeBroadcast = uiState.activeBroadcast
                if (activeBroadcast != null) {
                    AlertDialog(
                        onDismissRequest = {
                            viewModel.dismissBroadcast(activeBroadcast.id)
                        },
                        title = {
                            Text(
                                text = if (activeBroadcast.title.isNotBlank()) activeBroadcast.title else "رسالة هامة",
                                color = GoldBright,
                                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                            )
                        },
                        text = {
                            Text(
                                text = activeBroadcast.text,
                                color = TextPrimary,
                                fontSize = 16.sp,
                                lineHeight = 24.sp
                            )
                        },
                        containerColor = CardSurface,
                        confirmButton = {
                            Button(
                                onClick = {
                                    viewModel.dismissBroadcast(activeBroadcast.id)
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = GoldPrimary,
                                    contentColor = Color.Black
                                ),
                                shape = androidx.compose.foundation.shape.RoundedCornerShape(14.dp)
                            ) {
                                Text("موافق", fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                            }
                        }
                    )
                }
                
                val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
                DisposableEffect(lifecycleOwner) {
                    val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
                        if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
                            viewModel.checkSystemPermissions()
                        }
                    }
                    lifecycleOwner.lifecycle.addObserver(observer)
                    onDispose {
                        lifecycleOwner.lifecycle.removeObserver(observer)
                    }
                }
                
                var exactAlarmDialogDismissed by remember { mutableStateOf(false) }
                
                if (!uiState.exactAlarmsAllowed && android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S && !exactAlarmDialogDismissed) {
                    AlertDialog(
                        onDismissRequest = { exactAlarmDialogDismissed = true },
                        title = { Text(text = "صلاحية التنبيهات الدقيقة", color = GoldBright) },
                        text = { Text(text = "لضمان دقة وقت الأذان وعدم تأخره عن الوقت الفعلي، يرجى تفعيل صلاحية 'الإنذارات الدقيقة' (Alarms & Reminders) من إعدادات التطبيق.", color = TextPrimary) },
                        containerColor = CardSurface,
                        confirmButton = {
                            TextButton(onClick = {
                                try {
                                    val intent = android.content.Intent(android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                                        data = android.net.Uri.parse("package:$packageName")
                                    }
                                    startActivity(intent)
                                } catch (e: Exception) {}
                                exactAlarmDialogDismissed = true
                            }) {
                                Text("تفعيل الآن", color = GoldPrimary, fontWeight = FontWeight.Bold)
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { exactAlarmDialogDismissed = true }) {
                                Text("تجاهل", color = TextSecondary)
                            }
                        }
                    )
                }

                val permissionLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestMultiplePermissions()
                ) { permissions ->
                    val locationGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                            permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
                    if (locationGranted) {
                        requestFreshLocation(viewModel)
                    }
                }

                LaunchedEffect(Unit) {
                    val permissionsToRequest = mutableListOf<String>()
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        if (ContextCompat.checkSelfPermission(this@MainActivity, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                            permissionsToRequest.add(Manifest.permission.POST_NOTIFICATIONS)
                        }
                    }
                    if (ContextCompat.checkSelfPermission(this@MainActivity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        permissionsToRequest.add(Manifest.permission.ACCESS_FINE_LOCATION)
                        permissionsToRequest.add(Manifest.permission.ACCESS_COARSE_LOCATION)
                    }
                    if (permissionsToRequest.isNotEmpty()) {
                        permissionLauncher.launch(permissionsToRequest.toTypedArray())
                    } else {
                        requestFreshLocation(viewModel)
                    }
                }

                val activeAthkarNotif = uiState.activeAthkarNotification
                LaunchedEffect(activeAthkarNotif) {
                    if (activeAthkarNotif != null) {
                        showAthkarNotification(
                            this@MainActivity,
                            activeAthkarNotif.title,
                            activeAthkarNotif.body,
                            activeAthkarNotif.categoryId
                        )
                        val voiceMsg = when (activeAthkarNotif.categoryId) {
                            "morning" -> "حان وقت قراءة أذكار الصباح"
                            "evening" -> "حان وقت قراءة أذكار المساء"
                            "sleep" -> "حان وقت قراءة أذكار النوم"
                            else -> "حان وقت قراءة الأذكار"
                        }
                        speakText(voiceMsg)
                        viewModel.clearAthkarNotification()
                    }
                }

                val activeAdhanName = uiState.activeAdhanPrayerName
                val currentMuezzin = remember(uiState.selectedMuezzinId) {
                    AthkarData.muezzinsList.find { it.id == uiState.selectedMuezzinId } ?: AthkarData.muezzinsList[0]
                }

                LaunchedEffect(activeAdhanName, currentMuezzin) {
                    if (activeAdhanName != null) {
                        showPrayerNotification(this@MainActivity, activeAdhanName)
                        val serviceIntent = android.content.Intent(this@MainActivity, com.example.service.AdhanForegroundService::class.java).apply {
                            action = com.example.service.AdhanForegroundService.ACTION_PLAY_ADHAN
                            putExtra(com.example.service.AdhanForegroundService.EXTRA_PRAYER_NAME, activeAdhanName)
                            putExtra(com.example.service.AdhanForegroundService.EXTRA_MUEZZIN_ID, uiState.selectedMuezzinId)
                        }
                        try {
                            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                                this@MainActivity.startForegroundService(serviceIntent)
                            } else {
                                this@MainActivity.startService(serviceIntent)
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    } else {
                        val stopIntent = android.content.Intent(this@MainActivity, com.example.service.AdhanForegroundService::class.java).apply {
                            action = com.example.service.AdhanForegroundService.ACTION_STOP_ADHAN
                        }
                        this@MainActivity.startService(stopIntent)
                    }
                }

                DisposableEffect(Unit) {
                    val receiver = object : android.content.BroadcastReceiver() {
                        override fun onReceive(context: android.content.Context, intent: android.content.Intent) {
                            if (intent.action == com.example.service.AdhanForegroundService.ACTION_ADHAN_STOPPED) {
                                viewModel.stopAdhan()
                            }
                        }
                    }
                    val filter = android.content.IntentFilter(com.example.service.AdhanForegroundService.ACTION_ADHAN_STOPPED)
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                        this@MainActivity.registerReceiver(receiver, filter, android.content.Context.RECEIVER_NOT_EXPORTED)
                    } else {
                        this@MainActivity.registerReceiver(receiver, filter)
                    }
                    onDispose {
                        try {
                            this@MainActivity.unregisterReceiver(receiver)
                        } catch (_: Exception) {}
                    }
                }

                val items = listOf(
                    Screen.Home,
                    Screen.Tasbih,
                    Screen.Prayer,
                    Screen.Radio,
                    Screen.Fortress,
                    Screen.Convert,
                    Screen.Settings
                )

                Box(modifier = Modifier.fillMaxSize()) {
                    UpdateCheckerEffect()

                    val haptic = LocalHapticFeedback.current
                    val navBackStackEntry by navController.currentBackStackEntryAsState()
                    val currentRoute = navBackStackEntry?.destination?.route

                    Scaffold(
                        modifier = Modifier.fillMaxSize(),
                        containerColor = DarkCanvas,
                        bottomBar = {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .navigationBarsPadding(),
                                contentAlignment = Alignment.BottomCenter
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
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
                                            items.forEachIndexed { index, screen ->
                                                val isSelected = currentRoute == screen.route

                                                Box(
                                                    modifier = Modifier
                                                        .weight(1f)
                                                        .clip(RoundedCornerShape(20.dp))
                                                        .background(
                                                            if (isSelected) GoldPrimary.copy(alpha = 0.1f) else androidx.compose.ui.graphics.Color.Transparent
                                                        )
                                                        .clickable {
                                                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
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
                                                            fontSize = 9.sp,
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
                        }
                    ) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = Screen.Home.route,
                        modifier = Modifier.padding(innerPadding),
                        enterTransition = { fadeIn(animationSpec = tween(60)) },
                        exitTransition = { fadeOut(animationSpec = tween(60)) },
                        popEnterTransition = { fadeIn(animationSpec = tween(60)) },
                        popExitTransition = { fadeOut(animationSpec = tween(60)) }
                    ) {
                        composable(Screen.Home.route) {
                            HomeScreen(
                                uiState = uiState,
                                onCategorySelected = { viewModel.selectCategory(it) },
                                onDecrementDhikr = { catId, dhikrId, maxCount ->
                                    viewModel.decrementDhikr(catId, dhikrId, maxCount)
                                },
                                onSendBroadcast = { text, url, onResult ->
                                    viewModel.sendBroadcastMessage(text, url, onResult)
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
                                onMethodSelected = { viewModel.selectMethod(it) },
                                onToggleMute = { viewModel.togglePrayerMute(it) }
                            )
                        }

                        composable(Screen.Radio.route) {
                            QuranRadioScreen(onBackClick = { navController.popBackStack() })
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
                                onToggleMorningAthkar = { viewModel.toggleMorningAthkarNotify() },
                                onToggleEveningAthkar = { viewModel.toggleEveningAthkarNotify() },
                                onToggleSleepAthkar = { viewModel.toggleSleepAthkarNotify() },
                                onMethodSelected = { viewModel.selectMethod(it) },
                                onSetCustomLocation = { lat, lon, name -> viewModel.setCustomLocation(lat, lon, name) },
                                onResetAll = { viewModel.resetAllData() },
                                onSelectMuezzin = { viewModel.selectMuezzin(it) },
                                onCitySelected = { viewModel.selectCity(it) },
                                onGPSRequested = { requestFreshLocation(viewModel) }
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

    private fun showPrayerNotification(context: android.content.Context, prayerName: String) {
        val channelId = "prayer_times_channel"
        val notificationManager = context.getSystemService(android.content.Context.NOTIFICATION_SERVICE) as android.app.NotificationManager

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val channel = android.app.NotificationChannel(
                channelId,
                "مواقيت الصلاة والأذان",
                android.app.NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "تنبيهات مواقيت الصلاة والأذان"
                enableLights(true)
                lightColor = android.graphics.Color.parseColor("#FFD700")
                enableVibration(true)
            }
            notificationManager.createNotificationChannel(channel)
        }

        val intent = android.content.Intent(context, MainActivity::class.java).apply {
            flags = android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP or android.content.Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val pendingIntent = android.app.PendingIntent.getActivity(
            context,
            0,
            intent,
            android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
        )

        val notification = androidx.core.app.NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle("حان الآن موعد صلاة $prayerName")
            .setContentText("الله أكبر الله أكبر.. حان الآن موعد الأذان حسب توقيتك المحلي")
            .setPriority(androidx.core.app.NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setCategory(androidx.core.app.NotificationCompat.CATEGORY_ALARM)
            .build()

        notificationManager.notify(1001, notification)
    }

    private fun showAthkarNotification(context: android.content.Context, title: String, body: String, categoryId: String) {
        val channelId = "athkar_reminder_channel"
        val notificationManager = context.getSystemService(android.content.Context.NOTIFICATION_SERVICE) as android.app.NotificationManager

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val channel = android.app.NotificationChannel(
                channelId,
                "تذكير الأذكار",
                android.app.NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "تنبيهات وتذكيرات بالأذكار اليومية"
                enableLights(true)
                lightColor = android.graphics.Color.parseColor("#FFD700")
            }
            notificationManager.createNotificationChannel(channel)
        }

        val intent = android.content.Intent(context, MainActivity::class.java).apply {
            flags = android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP or android.content.Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra("target_category", categoryId)
        }
        val pendingIntent = android.app.PendingIntent.getActivity(
            context,
            categoryId.hashCode(),
            intent,
            android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
        )

        val notification = androidx.core.app.NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_popup_reminder)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(androidx.core.app.NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(androidx.core.app.NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        notificationManager.notify(categoryId.hashCode(), notification)
    }

    private fun requestFreshLocation(viewModel: com.example.ui.MainViewModel) {
        if (androidx.core.content.ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == android.content.pm.PackageManager.PERMISSION_GRANTED ||
            androidx.core.content.ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) == android.content.pm.PackageManager.PERMISSION_GRANTED) {
            
            try {
                val fusedLocationClient = com.google.android.gms.location.LocationServices.getFusedLocationProviderClient(this)
                
                // First try lastLocation for a quick result
                fusedLocationClient.lastLocation.addOnSuccessListener { loc ->
                    if (loc != null) {
                        resolveAndSetLocation(viewModel, loc.latitude, loc.longitude)
                    } else {
                        // If null, request current location (high accuracy)
                        requestFreshGPSCurrentLocation(viewModel, fusedLocationClient)
                    }
                }.addOnFailureListener {
                    requestFreshGPSCurrentLocation(viewModel, fusedLocationClient)
                }
            } catch (e: Exception) {
                android.widget.Toast.makeText(this, "فشل في تشغيل نظام تحديد المواقع", android.widget.Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun requestFreshGPSCurrentLocation(viewModel: com.example.ui.MainViewModel, fusedLocationClient: com.google.android.gms.location.FusedLocationProviderClient) {
        try {
            val locationRequest = com.google.android.gms.location.LocationRequest.Builder(
                com.google.android.gms.location.Priority.PRIORITY_HIGH_ACCURACY, 1000
            ).setMaxUpdates(1).build()

            val locationCallback = object : com.google.android.gms.location.LocationCallback() {
                override fun onLocationResult(locationResult: com.google.android.gms.location.LocationResult) {
                    val lastLoc = locationResult.lastLocation
                    if (lastLoc != null) {
                        resolveAndSetLocation(viewModel, lastLoc.latitude, lastLoc.longitude)
                    } else {
                        android.widget.Toast.makeText(this@MainActivity, "تعذر الحصول على إحداثيات GPS حاليًا. تأكد من تفعيل الموقع بالهاتف.", android.widget.Toast.LENGTH_LONG).show()
                    }
                }
            }
            
            fusedLocationClient.requestLocationUpdates(
                locationRequest, locationCallback, android.os.Looper.getMainLooper()
            )
        } catch (e: Exception) {
            android.widget.Toast.makeText(this, "فشل في تحديد الموقع بدقة", android.widget.Toast.LENGTH_SHORT).show()
        }
    }

    private fun resolveAndSetLocation(viewModel: com.example.ui.MainViewModel, latitude: Double, longitude: Double) {
        var cityName = "موقعي الحالي (GPS)"
        try {
            val geocoder = android.location.Geocoder(this@MainActivity, java.util.Locale("ar"))
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                geocoder.getFromLocation(latitude, longitude, 1) { addresses ->
                    if (!addresses.isNullOrEmpty()) {
                        val address = addresses[0]
                        cityName = address.locality ?: address.subAdminArea ?: address.adminArea ?: "موقعي الحالي (GPS)"
                    }
                    runOnUiThread {
                        viewModel.setCustomLocation(latitude, longitude, cityName)
                        android.widget.Toast.makeText(this@MainActivity, "تم تحديد الموقع بنجاح: $cityName", android.widget.Toast.LENGTH_LONG).show()
                    }
                }
                return
            } else {
                @Suppress("DEPRECATION")
                val addresses = geocoder.getFromLocation(latitude, longitude, 1)
                if (!addresses.isNullOrEmpty()) {
                    val address = addresses[0]
                    cityName = address.locality ?: address.subAdminArea ?: address.adminArea ?: "موقعي الحالي (GPS)"
                }
            }
        } catch (_: Exception) {}
        
        viewModel.setCustomLocation(latitude, longitude, cityName)
        android.widget.Toast.makeText(this@MainActivity, "تم تحديد الموقع بنجاح: $cityName", android.widget.Toast.LENGTH_LONG).show()
    }
}
