package com.cuatrodivinas.seekandsolve.Logica

import android.content.Intent
import android.database.Cursor
import android.database.MatrixCursor
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.cuatrodivinas.seekandsolve.Datos.Carrera
import com.cuatrodivinas.seekandsolve.Datos.CarreraActual
import com.cuatrodivinas.seekandsolve.Datos.Data.Companion.PATH_DESAFIOS
import com.cuatrodivinas.seekandsolve.Datos.Data.Companion.PATH_USERS
import com.cuatrodivinas.seekandsolve.Datos.Data.Companion.auth
import com.cuatrodivinas.seekandsolve.Datos.Data.Companion.database
import com.cuatrodivinas.seekandsolve.Datos.Data.Companion.storage
import com.cuatrodivinas.seekandsolve.Datos.Desafio
import com.cuatrodivinas.seekandsolve.Datos.Usuario
import com.cuatrodivinas.seekandsolve.R
import com.cuatrodivinas.seekandsolve.databinding.ActivityConfigurarCarreraBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.StorageReference
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class ConfigurarCarreraActivity : AppCompatActivity() {
    private lateinit var binding: ActivityConfigurarCarreraBinding
    private lateinit var desafio: Desafio
    // Mapa de amigos invitados, con el ID del amigo y el nombre
    private var amigosInvitados: MutableMap<String, String> = mutableMapOf()
    private var amigosNoInvitados: MutableMap<String, String> = mutableMapOf()
    private lateinit var refImg: StorageReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityConfigurarCarreraBinding.inflate(layoutInflater)
        setContentView(binding.root)

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

        fetchAmigos()
    }

    private fun fetchAmigos() {
        val userRef = database.getReference("$PATH_USERS/${auth.currentUser!!.uid}/amigos")
        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val amigosUids = snapshot.children.mapNotNull { it.key }
                fetchAmigosInfo(amigosUids)
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
            }
        })
    }

    private fun fetchAmigosInfo(amigosUids: List<String>) {
        amigosUids.forEach { uid ->
            val amigoRef = database.getReference("$PATH_USERS/$uid")
            amigoRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val amigo = snapshot.getValue(Usuario::class.java)
                    amigo?.let {
                        amigosNoInvitados.put(uid, amigo.nombreUsuario)
                        setListAmigosInvitados()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    // Handle error
                }
            })
        }
    }

    override fun onResume() {
        super.onResume()
        binding.invitarAmigos.setOnClickListener {
            val intentInvitarAmigos = Intent(this, InvitarAmigosDesafioActivity::class.java)
            // Envía la lista de amigos invitados y no invitados que son MutableMap
            intentInvitarAmigos.putExtra("amigosInvitados", HashMap(amigosInvitados))
            intentInvitarAmigos.putExtra("amigosNoInvitados", HashMap(amigosNoInvitados))
            // Hacer el intent hacia intentInvitarAmigos, mandando la lista de amigos invitados y no invitados
            // Recibiremos lo que se haya seleccionado de la lista de amigos
            startActivityForResult(intentInvitarAmigos, REQUEST_CODE_INVITAR_AMIGOS)
        }

        binding.jugarDesafio.setOnClickListener {
            val intentJugar = Intent(this, IniciarRutaActivity::class.java).putExtra("desafio", desafio)
            val horaActual = LocalDateTime.now()  // Obtiene la fecha actual
            // Poner un formato que incluya la hora con segundos
            val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")
            val carreraId = database.getReference("carreras").push().key ?: return@setOnClickListener
            amigosInvitados[auth.currentUser!!.uid] = "Tú"
            val carrera = Carrera(carreraId, desafio.id, horaActual.format(formatter), mutableMapOf(), amigosInvitados)

            // Guardar la carrera en la base de datos
            database.getReference("carreras").child(carreraId).setValue(carrera)

            // Inicializar CarreraActual para el usuario actual
            val carreraActual = CarreraActual(carreraId, 0, 0.0, mutableListOf())
            database.getReference("usuarios/${auth.currentUser!!.uid}/carreraActual").setValue(carreraActual)

            // Inicializar CarreraActual para los amigos invitados
            amigosInvitados.forEach { (uid, _) ->
                database.getReference("usuarios/$uid/carreraActual").setValue(carreraActual)
            }

            intentJugar.putExtra("desafio", desafio)
            intentJugar.putExtra("carreraActual", carreraActual)
            startActivity(intentJugar)
        }

        binding.backButtonConfigChallenge.setOnClickListener {
            finish()
        }
    }

    private fun setListAmigosInvitados() {
        val columns = arrayOf("_id", "idUser", "nombre")
        val matrixCursor = MatrixCursor(columns)
        var idCounter = 1L
        amigosInvitados.forEach { amigo ->
            matrixCursor.addRow(arrayOf(idCounter, amigo.key, amigo.value))
            idCounter++
        }
        val cursor: Cursor = matrixCursor
        val amigosAdapter = AmigosAdapter(this, cursor, 0)
        binding.listaAmigos.adapter = amigosAdapter
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            when (requestCode) {
                REQUEST_CODE_INVITAR_AMIGOS -> {
                    val updatedAmigosInvitados = data?.getSerializableExtra("amigosInvitados") as? HashMap<String, String>
                    val updatedAmigosNoInvitados = data?.getSerializableExtra("amigosNoInvitados") as? HashMap<String, String>
                    updatedAmigosInvitados?.let {
                        amigosInvitados.clear()
                        amigosInvitados.putAll(it)
                    }
                    updatedAmigosNoInvitados?.let {
                        amigosNoInvitados.clear()
                        amigosNoInvitados.putAll(it)
                    }
                    setListAmigosInvitados()
                }
            }
        }
    }

    companion object {
        private const val REQUEST_CODE_INVITAR_AMIGOS = 1005
    }
}