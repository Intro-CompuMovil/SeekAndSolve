package com.cuatrodivinas.seekandsolve.Datos

class Pregunta(var id: String,
               var enunciado: String,
               var opciones: MutableList<String>,
               var respuestaCorrecta: String, var imagenUrl: String) {

    constructor(): this("", "", mutableListOf(), "", "")
}