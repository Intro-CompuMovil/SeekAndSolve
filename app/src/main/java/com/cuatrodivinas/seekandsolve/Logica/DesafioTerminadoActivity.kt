package com.cuatrodivinas.seekandsolve.Logica

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.cuatrodivinas.seekandsolve.Datos.Carrera
import com.cuatrodivinas.seekandsolve.Datos.CarreraActual
import com.cuatrodivinas.seekandsolve.Datos.CarreraUsuarioCompletada
import com.cuatrodivinas.seekandsolve.Datos.Data.Companion.PATH_CARRERAS
import com.cuatrodivinas.seekandsolve.Datos.Data.Companion.PATH_DESAFIOS
import com.cuatrodivinas.seekandsolve.Datos.Data.Companion.PATH_RECOMPENSAS
import com.cuatrodivinas.seekandsolve.Datos.Data.Companion.PATH_USERS
import com.cuatrodivinas.seekandsolve.Datos.Data.Companion.auth
import com.cuatrodivinas.seekandsolve.Datos.Data.Companion.database
import com.cuatrodivinas.seekandsolve.Datos.Data.Companion.storage
import com.cuatrodivinas.seekandsolve.Datos.Desafio
import com.cuatrodivinas.seekandsolve.Datos.InfoRecompensa
import com.cuatrodivinas.seekandsolve.Datos.Recompensa
import com.cuatrodivinas.seekandsolve.R
import com.cuatrodivinas.seekandsolve.databinding.ActivityDesafioTerminadoBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.StorageReference
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.random.Random

class DesafioTerminadoActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDesafioTerminadoBinding
    private lateinit var desafio: Desafio
    private lateinit var carreraActual: CarreraActual
    private lateinit var carreraCompletada: CarreraUsuarioCompletada
    private lateinit var infoRecompensaAsignada: InfoRecompensa

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDesafioTerminadoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        desafio = intent.getSerializableExtra("desafio") as Desafio
        carreraActual = intent.getSerializableExtra("carreraActual") as CarreraActual

        agregarRecompensaAleatoria()
        eliminarCarreraActualYGuardarCompletada()
    }

    private fun eliminarCarreraActualYGuardarCompletada() {
        val userUid = auth.currentUser!!.uid
        val carreraId = carreraActual.idCarrera

        // Eliminar carreraActual del usuario
        database.getReference("$PATH_USERS/$userUid/carreraActual").removeValue()

        // Obtener la hora de inicio de la carrera
        database.getReference("$PATH_CARRERAS/$carreraId/horaInicio").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val horaInicioStr = snapshot.getValue(String::class.java) ?: return
                val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")

                // Parsear el string a LocalDateTime
                val horaInicio = try {
                    LocalDateTime.parse(horaInicioStr, formatter)
                } catch (e: Exception) {
                    e.printStackTrace()
                    null
                }
                var tiempoTotal: Int
                if (horaInicio == null) {
                    tiempoTotal = 0
                } else {
                    tiempoTotal = ChronoUnit.SECONDS.between(horaInicio, LocalDateTime.now()).toInt()
                }

                // Crear CarreraUsuarioCompletada
                carreraCompletada = CarreraUsuarioCompletada(
                    tiempoTotal,
                    carreraActual.acertijosPrimerIntento,
                    carreraActual.distanciaRecorrida,
                    LocalDateTime.now().toString()
                )

                // Guardar CarreraUsuarioCompletada en carreras/idCarrera/usuariosCompletaron/userUid
                database.getReference("$PATH_CARRERAS/$carreraId/usuariosCompletaron/$userUid").setValue(carreraCompletada)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("Firebase", "Error al obtener la hora de inicio", error.toException())
            }
        })
    }

    private fun inicializarElementos() {
        binding.tituloDesafio.text = "Completaste ${desafio.nombre}"
        binding.descripcionRecompensa.text = infoRecompensaAsignada.nombreRecompensa

        val idDesafio = desafio.id
        val refImgDesafio = storage.getReference(PATH_DESAFIOS).child("$idDesafio.jpg")
        refImgDesafio.downloadUrl.addOnSuccessListener { uri ->
            Glide.with(this)
                .load(uri)
                .into(binding.imagenDesafio)
        }.addOnFailureListener {
            binding.imagenDesafio.setImageResource(R.drawable.foto_bandera)
        }

        val idRecompensa = infoRecompensaAsignada.idRecompensa
        val refImgRecompensa = storage.getReference(PATH_RECOMPENSAS).child("$idRecompensa.jpg")
        refImgRecompensa.downloadUrl.addOnSuccessListener { uri ->
            Glide.with(this)
                .load(uri)
                .into(binding.imagenRecompensa)
        }.addOnFailureListener {
            binding.imagenRecompensa.setImageResource(R.drawable.foto_bandera)
        }
    }

    override fun onResume() {
        super.onResume()
        binding.estadisticas.setOnClickListener {
            val intentEstadisticas = Intent(this, EstadisticasCarreraActivity::class.java)
            intentEstadisticas.putExtra("carreraUsuarioCompletada", carreraCompletada)
            // Mandar el int con checkpoints marcados = carreraActual.puntosCompletados.size
            intentEstadisticas.putExtra("checkpointsMarcados", carreraActual.puntosCompletados.size)
            intentEstadisticas.putExtra("infoRecompensa", infoRecompensaAsignada)
            startActivity(intentEstadisticas)
        }
    }

    private fun agregarRecompensaAleatoria() {
        val recompensasRef = database.reference.child(PATH_RECOMPENSAS)
        val usuarioRecompensasRef = database.reference.child(PATH_USERS)
            .child(auth.currentUser?.uid ?: return).child(PATH_RECOMPENSAS)

        recompensasRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val recompensasList = mutableListOf<Recompensa>()
                for (recompensaSnapshot in dataSnapshot.children) {
                    // Leer el value que es un string, crear el objeto y asignarle el id y agregarlo a la lista
                    val nombreRecompensa = recompensaSnapshot.value as String
                    val recompensa = Recompensa(recompensaSnapshot.key ?: "", nombreRecompensa)
                    recompensasList.add(recompensa)
                }

                if (recompensasList.isNotEmpty()) {
                    val recompensaAleatoria = recompensasList[Random.nextInt(recompensasList.size)]
                    infoRecompensaAsignada = InfoRecompensa(
                        recompensaAleatoria.id,
                        recompensaAleatoria.nombre,
                        desafio.nombre,
                        LocalDate.now().toString()
                    )
                    val nuevaRecompensaRef = usuarioRecompensasRef.child(carreraActual.idCarrera)
                    nuevaRecompensaRef.setValue(infoRecompensaAsignada)

                    inicializarElementos()
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e("Firebase", "Error al leer las recompensas", databaseError.toException())
            }
        })
    }
}