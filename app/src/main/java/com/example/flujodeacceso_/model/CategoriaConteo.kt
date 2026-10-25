package com.example.flujodeacceso_.model

/** Botones del contador. TRANSITO es un subconjunto: no se suma al total de ingresos. */
enum class CategoriaConteo(val etiqueta: String, val descripcion: String) {
    HOMBRE("Hombre adulto", "18 años o más (estimado)"),
    MUJER("Mujer adulta", "18 años o más (estimado)"),
    MENOR("Menor de edad", "Ingresa acompañado"),
    SIN_CLASIFICAR("Sin clasificar", "No fue posible clasificar"),
    TRANSITO("Solo cruzaron la tienda", "Salieron por otro acceso sin detenerse")
}