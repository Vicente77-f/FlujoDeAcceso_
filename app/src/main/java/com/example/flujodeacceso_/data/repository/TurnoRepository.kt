package com.example.flujodeacceso_.data.repository

import com.example.flujodeacceso_.data.local.turno.TurnoDao
import com.example.flujodeacceso_.data.local.turno.aEntidad
import com.example.flujodeacceso_.data.local.turno.aModelo
import com.example.flujodeacceso_.model.ConteoAcceso
import com.example.flujodeacceso_.model.Turno
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class TurnoRepository(private val dao: TurnoDao) {

    suspend fun crearTurno(turno: Turno): Long = dao.insertarTurno(turno.aEntidad())

    suspend fun obtenerTurno(id: Long): Turno? = dao.buscarTurno(id)?.aModelo()

    suspend fun cerrarTurno(id: Long, horaFin: String) = dao.cerrarTurno(id, horaFin)

    /** Conteos de un turno, ordenados por hora. */
    suspend fun conteosDeTurno(turnoId: Long): List<ConteoAcceso> =
        dao.conteosDeTurno(turnoId).map { it.aModelo() }

    /** Guarda el conteo de una hora: si ya existía para ese turno y hora, lo reemplaza. */
    suspend fun guardarConteo(conteo: ConteoAcceso) = dao.guardarConteo(conteo.aEntidad())

    // ---------- Para el resumen, el panel y la reapertura de la app ----------
    suspend fun turnoAbiertoDe(usuarioId: Long): Turno? = dao.turnoAbiertoDe(usuarioId)?.aModelo()

    suspend fun marcarExportado(id: Long) {
        val turno = dao.buscarTurno(id) ?: return
        dao.actualizarTurno(turno.copy(exportado = true))
    }

    suspend fun actualizarConteo(conteo: ConteoAcceso) = dao.actualizarConteo(conteo.aEntidad())

    suspend fun eliminarTurno(turno: Turno) = dao.eliminarTurno(turno.aEntidad())

    fun turnosDeFecha(fecha: String): Flow<List<Turno>> =
        dao.turnosDeFecha(fecha).map { lista -> lista.map { it.aModelo() } }

    fun conteosDeFecha(fecha: String): Flow<List<ConteoAcceso>> =
        dao.conteosDeFecha(fecha).map { lista -> lista.map { it.aModelo() } }
}