package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrayerTimesScreen(
    uiState: AthkarUiState,
    onCitySelected: (String) -> Unit,
    onMethodSelected: (String) -> Unit
) {
    var showCityPicker by remember { mutableStateOf(false) }
    val context = LocalContext.current

    data class PrayerRowData(
        val key: String,
        val name: String,
        val time: String,
        val icon: ImageVector
    )

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
                        .padding(vertical = 4.dp),
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

        // City Selector Bottom Sheet
        if (showCityPicker) {
            ModalBottomSheet(
                onDismissRequest = { showCityPicker = false },
                containerColor = CardSurface,
                scrimColor = androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.6f)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "اختر المدينة لحساب أوقات الصلاة",
                        style = MaterialTheme.typography.headlineMedium,
                        color = GoldBright,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    AthkarData.presetCities.forEach { city ->
                        val isSelected = uiState.selectedCityKey == city.key && uiState.customLocation == null

                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .clickable {
                                    onCitySelected(city.key)
                                    showCityPicker = false
                                },
                            color = if (isSelected) CardSurfaceVariant else androidx.compose.ui.graphics.Color.Transparent,
                            border = if (isSelected) androidx.compose.foundation.BorderStroke(1.dp, GoldPrimary) else null
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = city.name,
                                    style = MaterialTheme.typography.titleLarge,
                                    color = TextPrimary,
                                    modifier = Modifier.weight(1f)
                                )
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.LocationOn,
                                        contentDescription = "محدد",
                                        tint = GoldPrimary
                                    )
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}
