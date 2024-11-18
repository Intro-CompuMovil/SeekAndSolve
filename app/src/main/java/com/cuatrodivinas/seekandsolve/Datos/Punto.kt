package com.cuatrodivinas.seekandsolve.Datos

import java.io.Serializable

class Punto(var latitud: Double, var longitud: Double): Serializable {
    constructor(): this(0.0, 0.0)
}