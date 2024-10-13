package com.cuatrodivinas.seekandsolve.Logica

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.text.InputType
import android.util.Log
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
import kotlin.properties.Delegates

class EditarPerfil : AppCompatActivity() {
    private lateinit var binding: ActivityEditarPerfilBinding
    private var contraseniaVisible = false

    private var id by Delegates.notNull<Int>()
    private lateinit var nombre: String
    private lateinit var username: String
    private lateinit var correo: String
    private lateinit var contrasena: String
    private lateinit var fotoUrl: String
    private lateinit var fechaNacimiento: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditarPerfilBinding.inflate(layoutInflater)
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
        quemarDatos()
        eventoCambiarFoto()
        eventoVolver()
        eventoAplicarCambios()
    }

    private fun quemarDatos() {
        binding.nombreETxt.setText(nombre)
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
        /*if (sessionType == "Google") {
            binding.nombreUsuarioETxtLayout.visibility = View.GONE
            binding.contraETxtLayout.visibility = View.GONE
        } else {
            binding.nombreUsuarioETxt.isEnabled = true
            binding.contraETxt.isEnabled = true
        } */
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
            takePicture()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            PERMISO_CAMARA -> {
                // If request is cancelled, the result arrays are empty.
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    Toast.makeText(this, "Permiso otorgado", Toast.LENGTH_SHORT).show()
                    takePicture()
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

    private fun takePicture() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        try {
            startActivityForResult(takePictureIntent, PERMISO_CAMARA)
        } catch (e: ActivityNotFoundException) {
            e.message?. let{ Log.e("PERMISSION_APP",it) }
            Toast.makeText(this, "No se puede abrir la cámara", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onActivityResult (requestCode: Int, resultCode:Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PERMISO_CAMARA && resultCode == RESULT_OK) {
            if (data?.extras == null) {
                Toast.makeText(this, "Me corrol", Toast.LENGTH_SHORT).show()
            } else {
                val imageBitmap = data.extras?.get("data") as? Bitmap
                if (imageBitmap == null) {
                    Toast.makeText(this, "No se pudo obtener la foto de perfil", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Foto de perfil actualizada", Toast.LENGTH_SHORT).show()
                    binding.imagenPerfil.setImageBitmap(imageBitmap)
                }
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
            //actualizarInfo(id, binding.nombreETxt.text.toString(), binding.nombreUsuarioETxt.text.toString(), binding.corrreoETxt.toString(), binding.contraETxt.text.toString())
            finish()
        }
    }
}