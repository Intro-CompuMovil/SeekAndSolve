package com.cuatrodivinas.seekandsolve.Logica

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.cuatrodivinas.seekandsolve.databinding.ActivityInvitarAmigosDesafioBinding

class InvitarAmigosDesafioActivity : AppCompatActivity() {
    private lateinit var binding: ActivityInvitarAmigosDesafioBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityInvitarAmigosDesafioBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    override fun onResume() {
        super.onResume()
        binding.backButtonInvite.setOnClickListener {
            finish()
        }

        binding.invitarAmigos.setOnClickListener {
            val intentConfigurar = Intent(this, ConfigurarCarreraActivity::class.java)
            intentConfigurar.putExtra("bundle", intent.getBundleExtra("bundle"))
            startActivity(intentConfigurar)
            finish()
        }
    }
}