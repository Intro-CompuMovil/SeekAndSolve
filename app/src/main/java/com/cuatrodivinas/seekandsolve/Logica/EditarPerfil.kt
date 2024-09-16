package com.cuatrodivinas.seekandsolve.Logica

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.text.InputType
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.cuatrodivinas.seekandsolve.Datos.Data.Companion.PERMISO_CAMARA
import com.cuatrodivinas.seekandsolve.R
import com.cuatrodivinas.seekandsolve.databinding.ActivityEditarPerfilBinding
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.io.InputStream

class EditarPerfil : AppCompatActivity() {
    private lateinit var binding: ActivityEditarPerfilBinding
    private var contraseniaVisible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditarPerfilBinding.inflate(layoutInflater)
        setContentView(binding.root)
        quemarDatos()
        eventoCambiarFoto()
        eventoVolver()
        eventoAplicarCambios()
    }

    private fun quemarDatos() {
        val user = getLastRegisteredUser()
        if (user != null) {
            val username = user.getString("username")
            val name = user.getString("name")
            val email = user.getString("email")
            val birthDate = user.getString("fechaNacimiento")
            val sessionType = user.getString("signInType")
            val photoUrl = if (user.has("photoUrl") && !user.isNull("photoUrl")) user.getString("photoUrl") else null
            Glide.with(this)
                .load(photoUrl)
                .error(getDrawable(R.drawable.profile_user_svgrepo_com)?.mutate()?.apply {
                    setTint(getColor(R.color.primaryColor))
                })
                .apply(RequestOptions.circleCropTransform())
                .into(binding.imagenPerfil)
            binding.nombreUsuarioETxt.setText(username)
            binding.nombreETxt.setText(name)
            binding.corrreoETxt.setText(email)
            binding.FechaETxt.setText(birthDate)
            if (sessionType == "Google") {
                binding.nombreUsuarioETxtLayout.visibility = View.GONE
                binding.contraETxtLayout.visibility = View.GONE
            } else {
                binding.nombreUsuarioETxt.isEnabled = true
                binding.contraETxt.isEnabled = true
            }
        }
    }

    private fun getLastRegisteredUser(): JSONObject? {
        try {
            val inputStream: InputStream = openFileInput("user_data.json")
            val size: Int = inputStream.available()
            val buffer = ByteArray(size)
            inputStream.read(buffer)
            inputStream.close()
            val json = String(buffer, Charsets.UTF_8)
            val jsonArray = JSONArray(json)
            if (jsonArray.length() > 0) {
                // Devuelve el último usuario registrado
                return jsonArray.getJSONObject(jsonArray.length() - 1)
            }
        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        return null
    }

    private fun eventoCambiarFoto(){
        binding.cambiarFotoBtn.setOnClickListener {
            pedirPermiso(this, android.Manifest.permission.CAMERA,"Necesitamos el permiso de cámara para cambiar tu foto de perfil", PERMISO_CAMARA)
        }
    }

    private fun pedirPermiso(context: Context, permiso: String, justificacion: String,
                             idCode: Int){
        if(ContextCompat.checkSelfPermission(context, permiso) !=
            PackageManager.PERMISSION_GRANTED){
            if (shouldShowRequestPermissionRationale(permiso)) {
                // Explicar al usuario por qué necesitamos el permiso
                Toast.makeText(context, justificacion, Toast.LENGTH_SHORT).show()
            }
            requestPermissions(arrayOf(permiso), idCode)
        }
        else{
            Toast.makeText(context, "Permiso otorgado", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            PERMISO_CAMARA -> {
                // If request is cancelled, the result arrays are empty.
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    Toast.makeText(this, "Permiso otorgado", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "FUNCIONALIDADES REDUCIDAS", Toast.LENGTH_LONG).show()
                }
                return
            }
            else -> {
                // Ignore all other requests.
            }
        }
    }


    private fun eventoVolver() {
        binding.backButtonEditarPerfil.setOnClickListener {
            finish()
        }
    }

    fun eventoAplicarCambios() {
        binding.aplicarCambiosBtn.setOnClickListener {
            finish()
        }
    }
}