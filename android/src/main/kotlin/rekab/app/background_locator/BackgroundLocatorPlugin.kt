package rekab.app.background_locator

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Handler
import android.util.Log
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationRequest
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.embedding.engine.plugins.activity.ActivityAware
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding
import io.flutter.plugin.common.BinaryMessenger
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import io.flutter.plugin.common.PluginRegistry
import rekab.app.background_locator.Keys.Companion.ARG_CALLBACK
import rekab.app.background_locator.Keys.Companion.ARG_CALLBACK_DISPATCHER
import rekab.app.background_locator.Keys.Companion.ARG_DISPOSE_CALLBACK
import rekab.app.background_locator.Keys.Companion.ARG_INIT_CALLBACK
import rekab.app.background_locator.Keys.Companion.ARG_INIT_DATA_CALLBACK
import rekab.app.background_locator.Keys.Companion.ARG_NOTIFICATION_CALLBACK
import rekab.app.background_locator.Keys.Companion.ARG_SETTINGS
import rekab.app.background_locator.Keys.Companion.BACKGROUND_CHANNEL_ID
import rekab.app.background_locator.Keys.Companion.BCM_NOTIFICATION_CLICK
import rekab.app.background_locator.Keys.Companion.CALLBACK_DISPATCHER_HANDLE_KEY
import rekab.app.background_locator.Keys.Companion.CALLBACK_HANDLE_KEY
import rekab.app.background_locator.Keys.Companion.CHANNEL_ID
import rekab.app.background_locator.Keys.Companion.DISPOSE_CALLBACK_HANDLE_KEY
import rekab.app.background_locator.Keys.Companion.INIT_CALLBACK_HANDLE_KEY
import rekab.app.background_locator.Keys.Companion.INIT_DATA_CALLBACK_KEY
import rekab.app.background_locator.Keys.Companion.METHOD_PLUGIN_INITIALIZE_SERVICE
import rekab.app.background_locator.Keys.Companion.METHOD_PLUGIN_IS_REGISTER_LOCATION_UPDATE
import rekab.app.background_locator.Keys.Companion.METHOD_PLUGIN_IS_SERVICE_RUNNING
import rekab.app.background_locator.Keys.Companion.METHOD_PLUGIN_REGISTER_LOCATION_UPDATE
import rekab.app.background_locator.Keys.Companion.METHOD_PLUGIN_UN_REGISTER_LOCATION_UPDATE
import rekab.app.background_locator.Keys.Companion.METHOD_PLUGIN_UPDATE_NOTIFICATION
import rekab.app.background_locator.Keys.Companion.NOTIFICATION_ACTION
import rekab.app.background_locator.Keys.Companion.NOTIFICATION_CALLBACK_HANDLE_KEY
import rekab.app.background_locator.Keys.Companion.SETTINGS_ACCURACY
import rekab.app.background_locator.Keys.Companion.SETTINGS_ANDROID_NOTIFICATION_BIG_MSG
import rekab.app.background_locator.Keys.Companion.SETTINGS_ANDROID_NOTIFICATION_CHANNEL_NAME
import rekab.app.background_locator.Keys.Companion.SETTINGS_ANDROID_NOTIFICATION_ICON
import rekab.app.background_locator.Keys.Companion.SETTINGS_ANDROID_NOTIFICATION_ICON_COLOR
import rekab.app.background_locator.Keys.Companion.SETTINGS_ANDROID_NOTIFICATION_MSG
import rekab.app.background_locator.Keys.Companion.SETTINGS_ANDROID_NOTIFICATION_TITLE
import rekab.app.background_locator.Keys.Companion.SETTINGS_ANDROID_WAKE_LOCK_TIME
import rekab.app.background_locator.Keys.Companion.SETTINGS_DISTANCE_FILTER
import rekab.app.background_locator.Keys.Companion.SETTINGS_INTERVAL
import rekab.app.background_locator.Keys.Companion.SHARED_PREFERENCES_KEY
import rekab.app.background_locator.provider.*


class BackgroundLocatorPlugin
    : MethodCallHandler, FlutterPlugin, PluginRegistry.NewIntentListener, ActivityAware {
    private lateinit var locatorClient: BLLocationProvider
    private var context: Context? = null
    private var activity: Activity? = null

    companion object {
        @JvmStatic
        private var isLocationServiceRunning: Boolean = false

        @JvmStatic
        private var channel: MethodChannel? = null

        @SuppressLint("MissingPermission")
        @JvmStatic
        private fun registerLocator(context: Context,
                                    client: BLLocationProvider,
                                    args: Map<Any, Any>,
                                    result: Result?) {
            if (IsolateHolderService.isRunning) {
                // The service is running already
                isLocationServiceRunning = true

                Log.d("BackgroundLocatorPlugin", "Locator service is already running")
                return
            }

            Log.d("BackgroundLocatorPlugin",
                    "start locator with ${PreferencesManager.getLocationClient(context)} client")

            isLocationServiceRunning = true

            val callbackHandle = args[ARG_CALLBACK] as Long
            setCallbackHandle(context, CALLBACK_HANDLE_KEY, callbackHandle)

            val notificationCallback = args[ARG_NOTIFICATION_CALLBACK] as? Long
            setCallbackHandle(context, NOTIFICATION_CALLBACK_HANDLE_KEY, notificationCallback)

            val initCallback = args[ARG_INIT_CALLBACK] as? Long
            setCallbackHandle(context, INIT_CALLBACK_HANDLE_KEY, initCallback)

            val disposeCallback = args[ARG_DISPOSE_CALLBACK] as? Long
            setCallbackHandle(context, DISPOSE_CALLBACK_HANDLE_KEY, disposeCallback)

            val settings = args[ARG_SETTINGS] as Map<*, *>

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                    context.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_DENIED) {

                val msg = "'registerLocator' requires the ACCESS_FINE_LOCATION permission."
                result?.error(msg, null, null)
            }

            val initialDataMap = args[ARG_INIT_DATA_CALLBACK] as? Map<*, *>
            setDataCallback(context, INIT_DATA_CALLBACK_KEY, initialDataMap)
            startIsolateService(context, settings)

            client.requestLocationUpdates(getLocationRequest(settings),
                    getLocatorPendingIndent(context))
        }

        @JvmStatic
        private fun startIsolateService(context: Context, settings: Map<*, *>) {
            val intent = Intent(context, IsolateHolderService::class.java)
            intent.action = IsolateHolderService.ACTION_START
            intent.putExtra(SETTINGS_ANDROID_NOTIFICATION_CHANNEL_NAME, settings[SETTINGS_ANDROID_NOTIFICATION_CHANNEL_NAME] as String)
            intent.putExtra(SETTINGS_ANDROID_NOTIFICATION_TITLE, settings[SETTINGS_ANDROID_NOTIFICATION_TITLE] as String)
            intent.putExtra(SETTINGS_ANDROID_NOTIFICATION_MSG, settings[SETTINGS_ANDROID_NOTIFICATION_MSG] as String)
            intent.putExtra(SETTINGS_ANDROID_NOTIFICATION_BIG_MSG, settings[SETTINGS_ANDROID_NOTIFICATION_BIG_MSG] as String)
            intent.putExtra(SETTINGS_ANDROID_NOTIFICATION_ICON, settings[SETTINGS_ANDROID_NOTIFICATION_ICON] as String)
            intent.putExtra(SETTINGS_ANDROID_NOTIFICATION_ICON_COLOR, settings[SETTINGS_ANDROID_NOTIFICATION_ICON_COLOR] as Long)

            if (settings.containsKey(SETTINGS_ANDROID_WAKE_LOCK_TIME)) {
                intent.putExtra(SETTINGS_ANDROID_WAKE_LOCK_TIME, settings[SETTINGS_ANDROID_WAKE_LOCK_TIME] as Int)
            }

            ContextCompat.startForegroundService(context, intent)
        }

        @JvmStatic
        private fun stopIsolateService(context: Context) {
            val intent = Intent(context, IsolateHolderService::class.java)
            intent.action = IsolateHolderService.ACTION_SHUTDOWN
            ContextCompat.startForegroundService(context, intent)
        }

        @JvmStatic
        private fun initializeService(context: Context, args: Map<Any, Any>) {
            val callbackHandle: Long = args[ARG_CALLBACK_DISPATCHER] as Long
            setCallbackDispatcherHandle(context, callbackHandle)
        }

        @JvmStatic
        private fun getLocatorPendingIndent(context: Context): PendingIntent {
            val intent = Intent(context, LocatorBroadcastReceiver::class.java)
            return PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        }

        @JvmStatic
        private fun getLocationRequest(settings: Map<*, *>): LocationRequestOptions {
            val interval: Long = (settings[SETTINGS_INTERVAL] as Int * 1000).toLong()
            val accuracyKey = settings[SETTINGS_ACCURACY] as Int
            val accuracy = getAccuracy(accuracyKey)
            val distanceFilter = settings[SETTINGS_DISTANCE_FILTER] as Double

            return LocationRequestOptions(interval, accuracy, distanceFilter.toFloat())
        }

        @JvmStatic
        private fun getAccuracy(key: Int): Int {
            return when (key) {
                0 -> LocationRequest.PRIORITY_NO_POWER
                1 -> LocationRequest.PRIORITY_LOW_POWER
                2 -> LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY
                3 -> LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY
                4 -> LocationRequest.PRIORITY_HIGH_ACCURACY
                else -> LocationRequest.PRIORITY_HIGH_ACCURACY
            }
        }

        @JvmStatic
        private fun removeLocator(context: Context,
                                  client: BLLocationProvider) {
            isLocationServiceRunning = false
            if (!IsolateHolderService.isRunning) {
                // The service is not running
                Log.d("BackgroundLocatorPlugin", "Locator service is not running, nothing to stop")
                return
            }

            client.removeLocationUpdates(getLocatorPendingIndent(context))
            stopIsolateService(context)
        }

        @JvmStatic
        private fun isRegisterLocator(result: Result?) {
            if (IsolateHolderService.isRunning) {
                result?.success(true)
            } else {
                result?.success(false)
            }
            return
        }

        @JvmStatic
        private fun isServiceRunning(result: Result?) {
            if (isLocationServiceRunning) {
                result?.success(true)
            } else {
                result?.success(false)
            }
            return
        }

        @JvmStatic
        private fun updateNotificationText(context: Context, args: Map<Any, Any>) {
            val intent = Intent(context, IsolateHolderService::class.java)
            intent.action = IsolateHolderService.ACTION_UPDATE_NOTIFICATION
            if (args.containsKey(SETTINGS_ANDROID_NOTIFICATION_TITLE)) {
                intent.putExtra(SETTINGS_ANDROID_NOTIFICATION_TITLE,
                        args[SETTINGS_ANDROID_NOTIFICATION_TITLE] as String)
            }
            if (args.containsKey(SETTINGS_ANDROID_NOTIFICATION_MSG)) {
                intent.putExtra(SETTINGS_ANDROID_NOTIFICATION_MSG,
                        args[SETTINGS_ANDROID_NOTIFICATION_MSG] as String)
            }
            if (args.containsKey(SETTINGS_ANDROID_NOTIFICATION_BIG_MSG)) {
                intent.putExtra(SETTINGS_ANDROID_NOTIFICATION_BIG_MSG,
                        args[SETTINGS_ANDROID_NOTIFICATION_BIG_MSG] as String)
            }

            ContextCompat.startForegroundService(context, intent)
        }

        @JvmStatic
        private fun setCallbackDispatcherHandle(context: Context, handle: Long) {
            context.getSharedPreferences(SHARED_PREFERENCES_KEY, Context.MODE_PRIVATE)
                    .edit()
                    .putLong(CALLBACK_DISPATCHER_HANDLE_KEY, handle)
                    .apply()
        }

        @JvmStatic
        fun setCallbackHandle(context: Context, key: String, handle: Long?) {
            if (handle == null) {
                context.getSharedPreferences(SHARED_PREFERENCES_KEY, Context.MODE_PRIVATE)
                        .edit()
                        .remove(key)
                        .apply()
                return
            }

            context.getSharedPreferences(SHARED_PREFERENCES_KEY, Context.MODE_PRIVATE)
                    .edit()
                    .putLong(key, handle)
                    .apply()
        }

        @JvmStatic
        fun setDataCallback(context: Context, key: String, data: Map<*, *>?) {
            if (data == null) {
                context.getSharedPreferences(SHARED_PREFERENCES_KEY, Context.MODE_PRIVATE)
                        .edit()
                        .remove(key)
                        .apply()
                return
            }
            val dataStr = Gson().toJson(data)
            context.getSharedPreferences(SHARED_PREFERENCES_KEY, Context.MODE_PRIVATE)
                    .edit()
                    .putString(key, dataStr)
                    .apply()
        }

        @JvmStatic
        fun getCallbackHandle(context: Context, key: String): Long? {
            val sharedPreferences = context.getSharedPreferences(SHARED_PREFERENCES_KEY, Context.MODE_PRIVATE)
            if (sharedPreferences.contains(key)) return sharedPreferences.getLong(key, 0L)
            return null
        }

        @JvmStatic
        fun getDataCallback(context: Context, key: String): Map<*, *> {
            val initialDataStr = context.getSharedPreferences(SHARED_PREFERENCES_KEY, Context.MODE_PRIVATE)
                    .getString(key, null)
            val type = object : TypeToken<Map<*, *>>() {}.type
            return Gson().fromJson(initialDataStr, type)
        }

        @JvmStatic
        fun registerAfterBoot(context: Context) {
            val settings = PreferencesManager.getSettings(context)

            val plugin = BackgroundLocatorPlugin()
            plugin.context = context
            plugin.locatorClient = getLocationClient(context)

            initializeService(context, settings)
            registerLocator(context,
                    plugin.locatorClient,
                    settings, null)
        }

        @JvmStatic
        fun getLocationClient(context: Context): BLLocationProvider {
            return when (PreferencesManager.getLocationClient(context)) {
                LocationClient.Google -> GoogleLocationProviderClient(context)
                LocationClient.Android -> AndroidLocationProviderClient(context)
            }
        }
    }

    override fun onMethodCall(call: MethodCall, result: Result) {
        when (call.method) {
            METHOD_PLUGIN_INITIALIZE_SERVICE -> {
                val args: Map<Any, Any> = call.arguments()

                // save callback dispatcher to use it when device reboots
                PreferencesManager.saveCallbackDispatcher(context!!, args)

                initializeService(context!!, args)
                result.success(true)
            }
            METHOD_PLUGIN_REGISTER_LOCATION_UPDATE -> {
                val args: Map<Any, Any> = call.arguments()

                // save setting to use it when device reboots
                PreferencesManager.saveSettings(context!!, args)

                locatorClient = getLocationClient(context!!)
                registerLocator(context!!,
                        locatorClient,
                        args,
                        result)
            }
            METHOD_PLUGIN_UN_REGISTER_LOCATION_UPDATE -> removeLocator(context!!,
                    locatorClient)
            METHOD_PLUGIN_IS_REGISTER_LOCATION_UPDATE -> isRegisterLocator(result)
            METHOD_PLUGIN_IS_SERVICE_RUNNING -> isServiceRunning(result)
            METHOD_PLUGIN_UPDATE_NOTIFICATION -> {
                val args: Map<Any, Any> = call.arguments()
                updateNotificationText(context!!, args)
                result.success(true)
            }
            else -> result.notImplemented()
        }
    }

    override fun onAttachedToEngine(binding: FlutterPlugin.FlutterPluginBinding) {
        onAttachedToEngine(binding.applicationContext, binding.binaryMessenger)
    }

    override fun onDetachedFromEngine(binding: FlutterPlugin.FlutterPluginBinding) {
    }

    private fun onAttachedToEngine(context: Context, messenger: BinaryMessenger) {
        val plugin = BackgroundLocatorPlugin()
        plugin.context = context
        plugin.locatorClient = getLocationClient(context)

        channel = MethodChannel(messenger, CHANNEL_ID)
        channel?.setMethodCallHandler(plugin)
    }

    override fun onNewIntent(intent: Intent?): Boolean {
        if (intent?.action != NOTIFICATION_ACTION) {
            // this is not our notification
            return false
        }

        val notificationCallback = getCallbackHandle(activity!!, NOTIFICATION_CALLBACK_HANDLE_KEY)
        if (notificationCallback != null && IsolateHolderService.backgroundFlutterView != null) {
            val backgroundChannel = MethodChannel(IsolateHolderService.backgroundFlutterView,
                    BACKGROUND_CHANNEL_ID)
            activity?.mainLooper?.let {
                Handler(it)
                        .post {
                            backgroundChannel.invokeMethod(BCM_NOTIFICATION_CLICK,
                                    hashMapOf(ARG_NOTIFICATION_CALLBACK to notificationCallback))
                        }
            }
        }

        return true
    }

    override fun onDetachedFromActivity() {
    }

    override fun onReattachedToActivityForConfigChanges(binding: ActivityPluginBinding) {
    }

    override fun onAttachedToActivity(binding: ActivityPluginBinding) {
        activity = binding.activity
        binding.addOnNewIntentListener(this)
    }

    override fun onDetachedFromActivityForConfigChanges() {
    }
}
