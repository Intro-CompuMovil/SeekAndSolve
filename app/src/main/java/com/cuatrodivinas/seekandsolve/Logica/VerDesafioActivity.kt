package com.cuatrodivinas.seekandsolve.Logica

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.cuatrodivinas.seekandsolve.R

class VerDesafioActivity : AppCompatActivity() {
    lateinit var tituloDesafio: TextView
    lateinit var imagenDesafio: ImageView
    lateinit var descripcionDesafio: TextView
    lateinit var puntoInicial: TextView
    lateinit var puntoFinal: TextView
    lateinit var btnIniciarDesafio: Button
    lateinit var btnRevisarTrayecto: Button
    lateinit var intentIniciarDesafio: Intent
    lateinit var intentRevisarTrayecto: Intent
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ver_desafio)
        tituloDesafio = findViewById(R.id.tituloDesafio)
        imagenDesafio = findViewById(R.id.imagenDesafio)
        descripcionDesafio = findViewById(R.id.descripcionDesafio)
        puntoInicial = findViewById(R.id.puntoInicial)
        puntoFinal = findViewById(R.id.puntoFinal)
        btnIniciarDesafio = findViewById(R.id.iniciarDesafio)
        btnRevisarTrayecto = findViewById(R.id.revisarTrayecto)
        intentIniciarDesafio = Intent(this, IniciarRutaActivity::class.java)
        intentRevisarTrayecto = Intent(this, TrayectoDesafioActivity::class.java)

    }
}