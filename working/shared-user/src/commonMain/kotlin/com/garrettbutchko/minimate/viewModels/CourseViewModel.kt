package com.garrettbutchko.minimate.viewModels

import com.garrettbutchko.minimate.dataModels.MapRegionData
import com.garrettbutchko.minimate.dataModels.courseModels.Course
import com.garrettbutchko.minimate.dataModels.mapModels.MapItemDTO
import com.garrettbutchko.minimate.functions.getAddress
import com.garrettbutchko.minimate.logic.SearchResultBusinessLogic
import com.garrettbutchko.minimate.interfaces.LocationFinding
import com.garrettbutchko.minimate.repositories.CourseRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

interface CourseMapActions {
    fun getDirections(mapItem: MapItemDTO)
}

class CourseViewModel(
    private val courseRepo: CourseRepository,
    private val mapActions: CourseMapActions,
    private val locationHandler: LocationFinding
) {
    private val searchLogic = SearchResultBusinessLogic()

    val mapItems: StateFlow<List<MapItemDTO>> = locationHandler.mapItems
    val selectedMapItem: StateFlow<MapItemDTO?> = locationHandler.selectedItem

    private val _nameExists = MutableStateFlow<Map<String, Boolean>>(emptyMap())
    val nameExists: StateFlow<Map<String, Boolean>> = _nameExists.asStateFlow()

    private val _position = MutableStateFlow<MapRegionData?>(null)
    val position: StateFlow<MapRegionData?> = _position.asStateFlow()
    val hasLocationAccess: StateFlow<Boolean> = locationHandler.hasLocationAccess

    private val _isUpperHalf = MutableStateFlow(false)
    val isUpperHalf: StateFlow<Boolean> = _isUpperHalf.asStateFlow()

    private val _hasAppeared = MutableStateFlow(false)
    val hasAppeared: StateFlow<Boolean> = _hasAppeared.asStateFlow()

    private val _isLoadingCourses = MutableStateFlow(false)
    val isLoadingCourses: StateFlow<Boolean> = _isLoadingCourses.asStateFlow()

    private val _selectedCourse = MutableStateFlow<Course?>(null)
    val selectedCourse: StateFlow<Course?> = _selectedCourse.asStateFlow()

    private val scope = CoroutineScope(Dispatchers.Main)

    fun setNameExists(value: Map<String, Boolean>) {
        _nameExists.value = value
    }

    fun setIsUpperHalf(value: Boolean) {
        _isUpperHalf.value = value
    }

    fun setHasAppeared(value: Boolean) {
        _hasAppeared.value = value
    }

    fun setIsLoadingCourses(value: Boolean) {
        _isLoadingCourses.value = value
    }

    fun setCourse(course: Course?) {
        if (course != null) {
            _selectedCourse.value = course
        }
    }

    fun preloadNameChecks(items: List<MapItemDTO>) {
        items.forEach { item ->
            val name = item.name ?: return@forEach
            if (_nameExists.value.containsKey(name)) return@forEach

            scope.launch {
                val exists = courseRepo.courseNameExistsAndSupported(name)
                val updatedMap = _nameExists.value.toMutableMap()
                updatedMap[name] = exists
                _nameExists.value = updatedMap
            }
        }
    }

    fun onAppearance() {
        if (!_hasAppeared.value) {
            _hasAppeared.value = true
            _isUpperHalf.value = false
            locationHandler.requestLocationAccess()
            locationHandler.setMapItems(emptyList())
            locationHandler.setSelectedItem(null)
            _position.value = locationHandler.updateCameraRegion()
        }
    }

    fun requestLocationAccess() {
        locationHandler.requestLocationAccess()
    }

    fun setPosition(position: MapRegionData) {
        _position.value = position
    }

    fun searchNearby() {
        println("[CourseViewModel] searchNearby() start userLocation=${locationHandler.userLocation.value}")
        _isLoadingCourses.value = true
        _isUpperHalf.value = true
        
        locationHandler.searchNearbyCourses(0.03, 0.1, 0.1) { success, newPosition ->
            println("[CourseViewModel] searchNearby() completion success=$success items=${locationHandler.mapItems.value.size} newPosition=$newPosition")
            if (newPosition != null) {
                _position.value = newPosition
            }
            _isLoadingCourses.value = false
        }
    }

    fun cancel() {
        _isUpperHalf.value = false
        _selectedCourse.value = null
        locationHandler.setSelectedItem(null)
        locationHandler.setMapItems(emptyList())
        _position.value = locationHandler.updateCameraRegion()
    }

    fun updatePosition(mapItem: MapItemDTO) {
        locationHandler.setSelectedItem(mapItem)
        _position.value = locationHandler.updateCameraRegion()
    }

    fun closeSelectedCourse() {
        _selectedCourse.value = null
        locationHandler.setSelectedItem(null)
        _position.value = locationHandler.updateCameraRegion()
    }

    fun getDirections() {
        if (locationHandler.selectedItem.value != null) {
            mapActions.getDirections(locationHandler.selectedItem.value!!)
        }
    }

    fun getPostalAddress(mapItem: MapItemDTO): String {
        return getAddress(mapItem)
    }

    fun distanceInMeters(mapItem: MapItemDTO): Double? {
        val userLocation = locationHandler.userLocation.value ?: return null
        return haversineMeters(
            userLocation.latitude,
            userLocation.longitude,
            mapItem.coordinate.latitude,
            mapItem.coordinate.longitude
        )
    }

    fun getSearchResultSubtitle(mapItem: MapItemDTO): String {
        val distanceMeters = distanceInMeters(mapItem) ?: return getPostalAddress(mapItem)
        val distanceMiles = searchLogic.metersToMiles(distanceMeters)
        return searchLogic.buildSubtitle(distanceMiles, getPostalAddress(mapItem))
    }

    private fun haversineMeters(
        startLatitude: Double,
        startLongitude: Double,
        endLatitude: Double,
        endLongitude: Double
    ): Double {
        val earthRadiusMeters = 6_371_000.0
        val deltaLat = (endLatitude - startLatitude) * PI / 180.0
        val deltaLng = (endLongitude - startLongitude) * PI / 180.0
        val a = sin(deltaLat / 2) * sin(deltaLat / 2) +
            cos(startLatitude * PI / 180.0) *
            cos(endLatitude * PI / 180.0) *
            sin(deltaLng / 2) * sin(deltaLng / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return earthRadiusMeters * c
    }
}
