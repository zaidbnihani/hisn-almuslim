package com.example.ui.screens
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import kotlinx.coroutines.launch
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Check

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.ui.graphics.Color
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AthkarData
import com.example.ui.AthkarUiState
import com.example.ui.components.LanternLightSurround
import com.example.ui.components.PrayerTimerDisplay
import com.example.ui.theme.*

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.core.content.ContextCompat

data class PrayerRowData(
    val key: String,
    val name: String,
    val time: String,
    val icon: ImageVector
)

@OptIn(ExperimentalMaterial3Api::class, androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun PrayerTimesScreen(
    uiState: AthkarUiState,
    onMethodSelected: (String) -> Unit,
    onToggleMute: (String) -> Unit = {}
) {
    var showMuteDialog by remember { mutableStateOf(false) }
    var selectedPrayerToToggle by remember { mutableStateOf<PrayerRowData?>(null) }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val prayerList = listOf(
        PrayerRowData("fajr", "الفجر", uiState.prayerTimes.fajr, Icons.Default.Star),
        PrayerRowData("sunrise", "الشروق", uiState.prayerTimes.sunrise, Icons.Default.Info),
        PrayerRowData("dhuhr", "الظهر", uiState.prayerTimes.dhuhr, Icons.Default.DateRange),
        PrayerRowData("asr", "العصر", uiState.prayerTimes.asr, Icons.Default.DateRange),
        PrayerRowData("maghrib", "المغرب", uiState.prayerTimes.maghrib, Icons.Default.Star),
        PrayerRowData("isha", "العشاء", uiState.prayerTimes.isha, Icons.Default.Star)
    )

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
                    text = "مواقيت الصلاة والقرآن",
                    style = MaterialTheme.typography.headlineLarge,
                    color = GoldBright,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            // Battery Optimization Warning
            if (uiState.isBatteryOptimized) {
                item {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp)
                            .clip(RoundedCornerShape(16.dp)),
                        color = Color(0x22FF5252),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x66FF5252))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = "تحذير",
                                tint = Color(0xFFFF5252),
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "تحسين البطارية مفعل",
                                    color = Color(0xFFFF5252),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = "قد يتسبب ذلك في تأخير الأذان. يرجى إيقافه.",
                                    color = TextPrimary,
                                    fontSize = 12.sp,
                                    modifier = Modifier.padding(top = 2.dp)
                                )
                            }
                            Button(
                                onClick = {
                                    try {
                                        val intent = android.content.Intent(android.provider.Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
                                        context.startActivity(intent)
                                    } catch (e: Exception) {}
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF5252)),
                                shape = RoundedCornerShape(12.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                modifier = Modifier.height(36.dp)
                            ) {
                                Text("إصلاح", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // Current Location Selector Card
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
                        .padding(vertical = 6.dp)
                        .clip(RoundedCornerShape(20.dp)),
                    color = CardSurfaceVariant.copy(alpha = 0.5f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 18.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = CircleShape,
                                color = GoldPrimary.copy(alpha = 0.15f),
                                modifier = Modifier.size(38.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.LocationOn,
                                        contentDescription = null,
                                        tint = GoldBright,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "موقع حساب المواقيت الحالي",
                                    fontSize = 11.sp,
                                    color = TextSecondary
                                )
                                Text(
                                    text = currentLocName,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = GoldBright,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            // Adhan Stopwatch Display - Styled Box with Hours first then Minutes
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    shape = RoundedCornerShape(28.dp),
                    color = CardSurface,
                    border = androidx.compose.foundation.BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.5f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "موعد صلاة ${uiState.nextPrayerName}",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = GoldBright
                        )
                        Spacer(modifier = Modifier.height(14.dp))

                        // Explicit LTR for Digital Clock: [Hours] : [Minutes] : [Seconds]
                        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                // Hours Inner Frame
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Box(
                                        modifier = Modifier
                                            .size(width = 62.dp, height = 58.dp)
                                            .clip(RoundedCornerShape(20.dp))
                                            .background(CardSurfaceVariant)
                                            .border(1.dp, CardBorder, RoundedCornerShape(20.dp)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = String.format("%02d", uiState.countdownHours),
                                            fontSize = 24.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = GoldBright
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "ساعة",
                                        fontSize = 11.sp,
                                        color = TextSecondary
                                    )
                                }

                                Text(
                                    text = ":",
                                    fontSize = 26.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = GoldPrimary,
                                    modifier = Modifier.padding(horizontal = 6.dp)
                                )

                                // Minutes Inner Frame
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Box(
                                        modifier = Modifier
                                            .size(width = 62.dp, height = 58.dp)
                                            .clip(RoundedCornerShape(20.dp))
                                            .background(CardSurfaceVariant)
                                            .border(1.dp, CardBorder, RoundedCornerShape(20.dp)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = String.format("%02d", uiState.countdownMinutes),
                                            fontSize = 24.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = GoldBright
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "دقيقة",
                                        fontSize = 11.sp,
                                        color = TextSecondary
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Total Duration & Current Time Info
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "الوقت الحالي: ${java.text.SimpleDateFormat("hh:mm a", java.util.Locale("ar")).format(java.util.Date())}",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                            Text(
                                text = "  •  ",
                                color = GoldPrimary
                            )
                            Text(
                                text = "المدة المتبقية بدقة",
                                style = MaterialTheme.typography.bodySmall,
                                color = GoldPrimary
                            )
                        }
                    }
                }
            }

            // Prayer List
            items(prayerList) { prayer ->
                val isNext = prayer.key == uiState.nextPrayerKey

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .combinedClickable(
                            onClick = {},
                            onLongClick = {
                                selectedPrayerToToggle = prayer
                                showMuteDialog = true
                            }
                        ),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isNext) CardSurfaceVariant else CardSurface
                    ),
                    border = if (isNext) androidx.compose.foundation.BorderStroke(1.dp, GoldPrimary) else androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
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
                                imageVector = prayer.icon,
                                contentDescription = prayer.name,
                                tint = if (isNext) GoldBright else TextSecondary
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = prayer.name,
                                style = MaterialTheme.typography.titleMedium,
                                color = if (isNext) GoldBright else TextPrimary
                            )
                            if (uiState.mutedPrayers.contains(prayer.key)) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color(0x33FF8A8A),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFF8A8A))
                                ) {
                                    Text(
                                        text = "مكتوم",
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFFF8A8A)
                                    )
                                }
                            }
                            if (isNext) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = GoldPrimary
                                ) {
                                    Text(
                                        text = "التالية",
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = DarkCanvas
                                    )
                                }
                            }
                        }

                        Text(
                            text = prayer.time,
                            style = MaterialTheme.typography.titleLarge,
                            color = if (isNext) GoldBright else TextPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Mute/Unmute Confirmation Dialog
        val localPrayer = selectedPrayerToToggle
        if (showMuteDialog && localPrayer != null) {
            val isMuted = uiState.mutedPrayers.contains(localPrayer.key)

            AlertDialog(
                onDismissRequest = { showMuteDialog = false },
                containerColor = CardSurface,
                title = {
                    Text(
                        text = if (isMuted) "تفعيل صوت الأذان" else "إيقاف صوت الأذان",
                        style = MaterialTheme.typography.titleLarge,
                        color = GoldBright
                    )
                },
                text = {
                    Text(
                        text = if (isMuted) {
                            "هل تريد إعادة تفعيل صوت الأذان لصلاة ${localPrayer.name}؟"
                        } else {
                            "هل تريد إيقاف وكتم صوت الأذان لصلاة ${localPrayer.name}؟"
                        },
                        color = TextPrimary
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            onToggleMute(localPrayer.key)
                            showMuteDialog = false
                            selectedPrayerToToggle = null
                        }
                    ) {
                        Text(
                            text = if (isMuted) "تفعيل الصوت" else "إيقاف الصوت",
                            color = GoldPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showMuteDialog = false }) {
                        Text("إلغاء", color = TextSecondary)
                    }
                }
            )
        }
    }
}
