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

class DesafioTerminadoActivity : AppCompatActivity() {
    private lateinit var tituloDesafio: TextView
    private lateinit var imagenDesafio: ImageView

    private lateinit var imagenRecompensa: ImageView
    private lateinit var descripcionRecompensa: TextView

    private lateinit var botonEstadisticas: Button
    private lateinit var intent: Intent

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_desafio_terminado)

        tituloDesafio = findViewById(R.id.tituloDesafio)
        imagenDesafio = findViewById(R.id.imagenDesafio)

        imagenRecompensa = findViewById(R.id.imagenRecompensa)
        descripcionRecompensa = findViewById(R.id.descripcionRecompensa)

        botonEstadisticas = findViewById(R.id.estadisticas)
        intent = Intent(this, EstadisticasCarreraActivity::class.java)
    }

    override fun onResume() {
        super.onResume()
        botonEstadisticas.setOnClickListener {
            startActivity(intent)
        }
    }
}