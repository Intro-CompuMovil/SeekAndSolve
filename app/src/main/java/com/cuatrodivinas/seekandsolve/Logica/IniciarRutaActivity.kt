package com.cuatrodivinas.seekandsolve.Logica

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.cuatrodivinas.seekandsolve.R

class IniciarRutaActivity : AppCompatActivity() {
    private lateinit var tituloRuta: TextView

    private lateinit var botonAbandonar: Button
    private lateinit var intentAbandonar: Intent

    private lateinit var botonResolverAcertijo: Button
    private lateinit var intentResolverAcertijo: Intent

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_iniciar_ruta)

        tituloRuta = findViewById(R.id.tituloRuta)

        botonAbandonar = findViewById(R.id.abandonar)
        botonResolverAcertijo = findViewById(R.id.resolverAcertijo)

        intentAbandonar = Intent(this, MainActivity::class.java)
        intentResolverAcertijo = Intent(this, ResolverAcertijoActivity::class.java)
    }

    override fun onResume() {
        super.onResume()
        botonAbandonar.setOnClickListener {
            startActivity(intentAbandonar)
        }

        botonResolverAcertijo.setOnClickListener {
            startActivity(intentResolverAcertijo)
        }
    }
}