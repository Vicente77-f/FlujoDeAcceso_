package com.example.flujodeacceso_.model

enum class Acceso(
    val nombre: String,
    val conectaCon: String
){
   A1(nombre = "Acceso Galeria Central", conectaCon = "Pasillo principal del centro comercial"),
   A2(nombre = "Acceso Patio de Comidas", conectaCon = "Patio de Comidas, Nivel 2"),
   A3(nombre = "Acceso Estacionamientos", conectaCon = "Estacionamiento subterraneos"),
   A4(nombre = "Acceso Tienda Vecina", conectaCon = "Tienda departamental vecina")

}
