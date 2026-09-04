package com.example.ui.screens
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState

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
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
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
    onDecrementDhikr: (String, String, Int) -> Unit,
    onSendBroadcast: (String, String?, (Boolean, String) -> Unit) -> Unit = { _, _, _ -> }
) {
    var showCategorySheet by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    var secretTapCount by remember { mutableStateOf(0) }
    var lastTapTime by remember { mutableStateOf(0L) }
    var showSecretPinDialog by remember { mutableStateOf(false) }
    var showBroadcastDialog by remember { mutableStateOf(false) }
    var enteredPin by remember { mutableStateOf("") }
    var broadcastText by remember { mutableStateOf("") }
    var customFirebaseUrl by remember { mutableStateOf(com.example.data.FirebaseBroadcastManager.DEFAULT_FIREBASE_URL) }
    var isSendingBroadcast by remember { mutableStateOf(false) }
    var broadcastStatusMessage by remember { mutableStateOf<String?>(null) }

    val haptic = LocalHapticFeedback.current

    val activeCategory = AthkarData.categories.find { it.id == uiState.activeCategory }
        ?: AthkarData.categories[0]

    val dhikrs = remember(activeCategory.id) { AthkarData.getDhikrsForCategory(activeCategory.id) }
    val remainingMap = uiState.remainingCounts[activeCategory.id] ?: emptyMap()

    val completedCount = remember(dhikrs, remainingMap) {
        dhikrs.count { (remainingMap[it.id] ?: it.count) == 0 }
    }
    val totalCount = dhikrs.size
    val isCategoryDone = totalCount > 0 && completedCount == totalCount

    val activeDhikrs = remember(dhikrs, remainingMap) {
        dhikrs.filter { dhikr ->
            val remaining = remainingMap[dhikr.id] ?: dhikr.count
            remaining > 0
        }
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
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(CardSurface)
                        .border(1.dp, CardBorder.copy(alpha = 0.5f), RoundedCornerShape(24.dp))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            val now = System.currentTimeMillis()
                            if (now - lastTapTime > 3500) {
                                secretTapCount = 1
                            } else {
                                secretTapCount++
                            }
                            lastTapTime = now
                            if (secretTapCount >= 7) {
                                secretTapCount = 0
                                enteredPin = ""
                                showSecretPinDialog = true
                            }
                        }
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
                                    .clickable {
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        onCategorySelected(id)
                                    }
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
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            showCategorySheet = true
                        },
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
            val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
            ModalBottomSheet(
                onDismissRequest = { showCategorySheet = false },
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
                    Text(
                        text = "أقسام الأذكار والأدعية",
                        style = MaterialTheme.typography.titleMedium,
                        color = GoldBright,
                        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Bold
                    )

                    HorizontalDivider(color = CardBorder.copy(alpha = 0.5f), thickness = 1.dp)
                    Spacer(modifier = Modifier.height(14.dp))

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f, fill = false)
                            .verticalScroll(rememberScrollState())
                    ) {
                        AthkarData.categories.forEach { cat ->
                            val catDhikrs = AthkarData.getDhikrsForCategory(cat.id)
                            val catRemaining = uiState.remainingCounts[cat.id] ?: emptyMap()
                            val catCompleted = catDhikrs.count { (catRemaining[it.id] ?: it.count) == 0 }

                            val isSelected = uiState.activeCategory == cat.id
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 5.dp)
                                    .clip(RoundedCornerShape(18.dp))
                                    .clickable {
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        coroutineScope.launch {
                                            sheetState.hide()
                                            onCategorySelected(cat.id)
                                            showCategorySheet = false
                                        }
                                    },
                                shape = RoundedCornerShape(18.dp),
                                color = if (isSelected) GoldPrimary.copy(alpha = 0.15f) else CardSurfaceVariant.copy(alpha = 0.5f),
                                border = if (isSelected) androidx.compose.foundation.BorderStroke(1.5.dp, GoldBright) else androidx.compose.foundation.BorderStroke(0.5.dp, CardBorder)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = cat.name,
                                            style = MaterialTheme.typography.titleMedium,
                                            color = if (isSelected) GoldBright else TextPrimary,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                        )
                                    }
                                    if (isSelected) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = "نشط",
                                            tint = GoldBright
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Secret PIN Verification Dialog
        if (showSecretPinDialog) {
            AlertDialog(
                onDismissRequest = {
                    showSecretPinDialog = false
                    enteredPin = ""
                },
                containerColor = CardSurface,
                title = {
                    Text(
                        text = "رمز التحقق",
                        color = GoldBright,
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    Column {
                        Text(
                            text = "يرجى كتابة رمز الدخول:",
                            color = TextPrimary,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(
                            value = enteredPin,
                            onValueChange = { enteredPin = it },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                            placeholder = { Text("أدخل الرمز هنا", color = TextSecondary) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = GoldBright,
                                unfocusedBorderColor = CardBorder,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            ),
                            shape = RoundedCornerShape(14.dp)
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (enteredPin.trim() == "321465") {
                                showSecretPinDialog = false
                                enteredPin = ""
                                broadcastStatusMessage = null
                                showBroadcastDialog = true
                            } else {
                                // Close dialog on wrong pin
                                showSecretPinDialog = false
                                enteredPin = ""
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = GoldPrimary,
                            contentColor = Color.Black
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("تأكيد", fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            showSecretPinDialog = false
                            enteredPin = ""
                        }
                    ) {
                        Text("إلغاء", color = TextSecondary)
                    }
                }
            )
        }

        // Secret Broadcast Message Dialog
        if (showBroadcastDialog) {
            AlertDialog(
                onDismissRequest = {
                    if (!isSendingBroadcast) {
                        showBroadcastDialog = false
                        broadcastStatusMessage = null
                    }
                },
                containerColor = CardSurface,
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Campaign,
                            contentDescription = null,
                            tint = GoldBright,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "إرسال رسالة لجميع المستخدمين",
                            color = GoldBright,
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp
                        )
                    }
                },
                text = {
                    Column {
                        Text(
                            text = "اكتب النص الذي ترغب في إرساله وسيتم نشره عبر Firebase Realtime Database ليظهر فوراً لجميع مستخدمي التطبيق:",
                            color = TextPrimary,
                            fontSize = 13.sp,
                            lineHeight = 19.sp
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(
                            value = broadcastText,
                            onValueChange = { broadcastText = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 110.dp),
                            minLines = 3,
                            maxLines = 6,
                            placeholder = { Text("اكتب نص الرسالة هنا...", color = TextSecondary.copy(alpha = 0.7f)) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = GoldBright,
                                unfocusedBorderColor = CardBorder,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            ),
                            shape = RoundedCornerShape(14.dp)
                        )

                        if (broadcastStatusMessage != null) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = broadcastStatusMessage ?: "",
                                color = if (broadcastStatusMessage!!.contains("نجاح")) AccentEmerald else Color(0xFFFF6B6B),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (broadcastText.isNotBlank()) {
                                isSendingBroadcast = true
                                broadcastStatusMessage = "جارٍ الإرسال إلى Firebase Realtime Database..."
                                val url = customFirebaseUrl.ifBlank { null }
                                onSendBroadcast(broadcastText.trim(), url) { success, msg ->
                                    isSendingBroadcast = false
                                    broadcastStatusMessage = msg
                                    if (success) {
                                        coroutineScope.launch {
                                            delay(1400)
                                            showBroadcastDialog = false
                                            broadcastText = ""
                                            broadcastStatusMessage = null
                                        }
                                    }
                                }
                            }
                        },
                        enabled = broadcastText.isNotBlank() && !isSendingBroadcast,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = GoldPrimary,
                            contentColor = Color.Black
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        if (isSendingBroadcast) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                color = Color.Black,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                        }
                        Text("إرسال", fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            if (!isSendingBroadcast) {
                                showBroadcastDialog = false
                                broadcastStatusMessage = null
                            }
                        }
                    ) {
                        Text("إلغاء", color = TextSecondary)
                    }
                }
            )
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
    val haptic = LocalHapticFeedback.current

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onClick()
            }
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
