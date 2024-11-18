package com.cuatrodivinas.seekandsolve.Logica

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.cuatrodivinas.seekandsolve.R
import com.cuatrodivinas.seekandsolve.databinding.ActivityVerPerfilBinding
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.cuatrodivinas.seekandsolve.Datos.Data.Companion.PATH_ACTIVOS
import com.cuatrodivinas.seekandsolve.Datos.Data.Companion.PATH_IMAGENES
import com.cuatrodivinas.seekandsolve.Datos.Data.Companion.PATH_USERS
import com.cuatrodivinas.seekandsolve.Datos.Data.Companion.auth
import com.cuatrodivinas.seekandsolve.Datos.Data.Companion.database
import com.cuatrodivinas.seekandsolve.Datos.Data.Companion.storage
import com.cuatrodivinas.seekandsolve.Datos.Usuario
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.StorageReference
import java.io.File

class VerPerfil : AppCompatActivity() {
    private lateinit var binding: ActivityVerPerfilBinding
    private lateinit var refImg: StorageReference
    private lateinit var databaseRef: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVerPerfilBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Si hay un usuario autenticado, cargar sus datos
        val uid = auth.currentUser?.uid
        if (uid != null) {
            cargarDatosUsuario(uid)
        } else {
            showToast("No se encontró un usuario autenticado.")
            navigateToMain()
        }
        deshabilitarEditTexts()
        configurarEventos()
        //servicioActivos()
    }

    // Con el UID del usuario, obtener su información
    private fun cargarDatosUsuario(uid: String) {
        val userRef = database.getReference(PATH_USERS).child(uid)

        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // Convierte el snapshot a la clase User
                val usuario = snapshot.getValue(Usuario::class.java)
                val nombre = usuario!!.nombre
                val username = usuario.nombreUsuario
                val correo = usuario.correo
                val imagenUrl = usuario.imagenUrl
                val fechaNacimiento = usuario.fechaNacimiento

                actualizarUI(nombre, username, correo, imagenUrl, fechaNacimiento)
                Log.d("Usuario", "Datos actualizados del usuario: $username")
            }
            override fun onCancelled(error: DatabaseError) {
                Log.e("DatabaseError", "Error al suscribirse a los datos del usuario: ${error.message}")
            }
        })
    }

    private fun deshabilitarEditTexts(){
        binding.nombreETxt.isEnabled = false
        binding.corrreoETxt.isEnabled = false
        binding.FechaETxt.isEnabled = false
    }

    private fun actualizarUI(nombre: String, username: String, correo: String, fotoUrl: String, fechaNacimiento: String) {
        // Actualizar los campos de texto con la info del usuario
        binding.nombreUsuarioTxt.text = username
        binding.nombreETxt.setText(nombre)
        binding.corrreoETxt.setText(correo)
        binding.FechaETxt.setText(fechaNacimiento)
        // Manejar la foto de perfil
        Handler(Looper.getMainLooper()).postDelayed({
            refImg = storage.getReference(PATH_IMAGENES).child("${auth.currentUser!!.uid}.jpg")
            refImg.downloadUrl.addOnSuccessListener { uri ->
                Glide.with(this)
                    .load(uri) // Carga la imagen desde la URL
                    .into(binding.imagenPerfil)
            }.addOnFailureListener { exception ->
                binding.imagenPerfil.imageTintList = ContextCompat.getColorStateList(this, R.color.primaryColor)
            }
        }, 1500)
    }

    private fun configurarEventos() {
        binding.recompensasBtn.setOnClickListener {
            startActivity(Intent(this, RecompensasCarrera::class.java))
        }

        binding.estadisticasBtn.setOnClickListener {
            startActivity(Intent(this, EstadisticasCarreras::class.java))
        }

        binding.backButtonUser.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

        binding.logOutBtn.setOnClickListener {
            cerrarSesion()
        }

        binding.btnActivo.setOnClickListener {
            cambiarEstado()
        }

        binding.editarPerfilBtn.setOnClickListener {
            val intent = Intent(this, EditarPerfil::class.java)
            intent.putExtra("nombre", binding.nombreETxt.text.toString())
            intent.putExtra("username", binding.nombreUsuarioTxt.text.toString())
            intent.putExtra("correo", binding.corrreoETxt.text.toString())
            intent.putExtra("fechaNacimiento", binding.FechaETxt.text.toString())
            startActivity(intent)
        }
    }

    private fun cerrarSesion() {
        databaseRef = database.getReference(PATH_ACTIVOS).child(auth.currentUser!!.uid)
        databaseRef.removeValue()
        auth.signOut()
        val intent = Intent(this, LandingActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        startActivity(intent)
        finish()
    }

    private fun navigateToMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun cambiarEstado(){
        buscarDisponible(auth.currentUser!!.uid) { disponible ->
            if (disponible) {
                databaseRef = database.getReference(PATH_ACTIVOS).child(auth.currentUser!!.uid)
                databaseRef.removeValue()
                binding.btnActivo.text = "Establecerse activo"
                binding.btnActivo.backgroundTintList = ContextCompat.getColorStateList(this, R.color.primaryColor)
            } else {
                databaseRef = database.getReference(PATH_ACTIVOS).child(auth.currentUser!!.uid)
                databaseRef.setValue(auth.currentUser!!.uid)
                binding.btnActivo.text = "Establecerse inactivo"
                binding.btnActivo.backgroundTintList = ContextCompat.getColorStateList(this, R.color.secondaryColor)
            }
        }
    }

    private fun buscarDisponible(uid: String, onResult: (Boolean) -> Unit){
        val myRef = database.getReference(PATH_ACTIVOS)
        myRef.child(uid).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // Verifica si el UID existe
                if (snapshot.exists()) {
                    onResult(true) // UID encontrado
                } else {
                    onResult(false) // UID no encontrado
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Manejo de error si la operación fue cancelada
                Log.e("Firebase", "Error al verificar el UID", error.toException())
                onResult(false) // Retorna false en caso de error
            }
        })
    }

    private fun servicioActivos(){
        databaseRef = database.getReference("$PATH_ACTIVOS/")
        databaseRef.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(dataSnapshot: DataSnapshot, previousChildName: String?) {
                val userId = dataSnapshot.key
                userId?.let {
                    mostrarDisponibilidad(it)
                }
            }

            override fun onChildRemoved(dataSnapshot: DataSnapshot) {
                val userId = dataSnapshot.key
                userId?.let {
                    mostrarDisponibilidad(it, isUserLeaving = true)
                }
            }

            override fun onChildChanged(dataSnapshot: DataSnapshot, previousChildName: String?) {}

            override fun onChildMoved(dataSnapshot: DataSnapshot, previousChildName: String?) {}

            override fun onCancelled(databaseError: DatabaseError) {}
        })
    }

    private fun mostrarDisponibilidad(userId: String, isUserLeaving: Boolean = false) {
        val userDatabase = database.getReference("$PATH_USERS/$userId")

        userDatabase.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val nombreUsuario = dataSnapshot.child("nombreUsuario").getValue(String::class.java)

                if (nombreUsuario != null) {
                    //verificar si el usuario es amigo o no del usuario actual
                    var esAmigo = false
                    val amigosUsuario = database.getReference("$PATH_USERS/${auth.currentUser!!.uid}/amigos")
                    amigosUsuario.get().addOnSuccessListener { dataSnapshot ->
                        for (amigo in dataSnapshot.children) {
                            if (amigo.key == nombreUsuario) {
                                esAmigo = true
                                break
                            }
                        }
                        if (esAmigo || true) {
                            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                val channel = NotificationChannel(
                                    "wearable_channel", // ID del canal
                                    "Wearable Notifications", // Nombre del canal
                                    NotificationManager.IMPORTANCE_DEFAULT
                                )
                                notificationManager.createNotificationChannel(channel)
                            }
                            val notificationId = 1 // Un ID único para la notificación

                            val titulo = if (isUserLeaving) {
                                "$nombreUsuario ha deja de buscar Tesoros"
                            } else {
                                "$nombreUsuario ha comenzado a buscar Tesoros"
                            }

                            val contenido = if (isUserLeaving) {
                                "Aprovecha la oportunidad de ganarle en recompensas!"
                            }else{
                                "Juega con el y busca recompensas en las carreras"
                            }

                            val notification = NotificationCompat.Builder(this@VerPerfil, "wearable_channel")
                                .setSmallIcon(R.drawable.logo) // Icono de la notificación
                                .setContentTitle(titulo)
                                .setContentText(contenido)
                                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                                .setAutoCancel(true) // La notificación se eliminará al hacer clic
                                .build()
                            notificationManager.notify(notificationId, notification)
                        }
                    }.addOnFailureListener { exception -> }


                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
//                Toast.makeText(applicationContext, "Error: ${databaseError.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}