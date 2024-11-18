package com.cuatrodivinas.seekandsolve.Datos

class Pregunta(var id: String,
               var enunciado: String,
               var preguntas: MutableList<String>,
               var respuestaCorrecta: String, var imagenUrl: String) {

    constructor(): this("", "", mutableListOf(), "", "")
}