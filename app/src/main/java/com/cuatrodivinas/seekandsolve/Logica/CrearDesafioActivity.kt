package com.cuatrodivinas.seekandsolve.Logica

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.cuatrodivinas.seekandsolve.Datos.Data.Companion.PERMISO_CAMARA
import com.cuatrodivinas.seekandsolve.Datos.Desafio
import com.cuatrodivinas.seekandsolve.Datos.Punto
import com.cuatrodivinas.seekandsolve.R
import com.cuatrodivinas.seekandsolve.databinding.ActivityCrearDesafioBinding
import com.cuatrodivinas.seekandsolve.databinding.ActivityEditarPerfilBinding

class CrearDesafioActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCrearDesafioBinding

    private lateinit var intentEditarPunto: Intent

    private lateinit var intentCrearDesafio: Intent
    private lateinit var desafio: Desafio

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCrearDesafioBinding.inflate(layoutInflater)
        setContentView(binding.root)
        desafio = Desafio(0, "", "", "", Punto(0.0,0.0), mutableListOf(), Punto(0.0,0.0))
        binding.listaCheckpoints.adapter = CheckpointsAdapter(this, null, 0)

        intentEditarPunto = Intent(this, SeleccionarPuntoActivity::class.java)
        intentCrearDesafio = Intent(this, VerDesafiosActivity::class.java)
    }

    override fun onResume() {
        super.onResume()
        binding.editarPuntoInicial.setOnClickListener {
            val bundle = Bundle()
            bundle.putString("tipoPunto", "inicial")
            bundle.putSerializable("desafio", desafio)
            intentEditarPunto.putExtra("bundle", bundle)
            startActivity(intentEditarPunto)
        }

        binding.editarPuntoFinal.setOnClickListener {
            val bundle = Bundle()
            bundle.putString("tipoPunto", "final")
            bundle.putSerializable("desafio", desafio)
            intentEditarPunto.putExtra("bundle", bundle)
            startActivity(intentEditarPunto)
        }

        binding.agregarCheckpoint.setOnClickListener {
            val bundle = Bundle()
            bundle.putString("tipoPunto", "checkpoint")
            bundle.putSerializable("desafio", desafio)
            intentEditarPunto.putExtra("bundle", bundle)
            startActivity(intentEditarPunto)
        }

        binding.crearDesafio.setOnClickListener {
            startActivity(intentCrearDesafio)
        }

        eventoCambiarFoto()
    }

    private fun eventoCambiarFoto(){
        binding.cambiarFoto.setOnClickListener {
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
                    binding.imagenDesafio.setImageBitmap(imageBitmap)
                }
            }
        }
    }
}