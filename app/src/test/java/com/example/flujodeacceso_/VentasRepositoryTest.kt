package com.example.flujodeacceso_

import com.example.flujodeacceso_.data.local.database.FECHA_DEMO
import com.example.flujodeacceso_.data.repository.VentasRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class VentasRepositoryTest {

    @Test
    fun ventasDemoCoincidenConElMockup() {
        val ventas = VentasRepository.ventasDe(FECHA_DEMO)
        assertEquals(412, ventas.sumOf { it.transaccionesTradicionales })
        assertEquals(298, ventas.sumOf { it.transaccionesAutoservicio })
        assertEquals(710, ventas.sumOf { it.totalTransacciones })
    }

    @Test
    fun unDiaSinDatosNoTieneVentas() {
        assertTrue(VentasRepository.ventasDe("2026-11-02").isEmpty())
    }
}