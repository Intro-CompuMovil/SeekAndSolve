package com.cuatrodivinas.seekandsolve.Logica

import android.os.Bundle
import android.view.animation.AnimationUtils
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.cuatrodivinas.seekandsolve.R
import android.content.Intent
import android.widget.Button
import com.cuatrodivinas.seekandsolve.Datos.Data.Companion.auth
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import java.io.File

class LandingActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_landing)
        setupAnimation()
    }

    override fun onResume() {
        super.onResume()
        setupButton()
    }

    private fun setupAnimation() {
        val cofreLaunch: ImageView = findViewById(R.id.cofreLaunch)
        val jumpAnimation = AnimationUtils.loadAnimation(this, R.anim.jump)
        cofreLaunch.startAnimation(jumpAnimation)
    }

    private fun setupButton() {
        val button: Button = findViewById(R.id.comienzaTuAventura)
        button.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
            finish()
        }
    }
}