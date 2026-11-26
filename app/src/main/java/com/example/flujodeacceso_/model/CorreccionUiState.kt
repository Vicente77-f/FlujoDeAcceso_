package com.example.flujodeacceso_.model

/** Todo lo que muestra la pantalla Corregir conteo. Vive en el ViewModel: sobrevive al girar. */
data class CorreccionUiState(
    val cargando: Boolean = true,
    val titulo: String = "",                                   // "Turno AM · A1 · 2026-10-13"
    val horas: List<Int> = emptyList(),                        // Franjas del turno: 10, 11, 12...
    val horaSeleccionada: Int? = null,
    val valores: Map<CategoriaConteo, String> = emptyMap(),    // Texto de cada campo
    val guardando: Boolean = false,
    val mensaje: String? = null,
    val error: String? = null
)