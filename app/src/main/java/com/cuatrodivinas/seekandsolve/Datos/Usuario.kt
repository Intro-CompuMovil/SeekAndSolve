package com.cuatrodivinas.seekandsolve.Datos;

class Usuario(
    var nombre: String,
    var nombreUsuario: String,
    var correo: String,
    // TODO: quitar password del modelo
    var password: String,
    var imagenUrl: String,
    var fechaNacimiento: String,
    var amigos: MutableMap<String, String>,
    var carreraActual: CarreraActual?,
    // Que el id de cada recompensa sea el id de la carrera
    var recompensas: MutableMap<String, InfoRecompensa>
    ) {

    constructor() : this("", "", "", "", "", "", mutableMapOf(), null, mutableMapOf())
}
