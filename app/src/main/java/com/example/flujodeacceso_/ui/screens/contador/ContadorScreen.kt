package com.example.flujodeacceso_.ui.screens.contador

import androidx.compose.runtime.Composable
import com.example.flujodeacceso_.ui.components.PantallaProvisional

@Composable
fun ContadorScreen(onFinalizarTurno: () -> Unit) {
    PantallaProvisional(
        titulo = "Contador del turno",
        descripcion = "Botones − / + por categoría (pendiente).",
        acciones = listOf("Finalizar turno" to onFinalizarTurno)
    )
}