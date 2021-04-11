package rekab.app.background_locator

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Handler
import android.util.Log
import androidx.core.content.ContextCompat
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.embedding.engine.plugins.activity.ActivityAware
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding
import io.flutter.plugin.common.BinaryMessenger
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import io.flutter.plugin.common.PluginRegistry

class BackgroundLocatorPlugin
    : MethodCallHandler, FlutterPlugin, PluginRegistry.NewIntentListener, ActivityAware {
    private var context: Context? = null
    private var activity: Activity? = null

    companion object {
        @JvmStatic
        private var channel: MethodChannel? = null

        @SuppressLint("MissingPermission")
        @JvmStatic
        private fun registerLocator(context: Context,
                                    args: Map<Any, Any>,
                                    result: Result?) {
            if (PreferencesManager.isServiceRunning(context)) {
                // The service is running already
                Log.d("BackgroundLocatorPlugin", "Locator service is already running")
                return
            }

            Log.d("BackgroundLocatorPlugin",
                    "start locator with ${PreferencesManager.getLocationClient(context)} client")

            val callbackHandle = args[Keys.ARG_CALLBACK] as Long
            setCallbackHandle(context, Keys.CALLBACK_HANDLE_KEY, callbackHandle)

            val notificationCallback = args[Keys.ARG_NOTIFICATION_CALLBACK] as? Long
            setCallbackHandle(context, Keys.NOTIFICATION_CALLBACK_HANDLE_KEY, notificationCallback)

            val settings = args[Keys.ARG_SETTINGS] as Map<*, *>

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                    context.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_DENIED) {

                val msg = "'registerLocator' requires the ACCESS_FINE_LOCATION permission."
                result?.error(msg, null, null)
            }

            startIsolateService(context, settings)
            result?.success(true)
            PreferencesManager.setServiceRunning(context, true)
        }

        @JvmStatic
        private fun startIsolateService(context: Context, settings: Map<*, *>) {
            val intent = Intent(context, IsolateHolderService::class.java)
            intent.action = IsolateHolderService.ACTION_START
            intent.putExtra(Keys.SETTINGS_ANDROID_NOTIFICATION_CHANNEL_NAME,
                    settings[Keys.SETTINGS_ANDROID_NOTIFICATION_CHANNEL_NAME] as String)
            intent.putExtra(Keys.SETTINGS_ANDROID_NOTIFICATION_TITLE,
                    settings[Keys.SETTINGS_ANDROID_NOTIFICATION_TITLE] as String)
            intent.putExtra(Keys.SETTINGS_ANDROID_NOTIFICATION_MSG,
                    settings[Keys.SETTINGS_ANDROID_NOTIFICATION_MSG] as String)
            intent.putExtra(Keys.SETTINGS_ANDROID_NOTIFICATION_BIG_MSG,
                    settings[Keys.SETTINGS_ANDROID_NOTIFICATION_BIG_MSG] as String)
            intent.putExtra(Keys.SETTINGS_ANDROID_NOTIFICATION_ICON,
                    settings[Keys.SETTINGS_ANDROID_NOTIFICATION_ICON] as String)
            intent.putExtra(Keys.SETTINGS_ANDROID_NOTIFICATION_ICON_COLOR,
                    settings[Keys.SETTINGS_ANDROID_NOTIFICATION_ICON_COLOR] as Long)
            intent.putExtra(Keys.SETTINGS_INTERVAL, settings[Keys.SETTINGS_INTERVAL] as Int)
            intent.putExtra(Keys.SETTINGS_ACCURACY, settings[Keys.SETTINGS_ACCURACY] as Int)
            intent.putExtra(Keys.SETTINGS_DISTANCE_FILTER, settings[Keys.SETTINGS_DISTANCE_FILTER] as Double)

            if (settings.containsKey(Keys.SETTINGS_ANDROID_WAKE_LOCK_TIME)) {
                intent.putExtra(Keys.SETTINGS_ANDROID_WAKE_LOCK_TIME,
                        settings[Keys.SETTINGS_ANDROID_WAKE_LOCK_TIME] as Int)
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
            val callbackHandle: Long = args[Keys.ARG_CALLBACK_DISPATCHER] as Long
            setCallbackDispatcherHandle(context, callbackHandle)
        }

        @JvmStatic
        private fun unRegisterPlugin(context: Context) {
            if (!PreferencesManager.isServiceRunning(context)) {
                // The service is not running
                Log.d("BackgroundLocatorPlugin", "Locator service is not running, nothing to stop")
                return
            }

            PreferencesManager.setServiceRunning(context, false)
            stopIsolateService(context)
        }

        @JvmStatic
        private fun isServiceRunning(context: Context, result: Result?) {
            if (PreferencesManager.isServiceRunning(context)) {
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
            if (args.containsKey(Keys.SETTINGS_ANDROID_NOTIFICATION_TITLE)) {
                intent.putExtra(Keys.SETTINGS_ANDROID_NOTIFICATION_TITLE,
                        args[Keys.SETTINGS_ANDROID_NOTIFICATION_TITLE] as String)
            }
            if (args.containsKey(Keys.SETTINGS_ANDROID_NOTIFICATION_MSG)) {
                intent.putExtra(Keys.SETTINGS_ANDROID_NOTIFICATION_MSG,
                        args[Keys.SETTINGS_ANDROID_NOTIFICATION_MSG] as String)
            }
            if (args.containsKey(Keys.SETTINGS_ANDROID_NOTIFICATION_BIG_MSG)) {
                intent.putExtra(Keys.SETTINGS_ANDROID_NOTIFICATION_BIG_MSG,
                        args[Keys.SETTINGS_ANDROID_NOTIFICATION_BIG_MSG] as String)
            }

            ContextCompat.startForegroundService(context, intent)
        }

        @JvmStatic
        private fun setCallbackDispatcherHandle(context: Context, handle: Long) {
            context.getSharedPreferences(Keys.SHARED_PREFERENCES_KEY, Context.MODE_PRIVATE)
                    .edit()
                    .putLong(Keys.CALLBACK_DISPATCHER_HANDLE_KEY, handle)
                    .apply()
        }

        @JvmStatic
        fun setCallbackHandle(context: Context, key: String, handle: Long?) {
            if (handle == null) {
                context.getSharedPreferences(Keys.SHARED_PREFERENCES_KEY, Context.MODE_PRIVATE)
                        .edit()
                        .remove(key)
                        .apply()
                return
            }

            context.getSharedPreferences(Keys.SHARED_PREFERENCES_KEY, Context.MODE_PRIVATE)
                    .edit()
                    .putLong(key, handle)
                    .apply()
        }

        @JvmStatic
        fun getCallbackHandle(context: Context, key: String): Long? {
            val sharedPreferences = context.getSharedPreferences(Keys.SHARED_PREFERENCES_KEY, Context.MODE_PRIVATE)
            if (sharedPreferences.contains(key)) return sharedPreferences.getLong(key, 0L)
            return null
        }

        @JvmStatic
        fun registerAfterBoot(context: Context) {
            val settings = PreferencesManager.getSettings(context)

            val plugin = BackgroundLocatorPlugin()
            plugin.context = context

            initializeService(context, settings)
            startIsolateService(context, settings)
        }
    }

    override fun onMethodCall(call: MethodCall, result: Result) {
        when (call.method) {
            Keys.METHOD_PLUGIN_INITIALIZE_SERVICE -> {
                val args: Map<Any, Any> = call.arguments()

                // save callback dispatcher to use it when device reboots
                PreferencesManager.saveCallbackDispatcher(context!!, args)

                initializeService(context!!, args)
                result.success(true)
            }
            Keys.METHOD_PLUGIN_REGISTER_LOCATION_UPDATE -> {
                val args: Map<Any, Any> = call.arguments()

                // save setting to use it when device reboots
                PreferencesManager.saveSettings(context!!, args)

                registerLocator(context!!,
                        args,
                        result)
            }
            Keys.METHOD_PLUGIN_UN_REGISTER_LOCATION_UPDATE -> {
                unRegisterPlugin(context!!)
                result.success(true)
            }
            Keys.METHOD_PLUGIN_IS_REGISTER_LOCATION_UPDATE -> isServiceRunning(context!!, result)
            Keys.METHOD_PLUGIN_IS_SERVICE_RUNNING -> isServiceRunning(context!!, result)
            Keys.METHOD_PLUGIN_UPDATE_NOTIFICATION -> {
                if (!PreferencesManager.isServiceRunning(context!!)) {
                    return
                }

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

        channel = MethodChannel(messenger, Keys.CHANNEL_ID)
        channel?.setMethodCallHandler(plugin)
    }

    override fun onNewIntent(intent: Intent?): Boolean {
        if (intent?.action != Keys.NOTIFICATION_ACTION) {
            // this is not our notification
            return false
        }

        val notificationCallback = getCallbackHandle(activity!!, Keys.NOTIFICATION_CALLBACK_HANDLE_KEY)
        if (notificationCallback != null && IsolateHolderService.backgroundEngine != null) {
            val backgroundChannel =
                    MethodChannel(IsolateHolderService.backgroundEngine?.dartExecutor?.binaryMessenger, Keys.BACKGROUND_CHANNEL_ID)
            activity?.mainLooper?.let {
                Handler(it)
                        .post {
                            backgroundChannel.invokeMethod(Keys.BCM_NOTIFICATION_CLICK,
                                    hashMapOf(Keys.ARG_NOTIFICATION_CALLBACK to notificationCallback))
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
