package com.example.flujodeacceso_.model

/** Una fila de la tabla "Ingresos por acceso". */
data class FilaAcceso(val acceso: Acceso, val am: Int, val pm: Int) {
    val total: Int
        get() = am + pm
}

/** Un turno de la lista, con el nombre de quién lo hizo y su total. */
data class TurnoDelDia(val turno: Turno, val responsable: String, val totalIngresos: Int)

/** Todo lo que muestra el panel. Vive en el ViewModel: sobrevive al girar. */
data class PanelSupervisorUiState(
    val cargando: Boolean = true,
    val esDemo: Boolean = true,                              // true = 13-oct, false = hoy
    val totalAm: Int = 0,
    val totalPm: Int = 0,
    val transito: Int = 0,
    val composicion: Map<CategoriaConteo, Int> = emptyMap(), // Sin tránsito
    val filas: List<FilaAcceso> = emptyList(),
    val turnos: List<TurnoDelDia> = emptyList(),
    val transacciones: Int? = null,                          // null = no hay datos de caja ese día
    val transaccionesPor100: String = "",                    // "16,7"
    val turnoPorEliminar: Turno? = null,                     // distinto de null = mostrar el diálogo
    val mensaje: String? = null
) {
    val totalIngresos: Int
        get() = totalAm + totalPm
}