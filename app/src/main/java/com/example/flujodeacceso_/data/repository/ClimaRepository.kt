package com.example.flujodeacceso_.data.repository

import com.example.flujodeacceso_.data.remote.clima.ClimaApiService
import com.example.flujodeacceso_.data.remote.clima.aClima
import com.example.flujodeacceso_.model.Clima
import retrofit2.HttpException
import java.io.IOException

class ClimaRepository(private val api: ClimaApiService = ClimaApiService.instancia) {

    /** Si falla, la app sigue funcionando: el clima es información extra, no obligatoria. */
    suspend fun climaActual(): Result<Clima> = try {
        Result.success(api.climaActual(LATITUD_SANTIAGO, LONGITUD_SANTIAGO, CAMPOS).actual.aClima())
    } catch (e: IOException) {
        Result.failure(e)
    } catch (e: HttpException) {
        Result.failure(e)
    }

    companion object {
        const val LATITUD_SANTIAGO = -33.45
        const val LONGITUD_SANTIAGO = -70.66
        const val CAMPOS = "temperature_2m,precipitation,weather_code"
    }
}