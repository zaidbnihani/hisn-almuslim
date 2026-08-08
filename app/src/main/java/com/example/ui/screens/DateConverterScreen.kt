package com.example.ui.screens

import android.app.DatePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.AthkarUiState
import com.example.ui.theme.*

@Composable
fun DateConverterScreen(
    uiState: AthkarUiState,
    onDateChanged: (Int, Int, Int) -> Unit
) {
    val context = LocalContext.current

    val datePickerDialog = remember {
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                onDateChanged(year, month + 1, dayOfMonth)
            },
            uiState.convertYear,
            uiState.convertMonth - 1,
            uiState.convertDay
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkCanvas)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "تحويل التاريخ",
                    style = MaterialTheme.typography.headlineLarge,
                    color = GoldBright,
                    modifier = Modifier.padding(top = 16.dp, bottom = 24.dp)
                )

                // Date Picker Input Card - Smooth, Soft Surround & Enlarged Date
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = CardSurface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.4f))
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "التاريخ الميلادي المحدد",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = { datePickerDialog.show() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = CardSurfaceVariant,
                                contentColor = GoldBright
                            ),
                            shape = RoundedCornerShape(20.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, GoldPrimary),
                            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("btn_pick_date")
                        ) {
                            Icon(
                                imageVector = Icons.Default.DateRange,
                                contentDescription = "اختيار التاريخ",
                                tint = GoldPrimary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = String.format("%04d / %02d / %02d", uiState.convertYear, uiState.convertMonth, uiState.convertDay),
                                fontSize = 24.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = GoldBright
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Convert Action Button - Gorgeous Gradient & Premium Styling
                Button(
                    onClick = { /* Conversion is automatic via state */ },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent
                    ),
                    contentPadding = PaddingValues(),
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .background(
                            brush = androidx.compose.ui.graphics.Brush.horizontalGradient(
                                colors = listOf(AccentEmerald, AccentEmeraldLight)
                            ),
                            shape = RoundedCornerShape(24.dp)
                        )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 20.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "تحويل",
                            tint = GoldBright,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "حساب وتحويل التاريخ الهجري",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = GoldBright
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Hijri Result Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = CardSurfaceVariant),
                    border = androidx.compose.foundation.BorderStroke(1.dp, GoldPrimary)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "التاريخ الهجري المقابل",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = uiState.convertedHijriDate,
                            style = MaterialTheme.typography.headlineLarge,
                            color = GoldBright,
                            textAlign = TextAlign.Center,
                            lineHeight = 36.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(72.dp))
        }
    }
}
