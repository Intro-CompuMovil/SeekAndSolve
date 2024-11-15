package com.cuatrodivinas.seekandsolve.Datos;

class Usuario(var nombre: String, var nombreUsuario: String, var correo: String, var password: String,
                    var imagenUrl: String,
                     var fechaNacimiento: String) {

    constructor() : this("", "", "", "", "", "")
}
