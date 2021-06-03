package rekab.app.background_locator.provider

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import androidx.core.content.ContextCompat

class AndroidLocationProviderClient(context: Context, override var listener: LocationUpdateListener?) : BLLocationProvider, LocationListener {
    private val client: LocationManager? =
            ContextCompat.getSystemService(context, LocationManager::class.java)

    @SuppressLint("MissingPermission")
    override fun removeLocationUpdates() {
        client?.removeUpdates(this)
    }

    @SuppressLint("MissingPermission")
    override fun requestLocationUpdates(request: LocationRequestOptions) {
        client?.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                request.interval,
                request.distanceFilter,
                this)
    }

    override fun onLocationChanged(location: Location) {
        listener?.onLocationUpdated(LocationParserUtil.getLocationMapFromLocation(location))
    }

    override fun onProviderDisabled(provider: String) {
        // nop
    }

    override fun onProviderEnabled(provider: String) {
        // nop
    }
}