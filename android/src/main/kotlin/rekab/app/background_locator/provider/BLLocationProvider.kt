package rekab.app.background_locator.provider

import android.app.PendingIntent
import android.content.Intent
import java.util.HashMap

interface BLLocationProvider {
    fun removeLocationUpdates(pendingIntent: PendingIntent)

    fun requestLocationUpdates(request: LocationRequestOptions, pendingIntent: PendingIntent)
}

interface BLLocationParser {
    fun getLocationMapFromIntent(intent: Intent): HashMap<Any, Any>?
}