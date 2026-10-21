package com.example.flujodeacceso_.model

data class ConteoAcceso(
    val turnoId: Long,
    val acceso: Acceso,   // Acceso donde se registró el conteo
    val fecha: String,   // Fecha en formato "AAAA-MM-DD"
    val hora: Int,      // Inicio de la franja: 12 significa de 12:00 a 12:59
    val hombres: Int = 0,
    val mujeres: Int = 0,
    val menores: Int = 0,
    val sinClasificar: Int = 0,
    val transito: Int = 0

){
    /** Total de personas que ingresaron. El tránsito NO se suma: ya está incluido. */
    val totalIngresos: Int
        get() = hombres + mujeres + menores + sinClasificar

}

