package com.cuatrodivinas.seekandsolve.Logica

import android.content.Intent
import android.database.Cursor
import android.database.MatrixCursor
import android.os.Bundle
import android.view.animation.AnimationUtils
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.cuatrodivinas.seekandsolve.R
import com.cuatrodivinas.seekandsolve.databinding.ActivityRecompensasCarreraBinding
import com.cuatrodivinas.seekandsolve.databinding.ActivityVerPerfilBinding

class RecompensasCarrera : AppCompatActivity() {
    private lateinit var binding: ActivityRecompensasCarreraBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRecompensasCarreraBinding.inflate(layoutInflater)
        setContentView(binding.root)
        quemarDatos()
        eventoVolver()
        findViewById<ImageView>(R.id.medalImageView).startAnimation(AnimationUtils.loadAnimation(this, R.anim.blink))
    }

    private fun quemarDatos() {
        val columns = arrayOf("_id", "nombre", "lugar", "fecha")
        val matrixCursor = MatrixCursor(columns)
        matrixCursor.addRow(arrayOf(1, "Capitán Cuack Cuack", "Bogotá", "20/02/2021"))
        matrixCursor.addRow(arrayOf(2, "Denario del Rey", "Mosquera", "27/02/2022"))
        matrixCursor.addRow(arrayOf(3, "Roca Lunar(Especial 2024)", "B/bermeja", "16/04/2023"))
        matrixCursor.addRow(arrayOf(4, "Mascara Sospechosa", "San vicente", "10/05/2024"))
        val cursor: Cursor = matrixCursor
        val recompensasAdapter = RecompensasAdapter(this, cursor, 0)
        binding.recompensasLv.adapter = recompensasAdapter
    }

    private fun eventoVolver(){
        binding.backButtonRewards.setOnClickListener {
            finish()
        }
    }
}