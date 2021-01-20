package rekab.app.background_locator.provider

import android.app.PendingIntent
import android.content.Intent
import java.util.HashMap

interface BLLocationProvider {
    var listener: LocationUpdateListener?

    fun removeLocationUpdates()

    fun requestLocationUpdates(request: LocationRequestOptions)
}
