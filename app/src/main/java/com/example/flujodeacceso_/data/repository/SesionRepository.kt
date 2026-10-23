package com.example.flujodeacceso_.data.repository

import com.example.flujodeacceso_.model.Usuario
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

object SesionRepository {
    private val _usuarioActual = MutableStateFlow<Usuario?>(null)
    val usuarioActual: StateFlow<Usuario?> = _usuarioActual

    fun iniciar(usuario: Usuario) {
        _usuarioActual.value = usuario
    }

    fun cerrar() {
        _usuarioActual.value = null
    }
}