package com.example.flujodeacceso_.domain.validation

import com.example.flujodeacceso_.model.ConteoAcceso

/** Reglas que todo conteo debe cumplir. Devuelve el mensaje de error o null. */
fun validarConteo(conteo: ConteoAcceso): String? {
    val valores = listOf(conteo.hombres, conteo.mujeres, conteo.menores, conteo.sinClasificar, conteo.transito)
    if (valores.any { it < 0 }) return "No se puede restar: en esta franja ya está en 0"
    if (conteo.transito > conteo.totalIngresos) {
        return "Las personas en tránsito no pueden superar a las que ingresaron"
    }
    return null
}