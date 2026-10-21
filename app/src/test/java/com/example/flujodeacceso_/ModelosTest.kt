package com.example.flujodeacceso_

import com.example.flujodeacceso_.model.Acceso
import com.example.flujodeacceso_.model.ConteoAcceso
import com.example.flujodeacceso_.model.ResumenVentas
import com.example.flujodeacceso_.model.TipoTurno
import com.example.flujodeacceso_.model.Turno
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ModelosTest {

    // ---------- Acceso ----------
    @Test
    fun existenExactamenteCuatroAccesos() {
        assertEquals(4, Acceso.entries.size)
    }

    // ---------- ConteoAcceso ----------
    @Test
    fun totalIngresosSumaLasCuatroCategoriasSinContarTransito() {
        val conteo = ConteoAcceso(
            turnoId = 1, acceso = Acceso.A1, fecha = "2026-10-13", hora = 10,
            hombres = 30, mujeres = 36, menores = 10, sinClasificar = 2, transito = 14
        )
        assertEquals(78, conteo.totalIngresos)
    }

    @Test
    fun conteoNuevoPartedeCero() {
        val conteo = ConteoAcceso(turnoId = 1, acceso = Acceso.A2, fecha = "2026-10-13", hora = 11)
        assertEquals(0, conteo.totalIngresos)
    }

    @Test
    fun copyCreaUnConteoNuevoSinModificarElOriginal() {
        val original = ConteoAcceso(turnoId = 1, acceso = Acceso.A2, fecha = "2026-10-13", hora = 12, mujeres = 5)
        val actualizado = original.copy(mujeres = original.mujeres + 1)
        assertEquals(5, original.mujeres)
        assertEquals(6, actualizado.mujeres)
    }

    // ---------- TipoTurno ----------
    @Test
    fun lasQuinceHorasPertenecenSoloAlTurnoPM() {
        assertTrue(TipoTurno.AM.incluyeHora(14))
        assertFalse(TipoTurno.AM.incluyeHora(15))
        assertTrue(TipoTurno.PM.incluyeHora(15))
        assertFalse(TipoTurno.PM.incluyeHora(20))
    }

    @Test
    fun horarioSeMuestraComoTexto() {
        assertEquals("10:00 - 15:00", TipoTurno.AM.horario)
    }

    // ---------- Turno ----------
    @Test
    fun turnoSinHoraDeFinEstaAbierto() {
        val turno = Turno(
            usuarioId = 1, acceso = Acceso.A1, tipo = TipoTurno.AM, fecha = "2026-10-13",
            horaInicioReal = "10:02", horaFinReal = null, fotoUri = "content://demo/foto1"
        )
        assertTrue(turno.estaAbierto)
        assertFalse(turno.copy(horaFinReal = "15:00").estaAbierto)
    }

    // ---------- ResumenVentas ----------
    @Test
    fun totalTransaccionesSumaAmbosTiposDeCaja() {
        val ventas = ResumenVentas("2026-10-13", 12, 30, 20)
        assertEquals(50, ventas.totalTransacciones)
    }
}