package com.garrettbutchko.minimate.dataModels

import android.location.Address
import com.garrettbutchko.minimate.dataModels.mapModels.AddressDTO

fun Address.toDTO(): AddressDTO {
    val fullAddress = (0..maxAddressLineIndex).map { getAddressLine(it) }.joinToString("\n")
    val shortAddress = if (thoroughfare != null && locality != null) {
        "$thoroughfare, $locality"
    } else {
        fullAddress
    }
    
    return AddressDTO(
        fullAddress = fullAddress,
        shortAddress = shortAddress
    )
}
