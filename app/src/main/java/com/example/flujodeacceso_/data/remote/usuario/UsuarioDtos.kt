package com.example.flujodeacceso_.data.remote.usuario

import com.example.flujodeacceso_.model.Rol
import com.example.flujodeacceso_.model.Usuario

/** Cuerpo de POST /login. Los nombres deben coincidir con los del backend. */
data class LoginRequestDto(val correo: String, val clave: String)

/** Respuesta de usuarioMS. Nunca trae la contraseña ni su hash. */
data class UsuarioDto(val id: Long, val nombre: String, val correo: String, val rol: String)

/** Si llega un rol desconocido, se asigna el de menos permisos. */
fun UsuarioDto.aUsuario(): Usuario = Usuario(
    id = id, nombre = nombre, correo = correo,
    rol = Rol.entries.find { it.name == rol } ?: Rol.EMPLEADO
)