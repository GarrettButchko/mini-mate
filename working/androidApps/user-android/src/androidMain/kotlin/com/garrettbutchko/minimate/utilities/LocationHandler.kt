package com.garrettbutchko.minimate.utilities

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Looper
import androidx.compose.ui.platform.LocalConfiguration
import androidx.core.content.ContextCompat
import com.garrettbutchko.minimate.dataModels.MapRegionData
import com.garrettbutchko.minimate.dataModels.mapModels.CoordinateDTO
import com.garrettbutchko.minimate.dataModels.mapModels.MapItemDTO
import com.garrettbutchko.minimate.interfaces.LocationFinding
import com.garrettbutchko.minimate.modelKotlinExtensions.toDTO
import com.garrettbutchko.minimate.utilities.FoursquareSearchService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.invoke
import kotlin.text.toDouble
import co.touchlab.kermit.Logger

class LocationHandler(private val context: Context) : LocationFinding {

    private val log = Logger.withTag("LocationHandler")

    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    private var updatesRegistered = false
    private val placesSearchService = FoursquareSearchService()

    private val locationListener = LocationListener { location ->
        val coordinate = CoordinateDTO(
            latitude = location.latitude,
            longitude = location.longitude
        )
        _userLocation.value = coordinate
        log.i("[LocationHandler] location update lat=${coordinate.latitude} lng=${coordinate.longitude}")
    }

    private val _mapItems = MutableStateFlow<List<MapItemDTO>>(emptyList())
    override val mapItems: StateFlow<List<MapItemDTO>> = _mapItems.asStateFlow()

    private val _selectedItem = MutableStateFlow<MapItemDTO?>(null)
    override val selectedItem: StateFlow<MapItemDTO?> = _selectedItem.asStateFlow()

    private val _userLocation = MutableStateFlow<CoordinateDTO?>(null)
    override val userLocation: StateFlow<CoordinateDTO?> = _userLocation.asStateFlow()

    private val _hasLocationAccess = MutableStateFlow(false)
    override val hasLocationAccess: StateFlow<Boolean> = _hasLocationAccess.asStateFlow()

    override fun setMapItems(items: List<MapItemDTO>) {
        _mapItems.value = items
    }

    override fun setSelectedItem(item: MapItemDTO?) {
        _selectedItem.value = item
    }

    fun setUserLocation(location: CoordinateDTO?) {
        _userLocation.value = location
    }

    fun setHasLocationAccess(hasAccess: Boolean) {
        _hasLocationAccess.value = hasAccess
    }

    @SuppressLint("MissingPermission")
    override fun requestLocationAccess() {
        val fineGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val coarseGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val hasAccess = fineGranted || coarseGranted

        _hasLocationAccess.value = hasAccess
        log.i("[LocationHandler] requestLocationAccess hasAccess=$hasAccess")
        if (!hasAccess) {
            return
        }

        val bestLastKnown = listOf(
            LocationManager.GPS_PROVIDER,
            LocationManager.NETWORK_PROVIDER,
            LocationManager.PASSIVE_PROVIDER
        ).mapNotNull { provider ->
            runCatching { locationManager.getLastKnownLocation(provider) }.getOrNull()
        }.maxByOrNull { it.time }

        if (bestLastKnown != null) {
            _userLocation.value = CoordinateDTO(bestLastKnown.latitude, bestLastKnown.longitude)
            log.i("[LocationHandler] lastKnown location lat=${bestLastKnown.latitude} lng=${bestLastKnown.longitude}")
        }

        if (!updatesRegistered) {
            updatesRegistered = true
            runCatching {
                locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    5_000L,
                    10f,
                    locationListener,
                    Looper.getMainLooper()
                )
            }
            runCatching {
                locationManager.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER,
                    5_000L,
                    10f,
                    locationListener,
                    Looper.getMainLooper()
                )
            }
        }
    }

    override fun getPostalAddress(mapItem: MapItemDTO): String {
        return mapItem.address?.fullAddress ?: ""
    }

    override fun updateCameraRegion(): MapRegionData? {
        val selected = _selectedItem.value
        if (selected != null) {

            // Fixed span roughly equivalent to 500m distance for a detail view
            val latitudeDelta = 0.005
            val longitudeDelta = 0.005

            // Consistent with computeBoundingRegion:
            // puts the coordinate at 15% from the top of the map view.
            val topPaddingFactor = 0.3
            val centerLat = selected.coordinate.latitude - (latitudeDelta * (0.5 - topPaddingFactor))

            return MapRegionData(
                latitude = centerLat,
                longitude = selected.coordinate.longitude,
                latitudeDelta = latitudeDelta,
                longitudeDelta = longitudeDelta
            )
        } else if (_mapItems.value.isNotEmpty()) {
            val items = _mapItems.value


            // 2. Find the raw bounds
            val minLat = items.minOf { it.coordinate.latitude }
            val maxLat = items.maxOf { it.coordinate.latitude }
            val minLon = items.minOf { it.coordinate.longitude }
            val maxLon = items.maxOf { it.coordinate.longitude }

            val latRange = maxLat - minLat
            val lonRange = maxLon - minLon
            val topPaddingFactor = 0.3
            val bottomPaddingFactor = 0.1

            val paddedLatDelta = latRange / 0.5

            val topPadding = paddedLatDelta * topPaddingFactor
            val bottomPadding = paddedLatDelta * bottomPaddingFactor
            val latitudeDelta = paddedLatDelta + topPadding + bottomPadding

            val horizontalPaddingPoints = 50.0
            val screenWidthPoints = context.resources.configuration.screenWidthDp
                .toDouble()
                .coerceAtLeast(1.0)
            val horizontalPaddingFraction = horizontalPaddingPoints / screenWidthPoints

            val longitudeDelta = lonRange * (1.0 + horizontalPaddingFraction * 2.0)

            val regionTop = maxLat + topPadding
            val centerLat = regionTop - latitudeDelta * 0.5
            val centerLon = (minLon + maxLon) / 2.0

            return MapRegionData(
                latitude = centerLat,
                longitude = centerLon,
                latitudeDelta = latitudeDelta,
                longitudeDelta = longitudeDelta
            )
        } else if (_userLocation.value != null) {
            val userLoc = _userLocation.value
            return MapRegionData(
                latitude = userLoc?.latitude ?: 0.0,
                longitude = userLoc?.longitude ?: 0.0,
                latitudeDelta = 0.05,
                longitudeDelta = 0.05
            )
        }
        return null
    }



    override fun searchNearbyCourses(
        upwardOffset: Double,
        latitudeDelta: Double,
        longitudeDelta: Double,
        completion: (Boolean, MapRegionData?) -> Unit
    ) {
        val userLoc = _userLocation.value
        log.i("[LocationHandler] searchNearbyCourses start userLocation=$userLoc mapItems=${_mapItems.value.size}")
        if (userLoc == null) {
            log.i("[LocationHandler] searchNearbyCourses aborted: user location is null")
            requestLocationAccess()
            completion(false, null)
            return
        }

        scope.launch {
            val dtos = placesSearchService.searchMiniGolf(
                center = CoordinateDTO(userLoc.latitude + upwardOffset, userLoc.longitude)
            )

            _mapItems.value = dtos
            completion(dtos.isNotEmpty(), updateCameraRegion())
        }
    }

    override fun findClosestMiniGolf(completion: (MapItemDTO?) -> Unit) {
        val userLoc = _userLocation.value
        if (userLoc == null) {
            requestLocationAccess()
            completion(null)
            return
        }

        scope.launch {
            val dtos = placesSearchService.searchMiniGolf(center = userLoc, limit = 1)
            completion(dtos.firstOrNull())
        }
    }
}
