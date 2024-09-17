package com.cuatrodivinas.seekandsolve.Logica

import android.content.Intent
import android.database.Cursor
import android.database.MatrixCursor
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.cuatrodivinas.seekandsolve.R
import com.cuatrodivinas.seekandsolve.databinding.ActivityAmigosBinding

class Amigos : AppCompatActivity() {
    private lateinit var binding: ActivityAmigosBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAmigosBinding.inflate(layoutInflater)
        setContentView(binding.root)
        quemarDatos()
        eventoAgregarAmigos()
        eventoVolver()
    }

    private fun quemarDatos() {
        val columns = arrayOf("_id", "imagen", "nombre")
        val matrixCursor = MatrixCursor(columns)
        matrixCursor.addRow(arrayOf(1, R.drawable.foto_oscar, "El_Chotsi777"))
        matrixCursor.addRow(arrayOf(2, R.drawable.foto_oscar, "alexoberco"))
        matrixCursor.addRow(arrayOf(3, R.drawable.foto_oscar, "AlbertoElOmnisciente"))
        matrixCursor.addRow(arrayOf(4, R.drawable.foto_oscar, "DFallenKnight"))
        val cursor: Cursor = matrixCursor
        val amigosAdapter = AmigosAdapter(this, cursor, 0)
        binding.amigosLv.adapter = amigosAdapter
    }

    private fun eventoAgregarAmigos(){
        binding.agregarAmigosBtn.setOnClickListener {
            startActivity(Intent(this, AgregarAmigos::class.java))
        }
    }

    private fun eventoVolver(){
        binding.backButtonFriends.setOnClickListener {
            finish()
        }
    }
}