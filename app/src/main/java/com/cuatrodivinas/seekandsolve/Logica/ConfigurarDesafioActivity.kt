package com.cuatrodivinas.seekandsolve.Logica

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ListView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.cuatrodivinas.seekandsolve.R
import com.cuatrodivinas.seekandsolve.databinding.ActivityConfigurarDesafioBinding
import com.cuatrodivinas.seekandsolve.databinding.ActivityVerDesafiosBinding

class ConfigurarDesafioActivity : AppCompatActivity() {
    private lateinit var binding: ActivityConfigurarDesafioBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityConfigurarDesafioBinding.inflate(layoutInflater)
        setContentView(binding.root)

        inicializarElementos()
    }

    private fun inicializarElementos() {
        val desafio = intent.getBundleExtra("bundle")!!
        binding.tituloDesafio.text = desafio.getString("nombre")

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
            startActivity(Intent(this, IniciarRutaActivity::class.java))
        }
    }
}