package com.example.flujodeacceso_.navigation

sealed class Screen (val route: String) {
    data object Login : Screen ("login")
    data object IniciarTurno : Screen ("iniciar_turno")
    data object Contador : Screen ("contador")
    data object ResumenTurno : Screen ("resumen_turno")
    data object PanelSupervisor : Screen ("panel_supervisor")
}
