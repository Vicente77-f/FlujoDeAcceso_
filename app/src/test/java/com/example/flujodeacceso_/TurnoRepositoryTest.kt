package com.example.flujodeacceso_

import com.example.flujodeacceso_.data.local.turno.ConteoAccesoEntity
import com.example.flujodeacceso_.data.local.turno.TurnoDao
import com.example.flujodeacceso_.data.local.turno.TurnoEntity
import com.example.flujodeacceso_.data.remote.turno.CerrarTurnoRequestDto
import com.example.flujodeacceso_.data.remote.turno.ConteoDto
import com.example.flujodeacceso_.data.remote.turno.ConteoRequestDto
import com.example.flujodeacceso_.data.remote.turno.CorreccionRequestDto
import com.example.flujodeacceso_.data.remote.turno.TurnoApiService
import com.example.flujodeacceso_.data.remote.turno.TurnoDto
import com.example.flujodeacceso_.data.remote.turno.TurnoRequestDto
import com.example.flujodeacceso_.data.repository.TurnoRepository
import com.example.flujodeacceso_.model.Acceso
import com.example.flujodeacceso_.model.ConteoAcceso
import com.example.flujodeacceso_.model.TipoTurno
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException

class TurnoRepositoryTest {

    private val fecha = "2026-11-25"

    private fun turnoLocal(id: Long, idRemoto: Long? = null, horaFin: String? = null, sincronizado: Boolean = false) =
        TurnoEntity(
            id = id, usuarioId = 1, acceso = Acceso.A1, tipo = TipoTurno.AM, fecha = fecha,
            horaInicioReal = "10:00", horaFinReal = horaFin, fotoUri = "",
            idRemoto = idRemoto, sincronizado = sincronizado
        )

    private fun conteoLocal(turnoId: Long, hora: Int, sincronizado: Boolean = false) =
        ConteoAccesoEntity(turnoId, Acceso.A1, fecha, hora, 3, 2, 1, 0, 1, sincronizado)

    private val correccion = ConteoAcceso(
        turnoId = 1, acceso = Acceso.A1, fecha = fecha, hora = 10,
        hombres = 5, mujeres = 4, menores = 1, sinClasificar = 0, transito = 2
    )

    @Test
    fun sincronizarEnviaTurnoLuegoConteosYAlFinalElCierre() = runBlocking {
        val dao = TurnoDaoEnMemoria(mutableListOf(turnoLocal(1, horaFin = "15:00")), mutableListOf(conteoLocal(1, 10)))
        val api = TurnoApiFalsa()

        val conectado = TurnoRepository(dao, api).sincronizarPendientes()

        assertTrue(conectado)
        assertEquals(listOf("crear", "conteo 50/10", "cerrar 50"), api.llamadas)
        assertEquals(50L, dao.turnos.first().idRemoto)
        assertTrue(dao.turnos.first().sincronizado)
        assertTrue(dao.conteos.first().sincronizado)
    }

    @Test
    fun sinConexionTodoQuedaPendienteEnRoom() = runBlocking {
        val dao = TurnoDaoEnMemoria(mutableListOf(turnoLocal(1)), mutableListOf(conteoLocal(1, 10)))

        val conectado = TurnoRepository(dao, TurnoApiFalsa(sinConexion = true)).sincronizarPendientes()

        assertFalse(conectado)
        assertNull(dao.turnos.first().idRemoto)
        assertFalse(dao.conteos.first().sincronizado)
    }

    @Test
    fun noSeCierraEnElServidorMientrasQuedenConteosPendientes() = runBlocking {
        val dao = TurnoDaoEnMemoria(
            mutableListOf(turnoLocal(1, idRemoto = 50, horaFin = "15:00")),
            mutableListOf(conteoLocal(1, 10))
        )
        val api = TurnoApiFalsa(rechazarConteos = true)

        TurnoRepository(dao, api).sincronizarPendientes()

        assertEquals(listOf("conteo 50/10"), api.llamadas)   // Nunca se envió "cerrar"
        assertFalse(dao.turnos.first().sincronizado)
    }

    @Test
    fun correccionExitosaMarcaElTurnoComoCorregido() = runBlocking {
        val dao = TurnoDaoEnMemoria(mutableListOf(turnoLocal(1, idRemoto = 50, horaFin = "15:00", sincronizado = true)))

        val resultado = TurnoRepository(dao, TurnoApiFalsa()).corregirConteo(correccion, supervisorId = 3)

        assertTrue(resultado.isSuccess)
        assertTrue(dao.turnos.first().corregido)
        assertEquals(5, dao.conteos.first().hombres)
    }

    @Test
    fun correccionDeUnEmpleadoMuestraElMensajeDelServidor() = runBlocking {
        val dao = TurnoDaoEnMemoria(mutableListOf(turnoLocal(1, idRemoto = 50, horaFin = "15:00", sincronizado = true)))
        val api = TurnoApiFalsa(errorCorreccion = errorHttp(403, "Solo un supervisor puede corregir"))

        val resultado = TurnoRepository(dao, api).corregirConteo(correccion, supervisorId = 1)

        assertEquals("Solo un supervisor puede corregir", resultado.exceptionOrNull()?.message)
        assertFalse(dao.turnos.first().corregido)
    }

    @Test
    fun correccionSinConexionAvisaYNoGuarda() = runBlocking {
        val dao = TurnoDaoEnMemoria(mutableListOf(turnoLocal(1, idRemoto = 50, horaFin = "15:00", sincronizado = true)))

        val resultado = TurnoRepository(dao, TurnoApiFalsa(sinConexion = true)).corregirConteo(correccion, supervisorId = 3)

        assertEquals(TurnoRepository.MENSAJE_SIN_CONEXION, resultado.exceptionOrNull()?.message)
        assertTrue(dao.conteos.isEmpty())
    }
}

/** Respuesta de error como la que envía el backend: {"mensaje": "..."} */
private fun errorHttp(codigo: Int, mensaje: String) = HttpException(
    Response.error<Any>(
        codigo,
        "{\"mensaje\":\"$mensaje\"}".toResponseBody("application/json".toMediaTypeOrNull())
    )
)

/** API falsa: anota cada llamada para comprobar el ORDEN de la sincronización. */
private class TurnoApiFalsa(
    private val sinConexion: Boolean = false,
    private val rechazarConteos: Boolean = false,
    private val errorCorreccion: HttpException? = null
) : TurnoApiService {
    val llamadas = mutableListOf<String>()

    private fun revisarConexion() {
        if (sinConexion) throw IOException("Sin conexión")
    }

    private fun turnoDto(id: Long, horaFin: String? = null) =
        TurnoDto(id, 1, Acceso.A1, TipoTurno.AM, "2026-11-25", "10:00", horaFin, "", false, false)

    private fun conteoDto(id: Long, hora: Int) = ConteoDto(id, Acceso.A1, "2026-11-25", hora, 0, 0, 0, 0, 0)

    override suspend fun crearTurno(request: TurnoRequestDto): TurnoDto {
        revisarConexion(); llamadas += "crear"; return turnoDto(50)
    }
    override suspend fun turnosDeFecha(fecha: String): List<TurnoDto> {
        revisarConexion(); return emptyList()
    }
    override suspend fun obtenerTurno(id: Long): TurnoDto {
        revisarConexion(); return turnoDto(id)
    }
    override suspend fun cerrarTurno(id: Long, request: CerrarTurnoRequestDto): TurnoDto {
        revisarConexion(); llamadas += "cerrar $id"; return turnoDto(id, request.horaFin)
    }
    override suspend fun marcarExportado(id: Long): TurnoDto {
        revisarConexion(); llamadas += "exportado $id"; return turnoDto(id)
    }
    override suspend fun eliminarTurno(id: Long): Response<Unit> {
        revisarConexion(); llamadas += "eliminar $id"; return Response.success(Unit)
    }
    override suspend fun conteosDeTurno(id: Long): List<ConteoDto> {
        revisarConexion(); return emptyList()
    }
    override suspend fun guardarConteo(id: Long, hora: Int, request: ConteoRequestDto): ConteoDto {
        revisarConexion(); llamadas += "conteo $id/$hora"
        if (rechazarConteos) throw errorHttp(409, "El turno está cerrado")
        return conteoDto(id, hora)
    }
    override suspend fun corregirConteo(id: Long, hora: Int, request: CorreccionRequestDto): ConteoDto {
        revisarConexion(); llamadas += "corregir $id/$hora"
        errorCorreccion?.let { throw it }
        return conteoDto(id, hora)
    }
    override suspend fun conteosDeFecha(fecha: String): List<ConteoDto> {
        revisarConexion(); return emptyList()
    }
}

/** DAO falso: imita a Room con listas en memoria. */
private class TurnoDaoEnMemoria(
    val turnos: MutableList<TurnoEntity> = mutableListOf(),
    val conteos: MutableList<ConteoAccesoEntity> = mutableListOf()
) : TurnoDao {

    override suspend fun insertarTurno(turno: TurnoEntity): Long {
        val id = if (turno.id == 0L) (turnos.maxOfOrNull { it.id } ?: 0L) + 1 else turno.id
        turnos += turno.copy(id = id)
        return id
    }
    override suspend fun actualizarTurno(turno: TurnoEntity) = cambiarTurno(turno.id) { turno }
    override suspend fun eliminarTurno(turno: TurnoEntity) {
        turnos.removeAll { it.id == turno.id }
        conteos.removeAll { it.turnoId == turno.id }
    }
    override suspend fun buscarTurno(id: Long) = turnos.find { it.id == id }
    override suspend fun cerrarTurno(id: Long, horaFin: String) =
        cambiarTurno(id) { it.copy(horaFinReal = horaFin, sincronizado = false) }
    override suspend fun turnoAbiertoDe(usuarioId: Long) =
        turnos.lastOrNull { it.usuarioId == usuarioId && it.horaFinReal == null }
    override fun turnosDeFecha(fecha: String): Flow<List<TurnoEntity>> = flowOf(turnos.filter { it.fecha == fecha })

    override suspend fun guardarConteo(conteo: ConteoAccesoEntity) {
        conteos.removeAll { it.turnoId == conteo.turnoId && it.hora == conteo.hora }
        conteos += conteo
    }
    override suspend fun actualizarConteo(conteo: ConteoAccesoEntity) = guardarConteo(conteo)
    override suspend fun conteosDeTurno(turnoId: Long) = conteos.filter { it.turnoId == turnoId }.sortedBy { it.hora }
    override fun conteosDeFecha(fecha: String): Flow<List<ConteoAccesoEntity>> = flowOf(conteos.filter { it.fecha == fecha })

    override suspend fun turnosSinIdRemoto() = turnos.filter { it.idRemoto == null }
    override suspend fun asignarIdRemoto(id: Long, idRemoto: Long) = cambiarTurno(id) { it.copy(idRemoto = idRemoto) }
    override suspend fun turnosPendientes() = turnos.filter { it.idRemoto != null && !it.sincronizado }
    override suspend fun marcarTurnoSincronizado(id: Long, exportado: Boolean, horaFin: String?) =
        cambiarTurno(id) { if (it.exportado == exportado && it.horaFinReal == horaFin) it.copy(sincronizado = true) else it }
    override suspend fun buscarPorIdRemoto(idRemoto: Long) = turnos.find { it.idRemoto == idRemoto }
    override suspend fun listaTurnosDeFecha(fecha: String) = turnos.filter { it.fecha == fecha }
    override suspend fun buscarConteo(turnoId: Long, hora: Int) = conteos.find { it.turnoId == turnoId && it.hora == hora }
    override suspend fun conteosPendientes() = conteos.filter { !it.sincronizado }
    override suspend fun contarConteosPendientes(turnoId: Long) = conteos.count { it.turnoId == turnoId && !it.sincronizado }
    override suspend fun marcarConteoSincronizado(
        turnoId: Long, hora: Int, hombres: Int, mujeres: Int, menores: Int, sinClasificar: Int, transito: Int
    ) {
        val i = conteos.indexOfFirst {
            it.turnoId == turnoId && it.hora == hora && it.hombres == hombres && it.mujeres == mujeres &&
                    it.menores == menores && it.sinClasificar == sinClasificar && it.transito == transito
        }
        if (i >= 0) conteos[i] = conteos[i].copy(sincronizado = true)
    }

    private fun cambiarTurno(id: Long, cambio: (TurnoEntity) -> TurnoEntity) {
        val i = turnos.indexOfFirst { it.id == id }
        if (i >= 0) turnos[i] = cambio(turnos[i])
    }
}