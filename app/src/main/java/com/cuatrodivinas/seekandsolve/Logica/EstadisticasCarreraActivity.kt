package com.cuatrodivinas.seekandsolve.Logica

import android.content.Intent
import android.database.MatrixCursor
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.ListView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.cuatrodivinas.seekandsolve.Datos.Carrera
import com.cuatrodivinas.seekandsolve.Datos.CarreraUsuarioCompletada
import com.cuatrodivinas.seekandsolve.Datos.Data.Companion.PATH_RECOMPENSAS
import com.cuatrodivinas.seekandsolve.Datos.Data.Companion.storage
import com.cuatrodivinas.seekandsolve.Datos.InfoRecompensa
import com.cuatrodivinas.seekandsolve.Datos.Recompensa
import com.cuatrodivinas.seekandsolve.R
import com.cuatrodivinas.seekandsolve.databinding.ActivityEstadisticasCarreraBinding
import com.google.firebase.storage.StorageReference
import kotlin.properties.Delegates

class EstadisticasCarreraActivity : AppCompatActivity() {
    private lateinit var binding: ActivityEstadisticasCarreraBinding
    private lateinit var carreraCompletada: CarreraUsuarioCompletada
    private var checkpointsMarcados by Delegates.notNull<Int>()
    private lateinit var infoRecompensa: InfoRecompensa

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEstadisticasCarreraBinding.inflate(layoutInflater)
        setContentView(binding.root)

        carreraCompletada = intent.getSerializableExtra("carreraUsuarioCompletada") as CarreraUsuarioCompletada
        checkpointsMarcados = intent.getIntExtra("checkpointsMarcados", 0)
        infoRecompensa = intent.getSerializableExtra("infoRecompensa") as InfoRecompensa

        inicializarEstadisticas()
        inicializarRecompensa()
    }

    private fun inicializarEstadisticas() {
        val matrixCursor = MatrixCursor(arrayOf("_id", "imagen", "nombre", "dato"))
        matrixCursor.addRow(arrayOf(0, R.drawable.outline_timer_24, "Tiempo total", "${carreraCompletada.tiempoTotal / 60} min"))
        matrixCursor.addRow(arrayOf(1, R.drawable.start_point_marker, "Checkpoints marcados", checkpointsMarcados))
        matrixCursor.addRow(arrayOf(2, R.drawable.baseline_psychology_24, "Acertijos resueltos", carreraCompletada.acertijosPrimerIntento))
        // Usar solo 2 decimales para la velocidad y la distancia
        matrixCursor.addRow(arrayOf(3, R.drawable.baseline_speed_24, "Velocidad media", "${String.format("%.2f", carreraCompletada.distanciaTotal / carreraCompletada.tiempoTotal)} m/s"))
        matrixCursor.addRow(arrayOf(4, R.drawable.distancia, "Distancia recorrida", "${String.format("%.2f", carreraCompletada.distanciaTotal)} m"))
        val adapter = EstadisticasAdapter(this, matrixCursor, 0)
        binding.listaEstadisticas.adapter = adapter
    }

    private fun inicializarRecompensa() {
        binding.descripcionRecompensa.text = infoRecompensa.nombreRecompensa
        val idRecompensa = infoRecompensa.idRecompensa
        val refImg = storage.getReference(PATH_RECOMPENSAS).child("$idRecompensa.jpg")
        refImg.downloadUrl.addOnSuccessListener { uri ->
            Glide.with(this)
                .load(uri)
                .into(binding.imagenRecompensa)
        }.addOnFailureListener {
            binding.imagenRecompensa.setImageResource(R.drawable.foto_bandera)
        }
    }

    override fun onResume() {
        super.onResume()
        binding.volver.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
            finish()
        }
    }
}