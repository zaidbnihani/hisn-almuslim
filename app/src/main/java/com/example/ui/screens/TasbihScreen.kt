package com.example.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AthkarData
import com.example.ui.AthkarUiState
import com.example.ui.components.LanternLightSurround
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TasbihScreen(
    uiState: AthkarUiState,
    onIncrementTasbih: () -> Unit,
    onResetTasbih: () -> Unit,
    onSelectPhrase: (Int) -> Unit
) {
    var showPhraseSheet by remember { mutableStateOf(false) }
    var isPressed by remember { mutableStateOf(false) }

    val activePhrase = AthkarData.tasbihPhrases.getOrNull(uiState.activeTasbihIndex)
        ?: AthkarData.tasbihPhrases[0]

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.93f else 1.0f,
        animationSpec = tween(durationMillis = 100),
        label = "tasbihScale"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkCanvas)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header & Active Phrase - Pure Floating Text without Box or Frame
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showPhraseSheet = true }
                    .padding(vertical = 12.dp)
                    .testTag("phrase_selector")
            ) {
                Text(
                    text = "السبحة التفاعلية",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = activePhrase.text,
                    style = MaterialTheme.typography.headlineLarge,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = GoldBright,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "اضغط للتغيير  •  التالي: ${activePhrase.next}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = GoldPrimary,
                        textAlign = TextAlign.Center
                    )
                }
            }

            // Big Interactive Tasbih Circle (No Outer Border, keeping inner ring)
            Box(
                modifier = Modifier
                    .size(260.dp)
                    .scale(scale)
                    .clip(CircleShape)
                    .background(CardSurface)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        isPressed = true
                        onIncrementTasbih()
                        isPressed = false
                    }
                    .testTag("tasbih_counter_circle"),
                contentAlignment = Alignment.Center
            ) {
                // Dashed Islamic Ring Ornament
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawCircle(
                        color = GoldMuted,
                        radius = size.minDimension / 2 - 16.dp.toPx(),
                        style = Stroke(
                            width = 2.dp.toPx(),
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f), 0f)
                        )
                    )
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "${uiState.currentTasbihCount}",
                        fontSize = 64.sp,
                        fontWeight = FontWeight.Bold,
                        color = GoldBright
                    )
                    Text(
                        text = "/ ${activePhrase.target}",
                        style = MaterialTheme.typography.titleLarge,
                        color = TextSecondary
                    )
                }
            }

            // Stats Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = CardSurface,
                    border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder),
                    modifier = Modifier.weight(1f).padding(end = 8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "${uiState.tasbihTotal}",
                            style = MaterialTheme.typography.headlineMedium,
                            color = GoldBright
                        )
                        Text(
                            text = "إجمالي التسبيح",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = CardSurface,
                    border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder),
                    modifier = Modifier.weight(1f).padding(start = 8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "${uiState.tasbihSession}",
                            style = MaterialTheme.typography.headlineMedium,
                            color = GoldBright
                        )
                        Text(
                            text = "الجلسة الحالية",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
                    }
                }
            }

            // Reset Button
            Button(
                onClick = onResetTasbih,
                colors = ButtonDefaults.buttonColors(
                    containerColor = CardSurfaceVariant,
                    contentColor = TextPrimary
                ),
                shape = RoundedCornerShape(28.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("btn_reset_tasbih")
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "إعادة ضبط",
                    tint = GoldPrimary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "إعادة ضبط العداد",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(72.dp))
        }

        // Phrase Selector Bottom Sheet
        if (showPhraseSheet) {
            ModalBottomSheet(
                onDismissRequest = { showPhraseSheet = false },
                containerColor = CardSurface,
                scrimColor = Color.Black.copy(alpha = 0.6f)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "اختر الذكر النشط للسبحة",
                        style = MaterialTheme.typography.headlineMedium,
                        color = GoldBright,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    LazyColumn {
                        items(AthkarData.tasbihPhrases.size) { idx ->
                            val phrase = AthkarData.tasbihPhrases[idx]
                            val isSelected = uiState.activeTasbihIndex == idx

                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .clip(CircleShape)
                                    .clickable {
                                        onSelectPhrase(idx)
                                        showPhraseSheet = false
                                    },
                                shape = CircleShape,
                                color = if (isSelected) GoldPrimary.copy(alpha = 0.15f) else CardSurfaceVariant.copy(alpha = 0.4f),
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
                                            text = phrase.text,
                                            style = MaterialTheme.typography.titleLarge,
                                            color = TextPrimary
                                        )
                                        Text(
                                            text = "العدد المستهدف: ${phrase.target}",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = TextSecondary
                                        )
                                    }
                                    if (isSelected) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = "نشط",
                                            tint = GoldPrimary
                                        )
                                    }
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
