package com.example.flujodeacceso_.ui.screens.resumenturno

import androidx.compose.runtime.Composable
import com.example.flujodeacceso_.ui.components.PantallaProvisional

@Composable
fun ResumenTurnoScreen(onCerrarTurno: () -> Unit) {
    PantallaProvisional(
        titulo = "Resumen del turno",
        descripcion = "Totales por categoría y exportación CSV (pendiente).",
        acciones = listOf("Cerrar turno y sesión" to onCerrarTurno)
    )
}