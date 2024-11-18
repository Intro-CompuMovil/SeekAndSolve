package com.cuatrodivinas.seekandsolve.Datos;

class Usuario(
    var nombre: String,
    var nombreUsuario: String,
    var correo: String,
    // TODO: quitar password del modelo
    var password: String,
    var imagenUrl: String,
    var fechaNacimiento: String,
    var amigos: MutableList<String>,
    var carreraActual: CarreraActual?
    ) {

    constructor() : this("", "", "", "", "", "", mutableListOf(), null)
}
