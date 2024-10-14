package com.cuatrodivinas.seekandsolve.Logica

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.cuatrodivinas.seekandsolve.Datos.Desafio
import com.cuatrodivinas.seekandsolve.R
import com.cuatrodivinas.seekandsolve.databinding.ActivityConfigurarCarreraBinding

class ConfigurarCarreraActivity : AppCompatActivity() {
    private lateinit var binding: ActivityConfigurarCarreraBinding
    private lateinit var desafio: Desafio

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityConfigurarCarreraBinding.inflate(layoutInflater)
        setContentView(binding.root)

        inicializarElementos()
    }

    private fun inicializarElementos() {
        desafio = intent.getSerializableExtra("desafio") as Desafio
        binding.tituloDesafio.text = desafio.nombre

        val urlImagen = desafio.fotoUrl
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
        // Todo: Luego iría el else para añadirle el amigo a la lista de amigos invitados
    }

    override fun onResume() {
        super.onResume()
        binding.invitarAmigos.setOnClickListener {
            val intentInvitarAmigos = Intent(this, InvitarAmigosDesafioActivity::class.java)
            intentInvitarAmigos.putExtra("bundle", intent.getBundleExtra("bundle"))
            startActivity(intentInvitarAmigos)
        }

        binding.jugarDesafio.setOnClickListener {
            startActivity(Intent(this, IniciarRutaActivity::class.java).putExtra("desafio", desafio))
        }
    }
}