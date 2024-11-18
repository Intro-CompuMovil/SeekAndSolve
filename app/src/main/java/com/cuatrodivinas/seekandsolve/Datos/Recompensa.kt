package com.cuatrodivinas.seekandsolve.Datos

import java.io.Serializable

class Recompensa(
    var id: String,
    var nombre: String): Serializable {

        constructor(): this("", "")
}