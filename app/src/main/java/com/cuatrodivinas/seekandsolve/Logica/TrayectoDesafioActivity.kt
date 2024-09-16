package com.cuatrodivinas.seekandsolve.Logica

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.cuatrodivinas.seekandsolve.R
import com.google.android.gms.maps.MapView

class TrayectoDesafioActivity : AppCompatActivity() {
    lateinit var tituloTrayecto: TextView
    lateinit var mapa: MapView
    lateinit var btnIniciarDesafio: Button
    lateinit var intentIniciarRuta: Intent

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_trayecto_desafio)
        tituloTrayecto = findViewById(R.id.tituloTrayecto)
        mapa = findViewById(R.id.mapView)
        btnIniciarDesafio = findViewById(R.id.iniciarDesafio)
        intentIniciarRuta = Intent(this, IniciarRutaActivity::class.java)
    }

    override fun onResume() {
        super.onResume()
        btnIniciarDesafio.setOnClickListener {
            startActivity(intentIniciarRuta)
        }
    }
}