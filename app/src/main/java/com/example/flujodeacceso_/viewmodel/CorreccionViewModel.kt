package com.example.flujodeacceso_.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.example.flujodeacceso_.data.local.database.AppDatabase
import com.example.flujodeacceso_.data.repository.SesionRepository
import com.example.flujodeacceso_.data.repository.TurnoRepository
import com.example.flujodeacceso_.domain.validation.validarConteo
import com.example.flujodeacceso_.domain.valor
import com.example.flujodeacceso_.model.CategoriaConteo
import com.example.flujodeacceso_.model.ConteoAcceso
import com.example.flujodeacceso_.model.CorreccionUiState
import com.example.flujodeacceso_.model.Rol
import com.example.flujodeacceso_.model.Turno
import com.example.flujodeacceso_.navigation.ARG_TURNO_ID
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CorreccionViewModel(
    application: Application,          // El orden (Application, SavedStateHandle) es obligatorio
    savedStateHandle: SavedStateHandle
) : AndroidViewModel(application) {

    private val turnoRepository = TurnoRepository(AppDatabase.obtener(application).turnoDao())

    private val turnoId: Long = checkNotNull(savedStateHandle.get<Long>(ARG_TURNO_ID)) {
        "Falta el id del turno en la ruta"
    }

    private val _estado = MutableStateFlow(CorreccionUiState())
    val estado: StateFlow<CorreccionUiState> = _estado

    private var turno: Turno? = null
    private var conteos: List<ConteoAcceso> = emptyList()

    init {
        cargar()
    }

    private fun cargar() {
        viewModelScope.launch {
            val encontrado = turnoRepository.obtenerTurno(turnoId)
            if (encontrado == null) {
                _estado.update { it.copy(cargando = false, error = "No se encontró el turno") }
                return@launch
            }
            turno = encontrado
            conteos = turnoRepository.conteosDeTurno(turnoId)
            val horas = (encontrado.tipo.horaInicio until encontrado.tipo.horaFin).toList()
            _estado.update {
                it.copy(
                    cargando = false,
                    titulo = "${encontrado.tipo.etiqueta} · ${encontrado.acceso.name} · ${encontrado.fecha}",
                    horas = horas
                )
            }
            seleccionarHora(horas.first())
        }
    }

    /** Carga en los campos los valores guardados de esa hora (0 si no hubo registros). */
    fun seleccionarHora(hora: Int) {
        val actual = conteos.find { it.hora == hora }
        _estado.update {
            it.copy(
                horaSeleccionada = hora,
                valores = CategoriaConteo.entries.associateWith { categoria ->
                    (actual?.valor(categoria) ?: 0).toString()
                },
                mensaje = null,
                error = null
            )
        }
    }

    /** Solo acepta dígitos: así nunca llega un número negativo ni texto. */
    fun onValorChange(categoria: CategoriaConteo, texto: String) {
        if (texto.length > 5 || texto.any { !it.isDigit() }) return
        _estado.update { it.copy(valores = it.valores + (categoria to texto), mensaje = null, error = null) }
    }

    fun guardar() {
        val t = turno ?: return
        val actual = _estado.value
        val hora = actual.horaSeleccionada ?: return
        if (actual.guardando) return

        // 1) Solo un supervisor (el servidor lo vuelve a verificar con usuarioMS)
        val supervisor = SesionRepository.usuarioActual.value
        if (supervisor == null || supervisor.rol != Rol.SUPERVISOR) {
            _estado.update { it.copy(error = "Solo un supervisor puede corregir") }
            return
        }

        // 2) Todos los campos con número
        val numeros = CategoriaConteo.entries.associateWith { actual.valores[it]?.toIntOrNull() }
        if (numeros.values.any { it == null }) {
            _estado.update { it.copy(error = "Completa todos los campos") }
            return
        }
        fun n(categoria: CategoriaConteo): Int = numeros[categoria] ?: 0

        val conteo = ConteoAcceso(
            turnoId = t.id, acceso = t.acceso, fecha = t.fecha, hora = hora,
            hombres = n(CategoriaConteo.HOMBRE), mujeres = n(CategoriaConteo.MUJER),
            menores = n(CategoriaConteo.MENOR), sinClasificar = n(CategoriaConteo.SIN_CLASIFICAR),
            transito = n(CategoriaConteo.TRANSITO)
        )

        // 3) Las mismas reglas que el contador (el tránsito no puede superar los ingresos)
        validarConteo(conteo)?.let { mensaje ->
            _estado.update { it.copy(error = mensaje) }
            return
        }

        // 4) PUT /turnos/{id}/conteos/{hora}/correccion
        viewModelScope.launch {
            _estado.update { it.copy(guardando = true, mensaje = null, error = null) }
            turnoRepository.corregirConteo(conteo, supervisor.id).fold(
                onSuccess = {
                    conteos = conteos.filter { c -> c.hora != hora } + conteo
                    _estado.update { it.copy(guardando = false, mensaje = "Corrección guardada en el servidor ($hora:00)") }
                },
                onFailure = { e ->
                    _estado.update { it.copy(guardando = false, error = e.message) }
                }
            )
        }
    }
}