package com.example.ui.screens
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Radio
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import kotlinx.coroutines.launch
import com.example.service.RadioForegroundService
import com.example.service.RadioStationsData

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuranRadioScreen(
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var isPlaying by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var currentStationIndex by remember { mutableIntStateOf(0) }

    // Register BroadcastReceiver to receive service state and error updates
    DisposableEffect(Unit) {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                when (intent?.action) {
                    RadioForegroundService.ACTION_RADIO_STATE_CHANGED -> {
                        isPlaying = intent.getBooleanExtra(RadioForegroundService.EXTRA_IS_PLAYING, false)
                        currentStationIndex = intent.getIntExtra(RadioForegroundService.EXTRA_CURRENT_INDEX, 0)
                        isLoading = intent.getBooleanExtra(RadioForegroundService.EXTRA_IS_BUFFERING, false)
                    }
                    RadioForegroundService.ACTION_RADIO_ERROR -> {
                        val errorMsg = intent.getStringExtra(RadioForegroundService.EXTRA_ERROR_MESSAGE)
                            ?: "تعذر الاتصال ببث الإذاعة."
                        isLoading = false
                        isPlaying = false
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar(errorMsg)
                        }
                    }
                }
            }
        }

        val filter = IntentFilter().apply {
            addAction(RadioForegroundService.ACTION_RADIO_STATE_CHANGED)
            addAction(RadioForegroundService.ACTION_RADIO_ERROR)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.registerReceiver(receiver, filter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            context.registerReceiver(receiver, filter)
        }

        // Send a start service intent via ContextCompat to sync initial state
        try {
            val syncIntent = Intent(context, RadioForegroundService::class.java)
            ContextCompat.startForegroundService(context, syncIntent)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        onDispose {
            try {
                context.unregisterReceiver(receiver)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    val animatedRotation = 0f

    val currentStation = RadioStationsData.stations.getOrNull(currentStationIndex)

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = "إذاعة القرآن الكريم", 
                        fontWeight = FontWeight.Bold, 
                        color = com.example.ui.theme.GoldBright
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "عودة",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = com.example.ui.theme.DarkCanvas,
                    titleContentColor = com.example.ui.theme.GoldBright
                )
            )
        },
        containerColor = com.example.ui.theme.DarkCanvas
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(com.example.ui.theme.DarkCanvas),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Player Top Card / Visualizer Area
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Disk visualizer
                Box(
                    modifier = Modifier
                        .size(200.dp)
                        .clip(CircleShape)
                        .rotate(animatedRotation)
                        .background(
                            Brush.sweepGradient(
                                colors = listOf(
                                    com.example.ui.theme.GoldPrimary,
                                    com.example.ui.theme.GoldMuted,
                                    com.example.ui.theme.GoldContainer,
                                    com.example.ui.theme.GoldPrimary
                                )
                            )
                        )
                        .border(4.dp, com.example.ui.theme.GoldBright, CircleShape)
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                            .background(com.example.ui.theme.DarkCanvas),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Radio,
                            contentDescription = "Radio",
                            tint = com.example.ui.theme.GoldBright,
                            modifier = Modifier.size(80.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(36.dp))

                // Station Title & Subtitle
                Text(
                    text = currentStation?.name ?: "تحميل الإذاعة...",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                if (isLoading) {
                    Text(
                        text = "جاري الاتصال بالبث المباشر...",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                        color = com.example.ui.theme.GoldBright,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    Text(
                        text = currentStation?.subtitle ?: "",
                        fontSize = 15.sp,
                        color = com.example.ui.theme.TextSecondary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(36.dp))

                // Control Buttons: Prev, Play/Pause, Next
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Previous Button
                    IconButton(
                        onClick = {
                            val prevIdx = if (currentStationIndex - 1 < 0) RadioStationsData.stations.size - 1 else currentStationIndex - 1
                            currentStationIndex = prevIdx
                            isPlaying = true
                            isLoading = true
                            val intent = Intent(context, RadioForegroundService::class.java).apply {
                                action = RadioForegroundService.ACTION_PREV
                            }
                            ContextCompat.startForegroundService(context, intent)
                        },
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(com.example.ui.theme.CardSurface),
                    ) {
                        Icon(
                            imageVector = Icons.Default.SkipPrevious,
                            contentDescription = "السابق",
                            tint = com.example.ui.theme.GoldBright,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(28.dp))

                    // Play / Pause FAB
                    FloatingActionButton(
                        onClick = {
                            val targetState = !isPlaying
                            isPlaying = targetState
                            if (targetState) isLoading = true
                            val action = if (targetState) RadioForegroundService.ACTION_PLAY else RadioForegroundService.ACTION_PAUSE
                            val intent = Intent(context, RadioForegroundService::class.java).apply {
                                this.action = action
                                putExtra(RadioForegroundService.EXTRA_STATION_INDEX, currentStationIndex)
                            }
                            ContextCompat.startForegroundService(context, intent)
                        },
                        containerColor = com.example.ui.theme.GoldPrimary,
                        contentColor = com.example.ui.theme.DarkCanvas,
                        modifier = Modifier.size(76.dp),
                        shape = CircleShape
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(36.dp),
                                color = com.example.ui.theme.DarkCanvas,
                                strokeWidth = 3.dp
                            )
                        } else {
                            Icon(
                                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = if (isPlaying) "إيقاف مؤقت" else "تشغيل",
                                modifier = Modifier.size(38.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(28.dp))

                    // Next Button
                    IconButton(
                        onClick = {
                            val nextIdx = (currentStationIndex + 1) % RadioStationsData.stations.size
                            currentStationIndex = nextIdx
                            isPlaying = true
                            isLoading = true
                            val intent = Intent(context, RadioForegroundService::class.java).apply {
                                action = RadioForegroundService.ACTION_NEXT
                            }
                            ContextCompat.startForegroundService(context, intent)
                        },
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(com.example.ui.theme.CardSurface),
                    ) {
                        Icon(
                            imageVector = Icons.Default.SkipNext,
                            contentDescription = "التالي",
                            tint = com.example.ui.theme.GoldBright,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            }
        }
    }
}
