package com.cuatrodivinas.seekandsolve.Datos

class Pregunta(var id: String,
               var enunciado: String,
               var opciones: Array<String>,
               var respuestaCorrecta: String, var imagenUrl: String) {

    constructor(): this("", "", arrayOf(), "", "")
}