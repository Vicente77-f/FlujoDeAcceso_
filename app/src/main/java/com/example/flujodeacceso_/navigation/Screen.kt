package com.example.flujodeacceso_.navigation

const val ARG_TURNO_ID = "turnoId"

sealed class Screen(val route: String) {
    data object Login : Screen("login")
    data object IniciarTurno : Screen("iniciar_turno")

    data object Contador : Screen("contador/{$ARG_TURNO_ID}") {
        fun crearRuta(turnoId: Long) = "contador/$turnoId"
    }

    data object ResumenTurno : Screen("resumen_turno/{$ARG_TURNO_ID}") {
        fun crearRuta(turnoId: Long) = "resumen_turno/$turnoId"
    }

    data object PanelSupervisor : Screen("panel_supervisor")
}
