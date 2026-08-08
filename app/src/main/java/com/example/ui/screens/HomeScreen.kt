package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AthkarData
import com.example.data.DhikrItem
import com.example.ui.AthkarUiState
import com.example.ui.components.LanternLightSurround
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    uiState: AthkarUiState,
    onCategorySelected: (String) -> Unit,
    onDecrementDhikr: (String, String, Int) -> Unit
) {
    var showCategorySheet by remember { mutableStateOf(false) }

    val activeCategory = AthkarData.categories.find { it.id == uiState.activeCategory }
        ?: AthkarData.categories[0]

    val dhikrs = AthkarData.getDhikrsForCategory(activeCategory.id)
    val remainingMap = uiState.remainingCounts[activeCategory.id] ?: emptyMap()

    val completedCount = dhikrs.count { (remainingMap[it.id] ?: it.count) == 0 }
    val totalCount = dhikrs.size
    val isCategoryDone = totalCount > 0 && completedCount == totalCount

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
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(CardSurface)
                        .border(1.dp, CardBorder.copy(alpha = 0.5f), RoundedCornerShape(24.dp))
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "أذكار المسلم اليومية",
                        style = MaterialTheme.typography.headlineLarge,
                        color = GoldBright,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "صباح مبارك وطيب • نور وحصن دائم",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary,
                        textAlign = TextAlign.Center
                    )
                }
            }

            // Category Segmented Bar & Library Button
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .clip(CircleShape)
                            .background(CardSurfaceVariant)
                            .padding(4.dp)
                    ) {
                        listOf("morning" to "أذكار الصباح", "evening" to "أذكار المساء").forEach { (id, label) ->
                            val isActive = uiState.activeCategory == id
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(CircleShape)
                                    .background(if (isActive) AccentEmerald else Color.Transparent)
                                    .clickable { onCategorySelected(id) }
                                    .padding(vertical = 10.dp)
                                    .testTag("segment_$id"),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isActive) GoldBright else TextSecondary
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    IconButton(
                        onClick = { showCategorySheet = true },
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(CardSurfaceVariant)
                            .border(1.dp, CardBorder, CircleShape)
                            .testTag("btn_all_categories")
                    ) {
                        Icon(
                            imageVector = Icons.Default.List,
                            contentDescription = "جميع الأقسام",
                            tint = GoldBright
                        )
                    }
                }
            }

            // Category Title & Progress
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = activeCategory.name,
                        style = MaterialTheme.typography.titleLarge,
                        color = GoldPrimary
                    )

                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = CardSurfaceVariant,
                        border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
                    ) {
                        Text(
                            text = "$completedCount / $totalCount مكتمل",
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            style = MaterialTheme.typography.labelLarge,
                            color = GoldBright
                        )
                    }
                }
            }

            // Dhikr Cards List - Completed ones disappear immediately!
            val activeDhikrs = dhikrs.filter { dhikr ->
                val remaining = remainingMap[dhikr.id] ?: dhikr.count
                remaining > 0
            }

            items(activeDhikrs, key = { it.id }) { dhikr ->
                val remaining = remainingMap[dhikr.id] ?: dhikr.count
                DhikrCard(
                    dhikr = dhikr,
                    remaining = remaining,
                    onClick = {
                        onDecrementDhikr(activeCategory.id, dhikr.id, dhikr.count)
                    }
                )
            }

            // Category Completion Banner
            if (isCategoryDone) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 24.dp)
                            .clip(RoundedCornerShape(28.dp))
                            .background(AccentEmerald.copy(alpha = 0.3f))
                            .border(1.dp, AccentEmeraldLight, RoundedCornerShape(28.dp))
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(AccentEmerald)
                                .padding(12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "تم الإكمال",
                                tint = GoldBright,
                                modifier = Modifier.size(36.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "تقبل الله طاعتك",
                            style = MaterialTheme.typography.headlineMedium,
                            color = GoldBright,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "لقد أتممت قراءة أذكار هذا القسم بالكامل لليوم بنجاح، طاب يومك بذكر الله.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextPrimary,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }

        // Category Library Bottom Sheet
        if (showCategorySheet) {
            ModalBottomSheet(
                onDismissRequest = { showCategorySheet = false },
                containerColor = CardSurface,
                scrimColor = Color.Black.copy(alpha = 0.6f)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "أقسام الأذكار والأدعية",
                        style = MaterialTheme.typography.headlineMedium,
                        color = GoldBright,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    AthkarData.categories.forEach { cat ->
                        val catDhikrs = AthkarData.getDhikrsForCategory(cat.id)
                        val catRemaining = uiState.remainingCounts[cat.id] ?: emptyMap()
                        val catCompleted = catDhikrs.count { (catRemaining[it.id] ?: it.count) == 0 }

                        val isSelected = uiState.activeCategory == cat.id
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 5.dp)
                                .clip(CircleShape)
                                .clickable {
                                    onCategorySelected(cat.id)
                                    showCategorySheet = false
                                },
                            shape = CircleShape,
                            color = if (isSelected) GoldPrimary.copy(alpha = 0.15f) else CardSurfaceVariant.copy(alpha = 0.5f),
                            border = if (isSelected) androidx.compose.foundation.BorderStroke(1.5.dp, GoldBright) else androidx.compose.foundation.BorderStroke(0.5.dp, CardBorder)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = cat.name,
                                        style = MaterialTheme.typography.titleLarge,
                                        color = TextPrimary
                                    )
                                    Text(
                                        text = "${cat.desc} (تم إكمال $catCompleted / ${catDhikrs.size})",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = TextSecondary
                                    )
                                }
                                if (uiState.activeCategory == cat.id) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "نشط",
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

@Composable
fun DhikrCard(
    dhikr: DhikrItem,
    remaining: Int,
    onClick: () -> Unit
) {
    val isCompleted = remaining == 0

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable { onClick() }
            .testTag("dhikr_card_${dhikr.id}"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isCompleted) CardSurfaceVariant.copy(alpha = 0.8f) else CardSurface
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isCompleted) AccentEmerald.copy(alpha = 0.6f) else CardBorder
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Row: Title & Counter Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = dhikr.title,
                        style = MaterialTheme.typography.titleLarge,
                        color = if (isCompleted) TextMuted else GoldPrimary
                    )
                    if (isCompleted) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = AccentEmerald.copy(alpha = 0.2f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, AccentEmerald)
                        ) {
                            Text(
                                text = "مكتمل",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = AccentEmeraldLight
                            )
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(if (isCompleted) AccentEmerald.copy(alpha = 0.15f) else CardSurfaceVariant)
                        .border(
                            width = 1.5.dp,
                            color = if (isCompleted) AccentEmeraldLight else GoldPrimary,
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (isCompleted) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "تم",
                            tint = AccentEmeraldLight,
                            modifier = Modifier.size(18.dp)
                        )
                    } else {
                        Text(
                            text = "$remaining",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = GoldBright
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Main Dhikr Arabic Text
            Text(
                text = dhikr.text,
                style = MaterialTheme.typography.bodyLarge,
                color = if (isCompleted) TextSecondary else TextPrimary,
                lineHeight = 32.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Virtue / Note
            if (dhikr.note.isNotEmpty() || dhikr.source.isNotEmpty()) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = CardSurfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "۩ ",
                            color = GoldPrimary,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (dhikr.note.isNotEmpty()) dhikr.note else dhikr.source,
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
                    }
                }
            }
        }
    }
}
