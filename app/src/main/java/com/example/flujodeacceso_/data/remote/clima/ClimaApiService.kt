package com.example.flujodeacceso_.data.remote.clima

import com.example.flujodeacceso_.data.remote.RetrofitInstance
import retrofit2.http.GET
import retrofit2.http.Query

interface ClimaApiService {

    @GET("v1/forecast")
    suspend fun climaActual(
        @Query("latitude") latitud: Double,
        @Query("longitude") longitud: Double,
        @Query("current") campos: String
    ): ClimaRespuestaDto

    companion object {
        val instancia: ClimaApiService by lazy {
            RetrofitInstance.crear(RetrofitInstance.URL_CLIMA).create(ClimaApiService::class.java)
        }
    }
}