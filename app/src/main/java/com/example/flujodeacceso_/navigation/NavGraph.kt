package com.example.flujodeacceso_.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.flujodeacceso_.data.repository.SesionRepository
import com.example.flujodeacceso_.model.Rol
import com.example.flujodeacceso_.ui.screens.contador.ContadorScreen
import com.example.flujodeacceso_.ui.screens.iniciarturno.IniciarTurnoScreen
import com.example.flujodeacceso_.ui.screens.login.LoginScreen
import com.example.flujodeacceso_.ui.screens.panelsupervisor.PanelSupervisorScreen
import com.example.flujodeacceso_.ui.screens.resumenturno.ResumenTurnoScreen
import com.example.flujodeacceso_.ui.components.entradaAlVolver
import com.example.flujodeacceso_.ui.components.entradaNavegacion
import com.example.flujodeacceso_.ui.components.salidaAlVolver
import com.example.flujodeacceso_.ui.components.salidaNavegacion


@Composable
fun AppNavGraph(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController()
) {
    // Cierra la sesión y vuelve al login borrando todo el historial
    val irALogin: () -> Unit = {
        SesionRepository.cerrar()
        navController.navigate(Screen.Login.route) {
            popUpTo(navController.graph.id) { inclusive = true }
        }
    }

    // Declaración del parámetro turnoId como número Long
    val argumentoTurno = listOf(navArgument(ARG_TURNO_ID) { type = NavType.LongType })

    NavHost(
        navController = navController,
        startDestination = Screen.Login.route,
        modifier = modifier,
        enterTransition = entradaNavegacion,
        exitTransition = salidaNavegacion,
        popEnterTransition = entradaAlVolver,
        popExitTransition = salidaAlVolver
    ) {
        composable(Screen.Login.route) {
            LoginScreen(
                onLoginExitoso = { rol ->
                    val destino = if (rol == Rol.SUPERVISOR) {
                        Screen.PanelSupervisor.route
                    } else {
                        Screen.IniciarTurno.route
                    }
                    navController.navigate(destino) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.IniciarTurno.route) {
            IniciarTurnoScreen(
                onTurnoIniciado = { turnoId ->
                    navController.navigate(Screen.Contador.crearRuta(turnoId))
                },
                onCerrarSesion = irALogin
            )
        }

        composable(Screen.Contador.route, arguments = argumentoTurno) { entrada ->
            val turnoId = entrada.arguments?.getLong(ARG_TURNO_ID) ?: 0L
            ContadorScreen(
                onFinalizarTurno = {
                    navController.navigate(Screen.ResumenTurno.crearRuta(turnoId)) {
                        popUpTo(Screen.IniciarTurno.route)
                    }
                }
            )
        }

        composable(Screen.ResumenTurno.route, arguments = argumentoTurno) {
            ResumenTurnoScreen(onCerrarTurno = irALogin)
        }

        composable(Screen.PanelSupervisor.route) {
            PanelSupervisorScreen(onCerrarSesion = irALogin)
        }
    }
}