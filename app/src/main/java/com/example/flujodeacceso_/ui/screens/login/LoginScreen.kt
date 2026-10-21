package com.example.flujodeacceso_.ui.screens.login

import androidx.compose.runtime.Composable
import com.example.flujodeacceso_.ui.components.PantallaProvisional

@Composable
fun LoginScreen(
    onIngresarComoEmpleado: () -> Unit,
    onIngresarComoSupervisor: () -> Unit
) {
    PantallaProvisional(
        titulo = "Flujo de Acceso",
        descripcion = "Inicio de sesion (pendiente: formulario con validacion).",
        acciones = listOf(
            "ingresar como empleado" to onIngresarComoEmpleado,
            "Ingresar como supervisor" to onIngresarComoSupervisor
        )
    )
}
