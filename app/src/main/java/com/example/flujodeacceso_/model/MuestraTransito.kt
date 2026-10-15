package com.example.flujodeacceso_.model

data class MuestraTransito(
    val acceso: Acceso,
    val fecha: String,
    val hora: Int,
    val minutosObservados: Int,
    val personasObservadas: Int,
    val personasTransito: Int
)
