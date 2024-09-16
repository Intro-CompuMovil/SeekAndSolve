package com.cuatrodivinas.seekandsolve.Logica

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ListView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.cuatrodivinas.seekandsolve.R

class InvitarAmigosDesafioActivity : AppCompatActivity() {
    lateinit var listaAmigos: ListView
    lateinit var btnVolver: Button
    lateinit var btnInvitar: Button
    lateinit var intentConfigurar: Intent

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_invitar_amigos_desafio)
        listaAmigos = findViewById(R.id.listaAmigos)
        btnVolver = findViewById(R.id.volver)
        btnInvitar = findViewById(R.id.invitarAmigos)
        intentConfigurar = Intent(this, ConfigurarDesafioActivity::class.java)
    }

    override fun onResume() {
        super.onResume()
        btnVolver.setOnClickListener {
            startActivity(intentConfigurar)
        }

        btnInvitar.setOnClickListener {
            startActivity(intentConfigurar)
        }
    }
}