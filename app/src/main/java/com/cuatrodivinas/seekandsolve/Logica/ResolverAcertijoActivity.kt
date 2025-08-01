package com.cuatrodivinas.seekandsolve.Logica

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageView
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.cuatrodivinas.seekandsolve.Datos.CarreraActual
import com.cuatrodivinas.seekandsolve.Datos.Data.Companion.PATH_PREGUNTAS
import com.cuatrodivinas.seekandsolve.Datos.Data.Companion.PATH_USERS
import com.cuatrodivinas.seekandsolve.Datos.Data.Companion.auth
import com.cuatrodivinas.seekandsolve.Datos.Data.Companion.database
import com.cuatrodivinas.seekandsolve.Datos.Data.Companion.storage
import com.cuatrodivinas.seekandsolve.Datos.Desafio
import com.cuatrodivinas.seekandsolve.Datos.Pregunta
import com.cuatrodivinas.seekandsolve.Datos.Punto
import com.cuatrodivinas.seekandsolve.R
import com.google.firebase.database.DataSnapshot
import com.google.firebase.storage.StorageReference
import java.time.LocalDateTime

class ResolverAcertijoActivity : AppCompatActivity() {
    lateinit var acertijo: TextView
    lateinit var imagenAcertijo: ImageView
    lateinit var listaRespuestas: ListView
    lateinit var btnMarcarCheckpoint: Button
    lateinit var btnPista: Button
    lateinit var intentMarcarCheckpoint: Intent
    lateinit var intentPista: Intent
    lateinit var desafio: Desafio
    lateinit var carreraActual: CarreraActual
    lateinit var pregunta: Pregunta
    lateinit var preguntaSeleccionada: String
    lateinit var punto: Punto
    var intentos: Int = 0
    private lateinit var refImg: StorageReference

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
        punto = intent.getSerializableExtra("punto") as Punto
        carreraActual = intent.getSerializableExtra("carreraActual") as CarreraActual
        obtenerPregunta()
    }

    private fun obtenerPregunta(){
        val preguntaRef = database.getReference(PATH_PREGUNTAS)
        preguntaRef.get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val snapshot: DataSnapshot = task.result
                val preguntasMap = snapshot?.children?.map { it.key to it.getValue(Pregunta::class.java) }?.toMap()
                if (preguntasMap != null) {
                    val randomKey = preguntasMap.keys.random()
                    val preguntaAleatoria = preguntasMap[randomKey]
                    if (preguntaAleatoria != null) {
                        pregunta = preguntaAleatoria
                        inicializarElementos()
                    }
                }
            } else {
                Log.e("Firebase", "Error al obtener los datos", task.exception)
            }
        }
    }

    private fun inicializarElementos(){
        acertijo.text = pregunta.enunciado

        val idPregunta = pregunta.id
        refImg = storage.getReference(PATH_PREGUNTAS).child("$idPregunta.jpg")
        refImg.downloadUrl.addOnSuccessListener { uri ->
            Glide.with(this)
                .load(uri) // Carga la imagen desde la URL
                .into(imagenAcertijo)
        }.addOnFailureListener { exception ->
            imagenAcertijo.setImageResource(R.drawable.foto_bandera)
        }

        val adapter = ArrayAdapter(
            this,                     // Contexto
            android.R.layout.simple_list_item_1, // Diseño simple proporcionado por Android para cada elemento
            pregunta.opciones                      // Datos a mostrar
        )
        listaRespuestas.adapter = adapter
        var lastSelectedPosition: Int = -1
        listaRespuestas.setOnItemClickListener { parent, view, position, id ->
            if (lastSelectedPosition != -1) {
                // Si hay un ítem previamente seleccionado, se resalta de nuevo
                val previousView = parent.getChildAt(lastSelectedPosition)
                previousView.setBackgroundColor(resources.getColor(android.R.color.white)) // Volver al color normal
            }

            // Resaltar el ítem seleccionado
            view.setBackgroundColor(resources.getColor(android.R.color.darker_gray)) // Resaltar el ítem presionado
            lastSelectedPosition = position
            preguntaSeleccionada = pregunta.opciones[position]
            intentos++;
        }
    }

    override fun onResume() {
        super.onResume()
        btnMarcarCheckpoint.setOnClickListener {
            if(!isRespuestaCorrecta()){
                return@setOnClickListener
            }
            carreraActual.puntosCompletados.add(punto)
            if(intent.getBooleanExtra("puntoFinal", false)){
                val intentDesafio = Intent(this@ResolverAcertijoActivity, DesafioTerminadoActivity::class.java)
                intentDesafio.putExtra("desafio", desafio)
                intentDesafio.putExtra("carreraActual", carreraActual)
                startActivity(intentDesafio)
                return@setOnClickListener
            }

            // Update carreraActual in the database
            val userRef = database.getReference("$PATH_USERS/${auth.currentUser!!.uid}/carreraActual")
            userRef.setValue(carreraActual).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    intentMarcarCheckpoint.putExtra("desafio", desafio)
                    intentMarcarCheckpoint.putExtra("carreraActual", carreraActual)
                    startActivity(intentMarcarCheckpoint)
                } else {
                    Toast.makeText(this, "Error updating carreraActual", Toast.LENGTH_SHORT).show()
                }
            }
        }

        btnPista.setOnClickListener {
            startActivity(intentPista)
        }
    }

    private fun isRespuestaCorrecta(): Boolean{
        if(preguntaSeleccionada.equals(pregunta.respuestaCorrecta) && intentos == 1){
            Toast.makeText(this, "Respuesta correcta! A la primera!", Toast.LENGTH_LONG).show()
            carreraActual.acertijosPrimerIntento++
            return true
        }
        if(preguntaSeleccionada.equals(pregunta.respuestaCorrecta)){
            Toast.makeText(this, "Respuesta correcta!", Toast.LENGTH_LONG).show()
            return true
        }
        if(!preguntaSeleccionada.equals(pregunta.respuestaCorrecta)){
            Toast.makeText(this, "Respuesta incorrecta :(", Toast.LENGTH_LONG).show()
            return false
        }
        return false
    }
}