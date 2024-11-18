package com.cuatrodivinas.seekandsolve.Datos

import java.io.Serializable

class Desafio(
    var id: String,
    var uidCreador: String,
    var nombre: String,
    var imagenUrl: String,
    var descripcion: String,
    var puntoInicial: Punto,
    var puntosIntermedios: MutableList<Punto>,
    var puntoFinal: Punto): Serializable{

    // Convertir a Map para subir a Realtime Database (sin id)
    fun toMap(): Map<String, Any> {
        return mapOf(
            "uidCreador" to uidCreador,
            "nombre" to nombre,
            "imagenUrl" to imagenUrl,
            "descripcion" to descripcion,
            "puntoInicial" to puntoInicial,
            "puntosIntermedios" to puntosIntermedios,
            "puntoFinal" to puntoFinal
        )
    }
}