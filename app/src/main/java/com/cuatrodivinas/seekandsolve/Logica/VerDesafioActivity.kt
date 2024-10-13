package com.cuatrodivinas.seekandsolve.Logica

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.cuatrodivinas.seekandsolve.R
import com.cuatrodivinas.seekandsolve.databinding.ActivityVerDesafioBinding

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
            startActivity(intentIniciarDesafio)
        }

        binding.revisarTrayecto.setOnClickListener {
            val intentRevisarTrayecto = Intent(this, TrayectoDesafioActivity::class.java)
            intentRevisarTrayecto.putExtra("bundle", desafio)
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