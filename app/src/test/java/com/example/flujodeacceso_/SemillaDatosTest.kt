package com.example.flujodeacceso_

import com.example.flujodeacceso_.data.local.database.HASH_DEMO
import com.example.flujodeacceso_.data.local.database.HASH_SUPERVISOR
import com.example.flujodeacceso_.data.local.database.conteosSemilla
import com.example.flujodeacceso_.data.local.database.turnosSemilla
import com.example.flujodeacceso_.data.local.turno.aModelo
import com.example.flujodeacceso_.domain.repartir
import com.example.flujodeacceso_.domain.seguridad.hashClave
import com.example.flujodeacceso_.domain.validation.validarConteo
import com.example.flujodeacceso_.model.TipoTurno
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class SemillaDatosTest {

    @Test
    fun repartirNoPierdePersonas() {
        assertEquals(listOf(3, 3, 4), repartir(10, listOf(1, 1, 1)))
        assertEquals(540, repartir(540, listOf(16, 20, 24, 22, 18)).sum())
    }

    @Test
    fun totalesCoincidenConElMockup() {
        val conteos = conteosSemilla().map { it.aModelo() }
        val idsAm = turnosSemilla().filter { it.tipo == TipoTurno.AM }.map { it.id }.toSet()
        assertEquals(4240, conteos.sumOf { it.totalIngresos })
        assertEquals(2010, conteos.filter { it.turnoId in idsAm }.sumOf { it.totalIngresos })
        assertEquals(1020, conteos.sumOf { it.transito })
        assertEquals(40, conteos.size) // 8 turnos x 5 horas
    }

    @Test
    fun todosLosConteosSembradosSonValidos() {
        conteosSemilla().forEach { assertNull(validarConteo(it.aModelo())) }
    }

    @Test
    fun hashesSembradosCorrespondenALasClavesDemo() {
        assertEquals(hashClave("demo1234"), HASH_DEMO)
        assertEquals(hashClave("super1234"), HASH_SUPERVISOR)
    }
}