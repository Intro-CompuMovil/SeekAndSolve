package com.cuatrodivinas.seekandsolve.Datos

import java.io.Serializable

class Recompensa(
    val id: String,
    val nombre: String): Serializable {

        constructor(): this("", "")
}