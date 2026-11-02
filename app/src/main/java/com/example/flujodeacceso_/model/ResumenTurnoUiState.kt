package com.example.flujodeacceso_.model

/** Todo lo que muestra la pantalla Resumen del turno. Vive en el ViewModel: sobrevive al girar. */
data class ResumenTurnoUiState(
    val cargando: Boolean = true,
    val titulo: String = "",                              // "Turno AM · A1"
    val subtitulo: String = "",                           // "Acceso Galería Central · 2026-10-13"
    val responsable: String = "",                         // "Camila R."
    val horario: String = "",                             // "10:02 a 14:58"
    val fotoUri: String? = null,                          // null = turno sin foto (datos demo)
    val totales: Map<CategoriaConteo, Int> = emptyMap(),
    val porHora: List<ConteoAcceso> = emptyList(),        // Solo horas con registros
    val vistaPreviaCsv: String = "",
    val nombreArchivo: String = "",
    val exportado: Boolean = false,
    val exportando: Boolean = false,
    val mensaje: String? = null,                          // Éxito o aviso
    val error: String? = null
) {
    fun valor(categoria: CategoriaConteo): Int = totales[categoria] ?: 0

    /** Total de personas: el tránsito no se suma porque ya está incluido. */
    val totalIngresos: Int
        get() = valor(CategoriaConteo.HOMBRE) + valor(CategoriaConteo.MUJER) +
                valor(CategoriaConteo.MENOR) + valor(CategoriaConteo.SIN_CLASIFICAR)
}