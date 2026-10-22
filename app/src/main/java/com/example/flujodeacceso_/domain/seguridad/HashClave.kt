package com.example.flujodeacceso_.domain.seguridad

import java.security.MessageDigest

/** Convierte una contraseña en su hash SHA-256 (texto hexadecimal). Nunca se guarda la contraseña real. */
fun hashClave(clave: String): String =
    MessageDigest.getInstance("SHA-256")
        .digest(clave.toByteArray())
        .joinToString("") { "%02x".format(it) }