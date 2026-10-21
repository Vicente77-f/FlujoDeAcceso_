package com.example.flujodeacceso_.model

data class Turno(
    val id: Long = 0L,
    val usuarioId: Long,        // Quién trabajó el turno
    val acceso: Acceso,
    val tipo: TipoTurno,        // AM o PM
    val fecha: String,
    val horaInicioReal: String,
    val horaFinReal: String?,       // null = el turno sigue en curso
    val fotoUri: String,        // Ubicación de la foto del puesto (obligatoria)
    val exportado: Boolean = false
){
    val estaAbierto: Boolean
        get() = horaFinReal == null
}
