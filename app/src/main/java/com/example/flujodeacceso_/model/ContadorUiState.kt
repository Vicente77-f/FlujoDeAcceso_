package com.example.flujodeacceso_.model

data class ContadorUiState(
    val cargando: Boolean = true,
    val titulo: String = "",                                 // "Turno AM · A1"
    val subtitulo: String = "",                              // "Galería Central · 10:00 - 15:00"
    val franjaActual: Int = 0,                               // Hora donde se registran los toques
    val fueraDeHorario: Boolean = false,
    val totales: Map<CategoriaConteo, Int> = emptyMap(),     // Totales del turno por categoría
    val restables: Set<CategoriaConteo> = emptySet(),        // Categorías que se pueden restar ahora
    val mensaje: String? = null,                             // "Último: +1 Mujer adulta"
    val error: String? = null,
    val puedeDeshacer: Boolean = false,
    val turnoFinalizado: Boolean = false
) {
    fun valor(categoria: CategoriaConteo): Int = totales[categoria] ?: 0

    /** Total de personas: el tránsito no se suma porque ya está incluido. */
    val totalIngresos: Int
        get() = valor(CategoriaConteo.HOMBRE) + valor(CategoriaConteo.MUJER) +
                valor(CategoriaConteo.MENOR) + valor(CategoriaConteo.SIN_CLASIFICAR)
}