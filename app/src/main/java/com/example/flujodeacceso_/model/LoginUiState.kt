package com.example.flujodeacceso_.model

data class LoginUiState(
    val correo: String = "",
    val clave: String = "",
    val errorCorreo: String? = null,
    val errorClave: String? = null,
    val cargando: Boolean = false,
    val errorGeneral: String? = null,
    val usuarioIngresado: Usuario? = null
)