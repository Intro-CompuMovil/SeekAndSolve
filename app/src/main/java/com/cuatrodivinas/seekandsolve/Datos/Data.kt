package com.cuatrodivinas.seekandsolve.Datos

import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage

class Data {
    companion object {
        const val PERMISO_CAMARA = 0
        const val PERMISO_GALERIA = 1
        const val MY_PERMISSION_REQUEST_CAMERA = 2
        const val PERMISO_NOTIFICACIONES = 3
        const val MY_PERMISSION_REQUEST_GALLERY = 4
        const val PATH_USERS = "usuarios"
        const val PATH_DESAFIOS = "desafios"
        const val PATH_CARRERAS = "carreras"
        const val PATH_PREGUNTAS = "preguntas"
        const val PATH_IMAGENES = "profileImg"
        const val PATH_RECOMPENSAS = "recompensas"
        const val PATH_ACTIVOS = "activos"
        var auth: FirebaseAuth
        var database: FirebaseDatabase
        var storage: FirebaseStorage
        var entrou: Boolean = false

        init {
            auth = Firebase.auth
            database = FirebaseDatabase.getInstance()
            storage = FirebaseStorage.getInstance()
        }
    }
}