package com.example.flujodeacceso_.domain

import com.example.flujodeacceso_.model.ConteoAcceso
import com.example.flujodeacceso_.model.Turno

/** Primera línea del archivo: el nombre de cada columna. */
const val ENCABEZADO_CSV = "fecha,turno,acceso,hora,hombres,mujeres,menores,sin_clasificar,transito"

/** Arma el contenido del CSV: una línea por cada hora CON registros, ordenadas por hora. */
fun generarCsv(turno: Turno, conteos: List<ConteoAcceso>): String = buildString {
    appendLine(ENCABEZADO_CSV)
    conteos
        .filter { it.totalIngresos > 0 }
        .sortedBy { it.hora }
        .forEach { c ->
            appendLine(
                listOf(
                    turno.fecha, turno.tipo.name, turno.acceso.name, c.hora,
                    c.hombres, c.mujeres, c.menores, c.sinClasificar, c.transito
                ).joinToString(",")
            )
        }
}

/** Nombre sugerido para el archivo. Ej.: flujo_2026-10-13_AM_A1.csv */
fun nombreArchivoCsv(turno: Turno): String =
    "flujo_${turno  .fecha}_${turno.tipo.name}_${turno.acceso.name}.csv"