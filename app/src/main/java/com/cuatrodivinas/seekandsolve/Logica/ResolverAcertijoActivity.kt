package com.cuatrodivinas.seekandsolve.Logica

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.ListView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.cuatrodivinas.seekandsolve.R

class ResolverAcertijoActivity : AppCompatActivity() {
    lateinit var acertijo: TextView
    lateinit var imagenAcertijo: ImageView
    lateinit var listaRespuestas: ListView
    lateinit var btnMarcarCheckpoint: Button
    lateinit var btnPista: Button
    lateinit var intentMarcarCheckpoint: Intent
    lateinit var intentPista: Intent
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_resolver_acertijo)
        acertijo = findViewById(R.id.acertijo)
        imagenAcertijo = findViewById(R.id.imagenAcertijo)
        listaRespuestas = findViewById(R.id.listaRespuestas)
        btnMarcarCheckpoint = findViewById(R.id.marcarCheckpoint)
        btnPista = findViewById(R.id.pista)
        intentMarcarCheckpoint = Intent(this, IniciarRutaActivity::class.java)
        intentPista = Intent(this, PistaActivity::class.java)
    }

    override fun onResume() {
        super.onResume()
        btnMarcarCheckpoint.setOnClickListener {
            startActivity(intentMarcarCheckpoint)
        }

        btnPista.setOnClickListener {
            startActivity(intentPista)
        }
    }
}