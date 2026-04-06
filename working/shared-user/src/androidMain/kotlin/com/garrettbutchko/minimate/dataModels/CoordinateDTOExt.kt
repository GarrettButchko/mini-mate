package com.garrettbutchko.minimate.dataModels

import android.location.Location
import com.garrettbutchko.minimate.dataModels.mapModels.CoordinateDTO
import com.google.android.gms.maps.model.LatLng

fun Location.toDTO(): CoordinateDTO {
    return CoordinateDTO(
        latitude = this.latitude,
        longitude = this.longitude
    )
}

fun CoordinateDTO.toLatLng(): LatLng {
    return LatLng(this.latitude, this.longitude)
}

fun LatLng.toDTO(): CoordinateDTO {
    return CoordinateDTO(
        latitude = this.latitude,
        longitude = this.longitude
    )
}
