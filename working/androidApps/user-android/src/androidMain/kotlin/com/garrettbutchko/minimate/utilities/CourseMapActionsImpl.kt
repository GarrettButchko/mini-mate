package com.garrettbutchko.minimate.utilities

import android.content.Context
import android.content.Intent
import com.garrettbutchko.minimate.dataModels.mapModels.MapItemDTO
import com.garrettbutchko.minimate.viewModels.CourseMapActions
import androidx.core.net.toUri

class CourseMapActionsImpl(private val context: Context) : CourseMapActions {
    override fun getDirections(mapItem: MapItemDTO) {
        val lat = mapItem.coordinate.latitude
        val lon = mapItem.coordinate.longitude
        val name = mapItem.name ?: "Course"
        
        val gmmIntentUri = "google.navigation:q=$lat,$lon".toUri()
        val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
        mapIntent.setPackage("com.google.android.apps.maps")
        mapIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        
        try {
            context.startActivity(mapIntent)
        } catch (e: Exception) {
            val geoIntentUri = "geo:0,0?q=$lat,$lon($name)".toUri()
            val geoIntent = Intent(Intent.ACTION_VIEW, geoIntentUri)
            geoIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            try {
                context.startActivity(geoIntent)
            } catch (ex: Exception) {
                // If even generic geo intent fails (rare)
                ex.printStackTrace()
            }
        }
    }
}
