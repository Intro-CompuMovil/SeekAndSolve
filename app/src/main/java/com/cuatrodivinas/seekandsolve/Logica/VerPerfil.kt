package com.cuatrodivinas.seekandsolve.Logica

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.cuatrodivinas.seekandsolve.R
import com.cuatrodivinas.seekandsolve.databinding.ActivityVerPerfilBinding
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.cuatrodivinas.seekandsolve.Datos.Data.Companion.PATH_USERS
import com.cuatrodivinas.seekandsolve.Datos.Data.Companion.auth
import com.cuatrodivinas.seekandsolve.Datos.Data.Companion.database
import com.cuatrodivinas.seekandsolve.Datos.Usuario
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import java.io.File

class VerPerfil : AppCompatActivity() {
    private lateinit var binding: ActivityVerPerfilBinding

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
        if (fotoUrl.isNotEmpty() && fotoUrl.startsWith("/")) {
            val file = File(fotoUrl)
            if (file.exists()) {
                Glide.with(this)
                    .load(file)
                    .circleCrop()
                    .into(binding.imagenPerfil)
                return
            }
        }
        // Si fotoUrl está vacío, el archivo no existe o no es válido
        // Cargar imagen por defecto
        binding.imagenPerfil.imageTintList = ContextCompat.getColorStateList(this, R.color.primaryColor)
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

        binding.editarPerfilBtn.setOnClickListener {
            startActivity(Intent(this, EditarPerfil::class.java))
        }
    }

    private fun cerrarSesion() {
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
}