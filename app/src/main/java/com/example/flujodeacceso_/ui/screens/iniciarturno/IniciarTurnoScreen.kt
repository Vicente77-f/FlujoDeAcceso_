package com.example.flujodeacceso_.ui.screens.iniciarturno

import androidx.compose.runtime.Composable
import com.example.flujodeacceso_.ui.components.PantallaProvisional

@Composable
fun IniciarTurnoScreen(
    onComenzarTurno: () -> Unit,
    onCerrarSesion: () -> Unit
) {
    PantallaProvisional(
        titulo = "Iniciar turno",
        descripcion = "Elegir turno AM/PM, acceso y foto del puesto (pendiente).",
        acciones = listOf(
            "Comenzar turno" to onComenzarTurno,
            "Cerrar sesión" to onCerrarSesion
        )
    )
}