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
import com.cuatrodivinas.seekandsolve.databinding.ActivityAgregarAmigosBinding
import com.cuatrodivinas.seekandsolve.databinding.ActivityAmigosBinding

class AgregarAmigos : AppCompatActivity() {
    private lateinit var binding: ActivityAgregarAmigosBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAgregarAmigosBinding.inflate(layoutInflater)
        setContentView(binding.root)
        quemarDatos()
        eventoVolver()
    }

    private fun quemarDatos() {
        val columns = arrayOf("_id", "imagen", "nombre")
        val matrixCursor = MatrixCursor(columns)
        matrixCursor.addRow(arrayOf(1, R.drawable.foto_oscar, "alexoberco"))
        matrixCursor.addRow(arrayOf(2, R.drawable.foto_oscar, "alexis"))
        matrixCursor.addRow(arrayOf(3, R.drawable.foto_oscar, "almaMater52"))
        matrixCursor.addRow(arrayOf(4, R.drawable.foto_oscar, "AlbertoElOmnisciente"))
        val cursor: Cursor = matrixCursor
        val agregarAmigosAdapter = AgregarAmigosAdapter(this, cursor, 0)
        binding.amigosLv.adapter = agregarAmigosAdapter
    }

    private fun eventoVolver(){
        binding.volverBtn.setOnClickListener {
            startActivity(Intent(this, Amigos::class.java))
        }

        binding.backButtonAddFriends.setOnClickListener {
            finish()
        }
    }
}