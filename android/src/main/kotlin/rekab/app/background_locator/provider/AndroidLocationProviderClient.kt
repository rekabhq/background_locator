package rekab.app.background_locator.provider

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.location.Location
import android.location.LocationManager
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationRequest
import java.util.*

class AndroidLocationProviderClient(context: Context) : BLLocationProvider {
    private val client: LocationManager? =
            ContextCompat.getSystemService(context, LocationManager::class.java)

    override fun removeLocationUpdates(pendingIntent: PendingIntent) {
        client?.removeUpdates(pendingIntent)
    }

    @SuppressLint("MissingPermission")
    override fun requestLocationUpdates(request: LocationRequest, pendingIntent: PendingIntent) {
        client?.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                request.interval,
                request.smallestDisplacement,
                pendingIntent)
    }

    companion object : BLLocationParser {
        override fun getLocationMapFromIntent(intent: Intent): HashMap<Any, Any>? {
            val locationKey = LocationManager.KEY_LOCATION_CHANGED
            if (intent.hasExtra(locationKey)) {
                val location: Location = intent.extras!![locationKey] as Location
                return LocationParserUtil.getLocationMapFromLocation(location)
            }

            return null
        }
    }
}