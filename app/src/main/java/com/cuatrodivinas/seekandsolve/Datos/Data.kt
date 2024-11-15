package com.cuatrodivinas.seekandsolve.Datos

import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth

class Data {
    companion object{
        const val PERMISO_CAMARA = 0
        const val PERMISO_GALERIA = 1
        const val MY_PERMISSION_REQUEST_CAMERA = 2
        const val PATH_USERS = "usuarios/"
        lateinit var auth: FirebaseAuth
    }

    init {
        auth = Firebase.auth
    }
}