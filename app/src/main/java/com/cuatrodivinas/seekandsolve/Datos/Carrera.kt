package com.cuatrodivinas.seekandsolve.Datos

data class Carrera(
    val idDesafio: String = "",
    // Map<uid, UsuarioCarrera> ---> "uid1": { "idRecompensa": 1 },
    val usuarios: Map<String, UsuarioCarrera> = emptyMap(),
    // Tiempo total en minutos
    val tiempoTotal: Int = 0,
    val acertijosPrimerIntento: Int = 0,
    val velocidadMedia: Int = 0,
    val distanciaTotal: Int = 0,
    val fecha: String = "",
    var puntosCompletados: MutableList<Punto> = mutableListOf()
) {
    data class UsuarioCarrera(
        val idRecompensa: String = ""
    )
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
