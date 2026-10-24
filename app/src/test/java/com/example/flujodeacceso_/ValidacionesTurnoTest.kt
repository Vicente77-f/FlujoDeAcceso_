package com.example.flujodeacceso_

import com.example.flujodeacceso_.domain.validation.validarAcceso
import com.example.flujodeacceso_.domain.validation.validarFotosPuesto
import com.example.flujodeacceso_.domain.validation.validarTipoTurno
import com.example.flujodeacceso_.model.Acceso
import com.example.flujodeacceso_.model.TipoTurno
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class ValidacionesTurnoTest {

    @Test
    fun turnoEsObligatorio() {
        assertNotNull(validarTipoTurno(null))
        assertNull(validarTipoTurno(TipoTurno.AM))
    }

    @Test
    fun accesoEsObligatorio() {
        assertNotNull(validarAcceso(null))
        assertNull(validarAcceso(Acceso.A3))
    }

    @Test
    fun fotoEsObligatoria() {
        assertNotNull(validarFotosPuesto(null))
        assertNotNull(validarFotosPuesto("   "))
        assertNull(validarFotosPuesto("content://fotos/puesto_1.jpg"))
    }
}