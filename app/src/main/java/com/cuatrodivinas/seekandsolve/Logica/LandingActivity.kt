package com.cuatrodivinas.seekandsolve.Logica

import android.os.Bundle
import android.view.animation.AnimationUtils
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.cuatrodivinas.seekandsolve.R
import android.content.Intent
import android.widget.Button
import java.io.File

class LandingActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_landing)

        // Inicializar el ImageView
        val cofreLaunch: ImageView = findViewById(R.id.cofreLaunch)
        // Cargar la animación
        val jumpAnimation = AnimationUtils.loadAnimation(this, R.anim.jump)
        // Aplicar la animación al ImageView
        cofreLaunch.startAnimation(jumpAnimation)

        // Inicializar el botón
        val button: Button = findViewById(R.id.comienzaTuAventura)
        // Configurar el OnClickListener
        button.setOnClickListener {
            checkJsonAndNavigate()
        }
    }

    private fun checkJsonAndNavigate() {
        val file = File(filesDir, "user_data.json")
        val intent = if (file.exists()) {
            Intent(this, LoginActivity::class.java)
        } else {
            Intent(this, RegisterActivity::class.java)
        }
        startActivity(intent)
    }
}