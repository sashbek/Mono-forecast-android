package org.pakicek.monoforecast.domain.api.gnss.providers

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.location.LocationProvider
import android.os.Bundle
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.pakicek.monoforecast.domain.api.gnss.PermissionHelper

class GNSSProvider(private val context: Context) {

    private val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

    private val _locationState = MutableStateFlow<LocationState>(LocationState.Unavailable)
    val locationState: StateFlow<LocationState> = _locationState.asStateFlow()

    private val locationListener = object : LocationListener {
        override fun onLocationChanged(location: Location) {
            _locationState.value = LocationState.Available(
                latitude = location.latitude,
                longitude = location.longitude,
                altitude = if (location.hasAltitude()) location.altitude else null,
                speed = if (location.hasSpeed()) location.speed else null,
                bearing = if (location.hasBearing()) location.bearing else null,
                accuracy = if (location.hasAccuracy()) location.accuracy else null,
                provider = location.provider ?: "Unknown",
                timestamp = location.time
            )
        }

        override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {
            when (status) {
                LocationProvider.AVAILABLE -> {
                    // Провайдер доступен
                }
                LocationProvider.TEMPORARILY_UNAVAILABLE -> {
                    _locationState.value = LocationState.TemporarilyUnavailable
                }
                LocationProvider.OUT_OF_SERVICE -> {
                    _locationState.value = LocationState.OutOfService
                }
            }
        }

        override fun onProviderEnabled(provider: String) {
            _locationState.value = LocationState.Enabled
        }

        override fun onProviderDisabled(provider: String) {
            _locationState.value = LocationState.Disabled
        }
    }

    @SuppressLint("MissingPermission")
    fun startLocationUpdates(minTimeMs: Long = 1000, minDistanceM: Float = 0f) {
        try {
            val providers = listOf(
                LocationManager.GPS_PROVIDER,
                LocationManager.NETWORK_PROVIDER
            )

            for (provider in providers) {
                if (locationManager.isProviderEnabled(provider)) {
                    locationManager.requestLocationUpdates(
                        provider,
                        minTimeMs,
                        minDistanceM,
                        locationListener
                    )

                    locationManager.getLastKnownLocation(provider)?.let { location ->
                        locationListener.onLocationChanged(location)
                    }
                }
            }
        } catch (e: SecurityException) {
            _locationState.value = LocationState.PermissionDenied
        }
    }

    fun stopLocationUpdates() {
        locationManager.removeUpdates(locationListener)
    }

    fun checkPermissions(): Boolean {
        return PermissionHelper.hasLocationPermissions(context)
    }

    fun isGpsEnabled(): Boolean {
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
    }

    sealed class LocationState {
        object Unavailable : LocationState()
        object Enabled : LocationState()
        object Disabled : LocationState()
        object TemporarilyUnavailable : LocationState()
        object OutOfService : LocationState()
        object PermissionDenied : LocationState()

        data class Available(
            val latitude: Double,
            val longitude: Double,
            val altitude: Double?,
            val speed: Float?,
            val bearing: Float?,
            val accuracy: Float?,
            val provider: String,
            val timestamp: Long
        ) : LocationState()
    }
}