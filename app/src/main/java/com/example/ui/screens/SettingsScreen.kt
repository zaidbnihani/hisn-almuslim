package com.example.ui.screens
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState

import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.ui.platform.LocalContext
import android.media.MediaPlayer
import android.media.AudioAttributes
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AthkarData
import com.example.ui.AthkarUiState
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    uiState: AthkarUiState,
    onToggleSound: () -> Unit,
    onToggleNotifications: () -> Unit,
    onToggleMorningAthkar: () -> Unit = {},
    onToggleEveningAthkar: () -> Unit = {},
    onToggleSleepAthkar: () -> Unit = {},
    onMethodSelected: (String) -> Unit = {},
    onSetCustomLocation: (Double, Double, String) -> Unit = { _, _, _ -> },
    onResetAll: () -> Unit,
    onSelectMuezzin: (String) -> Unit = {},
    onCitySelected: (String) -> Unit = {},
    onGPSRequested: () -> Unit = {}
) {
    var showReciterPicker by remember { mutableStateOf(false) }
    var showLocationPicker by remember { mutableStateOf(false) }
    var previewMuezzinId by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var previewPlayer by remember { mutableStateOf<MediaPlayer?>(null) }
    var previewAfd by remember { mutableStateOf<android.content.res.AssetFileDescriptor?>(null) }

    val stopPreview: () -> Unit = {
        try {
            previewPlayer?.apply {
                if (isPlaying) stop()
                release()
            }
        } catch (_: Exception) {}
        try {
            previewAfd?.close()
        } catch (_: Exception) {}
        previewPlayer = null
        previewAfd = null
        previewMuezzinId = null
    }

    DisposableEffect(Unit) {
        onDispose {
            stopPreview()
        }
    }

    val playPreview: (com.example.data.MuezzinItem) -> Unit = { muezzin ->
        val isSame = (previewMuezzinId == muezzin.id)
        stopPreview()

        if (!isSame) {
            try {
                val mp = MediaPlayer().apply {
                    setAudioAttributes(
                        AudioAttributes.Builder()
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .build()
                    )
                    setVolume(1.0f, 1.0f)
                    
                    if (muezzin.audioUrl.startsWith("asset:")) {
                        val path = muezzin.audioUrl.removePrefix("asset:")
                        val afd = context.assets.openFd(path)
                        previewAfd = afd
                        setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
                    } else {
                        setDataSource(context, android.net.Uri.parse(muezzin.audioUrl))
                    }
                    setOnCompletionListener {
                        try { it.release() } catch (_: Exception) {}
                        previewPlayer = null
                        previewMuezzinId = null
                        try { previewAfd?.close() } catch (_: Exception) {}
                        previewAfd = null
                    }
                    setOnErrorListener { _, what, extra ->
                        android.util.Log.e("MediaPlayer", "Preview Error what: $what extra: $extra")
                        try { release() } catch (_: Exception) {}
                        previewPlayer = null
                        previewMuezzinId = null
                        try { previewAfd?.close() } catch (_: Exception) {}
                        previewAfd = null
                        true
                    }
                    setOnPreparedListener { 
                        it.start() 
                    }
                    prepareAsync()
                }
                previewPlayer = mp
                previewMuezzinId = muezzin.id
            } catch (e: Exception) {
                android.util.Log.e("MediaPlayer", "Preview Exception: ${e.message}", e)
                stopPreview()
            }
        }
    }

    val currentMuezzin = remember(uiState.selectedMuezzinId) {
        AthkarData.muezzinsList.find { it.id == uiState.selectedMuezzinId } ?: AthkarData.muezzinsList[0]
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkCanvas)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 96.dp)
        ) {
            // Header
            item {
                Text(
                    text = "الإعدادات والتفضيلات",
                    style = MaterialTheme.typography.headlineLarge,
                    color = GoldBright,
                    modifier = Modifier.padding(vertical = 12.dp)
                )
            }

            // Sound Toggle
            item {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    shape = RoundedCornerShape(22.dp),
                    color = CardSurface,
                    border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = "الصوت",
                                tint = GoldPrimary
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "المؤثرات الصوتية",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "تشغيل صوت النقرات عند التسبيح وقراءة الأذكار",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextSecondary
                                )
                            }
                        }

                        Switch(
                            checked = uiState.soundEnabled,
                            onCheckedChange = { onToggleSound() },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = DarkCanvas,
                                checkedTrackColor = GoldPrimary
                            ),
                            modifier = Modifier.testTag("switch_sound")
                        )
                    }
                }
            }

            // Notifications Toggle
            item {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    shape = RoundedCornerShape(22.dp),
                    color = CardSurface,
                    border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Notifications,
                                    contentDescription = "التنبيهات",
                                    tint = GoldPrimary
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "تنبيهات الأذكار",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = TextPrimary
                                    )
                                    Text(
                                        text = "تذكيرات اليومية بأذكار الصباح والمساء والنوم",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = TextSecondary
                                    )
                                }
                            }

                            Switch(
                                checked = uiState.notificationsEnabled,
                                onCheckedChange = { onToggleNotifications() },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = DarkCanvas,
                                    checkedTrackColor = GoldPrimary
                                ),
                                modifier = Modifier.testTag("switch_notif")
                            )
                        }

                        if (uiState.notificationsEnabled) {
                            Spacer(modifier = Modifier.height(12.dp))
                            HorizontalDivider(color = CardBorder.copy(alpha = 0.5f))
                            Spacer(modifier = Modifier.height(8.dp))

                            // Morning Athkar Switch
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "تنبيه أذكار الصباح (02:00 م)",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextPrimary
                                )
                                Switch(
                                    checked = uiState.morningAthkarNotify,
                                    onCheckedChange = { onToggleMorningAthkar() },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = DarkCanvas,
                                        checkedTrackColor = GoldPrimary
                                    )
                                )
                            }

                            // Evening Athkar Switch
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "تنبيه أذكار المساء (05:00 م)",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextPrimary
                                )
                                Switch(
                                    checked = uiState.eveningAthkarNotify,
                                    onCheckedChange = { onToggleEveningAthkar() },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = DarkCanvas,
                                        checkedTrackColor = GoldPrimary
                                    )
                                )
                            }

                            // Sleep Athkar Switch
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "تنبيه أذكار النوم (10:00 م)",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextPrimary
                                )
                                Switch(
                                    checked = uiState.sleepAthkarNotify,
                                    onCheckedChange = { onToggleSleepAthkar() },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = DarkCanvas,
                                        checkedTrackColor = GoldPrimary
                                    )
                                )
                            }
                        }
                    }
                }
            }

            // Geographical Location Settings Card
            item {
                val currentLocName = if (uiState.customLocation != null) {
                    uiState.customLocation.name
                } else {
                    val activeCity = AthkarData.presetCities.find { it.key == uiState.selectedCityKey }
                    activeCity?.name ?: "مكة المكرمة"
                }

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clip(RoundedCornerShape(22.dp))
                        .clickable { showLocationPicker = true }
                        .testTag("btn_select_location"),
                    color = CardSurface,
                    border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = "الموقع الجغرافي",
                                tint = GoldPrimary
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "الموقع الجغرافي",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = TextPrimary
                                )
                                Text(
                                    text = currentLocName,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = GoldBright
                                )
                            }
                        }
                    }
                }
            }

            // Select Reciter / Muezzin
            item {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clip(RoundedCornerShape(22.dp))
                        .clickable { showReciterPicker = true }
                        .testTag("btn_select_reciter"),
                    color = CardSurface,
                    border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = "المؤذن",
                                tint = GoldPrimary
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "مؤذن وقارئ الأذكار",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = TextPrimary
                                )
                                Text(
                                    text = currentMuezzin.name,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = GoldBright
                                )
                            }
                        }
                    }
                }
            }



            // More Apps Section
            item {
                Spacer(modifier = Modifier.height(20.dp))
                Card(
                    colors = CardDefaults.cardColors(containerColor = CardSurface),
                    border = BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "تطبيقاتنا الإسلامية",
                            style = MaterialTheme.typography.titleMedium,
                            color = GoldBright,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "استكشف المزيد من تطبيقاتنا المفيدة والنافعة",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Button(
                            onClick = {
                                try {
                                    val intent = Intent(
                                        Intent.ACTION_VIEW,
                                        Uri.parse("https://service-22989443747.europe-west2.run.app/")
                                    )
                                    context.startActivity(intent)
                                } catch (_: Exception) {}
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = GoldPrimary,
                                contentColor = Color.Black
                            ),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("btn_more_apps")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = Color.Black
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "المزيد من تطبيقاتنا",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Version Info
            item {
                Spacer(modifier = Modifier.height(24.dp))
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "تطبيق أذكاري - Azkari",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = GoldBright
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "الإصدار v${com.example.BuildConfig.VERSION_NAME}",
                        fontSize = 13.sp,
                        color = TextSecondary,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }

        // Reciters Picker Bottom Sheet
        if (showReciterPicker) {
            val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
            ModalBottomSheet(
                onDismissRequest = {
                    stopPreview()
                    showReciterPicker = false
                },
                sheetState = sheetState,
                containerColor = CardSurface,
                dragHandle = {
                    BottomSheetDefaults.DragHandle(color = GoldBright.copy(alpha = 0.5f))
                }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = 22.dp, vertical = 12.dp)
                ) {
                    // Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = CircleShape,
                                color = GoldPrimary.copy(alpha = 0.2f),
                                modifier = Modifier.size(42.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Notifications,
                                        contentDescription = null,
                                        tint = GoldBright,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "مكتبة أصوات الأذان",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = GoldBright
                                )
                                Text(
                                    text = "اختر المؤذن لتنبيهات الصلوات",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))
                    HorizontalDivider(color = CardBorder.copy(alpha = 0.5f), thickness = 1.dp)
                    Spacer(modifier = Modifier.height(14.dp))

                    // Muezzins List
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f, fill = false)
                            .verticalScroll(rememberScrollState())
                    ) {
                        AthkarData.muezzinsList.forEach { muezzin ->
                            val isSelected = uiState.selectedMuezzinId == muezzin.id

                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 5.dp)
                                    .clip(RoundedCornerShape(18.dp)),
                                shape = RoundedCornerShape(18.dp),
                                color = if (isSelected) GoldPrimary.copy(alpha = 0.22f) else CardSurfaceVariant.copy(alpha = 0.35f),
                                border = if (isSelected) androidx.compose.foundation.BorderStroke(1.5.dp, GoldBright) else androidx.compose.foundation.BorderStroke(0.5.dp, CardBorder)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 14.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    val isPreviewing = previewMuezzinId == muezzin.id
                                    IconButton(
                                        onClick = { playPreview(muezzin) },
                                        modifier = Modifier.size(40.dp)
                                    ) {
                                        Surface(
                                            shape = CircleShape,
                                            color = if (isPreviewing) Color(0xFFFF6B6B).copy(alpha = 0.2f) else GoldPrimary.copy(alpha = 0.15f),
                                            modifier = Modifier.size(36.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Icon(
                                                    imageVector = if (isPreviewing) Icons.Default.Close else Icons.Default.PlayArrow,
                                                    contentDescription = "تجربة الصوت",
                                                    tint = if (isPreviewing) Color(0xFFFF6B6B) else GoldBright,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(12.dp))

                                    Row(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clickable {
                                                stopPreview()
                                                onSelectMuezzin(muezzin.id)
                                                android.widget.Toast.makeText(
                                                    context,
                                                    "تم اختيار صوت ${muezzin.name} للأذان بنجاح",
                                                    android.widget.Toast.LENGTH_SHORT
                                                ).show()
                                                coroutineScope.launch {
                                                    sheetState.hide()
                                                    showReciterPicker = false
                                                }
                                            },
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = muezzin.name,
                                                style = MaterialTheme.typography.titleMedium,
                                                color = if (isSelected) GoldBright else TextPrimary,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                textAlign = TextAlign.Start
                                            )
                                            Text(
                                                text = muezzin.description,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = TextSecondary,
                                                fontSize = 11.sp
                                            )
                                        }

                                        if (isSelected) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = "محدد",
                                                tint = GoldBright,
                                                modifier = Modifier.size(20.dp)
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

        // Location Selector Bottom Sheet
        if (showLocationPicker) {
            val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
            var citySearchQuery by remember { mutableStateOf("") }

            ModalBottomSheet(
                onDismissRequest = { showLocationPicker = false },
                sheetState = sheetState,
                containerColor = CardSurface,
                dragHandle = {
                    BottomSheetDefaults.DragHandle(color = GoldBright.copy(alpha = 0.5f))
                }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = 22.dp, vertical = 12.dp)
                ) {
                    // Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = GoldPrimary.copy(alpha = 0.2f),
                            modifier = Modifier.size(42.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.LocationOn,
                                    contentDescription = null,
                                    tint = GoldBright,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "تحديد موقع مواقيت الصلاة لجميع الدول",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = GoldBright
                            )
                            Text(
                                text = "اختر مدينتك أو فعّل الـ GPS لتوقيت دقيق لأي دولة في العالم",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Search input
                    OutlinedTextField(
                        value = citySearchQuery,
                        onValueChange = { citySearchQuery = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        placeholder = {
                            Text("ابحث عن اسم المدينة أو الدولة (مثل: مكة، عمان، القاهرة، فرنسا...)", fontSize = 13.sp, color = TextSecondary.copy(alpha = 0.7f))
                        },
                        leadingIcon = {
                            Icon(Icons.Default.Search, contentDescription = "بحث", tint = GoldBright)
                        },
                        trailingIcon = {
                            if (citySearchQuery.isNotBlank()) {
                                IconButton(onClick = { citySearchQuery = "" }) {
                                    Icon(Icons.Default.Close, contentDescription = "مسح", tint = TextSecondary)
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GoldBright,
                            unfocusedBorderColor = CardBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        )
                    )

                    Spacer(modifier = Modifier.height(10.dp))
                    HorizontalDivider(color = CardBorder.copy(alpha = 0.5f), thickness = 1.dp)
                    Spacer(modifier = Modifier.height(10.dp))

                    // Main Scrollable Area
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f, fill = false)
                            .verticalScroll(rememberScrollState())
                    ) {
                        // 1. GPS Automatic Trigger Option
                        if (citySearchQuery.isBlank()) {
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp)
                                    .clip(RoundedCornerShape(18.dp))
                                    .clickable {
                                        onGPSRequested()
                                        coroutineScope.launch {
                                            sheetState.hide()
                                            showLocationPicker = false
                                        }
                                    },
                                shape = RoundedCornerShape(18.dp),
                                color = GoldPrimary.copy(alpha = 0.15f),
                                border = androidx.compose.foundation.BorderStroke(1.5.dp, GoldBright)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        shape = CircleShape,
                                        color = GoldBright.copy(alpha = 0.25f),
                                        modifier = Modifier.size(40.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = Icons.Default.LocationOn,
                                                contentDescription = null,
                                                tint = GoldBright,
                                                modifier = Modifier.size(22.dp)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(14.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "تحديد موقعي التلقائي (GPS)",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = GoldBright
                                        )
                                        Text(
                                            text = "حساب فلكي دقيق حسب إحداثياتك في أي دولة بالعالم",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = TextSecondary
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "المدن والدول المتاحة",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextSecondary,
                                modifier = Modifier.padding(start = 4.dp, bottom = 6.dp)
                            )
                        }

                        // 2. Preset Cities List (Filtered)
                        val filteredCities = if (citySearchQuery.isBlank()) {
                            AthkarData.presetCities
                        } else {
                            AthkarData.presetCities.filter {
                                it.name.contains(citySearchQuery, ignoreCase = true) ||
                                it.country.contains(citySearchQuery, ignoreCase = true) ||
                                it.key.contains(citySearchQuery, ignoreCase = true)
                            }
                        }

                        if (filteredCities.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "لم يتم العثور على مدينة مطابقة. يمكنك استخدام GPS لتحديد موقعك تلقائياً.",
                                    color = TextSecondary,
                                    fontSize = 13.sp,
                                    textAlign = TextAlign.Center
                                )
                            }
                        } else {
                            filteredCities.forEach { city ->
                                val isSelected = uiState.selectedCityKey == city.key && uiState.customLocation == null

                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 5.dp)
                                        .clip(RoundedCornerShape(18.dp))
                                        .clickable {
                                            onCitySelected(city.key)
                                            coroutineScope.launch {
                                                sheetState.hide()
                                                showLocationPicker = false
                                            }
                                        },
                                    shape = RoundedCornerShape(18.dp),
                                    color = if (isSelected) GoldPrimary.copy(alpha = 0.22f) else CardSurfaceVariant.copy(alpha = 0.35f),
                                    border = if (isSelected) androidx.compose.foundation.BorderStroke(1.5.dp, GoldBright) else androidx.compose.foundation.BorderStroke(0.5.dp, CardBorder)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 16.dp, vertical = 12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = city.name,
                                                style = MaterialTheme.typography.titleMedium,
                                                color = if (isSelected) GoldBright else TextPrimary,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                textAlign = TextAlign.Start
                                            )
                                            if (city.country.isNotBlank()) {
                                                Text(
                                                    text = city.country,
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = GoldBright.copy(alpha = 0.8f),
                                                    fontSize = 12.sp
                                                )
                                            }
                                        }
                                        if (isSelected) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = "محدد",
                                                tint = GoldBright,
                                                modifier = Modifier.size(20.dp)
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
    }
}
