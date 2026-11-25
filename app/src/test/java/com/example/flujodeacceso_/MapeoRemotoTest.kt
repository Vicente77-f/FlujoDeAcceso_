package com.example.flujodeacceso_

import com.example.flujodeacceso_.data.remote.clima.ClimaApiService
import com.example.flujodeacceso_.data.remote.clima.ClimaRespuestaDto
import com.example.flujodeacceso_.data.remote.turno.TurnoDto
import com.example.flujodeacceso_.data.remote.turno.aCorreccion
import com.example.flujodeacceso_.data.remote.turno.aTurno
import com.example.flujodeacceso_.data.repository.ClimaRepository
import com.example.flujodeacceso_.domain.describirClima
import com.example.flujodeacceso_.model.Acceso
import com.example.flujodeacceso_.model.ConteoAcceso
import com.example.flujodeacceso_.model.TipoTurno
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.IOException

class MapeoRemotoTest {

    @Test
    fun turnoDelServidorSeConvierteEnTurnoDeLaApp() {
        val dto = TurnoDto(5, 2, Acceso.A1, TipoTurno.PM, "2026-10-13", "15:00", null, null, false, false)

        val turno = dto.aTurno()

        assertTrue(turno.estaAbierto)
        assertEquals("", turno.fotoUri)
        assertEquals(Acceso.A1, turno.acceso)
    }

    @Test
    fun correccionLlevaElIdDelSupervisorYLosValores() {
        val conteo = ConteoAcceso(1, Acceso.A1, "2026-10-13", 10,
            hombres = 10, mujeres = 10, menores = 2, sinClasificar = 0, transito = 5)

        val request = conteo.aCorreccion(supervisorId = 3)

        assertEquals(3L, request.supervisorId)
        assertEquals(5, request.transito)
    }

    @Test
    fun codigoDeClimaSeTraduce() {
        assertEquals("Despejado", describirClima(0))
        assertEquals("Lluvia", describirClima(61))
    }

    @Test
    fun climaSinConexionDevuelveErrorSinCaerse() = runBlocking {
        val resultado = ClimaRepository(ClimaApiSinConexion()).climaActual()
        assertTrue(resultado.isFailure)
    }
}

private class ClimaApiSinConexion : ClimaApiService {
    override suspend fun climaActual(latitud: Double, longitud: Double, campos: String): ClimaRespuestaDto =
        throw IOException("Sin conexión")
}