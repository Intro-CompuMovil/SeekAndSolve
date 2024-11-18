package com.cuatrodivinas.seekandsolve.Datos

import java.io.Serializable

class CarreraActual(
    val idCarrera: String,
    var acertijosPrimerIntento: Int,
    var distanciaRecorrida: Int,
    var puntosCompletados: MutableList<Punto>
): Serializable {

    constructor(): this("", 0, 0, mutableListOf())
}