package com.example.flujodeacceso_

import com.example.flujodeacceso_.model.Acceso
import com.example.flujodeacceso_.model.ConteoAcceso
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ModelosTest {

    @Test
    fun existenExactamenteCuatroAccesos() {
        assertEquals(4, Acceso.entries.size)
    }

    @Test
    fun conteoSinSalidasSeRepresentaConNull() {
        val conteo = ConteoAcceso(
            acceso = Acceso.A1,
            fecha = "2026-10-13",
            hora = 10,
            ingresos = 120,
            salidas = null
        )
        assertNull(conteo.salidas)
    }

    @Test
    fun copyCreaUnConteoNuevoSinModificarElOrigen() {
        val original = ConteoAcceso (Acceso.A2, "2026-10-13", 12, 80, 75)
        val corregido = original.copy(ingresos =85)
        assertEquals(80, original.ingresos)
        assertEquals(85, corregido.ingresos)
    }
}