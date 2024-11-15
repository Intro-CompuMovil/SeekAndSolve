package com.cuatrodivinas.seekandsolve.Datos

import java.io.Serializable

class Desafio(var id: Int, var nombre: String, var fotoUrl: String, var descripcion: String,
              var puntoInicial:Punto,
              var puntosIntermedios: MutableList<Punto>, var puntoFinal: Punto, var puntosCompletados: MutableList<Punto>): Serializable{
}