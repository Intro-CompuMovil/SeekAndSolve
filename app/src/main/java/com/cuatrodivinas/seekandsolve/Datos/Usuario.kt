package com.cuatrodivinas.seekandsolve.Datos;

class Usuario(var nombre: String, var nombreUsuario: String, var correo: String, var password: String,
                var imagenUrl: String, var fechaNacimiento: String, var amigos: MutableList<String>) {

    constructor() : this("", "", "", "", "", "", mutableListOf())
}
