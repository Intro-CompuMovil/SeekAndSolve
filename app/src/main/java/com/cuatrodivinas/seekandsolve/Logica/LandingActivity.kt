package com.cuatrodivinas.seekandsolve.Logica

import android.os.Bundle
import android.view.animation.AnimationUtils
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.cuatrodivinas.seekandsolve.R
import android.content.Intent
import android.widget.Button
import com.cuatrodivinas.seekandsolve.Datos.Data.Companion.auth
import com.cuatrodivinas.seekandsolve.databinding.ActivityLandingBinding
import com.cuatrodivinas.seekandsolve.databinding.ActivityMainBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import java.io.File

class LandingActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLandingBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLandingBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupAnimation()
    }

    override fun onResume() {
        super.onResume()
        binding.comienzaTuAventura.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
            finish()
        }
    }

    private fun setupAnimation() {
        val jumpAnimation = AnimationUtils.loadAnimation(this, R.anim.jump)
        binding.cofreLaunch.startAnimation(jumpAnimation)
    }
}