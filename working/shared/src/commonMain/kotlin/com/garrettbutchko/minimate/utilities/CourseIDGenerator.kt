package com.garrettbutchko.minimate.utilities

import com.garrettbutchko.minimate.dataModels.mapModels.MapItemDTO

object CourseIDGenerator {

    /**
     * Public API to generate a course ID.
     */
    fun generateCourseID(item: MapItemDTO): String {
        val namePart = slugify(item.name ?: "unknown")

        // Round coordinates to ~1.1 meters of precision (5 decimal places)
        // to handle floating point differences between platforms.
        val lat = (item.coordinate.latitude * 100000.0).toLong() / 100000.0
        val lon = (item.coordinate.longitude * 100000.0).toLong() / 100000.0

        val hashInput = "$lat-$lon-${item.name ?: ""}"
        val hash = shortHash(hashInput)

        return "$namePart-$hash"
    }

    /**
     * Transforms a string into a URL-friendly slug.
     */
    private fun slugify(text: String): String {
        return text.lowercase()
            .trim()
            // Replace non-alphanumeric characters with hyphens
            .split(Regex("[^a-z0-9]+"))
            .filter { it.isNotEmpty() }
            .joinToString("-")
    }
}