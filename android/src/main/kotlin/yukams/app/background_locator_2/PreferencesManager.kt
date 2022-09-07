package yukams.app.background_locator_2

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import yukams.app.background_locator_2.provider.LocationClient

class PreferencesManager {
    companion object {
        private const val PREF_NAME = "background_locator_2"

        @JvmStatic
        fun saveCallbackDispatcher(context: Context, map: Map<Any, Any>) {
            val sharedPreferences =
                    context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

            sharedPreferences.edit()
                    .putLong(Keys.ARG_CALLBACK_DISPATCHER,
                            map[Keys.ARG_CALLBACK_DISPATCHER] as Long)
                    .apply()
        }

        @JvmStatic
        fun saveSettings(context: Context, map: Map<Any, Any>) {
            val sharedPreferences =
                    context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

            val callback = map[Keys.ARG_CALLBACK] as Number
            sharedPreferences.edit()
                    .putLong(Keys.ARG_CALLBACK,
                            callback.toLong())
                    .apply()

            if (map[Keys.ARG_NOTIFICATION_CALLBACK] as? Long != null) {
                sharedPreferences.edit()
                        .putLong(Keys.ARG_NOTIFICATION_CALLBACK,
                                map[Keys.ARG_NOTIFICATION_CALLBACK] as Long)
                        .apply()
            }

            val settings = map[Keys.ARG_SETTINGS] as Map<*, *>

            sharedPreferences.edit()
                    .putString(Keys.SETTINGS_ANDROID_NOTIFICATION_CHANNEL_NAME,
                            settings[Keys.SETTINGS_ANDROID_NOTIFICATION_CHANNEL_NAME] as String)
                    .apply()

            sharedPreferences.edit()
                    .putString(Keys.SETTINGS_ANDROID_NOTIFICATION_TITLE,
                            settings[Keys.SETTINGS_ANDROID_NOTIFICATION_TITLE] as String)
                    .apply()

            sharedPreferences.edit()
                    .putString(Keys.SETTINGS_ANDROID_NOTIFICATION_MSG,
                            settings[Keys.SETTINGS_ANDROID_NOTIFICATION_MSG] as String)
                    .apply()

            sharedPreferences.edit()
                    .putString(Keys.SETTINGS_ANDROID_NOTIFICATION_BIG_MSG,
                            settings[Keys.SETTINGS_ANDROID_NOTIFICATION_BIG_MSG] as String)
                    .apply()

            sharedPreferences.edit()
                    .putString(Keys.SETTINGS_ANDROID_NOTIFICATION_ICON,
                            settings[Keys.SETTINGS_ANDROID_NOTIFICATION_ICON] as String)
                    .apply()

            sharedPreferences.edit()
                    .putLong(Keys.SETTINGS_ANDROID_NOTIFICATION_ICON_COLOR,
                            settings[Keys.SETTINGS_ANDROID_NOTIFICATION_ICON_COLOR] as Long)
                    .apply()

            sharedPreferences.edit()
                    .putInt(Keys.SETTINGS_INTERVAL,
                            settings[Keys.SETTINGS_INTERVAL] as Int)
                    .apply()

            sharedPreferences.edit()
                    .putInt(Keys.SETTINGS_ACCURACY,
                            settings[Keys.SETTINGS_ACCURACY] as Int)
                    .apply()

            sharedPreferences.edit()
                    .putFloat(Keys.SETTINGS_DISTANCE_FILTER,
                            (settings[Keys.SETTINGS_DISTANCE_FILTER] as Double).toFloat())
                    .apply()

            if (settings.containsKey(Keys.SETTINGS_ANDROID_WAKE_LOCK_TIME)) {
                sharedPreferences.edit()
                        .putInt(Keys.SETTINGS_ANDROID_WAKE_LOCK_TIME,
                                settings[Keys.SETTINGS_ANDROID_WAKE_LOCK_TIME] as Int)
                        .apply()
            }

            sharedPreferences.edit()
                    .putInt(Keys.SETTINGS_ANDROID_LOCATION_CLIENT,
                            settings[Keys.SETTINGS_ANDROID_LOCATION_CLIENT] as Int)
                    .apply()
        }

        @JvmStatic
        fun getSettings(context: Context): Map<Any, Any> {
            val sharedPreferences =
                    context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

            val result = HashMap<Any, Any>()

            result[Keys.ARG_CALLBACK_DISPATCHER] = sharedPreferences.getLong(Keys.ARG_CALLBACK_DISPATCHER, 0)
            result[Keys.ARG_CALLBACK] = sharedPreferences.getLong(Keys.ARG_CALLBACK, 0)

            if (sharedPreferences.contains(Keys.ARG_NOTIFICATION_CALLBACK)) {
                result[Keys.ARG_NOTIFICATION_CALLBACK] =
                        sharedPreferences.getLong(Keys.ARG_NOTIFICATION_CALLBACK, 0)
            }

            val settings = HashMap<String, Any?>()

            settings[Keys.SETTINGS_ANDROID_NOTIFICATION_CHANNEL_NAME] =
                    sharedPreferences.getString(Keys.SETTINGS_ANDROID_NOTIFICATION_CHANNEL_NAME, "")

            settings[Keys.SETTINGS_ANDROID_NOTIFICATION_TITLE] =
                    sharedPreferences.getString(Keys.SETTINGS_ANDROID_NOTIFICATION_TITLE, "")

            settings[Keys.SETTINGS_ANDROID_NOTIFICATION_MSG] =
                    sharedPreferences.getString(Keys.SETTINGS_ANDROID_NOTIFICATION_MSG, "")

            settings[Keys.SETTINGS_ANDROID_NOTIFICATION_BIG_MSG] =
                    sharedPreferences.getString(Keys.SETTINGS_ANDROID_NOTIFICATION_BIG_MSG, "")

            settings[Keys.SETTINGS_ANDROID_NOTIFICATION_ICON] =
                    sharedPreferences.getString(Keys.SETTINGS_ANDROID_NOTIFICATION_ICON, "")

            settings[Keys.SETTINGS_ANDROID_NOTIFICATION_ICON_COLOR] =
                    sharedPreferences.getLong(Keys.SETTINGS_ANDROID_NOTIFICATION_ICON_COLOR, 0)

            settings[Keys.SETTINGS_INTERVAL] =
                    sharedPreferences.getInt(Keys.SETTINGS_INTERVAL, 0)

            settings[Keys.SETTINGS_ACCURACY] =
                    sharedPreferences.getInt(Keys.SETTINGS_ACCURACY, 0)

            settings[Keys.SETTINGS_DISTANCE_FILTER] =
                    sharedPreferences.getFloat(Keys.SETTINGS_DISTANCE_FILTER, 0f).toDouble()

            if (sharedPreferences.contains(Keys.SETTINGS_ANDROID_WAKE_LOCK_TIME)) {
                settings[Keys.SETTINGS_ANDROID_WAKE_LOCK_TIME] = sharedPreferences.getInt(Keys.SETTINGS_ANDROID_WAKE_LOCK_TIME, 0)
            }

            settings[Keys.SETTINGS_ANDROID_LOCATION_CLIENT] =
                    sharedPreferences.getInt(Keys.SETTINGS_ANDROID_LOCATION_CLIENT, 0)

            result[Keys.ARG_SETTINGS] = settings
            return result
        }

        @JvmStatic
        fun getLocationClient(context: Context): LocationClient {
            val sharedPreferences =
                    context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            val client = sharedPreferences.getInt(Keys.SETTINGS_ANDROID_LOCATION_CLIENT, 0)
            return LocationClient.fromInt(client) ?: LocationClient.Google
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
        fun setDataCallback(context: Context, key: String, data: Map<*, *>?) {
            if (data == null) {
                context.getSharedPreferences(Keys.SHARED_PREFERENCES_KEY, Context.MODE_PRIVATE)
                        .edit()
                        .remove(key)
                        .apply()
                return
            }
            val dataStr = Gson().toJson(data)
            context.getSharedPreferences(Keys.SHARED_PREFERENCES_KEY, Context.MODE_PRIVATE)
                    .edit()
                    .putString(key, dataStr)
                    .apply()
        }

        @JvmStatic
        fun getCallbackHandle(context: Context, key: String): Long? {
            val sharedPreferences = context.getSharedPreferences(Keys.SHARED_PREFERENCES_KEY, Context.MODE_PRIVATE)
            if (sharedPreferences.contains(key)) return sharedPreferences.getLong(key, 0L)
            return null
        }

        @JvmStatic
        fun getDataCallback(context: Context, key: String): Map<*, *> {
            val initialDataStr = context.getSharedPreferences(Keys.SHARED_PREFERENCES_KEY, Context.MODE_PRIVATE)
                    .getString(key, null)
            val type = object : TypeToken<Map<*, *>>() {}.type
            return Gson().fromJson(initialDataStr, type)
        }
    }
}