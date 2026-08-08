package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.addPathNodes
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

val ShieldIconVector: ImageVector = ImageVector.Builder(
    name = "ShieldIcon",
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

val OpenLinkIconVector: ImageVector = ImageVector.Builder(
    name = "OpenLinkIcon",
    defaultWidth = 24.dp,
    defaultHeight = 24.dp,
    viewportWidth = 24f,
    viewportHeight = 24f
).apply {
    addPath(
        fill = SolidColor(Color.White),
        pathData = addPathNodes("M19 19H5V5h7V3H5c-1.11 0-2 .9-2 2v14c0 1.1.89 2 2 2h14c1.1 0 2-.9 2-2v-7h-2v7zM14 3v2h3.59l-9.83 9.83 1.41 1.41L19 6.41V10h2V3h-7z")
    )
}.build()

@Composable
fun PrivacyConsentScreen(
    versionString: String,
    policyUrl: String,
    onAccept: () -> Unit,
    onDecline: () -> Unit
) {
    val context = LocalContext.current

    // Intercept Back Press - prevents bypassing the privacy gate
    BackHandler(enabled = true) {
        onDecline()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.85f))
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(20.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(26.dp))
                .background(CardSurface)
                .border(
                    1.5.dp,
                    Brush.verticalGradient(
                        listOf(AccentEmerald, GoldPrimary.copy(alpha = 0.3f), CardBorder)
                    ),
                    RoundedCornerShape(26.dp)
                )
                .padding(22.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Small Header Title: سياسة الخصوصية
            Text(
                text = "سياسة الخصوصية",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = AccentEmeraldLight,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "الموافقة على الشروط وأحكام الاستخدام",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "إصدار $versionString",
                fontSize = 11.sp,
                color = TextSecondary
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Scrollable Summary Box
            val scrollState = rememberScrollState()
            Column(
                modifier = Modifier
                    .weight(1f, fill = false)
                    .verticalScroll(scrollState)
                    .clip(RoundedCornerShape(16.dp))
                    .background(CardSurfaceVariant.copy(alpha = 0.6f))
                    .border(0.5.dp, CardBorder, RoundedCornerShape(16.dp))
                    .padding(14.dp)
            ) {
                PrivacyPointItem(
                    icon = Icons.Default.Lock,
                    title = "حفظ البيانات والخصوصية",
                    desc = "جميع أذكارك وإنجازاتك تُحفظ محلياً على جهازك فقط لضمان الخصوصية والسرعة."
                )

                Spacer(modifier = Modifier.height(10.dp))

                PrivacyPointItem(
                    icon = Icons.Default.Info,
                    title = "مواقيت الصلاة والموقع",
                    desc = "يُستخدم موقعك الجغرافي لحساب أوقات الصلاة والقبلة بدقة متناهية دون مشاركته مع أي طرف خارجي."
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Link at the bottom: اقرأها كاملاً
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(CircleShape)
                        .clickable {
                            try {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(policyUrl))
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                // Ignore if browser unavailable
                            }
                        }
                        .testTag("btn_read_full_privacy"),
                    color = AccentEmerald.copy(alpha = 0.12f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, AccentEmeraldLight)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = OpenLinkIconVector,
                            contentDescription = null,
                            tint = AccentEmeraldLight,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "اقرأها كاملاً",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = AccentEmeraldLight
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Action Buttons: Green Accept & Decline
            Button(
                onClick = onAccept,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("btn_accept_privacy"),
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = AccentEmerald,
                    contentColor = Color.White
                )
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = Color.White
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "موافق",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedButton(
                onClick = onDecline,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(46.dp)
                    .testTag("btn_decline_privacy"),
                shape = CircleShape,
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFF6B6B)),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Color(0xFFFF6B6B)
                )
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.ExitToApp,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "لا (إغلاق التطبيق)",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun PrivacyPointItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    desc: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(AccentEmerald.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = AccentEmeraldLight,
                modifier = Modifier.size(18.dp)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = desc,
                fontSize = 12.sp,
                color = TextSecondary,
                lineHeight = 18.sp
            )
        }
    }
}
