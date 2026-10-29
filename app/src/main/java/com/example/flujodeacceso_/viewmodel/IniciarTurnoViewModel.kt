package com.example.flujodeacceso_.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.flujodeacceso_.data.local.database.AppDatabase
import com.example.flujodeacceso_.data.repository.SesionRepository
import com.example.flujodeacceso_.data.repository.TurnoRepository
import com.example.flujodeacceso_.domain.fechaHoy
import com.example.flujodeacceso_.domain.horaActual
import com.example.flujodeacceso_.domain.horaActualTexto
import com.example.flujodeacceso_.domain.validation.validarAcceso
import com.example.flujodeacceso_.domain.validation.validarFotosPuesto
import com.example.flujodeacceso_.domain.validation.validarTipoTurno
import com.example.flujodeacceso_.model.Acceso
import com.example.flujodeacceso_.model.IniciarTurnoUiState
import com.example.flujodeacceso_.model.TipoTurno
import com.example.flujodeacceso_.model.Turno
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class IniciarTurnoViewModel(application: Application) : AndroidViewModel(application) {

    private val turnoRepository = TurnoRepository(AppDatabase.obtener(application).turnoDao())

    private val _estado = MutableStateFlow(
        IniciarTurnoUiState(
            nombreUsuario = SesionRepository.usuarioActual.value?.nombre ?: "",
            // Sugerencia: preselecciona el turno que corresponde a la hora actual (si hay uno)
            tipo = TipoTurno.entries.find { it.incluyeHora(horaActual()) }
        )
    )
    val estado: StateFlow<IniciarTurnoUiState> = _estado

    fun onTipoChange(tipo: TipoTurno) {
        _estado.update { it.copy(tipo = tipo, errorTipo = null) }
    }

    fun onAccesoChange(acceso: Acceso) {
        _estado.update { it.copy(acceso = acceso, errorAcceso = null) }
    }

    fun onFotoTomada(uri: String) {
        _estado.update { it.copy(fotoUri = uri, horaFoto = horaActualTexto(), errorFoto = null) }
    }

    fun comenzarTurno() {
        val actual = _estado.value
        if (actual.guardando) return

        // 1) Validación de los tres campos (se muestran todos los errores a la vez)
        val errorTipo = validarTipoTurno(actual.tipo)
        val errorAcceso = validarAcceso(actual.acceso)
        val errorFoto = validarFotosPuesto(actual.fotoUri)
        if (errorTipo != null || errorAcceso != null || errorFoto != null) {
            _estado.update { it.copy(errorTipo = errorTipo, errorAcceso = errorAcceso, errorFoto = errorFoto) }
            return
        }

        // 2) Copias locales: Kotlin ya sabe que no son null (smart cast)
        val tipo = actual.tipo ?: return
        val acceso = actual.acceso ?: return
        val foto = actual.fotoUri ?: return
        val usuario = SesionRepository.usuarioActual.value
        if (usuario == null) {
            _estado.update { it.copy(errorGeneral = "La sesión expiró. Vuelve a iniciar sesión.") }
            return
        }

        // 3) Guardar el turno en la tabla turnos (INSERT)
        viewModelScope.launch {
            _estado.update { it.copy(guardando = true, errorGeneral = null) }
            val id = turnoRepository.crearTurno(
                Turno(
                    usuarioId = usuario.id,
                    acceso = acceso,
                    tipo = tipo,
                    fecha = fechaHoy(),
                    horaInicioReal = actual.horaFoto ?: horaActualTexto(),
                    horaFinReal = null,
                    fotoUri = foto
                )
            )
            _estado.update { it.copy(guardando = false, turnoCreadoId = id) }
        }
    }

    fun onNavegacionRealizada() {
        _estado.update { it.copy(turnoCreadoId = null) }
    }
}