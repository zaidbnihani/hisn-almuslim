package com.example.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.media.app.NotificationCompat as MediaNotificationCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.exoplayer.ExoPlayer
import android.net.wifi.WifiManager
import android.os.PowerManager
import com.example.MainActivity
import com.example.R

data class RadioStation(
    val id: String,
    val name: String,
    val subtitle: String,
    val streamUrl: String
)

object RadioStationsData {
    val stations = listOf(
        RadioStation("taraji", "إذاعة تلاوات خاشعة", "تلاوات قرآنية خاشعة ومختارة", "https://backup.qurango.net/radio/salma"),
        RadioStation("mishaari", "إذاعة الشيخ مشاري العفاسي", "المصحف المرتل", "https://backup.qurango.net/radio/mishary_alafasi"),
        RadioStation("abdulbasit", "إذاعة الشيخ عبد الباسط عبد الصمد", "المصحف المرتل بالتلاوات الخالدة", "https://backup.qurango.net/radio/abdulbasit_abdulsamad"),
        RadioStation("hussary", "إذاعة الشيخ محمود خليل الحصري", "المصحف المرتل", "https://backup.qurango.net/radio/mahmoud_khalil_alhussary"),
        RadioStation("minshawi", "إذاعة الشيخ محمد صديق المنشاوي", "المصحف المرتل", "https://backup.qurango.net/radio/mohammed_siddiq_alminshawi"),
        RadioStation("maher", "إذاعة الشيخ ماهر المعيقلي", "المصحف المرتل", "https://backup.qurango.net/radio/maher_al_muaiqly"),
        RadioStation("ghamdi", "إذاعة الشيخ سعد الغامدي", "المصحف المرتل", "https://backup.qurango.net/radio/saad_alghamdi"),
        RadioStation("ajmy", "إذاعة الشيخ أحمد بن علي العجمي", "المصحف المرتل", "https://backup.qurango.net/radio/ahmad_alajmy")
    )
}

class RadioForegroundService : Service() {

    private var exoPlayer: ExoPlayer? = null
    private var currentStationIndex = 0
    private var isPlaying = false
    private var wakeLock: PowerManager.WakeLock? = null
    private var wifiLock: WifiManager.WifiLock? = null

    companion object {
        const val ACTION_PLAY = "com.example.action.RADIO_PLAY"
        const val ACTION_PAUSE = "com.example.action.RADIO_PAUSE"
        const val ACTION_STOP = "com.example.action.RADIO_STOP"
        const val ACTION_NEXT = "com.example.action.RADIO_NEXT"
        const val ACTION_PREV = "com.example.action.RADIO_PREV"
        const val ACTION_SELECT = "com.example.action.RADIO_SELECT"

        const val EXTRA_STATION_INDEX = "extra_station_index"

        const val ACTION_RADIO_STATE_CHANGED = "com.example.action.RADIO_STATE_CHANGED"
        const val ACTION_RADIO_ERROR = "com.example.action.RADIO_ERROR"
        const val EXTRA_IS_PLAYING = "extra_is_playing"
        const val EXTRA_CURRENT_INDEX = "extra_current_index"
        const val EXTRA_IS_BUFFERING = "extra_is_buffering"
        const val EXTRA_ERROR_MESSAGE = "extra_error_message"

        private const val CHANNEL_ID = "quran_radio_channel"
        private const val NOTIFICATION_ID = 2002
        private const val MAX_RETRIES = 2
    }

    private var isBuffering = false
    private var retryCount = 0

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        try {
            val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
            wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "Athkar:RadioWakeLock")
            
            val wifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
            wifiLock = wifiManager.createWifiLock(WifiManager.WIFI_MODE_FULL_HIGH_PERF, "Athkar:RadioWifiLock")
        } catch (_: Exception) {}
        initPlayer()
        createNotificationChannel()
    }

    private fun acquireWakeWifiLocks() {
        try {
            if (wakeLock?.isHeld == false) wakeLock?.acquire()
            if (wifiLock?.isHeld == false) wifiLock?.acquire()
        } catch (_: Exception) {}
    }

    private fun releaseWakeWifiLocks() {
        try {
            if (wakeLock?.isHeld == true) wakeLock?.release()
            if (wifiLock?.isHeld == true) wifiLock?.release()
        } catch (_: Exception) {}
    }

    private fun initPlayer() {
        if (exoPlayer == null) {
            val audioAttributes = AudioAttributes.Builder()
                .setUsage(C.USAGE_MEDIA)
                .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
                .build()

            exoPlayer = ExoPlayer.Builder(this)
                .build().apply {
                setAudioAttributes(audioAttributes, true)
                setWakeMode(C.WAKE_MODE_NETWORK)
                addListener(object : Player.Listener {
                    override fun onIsPlayingChanged(playing: Boolean) {
                        this@RadioForegroundService.isPlaying = playing
                        if (playing) {
                            isBuffering = false
                        }
                        broadcastState()
                        updateNotification()
                        if (playing) {
                            acquireWakeWifiLocks()
                        } else {
                            releaseWakeWifiLocks()
                        }
                    }

                    override fun onPlaybackStateChanged(playbackState: Int) {
                        when (playbackState) {
                            Player.STATE_BUFFERING -> {
                                isBuffering = true
                                broadcastState()
                            }
                            Player.STATE_READY -> {
                                isBuffering = false
                                retryCount = 0
                                broadcastState()
                            }
                            Player.STATE_ENDED -> {
                                isBuffering = false
                                this@RadioForegroundService.isPlaying = false
                                broadcastState()
                                updateNotification()
                                releaseWakeWifiLocks()
                            }
                        }
                    }

                    override fun onPlayerError(error: androidx.media3.common.PlaybackException) {
                        android.util.Log.e("RadioService", "ExoPlayer Error (retry $retryCount/$MAX_RETRIES): ${error.message}", error)
                        if (retryCount < MAX_RETRIES) {
                            retryCount++
                            isBuffering = true
                            broadcastState()
                            exoPlayer?.let { player ->
                                player.prepare()
                                player.play()
                            }
                        } else {
                            retryCount = 0
                            isBuffering = false
                            this@RadioForegroundService.isPlaying = false
                            broadcastState()
                            updateNotification()
                            releaseWakeWifiLocks()
                            broadcastError("تعذر الاتصال ببث الإذاعة. يرجى التحقق من الاتصال بالإنترنت أو تجربة محطة أخرى.")
                        }
                    }
                })
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action
        val newIndex = intent?.getIntExtra(EXTRA_STATION_INDEX, currentStationIndex) ?: currentStationIndex

        updateNotification()

        when (action) {
            ACTION_PLAY -> {
                isPlaying = true
                broadcastState()
                updateNotification()
                if (newIndex != currentStationIndex || exoPlayer?.mediaItemCount == 0) {
                    playStation(newIndex)
                } else {
                    exoPlayer?.play()
                }
            }
            ACTION_PAUSE -> {
                isPlaying = false
                broadcastState()
                updateNotification()
                exoPlayer?.pause()
            }
            ACTION_STOP -> {
                stopRadioService()
                return START_NOT_STICKY
            }
            ACTION_NEXT -> {
                isPlaying = true
                val nextIdx = (currentStationIndex + 1) % RadioStationsData.stations.size
                playStation(nextIdx)
            }
            ACTION_PREV -> {
                isPlaying = true
                val prevIdx = if (currentStationIndex - 1 < 0) RadioStationsData.stations.size - 1 else currentStationIndex - 1
                playStation(prevIdx)
            }
            ACTION_SELECT -> {
                isPlaying = true
                playStation(newIndex)
            }
            else -> {
                if (intent != null && intent.hasExtra(EXTRA_STATION_INDEX)) {
                    isPlaying = true
                    playStation(newIndex)
                }
            }
        }

        updateNotification()
        broadcastState()
        return START_STICKY
    }

    private fun playStation(index: Int) {
        if (index in RadioStationsData.stations.indices) {
            currentStationIndex = index
            val station = RadioStationsData.stations[index]
            val mediaItem = MediaItem.fromUri(station.streamUrl)
            isPlaying = true
            isBuffering = true
            retryCount = 0
            broadcastState()
            updateNotification()
            exoPlayer?.apply {
                stop()
                clearMediaItems()
                setMediaItem(mediaItem)
                prepare()
                play()
            }
        }
    }

    private fun updateNotification() {
        val station = RadioStationsData.stations.getOrNull(currentStationIndex) ?: return

        val openAppIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra("target_screen", "quran_radio")
        }
        val openAppPendingIntent = PendingIntent.getActivity(
            this,
            0,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val playPauseAction = if (isPlaying) {
            val pauseIntent = Intent(this, RadioForegroundService::class.java).apply { action = ACTION_PAUSE }
            val pausePending = PendingIntent.getService(this, 1, pauseIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
            NotificationCompat.Action.Builder(android.R.drawable.ic_media_pause, "إيقاف مؤقت", pausePending).build()
        } else {
            val playIntent = Intent(this, RadioForegroundService::class.java).apply { action = ACTION_PLAY }
            val playPending = PendingIntent.getService(this, 2, playIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
            NotificationCompat.Action.Builder(android.R.drawable.ic_media_play, "تشغيل", playPending).build()
        }

        val nextIntent = Intent(this, RadioForegroundService::class.java).apply { action = ACTION_NEXT }
        val nextPending = PendingIntent.getService(this, 3, nextIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        val nextAction = NotificationCompat.Action.Builder(android.R.drawable.ic_media_next, "التالي", nextPending).build()

        val stopIntent = Intent(this, RadioForegroundService::class.java).apply { action = ACTION_STOP }
        val stopPending = PendingIntent.getService(this, 4, stopIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        val stopAction = NotificationCompat.Action.Builder(android.R.drawable.ic_menu_close_clear_cancel, "إغلاق", stopPending).build()

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setContentTitle(station.name)
            .setContentText(station.subtitle)
            .setOngoing(isPlaying)
            .setContentIntent(openAppPendingIntent)
            .addAction(playPauseAction)
            .addAction(nextAction)
            .addAction(stopAction)
            .setStyle(MediaNotificationCompat.MediaStyle().setShowActionsInCompactView(0, 1))
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()

        startForeground(NOTIFICATION_ID, notification)
    }

    private fun broadcastState() {
        val intent = Intent(ACTION_RADIO_STATE_CHANGED).apply {
            putExtra(EXTRA_IS_PLAYING, isPlaying)
            putExtra(EXTRA_CURRENT_INDEX, currentStationIndex)
            putExtra(EXTRA_IS_BUFFERING, isBuffering)
            setPackage(packageName)
        }
        sendBroadcast(intent)
    }

    private fun broadcastError(message: String) {
        val intent = Intent(ACTION_RADIO_ERROR).apply {
            putExtra(EXTRA_ERROR_MESSAGE, message)
            setPackage(packageName)
        }
        sendBroadcast(intent)
    }

    private fun stopRadioService() {
        exoPlayer?.stop()
        exoPlayer?.release()
        exoPlayer = null
        isPlaying = false
        broadcastState()
        releaseWakeWifiLocks()
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "إذاعة القرآن الكريم",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "تشغيل البث المباشر لإذاعة القرآن الكريم"
            }
            val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            nm.createNotificationChannel(channel)
        }
    }

    override fun onDestroy() {
        stopRadioService()
        super.onDestroy()
    }
}
