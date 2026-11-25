package com.example.flujodeacceso_.data.repository

import com.example.flujodeacceso_.data.local.usuario.UsuarioDao
import com.example.flujodeacceso_.data.local.usuario.UsuarioEntity
import com.example.flujodeacceso_.data.local.usuario.aModelo
import com.example.flujodeacceso_.data.remote.usuario.LoginRequestDto
import com.example.flujodeacceso_.data.remote.usuario.UsuarioApiService
import com.example.flujodeacceso_.data.remote.usuario.aUsuario
import com.example.flujodeacceso_.domain.seguridad.hashClave
import com.example.flujodeacceso_.model.Usuario
import retrofit2.HttpException
import java.io.IOException

const val MENSAJE_LOGIN_INVALIDO = "Correo o contraseña incorrectos"

class UsuarioRepository(
    private val dao: UsuarioDao,
    private val api: UsuarioApiService = UsuarioApiService.instancia  // Las pruebas pasan una API falsa
) {

    /** Primero pregunta a usuarioMS; si no hay conexión, revisa la copia local (Room). */
    suspend fun iniciarSesion(correo: String, clave: String): Result<Usuario> {
        val correoLimpio = correo.trim()
        return try {
            val usuario = api.login(LoginRequestDto(correoLimpio, clave)).aUsuario()
            // Copia local para poder entrar después sin conexión
            dao.guardar(UsuarioEntity(usuario.id, usuario.nombre, usuario.correo, usuario.rol, hashClave(clave)))
            Result.success(usuario)
        } catch (e: HttpException) {
            if (e.code() == 401 || e.code() == 400) {
                Result.failure(IllegalArgumentException(MENSAJE_LOGIN_INVALIDO))  // El servidor la rechazó
            } else {
                iniciarSesionLocal(correoLimpio, clave)                          // Error del servidor (500, 503)
            }
        } catch (e: IOException) {
            iniciarSesionLocal(correoLimpio, clave)                              // Sin red o servidor apagado
        }
    }

    private suspend fun iniciarSesionLocal(correo: String, clave: String): Result<Usuario> {
        val cuenta = dao.buscarPorCorreo(correo)
        return if (cuenta != null && cuenta.claveHash == hashClave(clave)) {
            Result.success(cuenta.aModelo())
        } else {
            Result.failure(IllegalArgumentException(MENSAJE_LOGIN_INVALIDO))
        }
    }

    suspend fun obtenerPorId(id: Long): Usuario? = dao.buscarPorId(id)?.aModelo()
}