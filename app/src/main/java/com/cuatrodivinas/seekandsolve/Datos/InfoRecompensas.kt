package com.cuatrodivinas.seekandsolve.Datos

import java.io.Serializable

class InfoRecompensa(
    val idRecompensa: String,
    val nombreRecompensa: String,
    val nombreDesafio: String,
    val fecha: String
): Serializable {
    constructor() : this("", "", "", "")
}