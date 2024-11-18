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
    var usuariosEnProgreso: MutableList<String> = mutableListOf(),
): Serializable {
}

/*
"idDesafio": 1,
"usuarios": {
    "uid1": { "idRecompensa": 1 },
    "uid2": { "idRecompensa": 3 }
}
"tiempoTotal": 100,
"acertijosPrimerIntento": 5,
"velocidadMedia": 10,
"distanciaTotal": 1000,
"fecha": "16/11/2024"*/
