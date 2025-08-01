package com.cuatrodivinas.seekandsolve.Datos

import java.io.Serializable
import java.time.LocalDate
import java.time.LocalDateTime

data class Carrera(
    var id: String = "",
    var idDesafio: String = "",
    var horaInicio: String = "",
    // uid -> CarreraUsuarioCompletada
    var usuariosCompletaron: MutableMap<String, CarreraUsuarioCompletada> = mutableMapOf(),
    // uids de los usuarios que están en progreso (cada uno almacena su progreso en usuarios/uid/carreraActual)
    // uid -> nombreUsuario
    var usuariosEnProgreso: MutableMap<String, String> = mutableMapOf(),
): Serializable {

    constructor(): this("", "", "", mutableMapOf(), mutableMapOf())
}
