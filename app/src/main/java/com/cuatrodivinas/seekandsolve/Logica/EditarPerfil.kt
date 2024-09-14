package com.cuatrodivinas.seekandsolve.Logica

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.view.MotionEvent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.cuatrodivinas.seekandsolve.R
import com.cuatrodivinas.seekandsolve.databinding.ActivityEditarPerfilBinding

class EditarPerfil : AppCompatActivity() {
    private lateinit var binding: ActivityEditarPerfilBinding
    private var contraseniaVisible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditarPerfilBinding.inflate(layoutInflater)
        setContentView(binding.root)
        quemarDatos()
        eventoVolver()
        eventoAplicarCambios()
    }

    private fun quemarDatos() {
        binding.nombreUsuarioETxt.setHint("Osquitar_El_Wapo")
        binding.imagenPerfil.setImageResource(R.drawable.foto_oscar)
        binding.nombreETxt.setHint("Oscar Danilo Martínez Bernal")
        binding.corrreoETxt.setHint("oscar@ejemplo.com")
        binding.contraETxt.setHint("*********")
        binding.FechaETxt.setHint("11/01/1865")
    }

    private fun eventoVolver() {
        binding.volverBtn.setOnClickListener {
            startActivity(Intent(this, VerPerfil::class.java))
        }
    }

    private fun eventoAplicarCambios() {
        binding.aplicarCambiosBtn.setOnClickListener {
            startActivity(Intent(this, VerPerfil::class.java))
        }
    }
}