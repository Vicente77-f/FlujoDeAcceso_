package com.example.flujodeacceso_.model

data class ResumenVentas(
    val fecha: String,
    val hora: Int,
    val transaccionesTradicionales: Int,
    val transaccionesAutoservicio: Int
) {

    val totalTransacciones: Int
        get() = transaccionesTradicionales + transaccionesAutoservicio
}
