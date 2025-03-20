package com.example.sciencequest.Nasa

import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.http.Path

interface NasaApiService {
    // Astronomy Picture of the Day (APOD)
    @GET("planetary/apod")
    suspend fun getApod(@Query("api_key") apiKey: String): NasaApodResponse

    // New function to get Earth images from EPIC API
    @GET("epic/api/natural/images")
    suspend fun getEarthImages(@Query("api_key") apiKey: String): List<EarthImage>

//    // Asteroid Tracker (NeoWs)
//    @GET("neo/rest/v1/feed")
//    suspend fun getAsteroids(
//        @Query("api_key") apiKey: String,
//        @Query("start_date") startDate: String,
//        @Query("end_date") endDate: String
//    ): AsteroidResponse
//
//    // Exoplanet Archive (Solar System Explorer)
//    @GET("api/v1/exoplanets")
//    suspend fun getExoplanets(@Query("api_key") apiKey: String): List<Exoplanet>
//
//    // Space Weather Alerts (DONKI)
//    @GET("DONKI/FLR")
//    suspend fun getSpaceWeatherAlerts(@Query("api_key") apiKey: String): List<SpaceWeatherAlert>
}
