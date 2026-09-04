package com.example.update

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.BuildConfig
import com.example.ui.theme.*
import kotlinx.coroutines.launch
import java.io.File

val UpdateIconVector: ImageVector = ImageVector.Builder(
    name = "UpdateIcon",
    defaultWidth = 24.dp,
    defaultHeight = 24.dp,
    viewportWidth = 24f,
    viewportHeight = 24f
).apply {
    addPath(
        fill = SolidColor(Color.White),
        pathData = addPathNodes("M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm1 14h-2v-4H8l4-4 4 4h-3v4z")
    )
}.build()

@Composable
fun UpdateCheckerEffect(
    owner: String = UpdateChecker.GITHUB_OWNER,
    repo: String = UpdateChecker.GITHUB_REPO,
    currentVersion: String = BuildConfig.VERSION_NAME
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var updateInfo by remember { mutableStateOf<UpdateInfo?>(null) }
    var showDialog by remember { mutableStateOf(false) }
    var downloadState by remember { mutableStateOf<DownloadState>(DownloadState.Idle) }

    LaunchedEffect(owner, repo, currentVersion) {
        val result = UpdateChecker.checkForUpdate(
            currentVersion = currentVersion,
            owner = owner,
            repo = repo
        )
        if (result != null) {
            updateInfo = result
            showDialog = true
        }
    }

    if (showDialog && updateInfo != null) {
        val info = updateInfo!!

        Dialog(onDismissRequest = {
            if (downloadState !is DownloadState.Downloading) {
                showDialog = false
            }
        }) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(26.dp))
                    .background(CardSurface)
                    .border(
                        1.5.dp,
                        Brush.verticalGradient(
                            listOf(AccentEmerald, GoldPrimary.copy(alpha = 0.4f), CardBorder)
                        ),
                        RoundedCornerShape(26.dp)
                    )
                    .padding(22.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(AccentEmerald.copy(alpha = 0.15f))
                            .border(1.dp, AccentEmeraldLight, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = UpdateIconVector,
                            contentDescription = "تحديث جديد",
                            tint = AccentEmeraldLight,
                            modifier = Modifier.size(30.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "يتوفر تحديث جديد! 🎉",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "الإصدار الجديد: v${info.newVersion} (الحالي: v$currentVersion)",
                        style = MaterialTheme.typography.bodyMedium,
                        color = GoldBright,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    val scrollState = rememberScrollState()
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 140.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(CardSurfaceVariant.copy(alpha = 0.7f))
                            .border(0.5.dp, CardBorder, RoundedCornerShape(16.dp))
                            .padding(14.dp)
                            .verticalScroll(scrollState)
                    ) {
                        Text(
                            text = "ملاحظات التحديث:",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = AccentEmeraldLight
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = info.releaseNotes,
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary,
                            lineHeight = 18.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    when (val state = downloadState) {
                        is DownloadState.Idle -> {
                            Button(
                                onClick = {
                                    if (info.downloadUrl.endsWith(".apk", ignoreCase = true)) {
                                        coroutineScope.launch {
                                            ApkInstaller.downloadApkFlow(context, info.downloadUrl).collect {
                                                downloadState = it
                                            }
                                        }
                                    } else {
                                        try {
                                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(info.downloadUrl))
                                            context.startActivity(intent)
                                            showDialog = false
                                        } catch (_: Exception) {}
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp),
                                shape = CircleShape,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = AccentEmerald,
                                    contentColor = Color.White
                                )
                            ) {
                                Text(
                                    text = "تنزيل وتثبيت الآن",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            OutlinedButton(
                                onClick = { showDialog = false },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(44.dp),
                                shape = CircleShape,
                                border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
                            ) {
                                Text(
                                    text = "لاحقاً",
                                    fontSize = 14.sp,
                                    color = TextMuted,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        is DownloadState.Downloading -> {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "جاري تنزيل التحديث... ${state.progress}%",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = AccentEmeraldLight,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                LinearProgressIndicator(
                                    progress = { state.progress / 100f },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(8.dp)
                                        .clip(CircleShape),
                                    color = AccentEmerald,
                                    trackColor = CardSurfaceVariant
                                )
                            }
                        }

                        is DownloadState.Downloaded -> {
                            Button(
                                onClick = {
                                    ApkInstaller.installApk(context, state.file)
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp),
                                shape = CircleShape,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = AccentEmerald,
                                    contentColor = Color.White
                                )
                            ) {
                                Text(
                                    text = "تثبيت الآن",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        is DownloadState.NeedPermission -> {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "يلزم السماح لتثبيت التطبيقات الخارجية من الإعدادات",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = GoldBright,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Button(
                                    onClick = {
                                        ApkInstaller.openInstallUnknownAppsSettings(context)
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                    .height(48.dp),
                                    shape = CircleShape,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = GoldPrimary,
                                        contentColor = Color.Black
                                    )
                                ) {
                                    Text(
                                        text = "الذهاب للإعدادات للسماح بالتثبيت",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                OutlinedButton(
                                    onClick = {
                                        if (ApkInstaller.canInstallPackages(context)) {
                                            ApkInstaller.installApk(context, state.file)
                                        }
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(42.dp),
                                    shape = CircleShape
                                ) {
                                    Text(text = "تمت إعطاء الصلاحية - تثبيت", fontSize = 13.sp, color = TextPrimary)
                                }
                            }
                        }

                        is DownloadState.Error -> {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = state.message,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFFFF6B6B),
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Button(
                                    onClick = {
                                        try {
                                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(info.downloadUrl))
                                            context.startActivity(intent)
                                            showDialog = false
                                        } catch (_: Exception) {}
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(44.dp),
                                    shape = CircleShape
                                ) {
                                    Text("فتح صفحة التنزيل في المتصفح")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
