package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.data.AthkarData
import com.example.ui.AthkarUiState
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    uiState: AthkarUiState,
    onToggleSound: () -> Unit,
    onToggleNotifications: () -> Unit,
    onMethodSelected: (String) -> Unit,
    onSetCustomLocation: (Double, Double, String) -> Unit,
    onResetAll: () -> Unit,
    onSelectMuezzin: (String) -> Unit = {}
) {
    val context = LocalContext.current
    var showResetDialog by remember { mutableStateOf(false) }
    var showMethodPicker by remember { mutableStateOf(false) }
    var showReciterPicker by remember { mutableStateOf(false) }
    var selectedReciter by remember { mutableStateOf("الشيخ مشاري بن راشد العفاسي") }
    var isPlayingAdhanTest by remember { mutableStateOf(false) }

    val currentMuezzin = remember(uiState.selectedMuezzinId) {
        AthkarData.muezzinsList.find { it.id == uiState.selectedMuezzinId } ?: AthkarData.muezzinsList[0]
    }

    var mediaPlayer by remember { mutableStateOf<android.media.MediaPlayer?>(null) }

    LaunchedEffect(isPlayingAdhanTest, currentMuezzin) {
        if (isPlayingAdhanTest) {
            mediaPlayer?.release()
            mediaPlayer = null
            try {
                val mp = android.media.MediaPlayer().apply {
                    setAudioAttributes(
                        android.media.AudioAttributes.Builder()
                            .setContentType(android.media.AudioAttributes.CONTENT_TYPE_MUSIC)
                            .setUsage(android.media.AudioAttributes.USAGE_MEDIA)
                            .build()
                    )
                    setDataSource(currentMuezzin.audioUrl)
                    setOnPreparedListener {
                        it.start()
                    }
                    setOnCompletionListener {
                        isPlayingAdhanTest = false
                    }
                    setOnErrorListener { _, _, _ ->
                        isPlayingAdhanTest = false
                        true
                    }
                    prepareAsync()
                }
                mediaPlayer = mp
            } catch (e: Exception) {
                isPlayingAdhanTest = false
            }
        } else {
            try {
                mediaPlayer?.stop()
            } catch (e: Exception) {}
            mediaPlayer?.release()
            mediaPlayer = null
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            try {
                mediaPlayer?.stop()
            } catch (e: Exception) {}
            mediaPlayer?.release()
            mediaPlayer = null
        }
    }

    var hasNotificationPermission by remember {
        mutableStateOf(
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            } else true
        )
    }

    val notifPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasNotificationPermission = isGranted
        if (isGranted) {
            onToggleNotifications()
        }
    }

    var fetchRealLocationLambda by remember { mutableStateOf<(() -> Unit)?>(null) }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            fetchRealLocationLambda?.invoke()
        } else {
            android.widget.Toast.makeText(context, "تم رفض صلاحية تحديد الموقع الجغرافي.", android.widget.Toast.LENGTH_SHORT).show()
        }
    }

    fun fetchRealLocation() {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            try {
                val fusedLocationClient = com.google.android.gms.location.LocationServices.getFusedLocationProviderClient(context)
                fusedLocationClient.lastLocation
                    .addOnSuccessListener { loc ->
                        if (loc != null) {
                            onSetCustomLocation(loc.latitude, loc.longitude, "موقعي الحالي (GPS)")
                            android.widget.Toast.makeText(context, "تم تحديد موقعك الجغرافي بنجاح", android.widget.Toast.LENGTH_SHORT).show()
                        } else {
                            val priority = com.google.android.gms.location.Priority.PRIORITY_HIGH_ACCURACY
                            fusedLocationClient.getCurrentLocation(priority, null)
                                .addOnSuccessListener { currentLoc ->
                                    if (currentLoc != null) {
                                        onSetCustomLocation(currentLoc.latitude, currentLoc.longitude, "موقعي الحالي (GPS)")
                                        android.widget.Toast.makeText(context, "تم تحديد موقعك الجغرافي بنجاح", android.widget.Toast.LENGTH_SHORT).show()
                                    } else {
                                        android.widget.Toast.makeText(context, "الموقع الحالي غير متاح حالياً. يرجى التأكد من تفعيل الـ GPS بالجهاز.", android.widget.Toast.LENGTH_LONG).show()
                                    }
                                }
                                .addOnFailureListener { err ->
                                    android.widget.Toast.makeText(context, "تعذر الحصول على الموقع: ${err.localizedMessage}", android.widget.Toast.LENGTH_LONG).show()
                                }
                        }
                    }
                    .addOnFailureListener { err ->
                        android.widget.Toast.makeText(context, "تعذر قراءة بيانات الموقع: ${err.localizedMessage}", android.widget.Toast.LENGTH_LONG).show()
                    }
            } catch (e: Exception) {
                android.widget.Toast.makeText(context, "حدث خطأ أثناء الاتصال بمزود الموقع: ${e.localizedMessage}", android.widget.Toast.LENGTH_LONG).show()
            }
        } else {
            locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    fetchRealLocationLambda = { fetchRealLocation() }

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
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
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
                                    text = "تذكيرات اليومية بأذكار الصباح والمساء",
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
                }
            }

            // GPS Location Setting
            item {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clip(RoundedCornerShape(22.dp))
                        .clickable { fetchRealLocation() }
                        .testTag("btn_gps_location"),
                    color = CardSurface,
                    border = androidx.compose.foundation.BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.4f))
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
                                contentDescription = "الموقع الحالي",
                                tint = GoldBright
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "تحديد موقعي الحالي عبر الـ GPS",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = TextPrimary
                                )
                                Text(
                                    text = uiState.customLocation?.let { "الموقع المحدد: ${it.name}" } ?: "انقر لتحديث إحداثياتك الفعليّة بدقة متناهية",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = GoldBright
                                )
                            }
                        }
                    }
                }
            }

            // Calculation Method Picker
            item {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clip(RoundedCornerShape(22.dp))
                        .clickable { showMethodPicker = true }
                        .testTag("btn_select_method"),
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
                                imageVector = Icons.Default.Settings,
                                contentDescription = "طريقة الحساب",
                                tint = GoldPrimary
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "طريقة حساب مواقيت الصلاة",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = TextPrimary
                                )
                                Text(
                                    text = AthkarData.calculationMethods[uiState.selectedMethodKey] ?: "أم القرى",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextSecondary
                                )
                            }
                        }
                    }
                }
            }

            // Select Reciter (اختيار القارئ)
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
                                imageVector = Icons.Default.Person,
                                contentDescription = "القارئ",
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

            // Adhan Background & Alarm Permissions (صلاحيات الأذان والعمل في الخلفية)
            item {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    shape = RoundedCornerShape(22.dp),
                    color = CardSurface,
                    border = androidx.compose.foundation.BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.4f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = "الصلاحيات",
                                tint = GoldBright
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "صلاحيات الأذان والعمل في الخلفية",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "لتشغيل صوت الأذان تلقائيًا عند دخول الوقت عبر التطبيقات والأدوات",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Button(
                                onClick = {
                                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                                        notifPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (hasNotificationPermission) AccentEmerald.copy(alpha = 0.2f) else GoldPrimary,
                                    contentColor = if (hasNotificationPermission) AccentEmeraldLight else DarkCanvas
                                ),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = if (hasNotificationPermission) "إذن الإشعارات مفعّل ✓" else "طلب إذن الإشعارات",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            OutlinedButton(
                                onClick = { isPlayingAdhanTest = !isPlayingAdhanTest },
                                shape = RoundedCornerShape(8.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, GoldBright),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Icon(
                                    imageVector = if (isPlayingAdhanTest) Icons.Default.Delete else Icons.Default.PlayArrow,
                                    contentDescription = "تجربة",
                                    tint = GoldBright,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (isPlayingAdhanTest) "إيقاف الأذان" else "تجربة صوت الأذان",
                                    fontSize = 12.sp,
                                    color = GoldBright
                                )
                            }
                        }
                    }
                }
            }



            // Reset Data
            item {
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = { showResetDialog = true },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF3D1E1E),
                        contentColor = Color(0xFFFF8A8A)
                    ),
                    shape = RoundedCornerShape(22.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("btn_reset_all")
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "إعادة تعيين",
                        tint = Color(0xFFFF8A8A)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "إعادة تعيين كافة البيانات والتقدم",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Version Info
            item {
                Spacer(modifier = Modifier.height(20.dp))
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
                        text = "الإصدار v2.0.0",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }

        // Reciters Picker Bottom Sheet
        if (showReciterPicker) {
            ModalBottomSheet(
                onDismissRequest = { showReciterPicker = false },
                containerColor = CardSurface,
                scrimColor = Color.Black.copy(alpha = 0.6f)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "مكتبة المؤذنين - اختر صوت الأذان",
                        style = MaterialTheme.typography.headlineMedium,
                        color = GoldBright,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    AthkarData.muezzinsList.forEach { muezzin ->
                        val isSelected = uiState.selectedMuezzinId == muezzin.id

                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clip(CircleShape)
                                .clickable {
                                    onSelectMuezzin(muezzin.id)
                                    showReciterPicker = false
                                },
                            shape = CircleShape,
                            color = if (isSelected) GoldPrimary.copy(alpha = 0.15f) else CardSurfaceVariant.copy(alpha = 0.4f),
                            border = if (isSelected) androidx.compose.foundation.BorderStroke(1.5.dp, GoldBright) else androidx.compose.foundation.BorderStroke(0.5.dp, CardBorder)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = muezzin.name,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = TextPrimary
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = muezzin.description,
                                        fontSize = 12.sp,
                                        color = TextSecondary
                                    )
                                }
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "محدد",
                                        tint = GoldBright
                                    )
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
        if (showMethodPicker) {
            ModalBottomSheet(
                onDismissRequest = { showMethodPicker = false },
                containerColor = CardSurface,
                scrimColor = Color.Black.copy(alpha = 0.6f)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "طريقة الحساب الرياضي",
                        style = MaterialTheme.typography.headlineMedium,
                        color = GoldBright,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    AthkarData.calculationMethods.forEach { (key, name) ->
                        val isSelected = uiState.selectedMethodKey == key

                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clip(CircleShape)
                                .clickable {
                                    onMethodSelected(key)
                                    showMethodPicker = false
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
                                Text(
                                    text = name,
                                    style = MaterialTheme.typography.titleLarge,
                                    color = TextPrimary,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }

        // Reset Confirmation Dialog
        if (showResetDialog) {
            AlertDialog(
                onDismissRequest = { showResetDialog = false },
                containerColor = CardSurface,
                title = {
                    Text(
                        text = "إعادة تعيين التطبيق",
                        style = MaterialTheme.typography.headlineMedium,
                        color = GoldBright
                    )
                },
                text = {
                    Text(
                        text = "هل أنت متأكد من رغبتك في إعادة تعيين كافة البيانات والتقدم المكتسب لجميع الأذكار والسبحة؟",
                        style = MaterialTheme.typography.bodyLarge,
                        color = TextPrimary
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            onResetAll()
                            showResetDialog = false
                        }
                    ) {
                        Text(
                            text = "نعم، إعادة تعيين",
                            color = Color(0xFFFF8A8A),
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showResetDialog = false }) {
                        Text(
                            text = "إلغاء",
                            color = TextSecondary
                        )
                    }
                }
            )
        }
    }
}
