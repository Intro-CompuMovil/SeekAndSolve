package com.cuatrodivinas.seekandsolve.Logica

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.cuatrodivinas.seekandsolve.R
import com.cuatrodivinas.seekandsolve.databinding.ActivityVerPerfilBinding
import android.graphics.drawable.Drawable
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import org.json.JSONArray
import org.json.JSONObject
import java.io.FileInputStream

class VerPerfil : AppCompatActivity() {
    private lateinit var binding: ActivityVerPerfilBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVerPerfilBinding.inflate(layoutInflater)
        setContentView(binding.root)
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
        val user = getLastRegisteredUser()
        if (user != null) {
            binding.nombreUsuarioTxt.text = user.getString("username")
            val photoUrl = if (user.has("photoUrl") && !user.isNull("photoUrl")) user.getString("photoUrl") else null
            Glide.with(this)
                .load(photoUrl)
                .error(getDrawable(R.drawable.profile_user_svgrepo_com)?.mutate()?.apply {
                    setTint(getColor(R.color.primaryColor))
                })
                .apply(RequestOptions.circleCropTransform())
                .into(binding.imagenPerfil)
            binding.nombreETxt.setText(user.getString("name"))
            binding.corrreoETxt.setText(user.getString("email"))
            binding.FechaETxt.setText(user.getString("fechaNacimiento"))
        }
    }

    private fun getLastRegisteredUser(): JSONObject? {
        val userDataJson = readJsonFromFile("user_data.json")
        if (userDataJson != null) {
            val usersArray = JSONArray(userDataJson)
            if (usersArray.length() > 0) {
                return usersArray.getJSONObject(usersArray.length() - 1) // Obtener el último usuario registrado
            }
        }
        return null
    }

    private fun readJsonFromFile(fileName: String): String? {
        return try {
            val fileInputStream: FileInputStream = openFileInput(fileName)
            val bytes = fileInputStream.readBytes()
            fileInputStream.close()
            String(bytes)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
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
            startActivity(Intent(this, EditarPerfil::class.java))
        }
    }
}