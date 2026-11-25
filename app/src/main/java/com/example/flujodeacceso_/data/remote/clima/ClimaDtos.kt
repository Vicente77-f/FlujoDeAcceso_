package com.example.flujodeacceso_.data.remote.clima

import com.example.flujodeacceso_.domain.describirClima
import com.example.flujodeacceso_.model.Clima
import com.google.gson.annotations.SerializedName

/** Open-Meteo responde {"current": {"temperature_2m": 18.3, "precipitation": 0.0, "weather_code": 3}} */
data class ClimaRespuestaDto(@SerializedName("current") val actual: ClimaActualDto)

data class ClimaActualDto(
    @SerializedName("temperature_2m") val temperatura: Double,
    @SerializedName("precipitation") val precipitacion: Double,
    @SerializedName("weather_code") val codigo: Int
)

fun ClimaActualDto.aClima(): Clima = Clima(temperatura, precipitacion, describirClima(codigo))