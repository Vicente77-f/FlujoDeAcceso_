package com.example.flujodeacceso_.model

data class Usuario(
    val id: Long = 0L,
    val nombre: String,
    val correo: String,
    val rol: Rol
)
