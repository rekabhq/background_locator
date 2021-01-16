package rekab.app.background_locator.provider

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import com.google.android.gms.location.*
import java.util.HashMap

class GoogleLocationProviderClient(context: Context, override var listener: LocationUpdateListener?) : BLLocationProvider {
    private val client: FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)
    private val locationCallback = LocationListener(listener)

    override fun removeLocationUpdates() {
        client.removeLocationUpdates(locationCallback)
    }

    @SuppressLint("MissingPermission")
    override fun requestLocationUpdates(request: LocationRequestOptions) {
        client.requestLocationUpdates(getLocationRequest(request), locationCallback, null)
    }

    private fun getLocationRequest(request: LocationRequestOptions): LocationRequest {
        val locationRequest = LocationRequest()

        locationRequest.interval = request.interval
        locationRequest.fastestInterval = request.interval
        locationRequest.maxWaitTime = request.interval
        locationRequest.priority = request.accuracy
        locationRequest.smallestDisplacement = request.distanceFilter

        return locationRequest
    }
}

private class LocationListener(val listener: LocationUpdateListener?): LocationCallback() {
    override fun onLocationResult(location: LocationResult?) {
        listener?.onLocationUpdated(LocationParserUtil.getLocationMapFromLocation(location))
    }
}