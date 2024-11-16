package com.cuatrodivinas.seekandsolve.Logica

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.cuatrodivinas.seekandsolve.Datos.Desafio
import com.cuatrodivinas.seekandsolve.Datos.Punto
import com.cuatrodivinas.seekandsolve.R
import com.cuatrodivinas.seekandsolve.databinding.ActivityVerDesafioBinding
import com.google.firebase.crashlytics.buildtools.reloc.com.google.common.reflect.TypeToken
import com.google.gson.Gson

class VerDesafioActivity : AppCompatActivity() {
    private lateinit var binding: ActivityVerDesafioBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVerDesafioBinding.inflate(layoutInflater)
        setContentView(binding.root)

        inicializarElementos()
    }

    private fun inicializarElementos() {
        val desafio = intent.getBundleExtra("bundle")!!
        binding.tituloDesafio.text = desafio.getString("nombre")
        binding.descripcionDesafio.text = desafio.getString("descripcion")
        val textoPuntoInicial: String = "Punto inicial: " + desafio.getString("puntoInicial")
        binding.puntoInicial.text = textoPuntoInicial
        val textoPuntoFinal: String = "Punto final: " + desafio.getString("puntoFinal")
        binding.puntoFinal.text = textoPuntoFinal

        val urlImagen = desafio.getString("imagen")
        if (urlImagen != null) {
            if (urlImagen.isNotEmpty()) {
                Glide.with(binding.imagenDesafio.context)
                    .load(urlImagen)
                    .into(binding.imagenDesafio)
                binding.imagenDesafio.background = null
            } else {
                binding.imagenDesafio.setImageResource(R.drawable.profile_user_svgrepo_com)
            }
        }

        binding.iniciarDesafio.setOnClickListener {
            val intentIniciarDesafio = Intent(this, ConfigurarCarreraActivity::class.java)
            intentIniciarDesafio.putExtra("bundle", desafio)
            val gson = Gson()
            val puntoListType = object : TypeToken<MutableList<Punto>>() {}.type
            val puntoInicial: Punto = gson.fromJson(desafio.getString("puntoInicial"), Punto::class.java)
            val puntoFinal: Punto = gson.fromJson(desafio.getString("puntoFinal"), Punto::class.java)
            var puntosIntermedios: MutableList<Punto> = mutableListOf()
            if(desafio.getString("puntosIntermedios") != null){
                puntosIntermedios = gson.fromJson(desafio.getString("puntosIntermedios"), puntoListType)
            }
            val desafioVar = Desafio(desafio.getString("uidCreador")!!, desafio.getString("nombre")!!, desafio.getString("imagen")!!,
                desafio.getString("descripcion")!!, puntoInicial,
                puntosIntermedios, puntoFinal)
            intentIniciarDesafio.putExtra("desafio", desafioVar)
            startActivity(intentIniciarDesafio)
        }

        binding.revisarTrayecto.setOnClickListener {
            val intentRevisarTrayecto = Intent(this, TrayectoDesafioActivity::class.java)
            val gson = Gson()
            val puntoListType = object : TypeToken<MutableList<Punto>>() {}.type
            val puntoInicial: Punto = gson.fromJson(desafio.getString("puntoInicial"), Punto::class.java)
            val puntoFinal: Punto = gson.fromJson(desafio.getString("puntoFinal"), Punto::class.java)
            var puntosIntermedios: MutableList<Punto> = mutableListOf()
            if(desafio.getString("puntosIntermedios") != null){
                puntosIntermedios = gson.fromJson(desafio.getString("puntosIntermedios"), puntoListType)
            }
            val desafioVar = Desafio(desafio.getString("uidCreador")!!, desafio.getString("nombre")!!, desafio.getString("imagen")!!,
                desafio.getString("descripcion")!!, puntoInicial,
                puntosIntermedios, puntoFinal)
            intentRevisarTrayecto.putExtra("desafio", desafioVar)
            startActivity(intentRevisarTrayecto)
        }

        eventoVolver()
    }

    private fun eventoVolver(){
        binding.backButtonChallenge.setOnClickListener {
            finish()
        }
    }
}