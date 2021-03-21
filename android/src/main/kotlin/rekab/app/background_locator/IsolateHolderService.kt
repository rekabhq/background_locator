package rekab.app.background_locator

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import androidx.core.app.NotificationCompat
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import java.util.concurrent.atomic.AtomicBoolean

class IsolateHolderService : MethodChannel.MethodCallHandler, Service() {
    companion object {
        @JvmStatic
        val ACTION_SHUTDOWN = "SHUTDOWN"

        @JvmStatic
        val ACTION_START = "START"

        @JvmStatic
        val ACTION_UPDATE_NOTIFICATION = "UPDATE_NOTIFICATION"

        @JvmStatic
        private val WAKELOCK_TAG = "IsolateHolderService::WAKE_LOCK"

        @JvmStatic
        var backgroundEngine: FlutterEngine? = null

        @JvmStatic
        var isRunning = false

        @JvmStatic
        internal val serviceStarted = AtomicBoolean(false)
    }

    private var notificationChannelName = "Flutter Locator Plugin"
    private var notificationTitle = "Start Location Tracking"
    private var notificationMsg = "Track location in background"
    private var notificationBigMsg = "Background location is on to keep the app up-tp-date with your location. This is required for main features to work properly when the app is not running."
    private var notificationIconColor = 0
    private var icon = 0
    private var wakeLockTime = 60 * 60 * 1000L // 1 hour default wake lock time
    private val notificationId = 1
    internal lateinit var backgroundChannel: MethodChannel
    internal lateinit var context: Context

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        startLocatorService(this)
    }

    private fun start() {
        if (isRunning) {
            return
        }

        (getSystemService(Context.POWER_SERVICE) as PowerManager).run {
            newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, WAKELOCK_TAG).apply {
                setReferenceCounted(false)
                acquire(wakeLockTime)
            }
        }

        // Starting Service as foreground with a notification prevent service from closing
        val notification = getNotification()
        startForeground(notificationId, notification)

        isRunning = true
    }

    private fun getNotification(): Notification {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Notification channel is available in Android O and up
            val channel = NotificationChannel(Keys.CHANNEL_ID, notificationChannelName,
                    NotificationManager.IMPORTANCE_LOW)

            (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
                    .createNotificationChannel(channel)
        }

        val intent = Intent(this, getMainActivityClass(this))
        intent.action = Keys.NOTIFICATION_ACTION

        val pendingIntent: PendingIntent = PendingIntent.getActivity(this,
                1, intent, PendingIntent.FLAG_UPDATE_CURRENT)

        return NotificationCompat.Builder(this, Keys.CHANNEL_ID)
                .setContentTitle(notificationTitle)
                .setContentText(notificationMsg)
                .setStyle(NotificationCompat.BigTextStyle()
                        .bigText(notificationBigMsg))
                .setSmallIcon(icon)
                .setColor(notificationIconColor)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setOnlyAlertOnce(true) // so when data is updated don't make sound and alert in android 8.0+
                .setOngoing(true)
                .build()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent == null) {
            return super.onStartCommand(intent, flags, startId)
        }

        when {
            ACTION_SHUTDOWN == intent.action -> {
                shutdownHolderService()
            }
            ACTION_START == intent.action -> {
                startHolderService(intent)
            }
            ACTION_UPDATE_NOTIFICATION == intent.action -> {
                if (isRunning) {
                    updateNotification(intent)
                }
            }
        }

        return START_STICKY
    }

    private fun startHolderService(intent: Intent) {
        notificationChannelName = intent.getStringExtra(Keys.SETTINGS_ANDROID_NOTIFICATION_CHANNEL_NAME).toString()
        notificationTitle = intent.getStringExtra(Keys.SETTINGS_ANDROID_NOTIFICATION_TITLE).toString()
        notificationMsg = intent.getStringExtra(Keys.SETTINGS_ANDROID_NOTIFICATION_MSG).toString()
        notificationBigMsg = intent.getStringExtra(Keys.SETTINGS_ANDROID_NOTIFICATION_BIG_MSG).toString()
        val iconNameDefault = "ic_launcher"
        var iconName = intent.getStringExtra(Keys.SETTINGS_ANDROID_NOTIFICATION_ICON)
        if (iconName == null || iconName.isEmpty()) {
            iconName = iconNameDefault
        }
        icon = resources.getIdentifier(iconName, "mipmap", packageName)
        notificationIconColor = intent.getLongExtra(Keys.SETTINGS_ANDROID_NOTIFICATION_ICON_COLOR, 0).toInt()
        wakeLockTime = intent.getIntExtra(Keys.SETTINGS_ANDROID_WAKE_LOCK_TIME, 60) * 60 * 1000L
        start()
    }

    private fun shutdownHolderService() {
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
    }

    private fun updateNotification(intent: Intent) {
        if (intent.hasExtra(Keys.SETTINGS_ANDROID_NOTIFICATION_TITLE)) {
            notificationTitle = intent.getStringExtra(Keys.SETTINGS_ANDROID_NOTIFICATION_TITLE).toString()
        }

        if (intent.hasExtra(Keys.SETTINGS_ANDROID_NOTIFICATION_MSG)) {
            notificationMsg = intent.getStringExtra(Keys.SETTINGS_ANDROID_NOTIFICATION_MSG).toString()
        }

        if (intent.hasExtra(Keys.SETTINGS_ANDROID_NOTIFICATION_BIG_MSG)) {
            notificationBigMsg = intent.getStringExtra(Keys.SETTINGS_ANDROID_NOTIFICATION_BIG_MSG).toString()
        }

        val notification = getNotification()
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(notificationId, notification)
    }

    private fun getMainActivityClass(context: Context): Class<*>? {
        val packageName = context.packageName
        val launchIntent = context.packageManager.getLaunchIntentForPackage(packageName)
        val className = launchIntent?.component?.className ?: return null

        return try {
            Class.forName(className)
        } catch (e: ClassNotFoundException) {
            e.printStackTrace()
            null
        }
    }

    override fun onMethodCall(call: MethodCall, result: MethodChannel.Result) {
        when (call.method) {
            Keys.METHOD_SERVICE_INITIALIZED -> {
                synchronized(serviceStarted) {
                    serviceStarted.set(true)
                }
            }
            else -> result.notImplemented()
        }

        result.success(null)
    }


}