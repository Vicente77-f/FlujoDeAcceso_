package com.example.flujodeacceso_.model

/** Clima actual en Santiago, obtenido de Open-Meteo (API externa). */
data class Clima(
    val temperatura: Double,     // °C
    val precipitacion: Double,   // mm
    val descripcion: String      // "Lluvia", "Despejado"...
)