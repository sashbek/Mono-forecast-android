package org.pakicek.monoforecast.data

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.tasks.await

class LocationProvider(private val context: Context) {

    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

    data class Coordinates(val lat: Double, val lon: Double)

    @SuppressLint("MissingPermission")
    suspend fun getCurrentLocation(): Coordinates? {
        return try {
            val location: Location? = fusedLocationClient.getCurrentLocation(
                Priority.PRIORITY_HIGH_ACCURACY,
                CancellationTokenSource().token
            ).await()

            if (location != null) {
                Coordinates(location.latitude, location.longitude)
            } else {
                val lastLoc = fusedLocationClient.lastLocation.await()
                if (lastLoc != null) Coordinates(lastLoc.latitude, lastLoc.longitude) else null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}