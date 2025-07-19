package io.githun.mucute.qwq.kolomitm.service

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import io.github.mucute.qwq.kolomitm.KoloMITM
import io.github.mucute.qwq.kolomitm.event.receiver.definitionReceiver
import io.github.mucute.qwq.kolomitm.event.receiver.echoCommandReceiver
import io.github.mucute.qwq.kolomitm.event.receiver.proxyPassReceiver
import io.github.mucute.qwq.kolomitm.event.receiver.transferCommandReceiver
import io.github.mucute.qwq.kolomitm.event.receiver.transferReceiver
import io.githun.mucute.qwq.kolomitm.R
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class KoloMITMService : Service() {

    private val coroutineScope =
        CoroutineScope(Dispatchers.IO + CoroutineName("KoloMITMCoroutine") + SupervisorJob())

    private var koloMITM: KoloMITM? = null

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
            .addAction(
                android.R.drawable.ic_menu_close_clear_cancel,
                getString(R.string.stop),
                createPendingIntent()
            )
            .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
            .build()
    }

    private fun createPendingIntent(): PendingIntent {
        return PendingIntent.getService(
            this,
            0,
            Intent(ACTION_STOP).also { it.`package` = packageName },
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
    }

    private fun startKoloMITM() {
        if (koloMITM != null) {
            return
        }

        coroutineScope.launch {
            koloMITM = KoloMITM().apply {
                account = null
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

        coroutineScope.launch {
            koloMITM?.let {
                it.serverChannel?.close()?.syncUninterruptibly()
                it.clientChannel?.close()?.syncUninterruptibly()
            }
            koloMITM = null
        }

        _activeFlow.value = false
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