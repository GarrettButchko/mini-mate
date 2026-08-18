package com.garrettbutchko.minimate.utilities
import com.garrettbutchko.minimate.BuildKonfig
import com.garrettbutchko.minimate.dataModels.mapModels.AddressDTO
import com.garrettbutchko.minimate.dataModels.mapModels.CoordinateDTO
import com.garrettbutchko.minimate.dataModels.mapModels.MapItemDTO
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

class FoursquareSearchService(private val apiKey: String = BuildKonfig.FOURSQUARE_API_KEY) {

    private val jsonParser = Json { ignoreUnknownKeys = true }

    private val httpClient = HttpClient {
        install(ContentNegotiation) {
            json(jsonParser)
        }
    }

    @Serializable
    private data class FsqSearchResponse(
        val results: List<FsqPlace> = emptyList()
    )

    @Serializable
    private data class FsqPlace(
        @SerialName("fsq_place_id") val fsqId: String,
        val name: String,
        val categories: List<FsqCategory> = emptyList(),
        val latitude: Double,
        val longitude: Double,
        val location: FsqLocation? = null,
        val distance: Int? = null,
        val tel: String? = null,
        val website: String? = null
    )

    @Serializable
    private data class FsqCategory(
        @SerialName("fsq_category_id") val id: String,
        val name: String
    )

    @Serializable
    private data class FsqLocation(
        @SerialName("formatted_address")
        val formattedAddress: String? = null,

        val address: String? = null
    )

    suspend fun searchMiniGolf(
        center: CoordinateDTO,
        radiusInMeters: Int = 50000,
        limit: Int = 10
    ): List<MapItemDTO> {
        try {
            val requestLimit = limit.coerceIn(1, 50)

            val response = httpClient.get("https://places-api.foursquare.com/places/search") {
                header("Authorization", "Bearer $apiKey")
                header("X-Places-Api-Version", "2025-06-17")
                header("Accept", "application/json")
                parameter("ll", "${center.latitude},${center.longitude}")
                parameter("radius", radiusInMeters)
                parameter("query", "mini golf")
                parameter("sort", "DISTANCE")
                parameter("limit", 50)
                parameter("fields", "fsq_place_id,name,categories,latitude,longitude,location,distance,tel,website")
            }

            if (response.status.value !in 200..299) {
                val errorBody = response.body<String>()
                AppLogger.error("FoursquareSearchService", "HTTP ${response.status.value}: $errorBody")
                return emptyList()
            }

            val body = response.body<FsqSearchResponse>()

            if (body.results.isEmpty()) {
                AppLogger.info("FoursquareSearchService", "no results from Foursquare for center=${center} radius=$radiusInMeters limit=$limit")
                return emptyList()
            }

            val mapped = body.results.map { r ->
                val lat = r.latitude
                val lon = r.longitude

                MapItemDTO(
                    placeID = r.fsqId,
                    name = r.name,
                    phoneNumber = r.tel,
                    url = r.website,
                    address = AddressDTO(
                        fullAddress = r.location?.formattedAddress ?: "",
                        shortAddress = r.location?.address ?: r.location?.formattedAddress ?: "N/A"
                    ),
                    coordinate = CoordinateDTO(latitude = lat, longitude = lon)
                )
            }.take(limit)

            AppLogger.info("FoursquareSearchService", "mapped=${mapped.size} raw=${body.results.size} limit=$limit")
            return mapped
        } catch (e: Exception) {
            AppLogger.error("FoursquareSearchService", "search failed: ${e.message}", e)
            return emptyList()
        }
    }
}
