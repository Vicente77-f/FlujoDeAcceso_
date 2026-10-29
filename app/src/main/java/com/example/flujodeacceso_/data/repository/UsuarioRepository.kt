package com.example.flujodeacceso_.data.repository

import com.example.flujodeacceso_.data.local.usuario.UsuarioDao
import com.example.flujodeacceso_.data.local.usuario.aModelo
import com.example.flujodeacceso_.domain.seguridad.hashClave
import com.example.flujodeacceso_.model.Usuario
import kotlinx.coroutines.delay

class UsuarioRepository(private val dao: UsuarioDao) {

    suspend fun iniciarSesion(correo: String, clave: String): Result<Usuario> {
        delay(700) // Pausa breve para que se vea el indicador de carga
        val cuenta = dao.buscarPorCorreo(correo.trim())
        return if (cuenta != null && cuenta.claveHash == hashClave(clave)) {
            Result.success(cuenta.aModelo())
        } else {
            // Mismo mensaje si falla el correo o la clave: no revela qué cuentas existen
            Result.failure(IllegalArgumentException("Correo o contraseña incorrectos"))
        }
    }

    suspend fun obtenerPorId(id: Long): Usuario? = dao.buscarPorId(id)?.aModelo()
}