package com.cuatrodivinas.seekandsolve.Logica

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageView
import android.widget.ListView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.cuatrodivinas.seekandsolve.Datos.Desafio
import com.cuatrodivinas.seekandsolve.Datos.Pregunta
import com.cuatrodivinas.seekandsolve.R
import com.squareup.picasso.Picasso

class ResolverAcertijoActivity : AppCompatActivity() {
    lateinit var acertijo: TextView
    lateinit var imagenAcertijo: ImageView
    lateinit var listaRespuestas: ListView
    lateinit var btnMarcarCheckpoint: Button
    lateinit var btnPista: Button
    lateinit var intentMarcarCheckpoint: Intent
    lateinit var intentPista: Intent
    lateinit var desafio: Desafio

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
        desafio = intent.getSerializableExtra("desafio") as Desafio
        inicializarElementos()
    }

    private fun inicializarElementos(){
        //Obtener la pregunta random
        val pregunta: Pregunta = Pregunta("Porque se extinguieron los mamuts", "a", "b", "C", "d", "d", "")
        acertijo.text = pregunta.enunciado
        Picasso.get()
            .load(pregunta.imagenUrl) // URL de la imagen
            .placeholder(R.drawable.cargando) // Imagen de marcador de posición mientras se carga
            .error(R.drawable.error) // Imagen en caso de error
            .into(imagenAcertijo)
        val datos = arrayListOf(pregunta.opcion1, pregunta.opcion2, pregunta.opcion3, pregunta.opcion4)
        val adapter = ArrayAdapter(
            this,                     // Contexto
            android.R.layout.simple_list_item_1, // Diseño simple proporcionado por Android para cada elemento
            datos                      // Datos a mostrar
        )
        listaRespuestas.adapter = adapter
    }

    override fun onResume() {
        super.onResume()
        btnMarcarCheckpoint.setOnClickListener {
            if(intent.getBooleanExtra("puntoFinal", false)){
                val intentDesafio = Intent(this@ResolverAcertijoActivity, DesafioTerminadoActivity::class.java)
                intentDesafio.putExtra("desafio", desafio)
                startActivity(intentDesafio)
                return@setOnClickListener
            }
            startActivity(intentMarcarCheckpoint)
        }

        btnPista.setOnClickListener {
            startActivity(intentPista)
        }
    }
}