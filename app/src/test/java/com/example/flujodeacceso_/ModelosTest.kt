package com.example.flujodeacceso_

import com.example.flujodeacceso_.model.Acceso
import com.example.flujodeacceso_.model.ConteoAcceso
import com.example.flujodeacceso_.model.ResumenVentas
import com.example.flujodeacceso_.model.MuestraTransito
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ModelosTest {

//-------------------------Acceso y ConteoAcceso--------------------------//


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
        val original = ConteoAcceso(Acceso.A2, "2026-10-13", 12, 80, 75)
        val corregido = original.copy(ingresos = 85)
        assertEquals(80, original.ingresos)
        assertEquals(85, corregido.ingresos)
    }


//-------------------------ResumenVentas y MuestraTransito--------------------------//

    @Test
    fun totalTransaccionesSumaAmbosTiposDeCaja() {
        val ventas = ResumenVentas(
            fecha = "2026-10-13",
            hora = 12,
            transaccionesTradicionales = 30,
            transaccionesAutoservicio = 20
        )
        assertEquals(50, ventas.totalTransacciones)
    }

    @Test
    fun totalTransaccionesSeRecalculaDespuesDeCopy() {
        val ventas = ResumenVentas("2026-10-13", 12, 30, 20)
        val corregidas = ventas.copy(transaccionesAutoservicio = 25)
        assertEquals(55, corregidas.totalTransacciones)
    }

    @Test
    fun dosMuestrasConLosMismosDatosSonIguales() {
        val muestra1 = MuestraTransito(Acceso.A3, "2026-10-13", 18, 20, 50, 15)
        val muestra2 = MuestraTransito(Acceso.A3, "2026-10-13", 18, 20, 50, 15)
        assertEquals(muestra1, muestra2)
    }
}