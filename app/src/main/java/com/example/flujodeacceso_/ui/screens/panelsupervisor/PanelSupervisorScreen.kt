package com.example.flujodeacceso_.ui.screens.panelsupervisor

import androidx.compose.runtime.Composable
import com.example.flujodeacceso_.ui.components.PantallaProvisional

@Composable
fun PanelSupervisorScreen(onCerrarSesion: () -> Unit) {
    PantallaProvisional(
        titulo = "Panel del supervisor",
        descripcion = "Ingresos por turno AM/PM, composición y accesos (pendiente).",
        acciones = listOf("Cerrar sesión" to onCerrarSesion)
    )
}