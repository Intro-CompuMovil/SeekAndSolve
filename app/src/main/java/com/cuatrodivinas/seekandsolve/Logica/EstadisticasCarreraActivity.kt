package com.cuatrodivinas.seekandsolve.Logica

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.ListView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.cuatrodivinas.seekandsolve.R

class EstadisticasCarreraActivity : AppCompatActivity() {
    private lateinit var listaEstadisticas: ListView

    private lateinit var imagenRecompensa: ImageView
    private lateinit var descripcionRecompensa: TextView

    private lateinit var botonVolverAInicio: Button
    private lateinit var intent: Intent

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_estadisticas_carrera)

        listaEstadisticas = findViewById(R.id.listaEstadisticas)

        imagenRecompensa = findViewById(R.id.imagenRecompensa)
        descripcionRecompensa = findViewById(R.id.descripcionRecompensa)

        botonVolverAInicio = findViewById(R.id.volver)
        intent = Intent(this, MainActivity::class.java)
    }

    override fun onResume() {
        super.onResume()
        botonVolverAInicio.setOnClickListener {
            startActivity(intent)
        }
    }
}