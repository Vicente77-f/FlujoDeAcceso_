package com.example.flujodeacceso_

import com.example.flujodeacceso_.domain.sumar
import com.example.flujodeacceso_.domain.validation.validarConteo
import com.example.flujodeacceso_.domain.valor
import com.example.flujodeacceso_.model.Acceso
import com.example.flujodeacceso_.model.CategoriaConteo
import com.example.flujodeacceso_.model.ConteoAcceso
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class ContadorLogicaTest {

    private val vacio = ConteoAcceso(turnoId = 1, acceso = Acceso.A1, fecha = "2026-10-13", hora = 10)

    @Test
    fun sumarAumentaSoloLaCategoriaIndicada() {
        val resultado = vacio.sumar(CategoriaConteo.MUJER, 1)
        assertEquals(1, resultado.valor(CategoriaConteo.MUJER))
        assertEquals(0, resultado.valor(CategoriaConteo.HOMBRE))
    }

    @Test
    fun noSePuedeQuedarEnNegativo() {
        assertNotNull(validarConteo(vacio.sumar(CategoriaConteo.HOMBRE, -1)))
    }

    @Test
    fun transitoNoPuedeSuperarLosIngresos() {
        assertNotNull(validarConteo(vacio.sumar(CategoriaConteo.TRANSITO, 1)))
    }

    @Test
    fun transitoIgualAlTotalEsValido() {
        val conteo = vacio.sumar(CategoriaConteo.HOMBRE, 1).sumar(CategoriaConteo.TRANSITO, 1)
        assertNull(validarConteo(conteo))
    }
}