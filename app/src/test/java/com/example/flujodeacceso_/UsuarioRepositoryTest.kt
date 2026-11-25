package com.example.flujodeacceso_

import com.example.flujodeacceso_.data.local.usuario.UsuarioDao
import com.example.flujodeacceso_.data.local.usuario.UsuarioEntity
import com.example.flujodeacceso_.data.remote.usuario.LoginRequestDto
import com.example.flujodeacceso_.data.remote.usuario.UsuarioApiService
import com.example.flujodeacceso_.data.remote.usuario.UsuarioDto
import com.example.flujodeacceso_.data.repository.MENSAJE_LOGIN_INVALIDO
import com.example.flujodeacceso_.data.repository.UsuarioRepository
import com.example.flujodeacceso_.domain.seguridad.hashClave
import com.example.flujodeacceso_.model.Rol
import kotlinx.coroutines.runBlocking
import okhttp3.ResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException

class UsuarioRepositoryTest {

    private val camila = UsuarioEntity(1, "Camila R.", "camila.r@duoc.cl", Rol.EMPLEADO, hashClave("demo1234"))

    @Test
    fun loginEnLineaDevuelveRolYGuardaCopiaLocal() = runBlocking {
        val dao = UsuarioDaoEnMemoria()
        val api = ApiFalsa { UsuarioDto(3, "Supervisora Demo", "supervisor@duoc.cl", "SUPERVISOR") }

        val resultado = UsuarioRepository(dao, api).iniciarSesion("supervisor@duoc.cl", "super1234")

        assertEquals(Rol.SUPERVISOR, resultado.getOrNull()?.rol)
        assertEquals(hashClave("super1234"), dao.buscarPorId(3)?.claveHash)
    }

    @Test
    fun sinConexionEntraConLaCopiaLocal() = runBlocking {
        val api = ApiFalsa { throw IOException("Sin conexión") }

        val resultado = UsuarioRepository(UsuarioDaoEnMemoria(camila), api).iniciarSesion("camila.r@duoc.cl", "demo1234")

        assertTrue(resultado.isSuccess)
    }

    @Test
    fun sinConexionYClaveIncorrectaFalla() = runBlocking {
        val api = ApiFalsa { throw IOException("Sin conexión") }

        val resultado = UsuarioRepository(UsuarioDaoEnMemoria(camila), api).iniciarSesion("camila.r@duoc.cl", "otra123")

        assertEquals(MENSAJE_LOGIN_INVALIDO, resultado.exceptionOrNull()?.message)
    }

    @Test
    fun siElServidorRechazaLaClaveNoSeUsaLaCopiaLocal() = runBlocking {
        val api = ApiFalsa { throw HttpException(Response.error<UsuarioDto>(401, ResponseBody.create(null, ""))) }

        val resultado = UsuarioRepository(UsuarioDaoEnMemoria(camila), api).iniciarSesion("camila.r@duoc.cl", "demo1234")

        assertTrue(resultado.isFailure)
    }
}

private class ApiFalsa(private val respuesta: () -> UsuarioDto) : UsuarioApiService {
    override suspend fun login(request: LoginRequestDto): UsuarioDto = respuesta()
    override suspend fun obtenerUsuario(id: Long): UsuarioDto = respuesta()
}

private class UsuarioDaoEnMemoria(vararg iniciales: UsuarioEntity) : UsuarioDao {
    private val cuentas = iniciales.toMutableList()
    override suspend fun buscarPorCorreo(correo: String) = cuentas.find { it.correo.equals(correo, ignoreCase = true) }
    override suspend fun buscarPorId(id: Long) = cuentas.find { it.id == id }
    override suspend fun guardar(usuario: UsuarioEntity) {
        cuentas.removeAll { it.id == usuario.id }
        cuentas.add(usuario)
    }
}