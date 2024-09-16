package com.cuatrodivinas.seekandsolve.Logica

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.cuatrodivinas.seekandsolve.R
import com.cuatrodivinas.seekandsolve.databinding.ActivityEstadisticasCarrerasBinding

class EstadisticasCarreras : AppCompatActivity() {
    private lateinit var binding: ActivityEstadisticasCarrerasBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEstadisticasCarrerasBinding.inflate(layoutInflater)
        setContentView(binding.root)
        quemarDatos()
        eventoVolver()
    }

    private fun quemarDatos() {
        binding.imagenEstadistica.setImageResource(R.drawable.estadistica_carreras)
    }

    private fun eventoVolver(){
        binding.backButtonStats.setOnClickListener {
            finish()
        }
    }
}