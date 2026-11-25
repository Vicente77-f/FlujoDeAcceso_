package com.example.flujodeacceso_.data.repository

import com.example.flujodeacceso_.data.local.turno.TurnoDao
import com.example.flujodeacceso_.data.local.turno.TurnoEntity
import com.example.flujodeacceso_.data.local.turno.aEntidad
import com.example.flujodeacceso_.data.local.turno.aModelo
import com.example.flujodeacceso_.data.remote.turno.CerrarTurnoRequestDto
import com.example.flujodeacceso_.data.remote.turno.TurnoApiService
import com.example.flujodeacceso_.data.remote.turno.TurnoDto
import com.example.flujodeacceso_.data.remote.turno.aConteo
import com.example.flujodeacceso_.data.remote.turno.aCorreccion
import com.example.flujodeacceso_.data.remote.turno.aRequest
import com.example.flujodeacceso_.model.ConteoAcceso
import com.example.flujodeacceso_.model.Turno
import com.google.gson.JsonParser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import retrofit2.HttpException
import java.io.IOException

class TurnoRepository(
    private val dao: TurnoDao,
    private val api: TurnoApiService = TurnoApiService.instancia
) {

    // ---------- Escrituras: primero Room, después el servidor ----------

    suspend fun crearTurno(turno: Turno): Long {
        val id = dao.insertarTurno(turno.aEntidad())   // Sin idRemoto: pendiente de enviar
        sincronizarEnSegundoPlano()
        return id
    }

    /** Guarda el conteo de una hora: si ya existía para ese turno y hora, lo reemplaza. */
    suspend fun guardarConteo(conteo: ConteoAcceso) {
        dao.guardarConteo(conteo.aEntidad())            // sincronizado = false
        sincronizarEnSegundoPlano()
    }

    suspend fun cerrarTurno(id: Long, horaFin: String) {
        dao.cerrarTurno(id, horaFin)                     // También lo marca como pendiente
        sincronizarEnSegundoPlano()
    }

    suspend fun marcarExportado(id: Long) {
        val turno = dao.buscarTurno(id) ?: return
        dao.actualizarTurno(turno.copy(exportado = true, sincronizado = false))
        sincronizarEnSegundoPlano()
    }

    /** Sin conexión NO se borra: si se borrara solo en Room, el turno volvería al descargar. */
    suspend fun eliminarTurno(turno: Turno): Result<Unit> {
        val entidad = dao.buscarTurno(turno.id) ?: return Result.success(Unit)
        val idRemoto = entidad.idRemoto
        if (idRemoto != null) {
            try {
                val respuesta = api.eliminarTurno(idRemoto)
                if (!respuesta.isSuccessful && respuesta.code() != 404) {
                    return Result.failure(IllegalStateException("El servidor no permitió eliminar el turno"))
                }
            } catch (e: IOException) {
                return Result.failure(IllegalStateException(MENSAJE_SIN_CONEXION))
            }
        }
        dao.eliminarTurno(entidad)
        return Result.success(Unit)
    }

    /** Solo el supervisor y solo con conexión: turnoMS verifica su rol con usuarioMS. */
    suspend fun corregirConteo(conteo: ConteoAcceso, supervisorId: Long): Result<Unit> {
        val turno = dao.buscarTurno(conteo.turnoId)
            ?: return Result.failure(IllegalStateException("No se encontró el turno"))
        val idRemoto = turno.idRemoto
            ?: return Result.failure(IllegalStateException("Este turno aún no llega al servidor. Intenta más tarde."))
        return try {
            api.corregirConteo(idRemoto, conteo.hora, conteo.aCorreccion(supervisorId))
            dao.guardarConteo(conteo.aEntidad().copy(sincronizado = true))
            dao.actualizarTurno(turno.copy(corregido = true, exportado = false))
            Result.success(Unit)
        } catch (e: HttpException) {
            Result.failure(IllegalStateException(mensajeDelServidor(e)))
        } catch (e: IOException) {
            Result.failure(IllegalStateException(MENSAJE_SIN_CONEXION))
        }
    }

    // ---------- Lecturas: siempre desde Room ----------

    suspend fun obtenerTurno(id: Long): Turno? = dao.buscarTurno(id)?.aModelo()

    suspend fun conteosDeTurno(turnoId: Long): List<ConteoAcceso> =
        dao.conteosDeTurno(turnoId).map { it.aModelo() }

    suspend fun turnoAbiertoDe(usuarioId: Long): Turno? = dao.turnoAbiertoDe(usuarioId)?.aModelo()

    fun turnosDeFecha(fecha: String): Flow<List<Turno>> =
        dao.turnosDeFecha(fecha).map { lista -> lista.map { it.aModelo() } }

    fun conteosDeFecha(fecha: String): Flow<List<ConteoAcceso>> =
        dao.conteosDeFecha(fecha).map { lista -> lista.map { it.aModelo() } }

    // ---------- Sincronización ----------

    private fun sincronizarEnSegundoPlano() {
        alcance.launch { sincronizarPendientes() }
    }

    /** Envía a turnoMS todo lo pendiente. Devuelve false si no hubo conexión. */
    suspend fun sincronizarPendientes(): Boolean = mutex.withLock {
        try {
            // 1) Turnos creados sin conexión
            for (turno in dao.turnosSinIdRemoto()) {
                try {
                    val creado = api.crearTurno(turno.aModelo().aRequest())
                    dao.asignarIdRemoto(turno.id, creado.id)
                } catch (e: HttpException) { /* Rechazado: queda pendiente */ }
            }
            // 2) Conteos pendientes: se envía el valor actual de cada hora
            for (conteo in dao.conteosPendientes()) {
                val idRemoto = dao.buscarTurno(conteo.turnoId)?.idRemoto ?: continue
                try {
                    api.guardarConteo(idRemoto, conteo.hora, conteo.aModelo().aRequest())
                    dao.marcarConteoSincronizado(
                        conteo.turnoId, conteo.hora, conteo.hombres, conteo.mujeres,
                        conteo.menores, conteo.sinClasificar, conteo.transito
                    )
                } catch (e: HttpException) { /* Por ejemplo 409: queda pendiente */ }
            }
            // 3) Cierre y exportación: solo cuando ya subieron todos sus conteos
            for (turno in dao.turnosPendientes()) {
                val idRemoto = turno.idRemoto ?: continue
                if (dao.contarConteosPendientes(turno.id) > 0) continue
                try {
                    turno.horaFinReal?.let { api.cerrarTurno(idRemoto, CerrarTurnoRequestDto(it)) }
                    if (turno.exportado) api.marcarExportado(idRemoto)
                    dao.marcarTurnoSincronizado(turno.id, turno.exportado, turno.horaFinReal)
                } catch (e: HttpException) { /* Queda pendiente */ }
            }
            true
        } catch (e: IOException) {
            false   // Sin conexión: todo sigue guardado en Room y se reintenta después
        }
    }

    /** Trae del servidor los turnos y conteos de un día y actualiza Room. Devuelve false si no hubo conexión. */
    suspend fun descargarDia(fecha: String): Boolean = mutex.withLock {
        try {
            val turnosServidor = api.turnosDeFecha(fecha)
            val conteosServidor = api.conteosDeFecha(fecha)
            val idLocalDe = mutableMapOf<Long, Long>()             // idRemoto → id en Room

            for (dto in turnosServidor) {
                val local = dao.buscarPorIdRemoto(dto.id)
                idLocalDe[dto.id] = when {
                    local == null -> dao.insertarTurno(dto.aEntidadLocal(idLocal = 0L, fotoLocal = null))
                    local.sincronizado -> {
                        dao.actualizarTurno(dto.aEntidadLocal(idLocal = local.id, fotoLocal = local.fotoUri))
                        local.id
                    }
                    else -> local.id                                  // Tiene cambios sin subir: no se pisan
                }
            }
            for (dto in conteosServidor) {
                val turnoId = idLocalDe[dto.turnoId] ?: continue
                val local = dao.buscarConteo(turnoId, dto.hora)
                if (local != null && !local.sincronizado) continue
                dao.guardarConteo(dto.aConteo().copy(turnoId = turnoId).aEntidad().copy(sincronizado = true))
            }
            // Turnos que otro supervisor eliminó en el servidor
            val idsServidor = turnosServidor.map { it.id }.toSet()
            dao.listaTurnosDeFecha(fecha)
                .filter { it.sincronizado && it.idRemoto != null && it.idRemoto !in idsServidor }
                .forEach { dao.eliminarTurno(it) }
            true
        } catch (e: IOException) {
            false
        } catch (e: HttpException) {
            false
        }
    }

    companion object {
        // Compartidos por todos los repositorios: nunca hay dos sincronizaciones a la vez
        private val mutex = Mutex()
        private val alcance = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        const val MENSAJE_SIN_CONEXION = "Sin conexión con el servidor. Intenta más tarde."
    }
}

/** Turno del servidor → fila de Room. La foto local se conserva: la del otro teléfono no existe aquí. */
private fun TurnoDto.aEntidadLocal(idLocal: Long, fotoLocal: String?): TurnoEntity = TurnoEntity(
    id = idLocal, usuarioId = usuarioId, acceso = acceso, tipo = tipo, fecha = fecha,
    horaInicioReal = horaInicioReal, horaFinReal = horaFinReal,
    fotoUri = fotoLocal ?: fotoUri ?: "", exportado = exportado,
    idRemoto = id, sincronizado = true, corregido = corregido
)

/** Lee {"mensaje": "..."} del cuerpo de error del backend. */
private fun mensajeDelServidor(e: HttpException): String {
    val cuerpo = e.response()?.errorBody()?.string()
    val mensaje = try {
        cuerpo?.let { JsonParser.parseString(it).asJsonObject.get("mensaje")?.asString }
    } catch (ex: Exception) {
        null
    }
    return mensaje ?: "El servidor respondió con error ${e.code()}"
}