package com.example.flujodeacceso_

import com.example.flujodeacceso_.data.local.usuario.UsuarioDao
import com.example.flujodeacceso_.data.local.usuario.UsuarioEntity
import com.example.flujodeacceso_.data.repository.UsuarioRepository
import com.example.flujodeacceso_.domain.seguridad.hashClave
import com.example.flujodeacceso_.domain.validation.validarClave
import com.example.flujodeacceso_.domain.validation.validarCorreo
import com.example.flujodeacceso_.model.Rol
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ValidacionesLoginTest {

    @Test
    fun correoVacioEsObligatorio() {
        assertEquals("El correo es obligatorio", validarCorreo(""))
    }

    @Test
    fun correoSinArrobaEsInvalido() {
        assertEquals("Formato de correo inválido", validarCorreo("camila.tienda.cl"))
    }

    @Test
    fun correoCorrectoNoTieneError() {
        assertNull(validarCorreo("camila.r@tienda-demo.cl"))
    }

    @Test
    fun claveCortaTieneError() {
        assertEquals("Debe tener al menos 6 caracteres", validarClave("123"))
    }

    @Test
    fun mismoTextoMismoHashYDistintoTextoDistintoHash() {
        assertEquals(hashClave("demo1234"), hashClave("demo1234"))
        assertNotEquals(hashClave("demo1234"), hashClave("demo1235"))
    }

    @Test
    fun loginCorrectoDevuelveUsuarioConSuRol() = runBlocking {
        val resultado = UsuarioRepository(UsuarioDaoFalso()).iniciarSesion("supervisor@tienda-demo.cl", "super1234")
        assertTrue(resultado.isSuccess)
        assertEquals(Rol.SUPERVISOR, resultado.getOrNull()?.rol)
    }

    @Test
    fun loginConClaveIncorrectaFalla() = runBlocking {
        val resultado = UsuarioRepository(UsuarioDaoFalso()).iniciarSesion("camila.r@tienda-demo.cl", "otra-clave")
        assertTrue(resultado.isFailure)
    }
}

/** DAO falso: imita a Room con una lista en memoria para probar el Repository sin Android. */
private class UsuarioDaoFalso : UsuarioDao {
    private val cuentas = listOf(
        UsuarioEntity(1, "Camila R.", "camila.r@tienda-demo.cl", Rol.EMPLEADO, hashClave("demo1234")),
        UsuarioEntity(3, "Supervisora Demo", "supervisor@tienda-demo.cl", Rol.SUPERVISOR, hashClave("super1234"))
    )

    override suspend fun buscarPorCorreo(correo: String): UsuarioEntity? =
        cuentas.find { it.correo.equals(correo, ignoreCase = true) }

    override suspend fun buscarPorId(id: Long): UsuarioEntity? =
        cuentas.find { it.id == id }
}