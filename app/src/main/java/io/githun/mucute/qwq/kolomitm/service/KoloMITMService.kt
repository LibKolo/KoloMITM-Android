package io.githun.mucute.qwq.kolomitm.service

import android.annotation.SuppressLint
import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.net.wifi.WifiManager
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import io.github.mucute.qwq.kolomitm.KoloMITM
import io.github.mucute.qwq.kolomitm.definition.Definitions
import io.github.mucute.qwq.kolomitm.event.receiver.definitionReceiver
import io.github.mucute.qwq.kolomitm.event.receiver.echoCommandReceiver
import io.github.mucute.qwq.kolomitm.event.receiver.proxyPassReceiver
import io.github.mucute.qwq.kolomitm.event.receiver.transferCommandReceiver
import io.github.mucute.qwq.kolomitm.event.receiver.transferReceiver
import io.githun.mucute.qwq.kolomitm.R
import io.githun.mucute.qwq.kolomitm.activity.MainActivity
import io.githun.mucute.qwq.kolomitm.manager.AccountManager
import io.githun.mucute.qwq.kolomitm.model.Account
import io.githun.mucute.qwq.kolomitm.util.BedrockAndroidAuth
import io.githun.mucute.qwq.kolomitm.util.BedrockIosAuth
import io.githun.mucute.qwq.kolomitm.util.BedrockNintendoAuth
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import net.raphimc.minecraftauth.MinecraftAuth

class KoloMITMService : Service() {

    private val coroutineScope =
        CoroutineScope(Dispatchers.IO + CoroutineName("KoloMITMCoroutine") + SupervisorJob())

    private var koloMITM: KoloMITM? = null

    private var wakeLock: PowerManager.WakeLock? = null

    private var wifiLock: WifiManager.WifiLock? = null

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        val notificationManagerCompat = NotificationManagerCompat.from(this)
        if (notificationManagerCompat.getNotificationChannelCompat(NOTIFICATION_CHANNEL_ID) == null) {
            notificationManagerCompat.createNotificationChannel(
                NotificationChannelCompat.Builder(
                    NOTIFICATION_CHANNEL_ID,
                    NotificationManagerCompat.IMPORTANCE_DEFAULT
                )
                    .setName(getString(R.string.app_name))
                    .build()
            )
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        stopKoloMITM()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {

            ACTION_START -> {
                startForeground(NOTIFICATION_ID, createNotification())
                startKoloMITM()
            }

            ACTION_STOP -> {
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
                stopKoloMITM()
            }

        }
        return START_NOT_STICKY
    }

    private fun createNotification(): Notification {
        return NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setContentTitle(getString(R.string.kolo_mitm))
            .setContentText(getString(R.string.kolo_mitm_running))
            .setSmallIcon(R.mipmap.ic_launcher)
            .setOngoing(true)
            .setContentIntent(createPendingIntent())
            .addAction(
                android.R.drawable.ic_menu_close_clear_cancel,
                getString(R.string.stop),
                createStopPendingIntent()
            )
            .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
            .build()
    }

    private fun createPendingIntent(): PendingIntent {
        return PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java).apply {
                addCategory(Intent.CATEGORY_LAUNCHER)
                action = Intent.ACTION_MAIN
            },
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
    }

    private fun createStopPendingIntent(): PendingIntent {
        return PendingIntent.getForegroundService(
            this,
            1,
            Intent(ACTION_STOP).also { it.`package` = packageName },
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
    }

    private fun startKoloMITM() {
        if (koloMITM != null) {
            return
        }

        acquireWakeLock()
        coroutineScope.launch {
            Definitions.loadBlockPalette()
            koloMITM = KoloMITM().apply {
                account = AccountManager.selectedAccount.value?.let {
                    if (it.session.isExpired) {
                        when (it.deviceType) {
                            Account.Android -> BedrockAndroidAuth
                            Account.iOS -> BedrockIosAuth
                            else -> BedrockNintendoAuth
                        }.refresh(MinecraftAuth.createHttpClient(), it.session)
                    } else {
                        it.session
                    }
                }
                koloSession.apply {
                    proxyPassReceiver()
                    definitionReceiver()
                    transferReceiver()
                    transferCommandReceiver()
                    echoCommandReceiver()
                }
                bootServer()
            }
        }

        _activeFlow.value = true
    }

    private fun stopKoloMITM() {
        if (koloMITM == null) {
            return
        }

        releaseWakeLock()
        coroutineScope.launch {
            koloMITM?.let {
                it.serverChannel?.close()?.syncUninterruptibly()
                it.clientChannel?.close()?.syncUninterruptibly()
            }
            koloMITM = null
        }

        _activeFlow.value = false
    }

    @Suppress("DEPRECATION")
    @SuppressLint("WakelockTimeout")
    private fun acquireWakeLock() {
        if (wakeLock != null) {
            return
        }

        val powerManager = getSystemService(POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK,
            "${packageName.lowercase()}:service-wakelock"
        ).also { it.acquire() }

        val wifiManager = getSystemService(WIFI_SERVICE) as WifiManager
        wifiLock = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            wifiManager.createWifiLock(
                WifiManager.WIFI_MODE_FULL_LOW_LATENCY,
                packageName.lowercase()
            )
        } else {
            wifiManager.createWifiLock(
                WifiManager.WIFI_MODE_FULL_HIGH_PERF,
                packageName.lowercase()
            )
        }.also { it.acquire() }
    }

    private fun releaseWakeLock() {
        if (wakeLock == null && wifiLock == null) {
            return
        }

        wakeLock?.release()
        wakeLock = null

        wifiLock?.release()
        wifiLock = null
    }

    companion object {

        private const val NOTIFICATION_CHANNEL_ID = "kolomitm_channel"

        private const val NOTIFICATION_ID = 100

        const val ACTION_START = "io.githun.mucute.qwq.kolomitm.service.KoloMITMService.start"

        const val ACTION_STOP = "io.githun.mucute.qwq.kolomitm.service.KoloMITMService.stop"

        private val _activeFlow = MutableStateFlow(false)

        var activeFlow = _activeFlow.asStateFlow()
            private set

    }

}