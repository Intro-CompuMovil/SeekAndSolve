package com.cuatrodivinas.seekandsolve.Logica

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.cuatrodivinas.seekandsolve.R

class PistaActivity : AppCompatActivity() {
    lateinit var subTituloPista: TextView
    lateinit var textoPista: TextView
    lateinit var btnVolver: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pista)
        subTituloPista = findViewById(R.id.acertijo)
        textoPista = findViewById(R.id.pista)
        btnVolver = findViewById(R.id.volver)

    }

    override fun onResume() {
        val intent = Intent(this, ResolverAcertijoActivity::class.java)
        super.onResume()
        btnVolver.setOnClickListener {
            startActivity(intent)
        }
    }
}