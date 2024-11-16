package com.cuatrodivinas.seekandsolve.Datos

import java.io.Serializable

class Desafio(var uidCreador: String, var nombre: String, var imagenUrl: String, var descripcion: String,
              var puntoInicial: Punto, var puntosIntermedios: MutableList<Punto>,
              var puntoFinal: Punto): Serializable{
}