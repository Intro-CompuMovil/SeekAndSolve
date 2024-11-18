package com.cuatrodivinas.seekandsolve.Logica

import android.content.Intent
import android.database.Cursor
import android.database.MatrixCursor
import android.opengl.Matrix
import android.os.Bundle
import android.widget.Button
import android.widget.CursorAdapter
import android.widget.ImageView
import android.widget.ListView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.cuatrodivinas.seekandsolve.Datos.Carrera
import com.cuatrodivinas.seekandsolve.Datos.Data.Companion.PATH_RECOMPENSAS
import com.cuatrodivinas.seekandsolve.Datos.Data.Companion.storage
import com.cuatrodivinas.seekandsolve.Datos.Recompensa
import com.cuatrodivinas.seekandsolve.R
import com.google.firebase.storage.StorageReference
import com.squareup.picasso.Picasso

class EstadisticasCarreraActivity : AppCompatActivity() {
    private lateinit var listaEstadisticas: ListView

    private lateinit var imagenRecompensa: ImageView
    private lateinit var descripcionRecompensa: TextView

    private lateinit var botonVolverAInicio: Button
    private lateinit var intentMain: Intent
    private lateinit var carrera: Carrera
    private lateinit var refImg: StorageReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_estadisticas_carrera)

        listaEstadisticas = findViewById(R.id.listaEstadisticas)

        imagenRecompensa = findViewById(R.id.imagenRecompensa)
        descripcionRecompensa = findViewById(R.id.descripcionRecompensa)
        carrera = intent.getSerializableExtra("carrera") as Carrera

        botonVolverAInicio = findViewById(R.id.volver)
        intentMain = Intent(this, MainActivity::class.java)
        inicializarEstadisticas()
        inicializarRecompensa()
    }

    private fun inicializarEstadisticas(){
        var matrixCursor = MatrixCursor(arrayOf("_id", "imagen", "nombre", "dato"))
        matrixCursor.addRow(arrayOf(0, R.drawable.outline_timer_24, "Tiempo total", carrera.tiempoTotal))
        matrixCursor.addRow(arrayOf(1, R.drawable.start_point_marker, "Checkpoints marcados", carrera.puntosCompletados.size - 2))
        matrixCursor.addRow(arrayOf(2, R.drawable.baseline_psychology_24, "Acertijos resueltos", carrera.acertijosPrimerIntento))
        matrixCursor.addRow(arrayOf(3, R.drawable.baseline_speed_24, "Velocidad media", carrera.velocidadMedia))
        matrixCursor.addRow(arrayOf(4, R.drawable.distancia, "Distancia recorrida", carrera.distanciaTotal))
        var adapter = EstadisticasAdapter(this, matrixCursor, 0)
        listaEstadisticas.adapter = adapter
    }

    private fun inicializarRecompensa(){
        val recompensa: Recompensa = Recompensa("1","Capitan Cuac Cuac")
        val idRecompensa = recompensa.id
        refImg = storage.getReference(PATH_RECOMPENSAS).child("$idRecompensa.jpg")
        refImg.downloadUrl.addOnSuccessListener { uri ->
            Glide.with(this)
                .load(uri) // Carga la imagen desde la URL
                .into(imagenRecompensa)
        }.addOnFailureListener { exception ->
            imagenRecompensa.imageTintList = getColorStateList(R.color.primaryColor)
        }
    }

    override fun onResume() {
        super.onResume()
        botonVolverAInicio.setOnClickListener {
            startActivity(intentMain)
        }
    }
}