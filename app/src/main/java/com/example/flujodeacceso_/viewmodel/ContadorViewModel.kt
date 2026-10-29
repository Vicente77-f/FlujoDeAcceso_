package com.example.flujodeacceso_.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.example.flujodeacceso_.data.local.database.AppDatabase
import com.example.flujodeacceso_.data.repository.TurnoRepository
import com.example.flujodeacceso_.domain.horaActual
import com.example.flujodeacceso_.domain.horaActualTexto
import com.example.flujodeacceso_.domain.sumar
import com.example.flujodeacceso_.domain.validation.validarConteo
import com.example.flujodeacceso_.domain.valor
import com.example.flujodeacceso_.model.CategoriaConteo
import com.example.flujodeacceso_.model.ConteoAcceso
import com.example.flujodeacceso_.model.ContadorUiState
import com.example.flujodeacceso_.model.Turno
import com.example.flujodeacceso_.navigation.ARG_TURNO_ID
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ContadorViewModel(
    application: Application,          // El orden (Application, SavedStateHandle) es obligatorio
    savedStateHandle: SavedStateHandle
) : AndroidViewModel(application) {

    // Debe declararse ANTES del bloque init, porque init ya lo usa
    private val turnoRepository = TurnoRepository(AppDatabase.obtener(application).turnoDao())

    // El turnoId llega desde la ruta "contador/{turnoId}"
    private val turnoId: Long = checkNotNull(savedStateHandle.get<Long>(ARG_TURNO_ID)) {
        "Falta el id del turno en la ruta"
    }

    private val _estado = MutableStateFlow(ContadorUiState())
    val estado: StateFlow<ContadorUiState> = _estado

    private var turno: Turno? = null
    private var conteos: List<ConteoAcceso> = emptyList() // Una fila por hora

    private data class Accion(val categoria: CategoriaConteo, val delta: Int, val hora: Int)
    private var ultimaAccion: Accion? = null

    init {
        cargarTurno()
    }

    private fun cargarTurno() {
        viewModelScope.launch {
            val encontrado = turnoRepository.obtenerTurno(turnoId)
            if (encontrado == null) {
                _estado.update { it.copy(cargando = false, error = "No se encontró el turno") }
                return@launch
            }
            turno = encontrado
            conteos = turnoRepository.conteosDeTurno(turnoId)
            publicarEstado(mensaje = null)
        }
    }

    fun sumar(categoria: CategoriaConteo) {
        cambiar(categoria, +1)
    }

    fun restar(categoria: CategoriaConteo) {
        cambiar(categoria, -1)
    }

    fun deshacer() {
        val accion = ultimaAccion ?: return
        if (aplicar(accion.categoria, -accion.delta, accion.hora)) {
            ultimaAccion = null
            publicarEstado(mensaje = "Último registro deshecho")
        }
    }

    fun finalizarTurno() {
        viewModelScope.launch {
            turnoRepository.cerrarTurno(turnoId, horaActualTexto())
            _estado.update { it.copy(turnoFinalizado = true) }
        }
    }

    // ---------- Lógica interna ----------

    private fun cambiar(categoria: CategoriaConteo, delta: Int) {
        val t = turno ?: return
        val hora = franjaActual(t)
        if (aplicar(categoria, delta, hora)) {
            ultimaAccion = Accion(categoria, delta, hora)
            val signo = if (delta > 0) "+1" else "−1"
            publicarEstado(mensaje = "Último: $signo ${categoria.etiqueta}")
        }
    }

    /** Aplica el cambio en el conteo de "hora", lo valida y lo guarda. Devuelve true si fue válido. */
    private fun aplicar(categoria: CategoriaConteo, delta: Int, hora: Int): Boolean {
        val t = turno ?: return false
        val actual = conteos.find { it.hora == hora }
            ?: ConteoAcceso(turnoId = t.id, acceso = t.acceso, fecha = t.fecha, hora = hora)
        val nuevo = actual.sumar(categoria, delta)

        val error = validarConteo(nuevo)
        if (error != null) {
            _estado.update { it.copy(error = error) }
            return false
        }

        conteos = conteos.filter { it.hora != hora } + nuevo
        viewModelScope.launch { turnoRepository.guardarConteo(nuevo) }
        return true
    }

    /** Hora donde se registran los toques. Fuera del horario del turno (pruebas), usa la primera franja. */
    private fun franjaActual(t: Turno): Int {
        val hora = horaActual()
        return if (t.tipo.incluyeHora(hora)) hora else t.tipo.horaInicio
    }

    private fun publicarEstado(mensaje: String?) {
        val t = turno ?: return
        val hora = franjaActual(t)
        val conteoDeLaFranja = conteos.find { it.hora == hora }
        _estado.update {
            it.copy(
                cargando = false,
                titulo = "${t.tipo.etiqueta} · ${t.acceso.name}",
                subtitulo = "${t.acceso.nombre} · ${t.tipo.horario}",
                franjaActual = hora,
                fueraDeHorario = !t.tipo.incluyeHora(horaActual()),
                totales = CategoriaConteo.entries.associateWith { categoria ->
                    conteos.sumOf { conteo -> conteo.valor(categoria) }
                },
                restables = CategoriaConteo.entries.filter { categoria ->
                    (conteoDeLaFranja?.valor(categoria) ?: 0) > 0
                }.toSet(),
                mensaje = mensaje,
                error = null,
                puedeDeshacer = ultimaAccion != null
            )
        }
    }
}