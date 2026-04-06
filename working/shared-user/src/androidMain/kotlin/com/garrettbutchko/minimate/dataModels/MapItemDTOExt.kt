package com.garrettbutchko.minimate.dataModels

import com.garrettbutchko.minimate.dataModels.mapModels.MapItemDTO
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

fun MapItemDTO.toMarkerOptions(): MarkerOptions {
    return MarkerOptions()
        .position(this.coordinate.toLatLng())
        .title(this.name)
        .snippet(this.address?.shortAddress)
}

fun MapItemDTO.toLatLng(): LatLng {
    return this.coordinate.toLatLng()
}
