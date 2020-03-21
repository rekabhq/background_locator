package rekab.app.background_locator

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import androidx.core.app.NotificationCompat
import io.flutter.view.FlutterNativeView
import rekab.app.background_locator.Keys.Companion.ARG_NOTIFICATION_ICON
import rekab.app.background_locator.Keys.Companion.ARG_NOTIFICATION_MSG
import rekab.app.background_locator.Keys.Companion.ARG_NOTIFICATION_TITLE
import rekab.app.background_locator.Keys.Companion.ARG_WAKE_LOCK_TIME
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
        var _backgroundFlutterView: FlutterNativeView? = null

        @JvmStatic
        fun setBackgroundFlutterView(view: FlutterNativeView?) {
            _backgroundFlutterView = view
        }

        @JvmStatic
        var isRunning = false
    }

    var notificationTitle = "Start Location Tracking"
    var notificationMsg = "Track location in background"
    var icon = 0
    var wakeLockTime = 60 * 60 * 1000L // 1 hour default wake lock time

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    private fun start() {
        if (isRunning) {
            return
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Notification channel is available in Android O and up
            val channel = NotificationChannel(CHANNEL_ID, "Flutter Locator Plugin",
                    NotificationManager.IMPORTANCE_LOW)

            (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
                    .createNotificationChannel(channel)
        }

        val intent = Intent(this, getMainActivityClass(this))
        val pendingIntent: PendingIntent = PendingIntent.getActivity(this, 1, intent, PendingIntent.FLAG_UPDATE_CURRENT)

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle(notificationTitle)
                .setContentText(notificationMsg)
                .setSmallIcon(icon)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .build()

        (getSystemService(Context.POWER_SERVICE) as PowerManager).run {
            newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, WAKELOCK_TAG).apply {
                setReferenceCounted(false)
                acquire(wakeLockTime)
            }
        }

        // Starting Service as foreground with a notification prevent service from closing
        startForeground(1, notification)

        isRunning = true
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
            isRunning = false
        } else if (intent.action == ACTION_START) {
            notificationTitle = intent.getStringExtra(ARG_NOTIFICATION_TITLE)
            notificationMsg = intent.getStringExtra(ARG_NOTIFICATION_MSG)
            val iconNameDefault = "ic_launcher"
            var iconName = intent.getStringExtra(ARG_NOTIFICATION_ICON)
            if (iconName == null || iconName.isEmpty()) {
                iconName = iconNameDefault
            }
            icon = resources.getIdentifier(iconName, "mipmap", packageName)
            wakeLockTime = intent.getIntExtra(ARG_WAKE_LOCK_TIME, 60) * 60 * 1000L
            start()
        }
        return START_STICKY;
    }

    private fun getMainActivityClass(context: Context): Class<*>? {
        val packageName = context.packageName
        val launchIntent = context.packageManager.getLaunchIntentForPackage(packageName)
        val className = launchIntent.component.className
        return try {
            Class.forName(className)
        } catch (e: ClassNotFoundException) {
            e.printStackTrace()
            null
        }
    }
}