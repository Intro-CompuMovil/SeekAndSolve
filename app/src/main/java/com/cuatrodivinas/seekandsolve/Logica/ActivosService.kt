package com.cuatrodivinas.seekandsolve.Logica

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.cuatrodivinas.seekandsolve.Datos.Data
import com.cuatrodivinas.seekandsolve.Datos.Data.Companion
import com.cuatrodivinas.seekandsolve.Datos.Data.Companion.PATH_ACTIVOS
import com.cuatrodivinas.seekandsolve.Datos.Data.Companion.PATH_USERS
import com.cuatrodivinas.seekandsolve.Datos.Data.Companion.database
import com.cuatrodivinas.seekandsolve.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ActivosService : Service() {
    private lateinit var databaseRef: DatabaseReference
    private lateinit var auth: FirebaseAuth

    override fun onCreate() {
        super.onCreate()
        auth = FirebaseAuth.getInstance()
        databaseRef = FirebaseDatabase.getInstance().getReference("$PATH_ACTIVOS/")

        // Configurar la notificación persistente del servicio
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "foreground_service_channel"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Servicio de Activos",
                NotificationManager.IMPORTANCE_LOW
            )
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("Servicio de búsqueda de Tesoros")
            .setContentText("Buscando jugadores activos...")
            .setSmallIcon(R.drawable.logo)
            .build()

        startForeground(1, notification)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Iniciar la lógica para escuchar cambios en Firebase
        servicioActivos()
        return START_STICKY
    }

    private fun servicioActivos() {
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
                    val amigosUsuario = database.getReference("$PATH_USERS/${Data.auth.currentUser!!.uid}/amigos")
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

                            val notification = NotificationCompat.Builder(this@ActivosService, "wearable_channel")
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

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}
