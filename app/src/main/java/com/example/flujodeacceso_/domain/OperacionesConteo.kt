package com.example.flujodeacceso_.domain

import com.example.flujodeacceso_.model.CategoriaConteo
import com.example.flujodeacceso_.model.ConteoAcceso

/** Valor de una categoría dentro de un conteo. */
fun ConteoAcceso.valor(categoria: CategoriaConteo): Int = when (categoria) {
    CategoriaConteo.HOMBRE -> hombres
    CategoriaConteo.MUJER -> mujeres
    CategoriaConteo.MENOR -> menores
    CategoriaConteo.SIN_CLASIFICAR -> sinClasificar
    CategoriaConteo.TRANSITO -> transito
}

/** Devuelve un conteo NUEVO con "delta" sumado a la categoría (delta = +1 o -1). */
fun ConteoAcceso.sumar(categoria: CategoriaConteo, delta: Int): ConteoAcceso = when (categoria) {
    CategoriaConteo.HOMBRE -> copy(hombres = hombres + delta)
    CategoriaConteo.MUJER -> copy(mujeres = mujeres + delta)
    CategoriaConteo.MENOR -> copy(menores = menores + delta)
    CategoriaConteo.SIN_CLASIFICAR -> copy(sinClasificar = sinClasificar + delta)
    CategoriaConteo.TRANSITO -> copy(transito = transito + delta)
}