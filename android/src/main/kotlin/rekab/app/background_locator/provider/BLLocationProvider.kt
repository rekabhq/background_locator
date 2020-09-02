package rekab.app.background_locator.provider

import android.app.PendingIntent
import com.google.android.gms.location.LocationRequest

interface BLLocationProvider {
    fun removeLocationUpdates(pendingIntent: PendingIntent)

    fun requestLocationUpdates(request: LocationRequest, pendingIntent: PendingIntent)
}