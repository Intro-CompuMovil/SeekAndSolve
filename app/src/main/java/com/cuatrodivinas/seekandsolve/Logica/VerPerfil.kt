package com.cuatrodivinas.seekandsolve.Logica

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.cuatrodivinas.seekandsolve.R
import com.cuatrodivinas.seekandsolve.databinding.ActivityVerPerfilBinding
import android.graphics.drawable.Drawable
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import org.json.JSONArray
import org.json.JSONObject
import java.io.FileInputStream
import kotlin.properties.Delegates

class VerPerfil : AppCompatActivity() {
    private lateinit var binding: ActivityVerPerfilBinding

    private var id by Delegates.notNull<Int>()
    private lateinit var nombre: String
    private lateinit var username: String
    private lateinit var correo: String
    private lateinit var contrasena: String
    private lateinit var fotoUrl: String
    private lateinit var fechaNacimiento: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVerPerfilBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val bundle = intent.extras
        if (bundle != null) {
            id = bundle.getInt("id")
            nombre = bundle.getString("nombre") ?: ""
            username = bundle.getString("username") ?: ""
            correo = bundle.getString("correo") ?: ""
            contrasena = bundle.getString("contrasena") ?: ""
            fotoUrl = bundle.getString("fotoUrl") ?: ""
            fechaNacimiento = bundle.getString("fechaNacimiento") ?: ""
        }
        inicializarComponentes()
        quemarDatos()
        eventoRecompensas()
        eventoEstadisticas()
        eventoVolver()
        eventoEditarPerfil()
        eventoLogout()
    }

    private fun inicializarComponentes(){
        binding.nombreETxt.isEnabled = false
        binding.corrreoETxt.isEnabled = false
        binding.FechaETxt.isEnabled = false
    }

    private fun quemarDatos(){
        binding.nombreUsuarioTxt.text = username
        if (fotoUrl != "") {
            Glide.with(this)
                .load(fotoUrl)
                .override(24, 24) // Establecer el tamaño de la imagen en 24x24 px
                .circleCrop() // Para hacer la imagen circular
                .into(binding.imagenPerfil) // Establecer la imagen en el ImageView
        } else {
            binding.imagenPerfil.imageTintList = ContextCompat.getColorStateList(this, R.color.primaryColor)
        }
        binding.nombreETxt.setText(nombre)
        binding.corrreoETxt.setText(correo)
        binding.FechaETxt.setText(fechaNacimiento)
    }

    private fun eventoRecompensas() {
        binding.recompensasBtn.setOnClickListener {
            startActivity(Intent(this, RecompensasCarrera::class.java))
        }
    }

    private fun eventoEstadisticas() {
        binding.estadisticasBtn.setOnClickListener {
            startActivity(Intent(this, EstadisticasCarreras::class.java))
        }
    }

    private fun eventoVolver() {
        binding.backButtonUser.setOnClickListener {
            finish()
        }
    }

    private fun eventoLogout() {
        binding.logOutBtn.setOnClickListener {
            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build()
            val googleSignInClient = GoogleSignIn.getClient(this, gso)
            val sharedPreferences = getSharedPreferences("user_session", Context.MODE_PRIVATE)
            with(sharedPreferences.edit()) {
                remove("session_id")
                apply()
            }
            googleSignInClient.signOut().addOnCompleteListener(this) {
                googleSignInClient.revokeAccess().addOnCompleteListener(this) {
                    val intent = Intent(this, LoginActivity::class.java)
                    startActivity(intent)
                    finish()
                }
            }
        }
    }

    private fun eventoEditarPerfil() {
        binding.editarPerfilBtn.setOnClickListener {
            val intent = Intent(this, EditarPerfil::class.java)
            val bundle = Bundle()
            bundle.putInt("id", id)
            bundle.putString("nombre", nombre)
            bundle.putString("username", username)
            bundle.putString("correo", correo)
            bundle.putString("contrasena", contrasena)
            bundle.putString("fotoUrl", fotoUrl)
            bundle.putString("fechaNacimiento", fechaNacimiento)
            intent.putExtras(bundle)
            startActivity(intent)
        }
    }
}