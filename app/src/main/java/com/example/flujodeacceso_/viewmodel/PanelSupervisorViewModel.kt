package com.example.flujodeacceso_.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.flujodeacceso_.data.local.database.AppDatabase
import com.example.flujodeacceso_.data.local.database.FECHA_DEMO
import com.example.flujodeacceso_.data.repository.TurnoRepository
import com.example.flujodeacceso_.data.repository.UsuarioRepository
import com.example.flujodeacceso_.data.repository.VentasRepository
import com.example.flujodeacceso_.domain.fechaHoy
import com.example.flujodeacceso_.domain.valor
import com.example.flujodeacceso_.model.Acceso
import com.example.flujodeacceso_.model.CategoriaConteo
import com.example.flujodeacceso_.model.ConteoAcceso
import com.example.flujodeacceso_.model.FilaAcceso
import com.example.flujodeacceso_.model.PanelSupervisorUiState
import com.example.flujodeacceso_.model.TipoTurno
import com.example.flujodeacceso_.model.Turno
import com.example.flujodeacceso_.model.TurnoDelDia
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Locale

class PanelSupervisorViewModel(application: Application) : AndroidViewModel(application) {

    private val baseDeDatos = AppDatabase.obtener(application)
    private val turnoRepository = TurnoRepository(baseDeDatos.turnoDao())
    private val usuarioRepository = UsuarioRepository(baseDeDatos.usuarioDao())

    private val hoy = fechaHoy()
    private val fecha = MutableStateFlow(FECHA_DEMO)          // Día que se está mirando
    private val turnoPorEliminar = MutableStateFlow<Turno?>(null)
    private val mensaje = MutableStateFlow<String?>(null)
    private val nombres = mutableMapOf<Long, String>()        // Caché: id del usuario → nombre

    // Cada vez que cambia el día, se observan los turnos y conteos de ESE día.
    // Room vuelve a emitir solo cuando cambian las tablas (por ejemplo, al eliminar un turno).
    @OptIn(ExperimentalCoroutinesApi::class)
    private val datosDelDia = fecha.flatMapLatest { dia ->
        combine(turnoRepository.turnosDeFecha(dia), turnoRepository.conteosDeFecha(dia)) { turnos, conteos ->
            turnos to conteos
        }
    }

    val estado: StateFlow<PanelSupervisorUiState> =
        combine(fecha, datosDelDia, turnoPorEliminar, mensaje) { dia, datos, eliminar, aviso ->
            val (turnos, conteos) = datos
            construirEstado(dia, turnos, conteos, eliminar, aviso)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), PanelSupervisorUiState())

    // ---------- Acciones de la pantalla ----------

    fun verDemo() {
        fecha.value = FECHA_DEMO
        mensaje.value = null
    }

    fun verHoy() {
        fecha.value = hoy
        mensaje.value = null
    }

    fun pedirEliminar(turno: Turno) {
        if (turno.estaAbierto) {
            mensaje.value = "No se puede eliminar un turno en curso"
            return
        }
        turnoPorEliminar.value = turno
    }

    fun cancelarEliminar() {
        turnoPorEliminar.value = null
    }

    /** DELETE en Room: el CASCADE borra también los conteos del turno. */
    fun confirmarEliminar() {
        val turno = turnoPorEliminar.value ?: return
        turnoPorEliminar.value = null
        viewModelScope.launch {
            turnoRepository.eliminarTurno(turno)
            mensaje.value = "Turno ${turno.tipo.name} · ${turno.acceso.name} eliminado"
        }
    }

    // ---------- Cálculos ----------

    private suspend fun construirEstado(
        dia: String,
        turnos: List<Turno>,
        conteos: List<ConteoAcceso>,
        eliminar: Turno?,
        aviso: String?
    ): PanelSupervisorUiState {
        val tipoDeTurno = turnos.associate { it.id to it.tipo }   // turnoId → AM o PM

        fun ingresos(tipo: TipoTurno, acceso: Acceso? = null): Int = conteos
            .filter { c -> tipoDeTurno[c.turnoId] == tipo && (acceso == null || c.acceso == acceso) }
            .sumOf { c -> c.totalIngresos }

        val totalAm = ingresos(TipoTurno.AM)
        val totalPm = ingresos(TipoTurno.PM)
        val total = totalAm + totalPm
        val transacciones = VentasRepository.ventasDe(dia).sumOf { it.totalTransacciones }.takeIf { it > 0 }

        return PanelSupervisorUiState(
            cargando = false,
            esDemo = dia == FECHA_DEMO,
            totalAm = totalAm,
            totalPm = totalPm,
            transito = conteos.sumOf { it.transito },
            composicion = CategoriaConteo.entries
                .filter { it != CategoriaConteo.TRANSITO }
                .associateWith { categoria -> conteos.sumOf { it.valor(categoria) } },
            filas = Acceso.entries.map { acceso ->
                FilaAcceso(acceso, am = ingresos(TipoTurno.AM, acceso), pm = ingresos(TipoTurno.PM, acceso))
            },
            turnos = turnos.map { turno ->
                TurnoDelDia(
                    turno = turno,
                    responsable = nombreDe(turno.usuarioId),
                    totalIngresos = conteos.filter { it.turnoId == turno.id }.sumOf { it.totalIngresos }
                )
            },
            transacciones = transacciones,
            transaccionesPor100 = if (transacciones != null && total > 0) {
                String.format(Locale("es", "CL"), "%.1f", transacciones * 100.0 / total)
            } else "",
            turnoPorEliminar = eliminar,
            mensaje = aviso
        )
    }

    private suspend fun nombreDe(usuarioId: Long): String =
        nombres.getOrPut(usuarioId) { usuarioRepository.obtenerPorId(usuarioId)?.nombre ?: "Desconocido" }
}