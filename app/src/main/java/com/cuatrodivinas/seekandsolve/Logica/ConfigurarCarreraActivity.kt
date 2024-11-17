package com.cuatrodivinas.seekandsolve.Logica

import android.content.Intent
import android.database.Cursor
import android.database.MatrixCursor
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.cuatrodivinas.seekandsolve.Datos.Carrera
import com.cuatrodivinas.seekandsolve.Datos.Desafio
import com.cuatrodivinas.seekandsolve.R
import com.cuatrodivinas.seekandsolve.databinding.ActivityConfigurarCarreraBinding
import org.json.JSONArray
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class ConfigurarCarreraActivity : AppCompatActivity() {
    private lateinit var binding: ActivityConfigurarCarreraBinding
    private lateinit var desafio: Desafio
    private var amigosInvitadosJsonArray: JSONArray = JSONArray()
    private var amigosNoInvitadosJsonArray: JSONArray = JSONArray()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityConfigurarCarreraBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Si hay algo en el intent, se actualiza la lista de amigos invitados y no invitados
        if (intent.hasExtra("amigosInvitadosJsonArray")) {
            amigosInvitadosJsonArray = JSONArray(intent.getStringExtra("amigosInvitadosJsonArray"))
            amigosNoInvitadosJsonArray = JSONArray(intent.getStringExtra("amigosNoInvitadosJsonArray"))
        }

        inicializarElementos()
    }

    private fun inicializarElementos() {
        desafio = intent.getSerializableExtra("desafio") as Desafio
        binding.tituloDesafio.text = desafio.nombre

        val urlImagen = desafio.imagenUrl
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

        setListAmigosInvitados()
    }

    override fun onResume() {
        super.onResume()
        binding.invitarAmigos.setOnClickListener {
            val intentInvitarAmigos = Intent(this, InvitarAmigosDesafioActivity::class.java)
            // Envía la lista de amigos invitados y no invitados
            intentInvitarAmigos.putExtra("amigosInvitadosJsonArray", amigosInvitadosJsonArray.toString())
            intentInvitarAmigos.putExtra("amigosNoInvitadosJsonArray", amigosNoInvitadosJsonArray.toString())
            // Enviar también el desafío
            intentInvitarAmigos.putExtra("desafio", desafio)
            startActivity(intentInvitarAmigos)
        }

        binding.jugarDesafio.setOnClickListener {
            var intentJugar = Intent(this, IniciarRutaActivity::class.java).putExtra("desafio", desafio)
            val fechaHoy = LocalDate.now()  // Obtiene la fecha actual
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
            var carrera = Carrera("", mutableMapOf(), 0, 0, 0, 0, fechaHoy.format(formatter), mutableListOf())
            intentJugar.putExtra("desafio", desafio)
            intentJugar.putExtra("carrera", carrera)
            intentJugar.putExtra("fechaInicio",LocalDateTime.now())
            startActivity(intentJugar)
        }

        binding.backButtonConfigChallenge.setOnClickListener {
            finish()
        }
    }

    private fun setListAmigosInvitados() {
        val columns = arrayOf("_id", "imagen", "nombre")
        val matrixCursor = MatrixCursor(columns)
        for (i in 0 until amigosInvitadosJsonArray.length()) {
            val jsonObject = amigosInvitadosJsonArray.getJSONObject(i)
            val id = jsonObject.getInt("id")
            val imagen = jsonObject.getString("fotoUrl")
            val nombre = jsonObject.getString("username")
            matrixCursor.addRow(arrayOf(id, imagen, nombre))
        }
        val cursor: Cursor = matrixCursor
        val amigosAdapter = AmigosAdapter(this, cursor, 0)
        binding.listaAmigos.adapter = amigosAdapter
    }
}