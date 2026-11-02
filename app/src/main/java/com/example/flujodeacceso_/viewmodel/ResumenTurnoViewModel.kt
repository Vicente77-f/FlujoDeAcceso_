package com.example.flujodeacceso_.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.example.flujodeacceso_.data.local.database.AppDatabase
import com.example.flujodeacceso_.data.repository.TurnoRepository
import com.example.flujodeacceso_.data.repository.UsuarioRepository
import com.example.flujodeacceso_.domain.generarCsv
import com.example.flujodeacceso_.domain.nombreArchivoCsv
import com.example.flujodeacceso_.domain.valor
import com.example.flujodeacceso_.model.CategoriaConteo
import com.example.flujodeacceso_.model.ResumenTurnoUiState
import com.example.flujodeacceso_.navigation.ARG_TURNO_ID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException

class ResumenTurnoViewModel(
    application: Application,          // El orden (Application, SavedStateHandle) es obligatorio
    savedStateHandle: SavedStateHandle
) : AndroidViewModel(application) {

    private val baseDeDatos = AppDatabase.obtener(application)
    private val turnoRepository = TurnoRepository(baseDeDatos.turnoDao())
    private val usuarioRepository = UsuarioRepository(baseDeDatos.usuarioDao())

    // El turnoId llega desde la ruta "resumen_turno/{turnoId}"
    private val turnoId: Long = checkNotNull(savedStateHandle.get<Long>(ARG_TURNO_ID)) {
        "Falta el id del turno en la ruta"
    }

    private val _estado = MutableStateFlow(ResumenTurnoUiState())
    val estado: StateFlow<ResumenTurnoUiState> = _estado

    private var contenidoCsv: String = ""

    init {
        cargarResumen()
    }

    /** Lee el turno, sus conteos y el nombre del responsable desde Room (SELECT). */
    private fun cargarResumen() {
        viewModelScope.launch {
            val turno = turnoRepository.obtenerTurno(turnoId)
            if (turno == null) {
                _estado.update { it.copy(cargando = false, error = "No se encontró el turno") }
                return@launch
            }
            val conteos = turnoRepository.conteosDeTurno(turnoId)
            val responsable = usuarioRepository.obtenerPorId(turno.usuarioId)?.nombre ?: "Desconocido"
            contenidoCsv = generarCsv(turno, conteos)

            _estado.update {
                it.copy(
                    cargando = false,
                    titulo = "${turno.tipo.etiqueta} · ${turno.acceso.name}",
                    subtitulo = "${turno.acceso.nombre} · ${turno.fecha}",
                    responsable = responsable,
                    horario = "${turno.horaInicioReal} a ${turno.horaFinReal ?: "en curso"}",
                    fotoUri = turno.fotoUri.ifBlank { null },
                    totales = CategoriaConteo.entries.associateWith { categoria ->
                        conteos.sumOf { conteo -> conteo.valor(categoria) }
                    },
                    porHora = conteos.filter { c -> c.totalIngresos > 0 }.sortedBy { c -> c.hora },
                    vistaPreviaCsv = contenidoCsv,
                    nombreArchivo = nombreArchivoCsv(turno),
                    exportado = turno.exportado
                )
            }
        }
    }

    /** Escribe el CSV en el archivo que el usuario eligió y marca el turno como exportado (UPDATE). */
    fun exportar(destino: Uri) {
        if (_estado.value.exportando) return // Evita exportar dos veces a la vez
        viewModelScope.launch {
            _estado.update { it.copy(exportando = true, mensaje = null, error = null) }
            try {
                withContext(Dispatchers.IO) {
                    val salida = getApplication<Application>().contentResolver.openOutputStream(destino)
                        ?: throw IOException("No se pudo abrir el archivo")
                    salida.use { it.write(contenidoCsv.toByteArray(Charsets.UTF_8)) }
                }
                turnoRepository.marcarExportado(turnoId)
                _estado.update {
                    it.copy(exportando = false, exportado = true, mensaje = "Archivo guardado: ${it.nombreArchivo}")
                }
            } catch (e: IOException) {
                _estado.update { it.copy(exportando = false, error = "No se pudo guardar el archivo. Intenta de nuevo.") }
            } catch (e: SecurityException) {
                _estado.update { it.copy(exportando = false, error = "No hay permiso para escribir en ese lugar.") }
            }
        }
    }

    /** El usuario cerró el selector sin elegir dónde guardar. */
    fun onExportacionCancelada() {
        _estado.update { it.copy(mensaje = "Exportación cancelada", error = null) }
    }
}