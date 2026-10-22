package com.example.flujodeacceso_.navigation

import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.flujodeacceso_.ui.screens.contador.ContadorScreen
import com.example.flujodeacceso_.ui.screens.iniciarturno.IniciarTurnoScreen
import com.example.flujodeacceso_.ui.screens.login.LoginScreen
import com.example.flujodeacceso_.ui.screens.panelsupervisor.PanelSupervisorScreen
import com.example.flujodeacceso_.ui.screens.resumenturno.ResumenTurnoScreen
import com.example.flujodeacceso_.model.Rol
@Composable
fun AppNavGraph(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController()
) {
    // Vuelve al login borrando todo el historial: "atrás" no debe regresar a la sesión cerrada
    val irALogin: () -> Unit = {
        navController.navigate(Screen.Login.route) {
            popUpTo(navController.graph.id) { inclusive = true }
        }
    }

    NavHost(
        navController = navController,
        startDestination = Screen.Login.route,
        modifier = modifier
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
                onComenzarTurno = { navController.navigate(Screen.Contador.route) },
                onCerrarSesion = irALogin
            )
        }
        composable(Screen.Contador.route) {
            ContadorScreen(
                onFinalizarTurno = {
                    navController.navigate(Screen.ResumenTurno.route) {
                        popUpTo(Screen.IniciarTurno.route)
                    }
                }
            )
        }
        composable(Screen.ResumenTurno.route) {
            ResumenTurnoScreen(onCerrarTurno = irALogin)
        }
        composable(Screen.PanelSupervisor.route) {
            PanelSupervisorScreen(onCerrarSesion = irALogin)
        }
    }
}