package com.example.flujodeacceso_.data.repository
import com.example.flujodeacceso_.model.ConteoAcceso
import com.example.flujodeacceso_.model.Turno

object TurnoRepository {
    private val turnos = mutableListOf<Turno>()
    private val conteos = mutableListOf<ConteoAcceso>()
    private var siguienteId = 1L

    suspend fun crearTurno(turno: Turno): Long {
        val nuevo = turno.copy(id = siguienteId)
        siguienteId++
        turnos.add(nuevo)
        return nuevo.id
    }

    suspend fun obtenerTurno(id: Long): Turno? = turnos.find { it.id == id }

    suspend fun cerrarTurno(id: Long, horaFin: String) {
        val posicion = turnos.indexOfFirst { it.id == id }
        if (posicion >= 0) {
            turnos[posicion] = turnos[posicion].copy(horaFinReal = horaFin)
        }
    }

    /** Conteos de un turno, ordenados por hora. */
    suspend fun conteosDeTurno(turnoId: Long): List<ConteoAcceso> =
        conteos.filter { it.turnoId == turnoId }.sortedBy { it.hora }

    /** Guarda el conteo de una hora: si ya existía uno para ese turno y hora, lo reemplaza. */
    suspend fun guardarConteo(conteo: ConteoAcceso) {
        conteos.removeAll { it.turnoId == conteo.turnoId && it.hora == conteo.hora }
        conteos.add(conteo)
    }
}