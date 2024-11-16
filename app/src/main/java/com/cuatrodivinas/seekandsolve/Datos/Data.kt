package com.cuatrodivinas.seekandsolve.Datos

import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class Data {
    companion object{
        const val PERMISO_CAMARA = 0
        const val PERMISO_GALERIA = 1
        const val MY_PERMISSION_REQUEST_CAMERA = 2
        const val PATH_USERS = "usuarios"
        const val PATH_DESAFIOS = "desafios"
        const val PATH_CARRERAS = "carreras"
        const val PATH_RECOMPENSAS = "recompensas"
        const val PATH_PREGUNTAS = "preguntas"
        lateinit var auth: FirebaseAuth
        lateinit var database: FirebaseDatabase
    }

    init {
        auth = Firebase.auth
        database = FirebaseDatabase.getInstance()
    }
}