package com.cuatrodivinas.seekandsolve.Logica

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.cuatrodivinas.seekandsolve.R
import com.cuatrodivinas.seekandsolve.databinding.ActivityVerPerfilBinding

class VerPerfil : AppCompatActivity() {
    private lateinit var binding: ActivityVerPerfilBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVerPerfilBinding.inflate(layoutInflater)
        setContentView(binding.root)
        inicializarComponentes()
        quemarDatos()
        eventoRecompensas()
        eventoEstadisticas()
        eventoVolver()
        eventoEditarPerfil()
    }

    fun inicializarComponentes(){
        binding.nombreETxt.isEnabled = false
        binding.corrreoETxt.isEnabled = false
        binding.FechaETxt.isEnabled = false
    }

    fun quemarDatos(){
        binding.nombreUsuarioTxt.text = "Osquitar_El_Wapo"
        binding.imagenPerfil.setImageResource(R.drawable.foto_oscar)
        binding.nombreETxt.setText("Oscar Danilo Martínez Bernal")
        binding.corrreoETxt.setText("oscar@ejemplo.com")
        binding.FechaETxt.setText("11/01/1865")
    }

    private fun eventoRecompensas() {
        binding.recompensasBtn.setOnClickListener {
            startActivity(Intent(this, RecompensasCarrera::class.java))
        }
    }

    private fun eventoEstadisticas() {
        binding.estadisticasBtn.setOnClickListener {
            startActivity(Intent(this, EstadisticasCarreras::class.java))
        }
    }

    private fun eventoVolver() {
        binding.volverBtn.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
        }
    }

    private fun eventoEditarPerfil() {
        binding.editarPerfilBtn.setOnClickListener {
            startActivity(Intent(this, EditarPerfil::class.java))
        }
    }
}