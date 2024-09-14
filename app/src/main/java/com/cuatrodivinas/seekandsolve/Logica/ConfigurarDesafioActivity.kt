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

class ConfigurarDesafioActivity : AppCompatActivity() {
    private lateinit var botonInvitarAmigos: Button
    private lateinit var intentInvitar: Intent
    private lateinit var botonJugarDesafio: Button
    private lateinit var intentJugar: Intent
    private lateinit var listaAmigos: ListView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_configurar_desafio)

        botonInvitarAmigos = findViewById(R.id.invitarAmigos)
        intentInvitar = Intent(this, InvitarAmigosDesafioActivity::class.java)
        botonJugarDesafio = findViewById(R.id.jugarDesafio)
        intentJugar = Intent(this, IniciarRutaActivity::class.java)

        listaAmigos = findViewById(R.id.listaAmigos)
    }

    override fun onResume() {
        super.onResume()
        botonInvitarAmigos.setOnClickListener {
            startActivity(intentInvitar)
        }

        botonJugarDesafio.setOnClickListener {
            startActivity(intentJugar)
        }
    }
}