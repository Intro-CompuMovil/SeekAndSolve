package com.cuatrodivinas.seekandsolve.Logica

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.cuatrodivinas.seekandsolve.R

class CrearDesafioActivity : AppCompatActivity() {
    private lateinit var etNombreDesafio: EditText

    private lateinit var botonCambiarFoto: Button


    private lateinit var botonEditarPuntoInicial: Button
    private lateinit var botonEditarPuntoFinal: Button
    private lateinit var intentEditarPunto: Intent

    private lateinit var listaCheckpoints: ListView
    private lateinit var botonAgregarCheckpoint: Button
    private lateinit var etDescripcionDesafio: EditText

    private lateinit var botonCrearDesafio: Button
    private lateinit var intentCrearDesafio: Intent

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_crear_desafio)

        etNombreDesafio = findViewById(R.id.nombreDesafio)
        botonCambiarFoto = findViewById(R.id.cambiarFoto)
        botonEditarPuntoInicial = findViewById(R.id.editarPuntoInicial)
        botonEditarPuntoFinal = findViewById(R.id.editarPuntoFinal)
        listaCheckpoints = findViewById(R.id.listaCheckpoints)
        botonAgregarCheckpoint = findViewById(R.id.agregarCheckpoint)
        etDescripcionDesafio = findViewById(R.id.descripcionDesafio)
        botonCrearDesafio = findViewById(R.id.crearDesafio)

        intentEditarPunto = Intent(this, SeleccionarPuntoActivity::class.java)
        intentCrearDesafio = Intent(this, VerDesafiosActivity::class.java)
    }

    override fun onResume() {
        super.onResume()
        botonEditarPuntoInicial.setOnClickListener {
            intentEditarPunto.putExtra("tipoPunto", "inicial")
            startActivity(intentEditarPunto)
        }

        botonEditarPuntoFinal.setOnClickListener {
            intentEditarPunto.putExtra("tipoPunto", "final")
            startActivity(intentEditarPunto)
        }

        botonCrearDesafio.setOnClickListener {
            startActivity(intentCrearDesafio)
        }
    }
}