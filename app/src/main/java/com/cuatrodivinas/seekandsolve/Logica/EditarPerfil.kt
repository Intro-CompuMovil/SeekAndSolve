package com.cuatrodivinas.seekandsolve.Logica

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.text.InputType
import android.view.MotionEvent
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.cuatrodivinas.seekandsolve.Datos.Data.Companion.PERMISO_CAMARA
import com.cuatrodivinas.seekandsolve.R
import com.cuatrodivinas.seekandsolve.databinding.ActivityEditarPerfilBinding

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
        binding.nombreUsuarioETxt.setHint("Osquitar_El_Wapo")
        binding.imagenPerfil.setImageResource(R.drawable.foto_oscar)
        binding.nombreETxt.setHint("Oscar Danilo Martínez Bernal")
        binding.corrreoETxt.setHint("oscar@ejemplo.com")
        binding.contraETxt.setHint("*********")
        binding.FechaETxt.setHint("11/01/1865")
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
        binding.volverBtn.setOnClickListener {
            finish()
        }
    }

    private fun eventoAplicarCambios() {
        binding.aplicarCambiosBtn.setOnClickListener {
            finish()
        }
    }
}