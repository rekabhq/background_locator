package rekab.app.background_locator.provider

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import java.util.HashMap

class GoogleLocationProviderClient(context: Context) : BLLocationProvider {
    private val client: FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)

    override fun removeLocationUpdates(pendingIntent: PendingIntent) {
        client.removeLocationUpdates(pendingIntent)
    }

    @SuppressLint("MissingPermission")
    override fun requestLocationUpdates(request: LocationRequest, pendingIntent: PendingIntent) {
        client.requestLocationUpdates(request, pendingIntent)
    }

    companion object: BLLocationParser {
        override fun getLocationMapFromIntent(intent: Intent): HashMap<Any, Any>? {
            if (LocationResult.hasResult(intent)) {
                val location = LocationResult.extractResult(intent).lastLocation
                return LocationParserUtil.getLocationMapFromLocation(location)
            }
            
            return null
        }
    }
}