package rekab.app.background_locator

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import androidx.core.app.NotificationCompat
import io.flutter.view.FlutterNativeView
import rekab.app.background_locator.Keys.Companion.ARG_NOTIFICATION_MSG
import rekab.app.background_locator.Keys.Companion.ARG_NOTIFICATION_TITLE
import rekab.app.background_locator.Keys.Companion.CHANNEL_ID

class IsolateHolderService : Service() {
    companion object {
        @JvmStatic
        val ACTION_SHUTDOWN = "SHUTDOWN"
        @JvmStatic
        val ACTION_START = "START"
        @JvmStatic
        private val WAKELOCK_TAG = "IsolateHolderService::WAKE_LOCK"
        @JvmStatic
        private var backgroundFlutterView: FlutterNativeView? = null

        @JvmStatic
        fun setBackgroundFlutterView(view: FlutterNativeView?) {
            backgroundFlutterView = view
        }
    }

    var notificationTitle = "Start Location Tracking"
    var notificationMsg = "Track location in background"

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    private fun start() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Notification channel is available in Android O and up
            val channel = NotificationChannel(CHANNEL_ID, "Flutter Locator Plugin",
                    NotificationManager.IMPORTANCE_LOW)

            (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).createNotificationChannel(channel)
        }

        val imageId = resources.getIdentifier("ic_launcher", "mipmap", packageName)
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)

                .setContentTitle(notificationTitle)
                .setContentText(notificationMsg)
                .setSmallIcon(imageId)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .build()

        (getSystemService(Context.POWER_SERVICE) as PowerManager).run {
            newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, WAKELOCK_TAG).apply {
                setReferenceCounted(false)
                // Maximum wake lock time is set to 60 minute to prevent any excessive use of battery
                acquire(60 * 60 * 1000L /*60 minutes*/)
            }
        }

        // Starting Service as foreground with a notification prevent service from closing
        startForeground(1, notification)
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        if (intent.action == ACTION_SHUTDOWN) {
            (getSystemService(Context.POWER_SERVICE) as PowerManager).run {
                newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, WAKELOCK_TAG).apply {
                    if (isHeld) {
                        release()
                    }
                }
            }
            stopForeground(true)
            stopSelf()
        } else if (intent.action == ACTION_START) {
            notificationTitle = intent.getStringExtra(ARG_NOTIFICATION_TITLE)
            notificationMsg = intent.getStringExtra(ARG_NOTIFICATION_MSG)

            start()
        }
        return START_STICKY;
    }
}