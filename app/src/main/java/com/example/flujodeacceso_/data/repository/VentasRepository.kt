package com.example.flujodeacceso_.data.repository

import com.example.flujodeacceso_.data.local.database.FECHA_DEMO
import com.example.flujodeacceso_.model.ResumenVentas

/**
 * Transacciones de caja por hora. Son datos FIJOS de demostración, en memoria:
 * no forman parte del trabajo del empleado. Totales del mockup: 412 + 298 = 710.
 */
object VentasRepository {

    private val tradicionales = listOf(30, 36, 42, 44, 40, 38, 44, 48, 46, 44)  // 10:00 a 19:00
    private val autoservicio = listOf(20, 26, 30, 32, 28, 26, 32, 36, 36, 32)

    private val ventasDemo: List<ResumenVentas> = (10 until 20).mapIndexed { i, hora ->
        ResumenVentas(
            fecha = FECHA_DEMO,
            hora = hora,
            transaccionesTradicionales = tradicionales[i],
            transaccionesAutoservicio = autoservicio[i]
        )
    }

    /** Ventas de un día. Para un día sin datos devuelve una lista vacía. */
    fun ventasDe(fecha: String): List<ResumenVentas> = ventasDemo.filter { it.fecha == fecha }
}