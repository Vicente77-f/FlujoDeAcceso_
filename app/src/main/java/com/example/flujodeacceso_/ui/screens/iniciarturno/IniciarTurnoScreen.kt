package com.example.flujodeacceso_.ui.screens.iniciarturno

import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import com.example.flujodeacceso_.data.repository.SesionRepository
import com.example.flujodeacceso_.data.repository.TurnoRepository
import com.example.flujodeacceso_.domain.fechaHoy
import com.example.flujodeacceso_.domain.horaActualTexto
import com.example.flujodeacceso_.model.Acceso
import com.example.flujodeacceso_.model.TipoTurno
import com.example.flujodeacceso_.model.Turno
import com.example.flujodeacceso_.ui.components.PantallaProvisional
import kotlinx.coroutines.launch

@Composable
fun IniciarTurnoScreen(
    onTurnoIniciado: (Long) -> Unit,
    onCerrarSesion: () -> Unit
) {
    val scope = rememberCoroutineScope()

    // PROVISIONAL: crea un turno de demostración (AM, A1, sin foto)
    val comenzarDemo: () -> Unit = {
        scope.launch {
            val usuario = SesionRepository.usuarioActual.value
            val id = TurnoRepository.crearTurno(
                Turno(
                    usuarioId = usuario?.id ?: 0L,
                    acceso = Acceso.A1,
                    tipo = TipoTurno.AM,
                    fecha = fechaHoy(),
                    horaInicioReal = horaActualTexto(),
                    horaFinReal = null,
                    fotoUri = "demo-sin-foto"
                )
            )
            onTurnoIniciado(id)
        }
    }

    PantallaProvisional(
        titulo = "Iniciar turno",
        descripcion = "Provisional: crea un turno de demostración para probar el contador.",
        acciones = listOf(
            "Comenzar turno de demostración" to comenzarDemo,
            "Cerrar sesión" to onCerrarSesion
        )
    )
}