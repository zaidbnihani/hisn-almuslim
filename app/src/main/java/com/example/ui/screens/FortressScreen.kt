package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
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
import com.example.data.FortressItem
import com.example.ui.AthkarUiState
import com.example.ui.theme.*

import com.example.ui.components.ShieldIcon

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FortressScreen(
    uiState: AthkarUiState,
    onQueryChanged: (String) -> Unit
) {
    var selectedCategory by remember { mutableStateOf("الكل") }
    var selectedItem by remember { mutableStateOf<FortressItem?>(null) }

    val categories = remember {
        listOf("الكل", "الهم والغم والشدة", "السفر والترحال", "الطعام والشراب", "المسجد والعبادة", "الصحة والرقية", "المنزل والمعاش", "الخلاء والوضوء", "الأحوال والطقس")
    }

    val filteredItems = remember(uiState.fortressQuery, selectedCategory) {
        val q = uiState.fortressQuery.trim().lowercase()
        AthkarData.fortressItems.filter { item ->
            val matchesCategory = (selectedCategory == "الكل" || item.category == selectedCategory)
            val matchesQuery = q.isEmpty() || (
                item.title.lowercase().contains(q) ||
                item.category.lowercase().contains(q) ||
                item.text.lowercase().contains(q)
            )
            matchesCategory && matchesQuery
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkCanvas)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            // Header
            Text(
                text = "حصن المسلم الجامع",
                style = MaterialTheme.typography.headlineLarge,
                color = GoldBright,
                modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
            )

            // Search Bar Input
            OutlinedTextField(
                value = uiState.fortressQuery,
                onValueChange = onQueryChanged,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .testTag("search_fortress"),
                placeholder = {
                    Text(
                        text = "ابحث عن دعاء، تصنيف، أو فضل ذكر...",
                        color = TextMuted,
                        fontSize = 14.sp
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "بحث",
                        tint = GoldPrimary
                    )
                },
                shape = RoundedCornerShape(24.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = CardSurface,
                    unfocusedContainerColor = CardSurface,
                    focusedBorderColor = GoldPrimary,
                    unfocusedBorderColor = CardBorder,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                ),
                singleLine = true
            )

            // Category Filter Chips
            androidx.compose.foundation.lazy.LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                items(categories.size) { index ->
                    val cat = categories[index]
                    val isSelected = (cat == selectedCategory)
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedCategory = cat },
                        label = {
                            Text(
                                text = cat,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) GoldBright else TextSecondary
                            )
                        },
                        shape = CircleShape,
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = CardSurface,
                            selectedContainerColor = AccentEmerald
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isSelected,
                            borderColor = CardBorder,
                            selectedBorderColor = AccentEmeraldLight,
                            borderWidth = 1.dp
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Grid of Fortress Cards
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 96.dp)
            ) {
                items(filteredItems) { item ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp)
                            .clickable { selectedItem = item }
                            .testTag("fortress_item_${item.id}"),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = CardSurface),
                        border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(12.dp),
                            verticalArrangement = Arrangement.SpaceBetween,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(CardSurfaceVariant),
                                contentAlignment = Alignment.Center
                            ) {
                                ShieldIcon(
                                    tint = GoldBright,
                                    modifier = Modifier.size(22.dp)
                                )
                            }

                            Text(
                                text = item.title,
                                style = MaterialTheme.typography.titleMedium,
                                color = TextPrimary,
                                textAlign = TextAlign.Center,
                                maxLines = 2
                            )

                            Text(
                                text = item.category,
                                style = MaterialTheme.typography.bodyMedium,
                                fontSize = 11.sp,
                                color = TextSecondary,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }

        // Dua Detail Sheet
        if (selectedItem != null) {
            val item = selectedItem!!
            ModalBottomSheet(
                onDismissRequest = { selectedItem = null },
                containerColor = CardSurface,
                scrimColor = Color.Black.copy(alpha = 0.6f)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.headlineMedium,
                        color = GoldBright,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = CardSurfaceVariant),
                        border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = item.text,
                                style = MaterialTheme.typography.bodyLarge,
                                color = TextPrimary,
                                textAlign = TextAlign.Center,
                                lineHeight = 32.sp
                            )

                            if (item.note.isNotEmpty() || item.source.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(16.dp))
                                Divider(color = CardBorder)
                                Spacer(modifier = Modifier.height(16.dp))

                                if (item.note.isNotEmpty()) {
                                    Text(
                                        text = item.note,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = TextSecondary,
                                        textAlign = TextAlign.Center
                                    )
                                }

                                if (item.source.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = item.source,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = GoldMuted,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}
