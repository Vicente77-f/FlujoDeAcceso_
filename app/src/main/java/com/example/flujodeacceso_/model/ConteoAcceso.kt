package com.example.flujodeacceso_.model

data class ConteoAcceso(
    val acceso: Acceso,   // Acceso donde se registró el conteo
    val fecha: String,   // Fecha en formato "AAAA-MM-DD"
    val hora: Int,      // Inicio de la franja: 12 significa de 12:00 a 12:59
    val Ingresos: Int,
    val Salidas: Int?
)
