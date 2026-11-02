package com.example.flujodeacceso_

import com.example.flujodeacceso_.domain.ENCABEZADO_CSV
import com.example.flujodeacceso_.domain.generarCsv
import com.example.flujodeacceso_.domain.nombreArchivoCsv
import com.example.flujodeacceso_.model.Acceso
import com.example.flujodeacceso_.model.ConteoAcceso
import com.example.flujodeacceso_.model.TipoTurno
import com.example.flujodeacceso_.model.Turno
import org.junit.Assert.assertEquals
import org.junit.Test

class GeneradorCsvTest {

    private val turno = Turno(
        id = 1, usuarioId = 1, acceso = Acceso.A1, tipo = TipoTurno.AM, fecha = "2026-10-13",
        horaInicioReal = "10:00", horaFinReal = "15:00", fotoUri = ""
    )

    private fun conteo(hora: Int, hombres: Int, transito: Int = 0) = ConteoAcceso(
        turnoId = 1, acceso = Acceso.A1, fecha = "2026-10-13", hora = hora,
        hombres = hombres, transito = transito
    )

    @Test
    fun primeraLineaEsElEncabezado() {
        assertEquals(ENCABEZADO_CSV, generarCsv(turno, emptyList()).lines().first())
    }

    @Test
    fun generaUnaLineaPorHoraOrdenadaPorHora() {
        val lineas = generarCsv(turno, listOf(conteo(11, 5), conteo(10, 30, transito = 14))).trim().lines()
        assertEquals(3, lineas.size)
        assertEquals("2026-10-13,AM,A1,10,30,0,0,0,14", lineas[1])
        assertEquals("2026-10-13,AM,A1,11,5,0,0,0,0", lineas[2])
    }

    @Test
    fun omiteLasHorasSinRegistros() {
        val lineas = generarCsv(turno, listOf(conteo(10, 0), conteo(12, 3))).trim().lines()
        assertEquals(2, lineas.size) // encabezado + solo la hora 12
    }

    @Test
    fun nombreDelArchivoSigueElFormato() {
        assertEquals("flujo_2026-10-13_AM_A1.csv", nombreArchivoCsv(turno))
    }
}