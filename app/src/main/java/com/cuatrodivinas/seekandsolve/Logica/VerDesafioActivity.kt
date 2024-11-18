package com.cuatrodivinas.seekandsolve.Logica

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.cuatrodivinas.seekandsolve.Datos.Carrera
import com.cuatrodivinas.seekandsolve.Datos.Data.Companion.PATH_DESAFIOS
import com.cuatrodivinas.seekandsolve.Datos.Data.Companion.PATH_IMAGENES
import com.cuatrodivinas.seekandsolve.Datos.Data.Companion.auth
import com.cuatrodivinas.seekandsolve.Datos.Data.Companion.storage
import com.cuatrodivinas.seekandsolve.Datos.Desafio
import com.cuatrodivinas.seekandsolve.Datos.Punto
import com.cuatrodivinas.seekandsolve.R
import com.cuatrodivinas.seekandsolve.databinding.ActivityVerDesafioBinding
import com.google.firebase.crashlytics.buildtools.reloc.com.google.common.reflect.TypeToken
import com.google.firebase.storage.StorageReference
import com.google.gson.Gson

class VerDesafioActivity : AppCompatActivity() {
    private lateinit var binding: ActivityVerDesafioBinding
    private lateinit var refImg: StorageReference
    private lateinit var desafio: Desafio

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVerDesafioBinding.inflate(layoutInflater)
        setContentView(binding.root)

        desafio = intent.getSerializableExtra("desafio") as Desafio

        inicializarElementos()
        setupListeners()
    }

    private fun inicializarElementos() {
        binding.tituloDesafio.text = desafio.nombre
        binding.descripcionDesafio.text = desafio.descripcion
        binding.puntoInicial.text = "Punto inicial: " + desafio.puntoInicial.latitud.toString() + ", " + desafio.puntoInicial.longitud.toString()
        binding.puntoFinal.text = "Punto final: " + desafio.puntoFinal!!.latitud.toString() + ", " + desafio.puntoFinal!!.longitud.toString()

        refImg = storage.getReference(PATH_DESAFIOS).child("${desafio.id}.jpg")
        refImg.downloadUrl.addOnSuccessListener { uri ->
            Glide.with(this)
                .load(uri)
                .into(binding.imagenDesafio)
        }.addOnFailureListener {
            binding.imagenDesafio.setImageResource(R.drawable.foto_bandera)
        }
    }

    private fun setupListeners() {
        binding.iniciarDesafio.setOnClickListener {
            val intent = Intent(this, ConfigurarCarreraActivity::class.java)
            intent.putExtra("desafio", desafio)
            startActivity(intent)
        }

        binding.revisarTrayecto.setOnClickListener {
            val intent = Intent(this, TrayectoDesafioActivity::class.java)
            intent.putExtra("desafio", desafio)
            startActivity(intent)
        }

        binding.backButtonChallenge.setOnClickListener {
            finish()
        }
    }
}