package com.example.flujodeacceso_.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.flujodeacceso_.data.repository.UsuarioRepository
import com.example.flujodeacceso_.domain.validation.validarClave
import com.example.flujodeacceso_.domain.validation.validarCorreo
import com.example.flujodeacceso_.model.LoginUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LoginViewModel(
    private val repositorio: UsuarioRepository = UsuarioRepository()
) : ViewModel() {

    private val _estado = MutableStateFlow(LoginUiState())
    val estado: StateFlow<LoginUiState> = _estado

    // Al escribir se actualiza el valor y se borra el error de ese campo
    fun onCorreoChange(valor: String) {
        _estado.update { it.copy(correo = valor, errorCorreo = null, errorGeneral = null) }
    }

    fun onClaveChange(valor: String) {
        _estado.update { it.copy(clave = valor, errorClave = null, errorGeneral = null) }
    }

    fun iniciarSesion() {
        val actual = _estado.value
        if (actual.cargando) return // Evita enviar dos veces

        // 1) Validación local: si hay errores, se muestran y NO se consulta al Repository
        val errorCorreo = validarCorreo(actual.correo)
        val errorClave = validarClave(actual.clave)
        if (errorCorreo != null || errorClave != null) {
            _estado.update { it.copy(errorCorreo = errorCorreo, errorClave = errorClave) }
            return
        }

        // 2) Verificación de credenciales (asíncrona)
        viewModelScope.launch {
            _estado.update { it.copy(cargando = true, errorGeneral = null) }
            val resultado = repositorio.iniciarSesion(actual.correo, actual.clave)
            _estado.update {
                resultado.fold(
                    onSuccess = { usuario -> it.copy(cargando = false, usuarioIngresado = usuario) },
                    onFailure = { error -> it.copy(cargando = false, errorGeneral = error.message) }
                )
            }
        }
    }

    /** Se llama después de navegar: limpia el formulario (la contraseña no queda en memoria). */
    fun onNavegacionRealizada() {
        _estado.value = LoginUiState()
    }
}