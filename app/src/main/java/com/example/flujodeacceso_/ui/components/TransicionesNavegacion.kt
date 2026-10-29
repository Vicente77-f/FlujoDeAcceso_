package com.example.flujodeacceso_.ui.components

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.navigation.NavBackStackEntry

/** Duración de cada transición en milisegundos. */
private const val DURACION_NAV = 350

/** Al avanzar: la pantalla nueva entra desde la derecha. */
val entradaNavegacion: AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition = {
    slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(DURACION_NAV)) +
            fadeIn(tween(DURACION_NAV))
}

/** Al avanzar: la pantalla anterior sale hacia la izquierda. */
val salidaNavegacion: AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition = {
    slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(DURACION_NAV)) +
            fadeOut(tween(DURACION_NAV))
}

/** Al volver atrás: la pantalla anterior regresa desde la izquierda. */
val entradaAlVolver: AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition = {
    slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(DURACION_NAV)) +
            fadeIn(tween(DURACION_NAV))
}

/** Al volver atrás: la pantalla actual sale hacia la derecha. */
val salidaAlVolver: AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition = {
    slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(DURACION_NAV)) +
            fadeOut(tween(DURACION_NAV))
}