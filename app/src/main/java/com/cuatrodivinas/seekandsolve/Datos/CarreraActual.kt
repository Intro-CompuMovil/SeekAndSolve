package com.cuatrodivinas.seekandsolve.Datos

import java.io.Serializable

class CarreraActual(
    val idCarrera: String,
    var acertijosPrimerIntento: Int,
    var distanciaRecorrida: Double,
    var puntosCompletados: MutableList<Punto>
): Serializable {

    constructor(): this("", 0, 0.0, mutableListOf())
}