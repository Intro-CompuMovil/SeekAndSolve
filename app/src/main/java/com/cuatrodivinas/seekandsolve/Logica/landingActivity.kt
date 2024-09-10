package com.cuatrodivinas.seekandsolve.Logica

import android.os.Bundle
import android.view.animation.AnimationUtils
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.cuatrodivinas.seekandsolve.R
import android.content.Intent
import android.widget.Button

class landingActivity : AppCompatActivity() {
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
            val intent = Intent(this, registerActivity::class.java)
            startActivity(intent)
        }
    }
}