package com.garrettbutchko.minimate.utilities

import com.garrettbutchko.minimate.dataModels.mapModels.CoordinateDTO
import com.garrettbutchko.minimate.dataModels.mapModels.MapItemDTO
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.statement.HttpResponse
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import kotlinx.serialization.Serializable
import com.garrettbutchko.minimate.BuildKonfig

@Serializable
data class MapboxSearchResponse(
    val features: List<MapboxFeature>
)

@Serializable
data class MapboxFeature(
    val id: String? = null,
    val properties: MapboxProperties,
    val geometry: MapboxGeometry
)

@Serializable
data class MapboxProperties(
    val name: String,
    val full_address: String? = null
)

@Serializable
data class MapboxGeometry(
    val coordinates: List<Double> // [longitude, latitude]
)

class MapboxSearchService(private val mapboxAccessToken: String = BuildKonfig.MAPBOX_ACCESS_TOKEN) {

    private val httpClient = HttpClient {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
    }

    suspend fun searchMiniGolf(
        center: CoordinateDTO,
        radiusInMeters: Double = 50000.0,
        limit: Int = 10
    ): List<MapItemDTO> {
        return try {
            val response: HttpResponse = httpClient.get("https://api.mapbox.com/search/searchbox/v1/forward") {
                parameter("q", "mini golf")
                parameter("access_token", mapboxAccessToken)
                parameter("proximity", "${center.longitude},${center.latitude}")
                parameter("types", "poi")
                parameter("limit", limit)
            }

            val searchResponse = response.body<MapboxSearchResponse>()

            searchResponse.features.map { feature ->
                MapItemDTO(
                    placeID = feature.id ?: "",
                    name = feature.properties.name,
                    coordinate = CoordinateDTO(
                        latitude = feature.geometry.coordinates[1],
                        longitude = feature.geometry.coordinates[0]
                    ),
                    phoneNumber = null,
                    url = null,
                    address = null // Can map properties.full_address if needed
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
}

