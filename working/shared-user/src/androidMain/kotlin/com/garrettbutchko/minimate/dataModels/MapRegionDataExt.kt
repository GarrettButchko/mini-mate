package com.garrettbutchko.minimate.dataModels

import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import kotlin.math.abs

fun MapRegionData.toLatLngBounds(): LatLngBounds {
    val southwest = LatLng(latitude - latitudeDelta / 2, longitude - longitudeDelta / 2)
    val northeast = LatLng(latitude + latitudeDelta / 2, longitude + longitudeDelta / 2)
    return LatLngBounds(southwest, northeast)
}

fun LatLngBounds.toMapRegionData(): MapRegionData {
    val center = this.center
    val latDelta = abs(northeast.latitude - southwest.latitude)
    val lonDelta = abs(northeast.longitude - southwest.longitude)
    return MapRegionData(
        latitude = center.latitude,
        longitude = center.longitude,
        latitudeDelta = latDelta,
        longitudeDelta = lonDelta
    )
}

fun MapRegionData.Companion.fromLatLngBounds(bounds: LatLngBounds): MapRegionData {
    return bounds.toMapRegionData()
}
