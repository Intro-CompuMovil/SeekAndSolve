package com.cuatrodivinas.seekandsolve.Logica

import android.content.Intent
import android.database.Cursor
import android.database.MatrixCursor
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.cuatrodivinas.seekandsolve.Datos.Carrera
import com.cuatrodivinas.seekandsolve.Datos.Data.Companion.PATH_DESAFIOS
import com.cuatrodivinas.seekandsolve.Datos.Data.Companion.auth
import com.cuatrodivinas.seekandsolve.Datos.Data.Companion.storage
import com.cuatrodivinas.seekandsolve.Datos.Desafio
import com.cuatrodivinas.seekandsolve.R
import com.cuatrodivinas.seekandsolve.databinding.ActivityConfigurarCarreraBinding
import com.google.firebase.storage.StorageReference
import org.json.JSONArray
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class ConfigurarCarreraActivity : AppCompatActivity() {
    private lateinit var binding: ActivityConfigurarCarreraBinding
    private lateinit var desafio: Desafio
    private var amigosInvitadosJsonArray: JSONArray = JSONArray()
    private var amigosNoInvitadosJsonArray: JSONArray = JSONArray()
    private lateinit var refImg: StorageReference

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

        refImg = storage.getReference(PATH_DESAFIOS).child("${desafio.id}.jpg")
        refImg.downloadUrl.addOnSuccessListener { uri ->
            Glide.with(this)
                .load(uri) // Carga la imagen desde la URL
                .into(binding.imagenDesafio)
        }.addOnFailureListener { exception ->
            binding.imagenDesafio.setImageResource(R.drawable.foto_bandera)
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
            // Poner un formato que incluya la hora con segundos
            val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")
            val carrera = Carrera("FALTAID", desafio.id, fechaHoy.format(formatter), mutableMapOf(), mutableListOf(auth.currentUser!!.uid))
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
        val columns = arrayOf("_id", "nombre")
        val matrixCursor = MatrixCursor(columns)
        for (i in 0 until amigosInvitadosJsonArray.length()) {
            val jsonObject = amigosInvitadosJsonArray.getJSONObject(i)
            val id = jsonObject.getInt("id")
            val nombre = jsonObject.getString("username")
            matrixCursor.addRow(arrayOf(id, nombre))
        }
        val cursor: Cursor = matrixCursor
        val amigosAdapter = AmigosAdapter(this, cursor, 0)
        binding.listaAmigos.adapter = amigosAdapter
    }
}