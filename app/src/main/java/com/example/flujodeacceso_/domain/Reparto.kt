package com.example.flujodeacceso_.domain

/**
 * Reparte "total" en partes proporcionales a "pesos".
 * La última parte recibe lo que sobra de las divisiones, así la suma es EXACTA.
 * Ej.: repartir(10, listOf(1, 1, 1)) = [3, 3, 4]
 */
fun repartir(total: Int, pesos: List<Int>): List<Int> {
    val sumaPesos = pesos.sum()
    val partes = pesos.dropLast(1).map { peso -> total * peso / sumaPesos }
    return partes + (total - partes.sum())
}