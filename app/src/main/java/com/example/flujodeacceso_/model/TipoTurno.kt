package com.example.flujodeacceso_.model

enum class TipoTurno(
    val etiqueta: String,
    val horaInicio: Int,
    val horaFin: Int
){
    AM(etiqueta = "Turno AM", horaInicio = 10, horaFin = 15),
    PM(etiqueta = "Turno PM", horaInicio = 15, horaFin = 20);

    val horario: String
        get() = "$horaInicio:00 - $horaFin:00"

    fun incluyeHora(hora: Int): Boolean = hora in horaInicio until horaFin
}