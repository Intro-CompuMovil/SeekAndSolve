package com.cuatrodivinas.seekandsolve.Datos

import java.io.Serializable

class CarreraUsuarioCompletada(
    var tiempoTotal: Int,
    var acertijosPrimerIntento: Int,
    // Velocidad media se puede calcular al final de la carrera dividiendo distanciaTotal entre tiempoTotal
    var distanciaTotal: Int,
    var  fechaCompletada: String,
    var idRecompensa: String): Serializable {

        constructor(): this(0, 0, 0, "", "")
}