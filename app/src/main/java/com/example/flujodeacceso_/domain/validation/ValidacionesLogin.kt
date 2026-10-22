package com.example.flujodeacceso_.domain.validation

private val FORMATO_CORREO = Regex("^[\\w.+-]+@[\\w-]+(\\.[\\w-]+)+$")

fun validarCorreo(correo: String): String? {
    if (correo.isBlank()) return "El correo es obligatorio"
    return if (!FORMATO_CORREO.matches(correo.trim())) "Formato de correo inválido" else null
}

fun validarClave(clave: String): String? {
    if (clave.isBlank()) return "La contraseña es obligatoria"
    if (clave.length < 6) return "Debe tener al menos 6 caracteres"
    return null
}