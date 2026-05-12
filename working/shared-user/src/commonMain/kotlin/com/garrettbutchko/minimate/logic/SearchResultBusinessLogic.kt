package com.garrettbutchko.minimate.logic

import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.math.round

/**
 * A platform-agnostic class designed to be easily migrated to a Kotlin Multiplatform shared module.
 * It contains pure business, calculation, and formatting rules for search results.
 */
class SearchResultBusinessLogic {
    
    // MARK: - Configuration Rules
    
    /**
     * The amount of time to wait before showing the retry button.
     */
    val retryButtonDelay: Double = 3.0
    
    // MARK: - Location Calculations
    
    /**
     * Calculates the distance between two coordinates in meters using the same haversine formula on every platform.
     */
    fun distanceInMeters(
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

    /**
     * Converts distance in meters to miles.
     */
    fun metersToMiles(meters: Double): Double {
        return meters / 1609.34
    }
    
    // MARK: - Presentation Formatting
    
    /**
     * Formats the distance to a single decimal point.
     */
    fun formatDistance(distanceInMiles: Double): String {
        val rounded = round(distanceInMiles * 10) / 10.0
        val str = rounded.toString()
        return if (!str.contains(".")) "$str.0" else str
    }
    
    /**
     * Constructs the final subtitle string combining distance and address.
     */
    fun buildSubtitle(distanceInMiles: Double, address: String): String {
        val formattedDistance = formatDistance(distanceInMiles)
        return "$formattedDistance mi - $address"
    }
}
