package com.example.almondires.models

data class Reservas(
    val id: Int,
    val nombre: String,
    val fechaInicio: String,
    val fechaFinal: String,
    val precio: Double,
    val telf: String, // Cambiado de Int a String para evitar desbordamiento
    val noches: Int
)