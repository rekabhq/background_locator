package rekab.app.background_locator.provider

import android.location.Location
import android.os.Build
import rekab.app.background_locator.Keys
import java.util.HashMap

class LocationParserUtil {
    companion object {
        fun getLocationMapFromLocation(location: Location): HashMap<Any, Any>? {
            var speedAccuracy = 0f
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                speedAccuracy = location.speedAccuracyMetersPerSecond
            }
            var isMocked = false
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                isMocked = location.isFromMockProvider
            }

            return hashMapOf(
                    Keys.ARG_IS_MOCKED to isMocked,
                    Keys.ARG_LATITUDE to location.latitude,
                    Keys.ARG_LONGITUDE to location.longitude,
                    Keys.ARG_ACCURACY to location.accuracy,
                    Keys.ARG_ALTITUDE to location.altitude,
                    Keys.ARG_SPEED to location.speed,
                    Keys.ARG_SPEED_ACCURACY to speedAccuracy,
                    Keys.ARG_HEADING to location.bearing,
                    Keys.ARG_TIME to location.time.toDouble())
        }
    }
}