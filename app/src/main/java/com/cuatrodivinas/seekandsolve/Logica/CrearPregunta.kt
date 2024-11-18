package com.cuatrodivinas.seekandsolve.Logica

import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.RadioButton
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.cuatrodivinas.seekandsolve.Datos.Data.Companion.MY_PERMISSION_REQUEST_GALLERY
import com.cuatrodivinas.seekandsolve.Datos.Data.Companion.PATH_DESAFIOS
import com.cuatrodivinas.seekandsolve.Datos.Data.Companion.PATH_PREGUNTAS
import com.cuatrodivinas.seekandsolve.Datos.Data.Companion.PERMISO_CAMARA
import com.cuatrodivinas.seekandsolve.Datos.Data.Companion.database
import com.cuatrodivinas.seekandsolve.Datos.Data.Companion.storage
import com.cuatrodivinas.seekandsolve.Datos.Pregunta
import com.cuatrodivinas.seekandsolve.Datos.Punto
import com.cuatrodivinas.seekandsolve.R
import com.cuatrodivinas.seekandsolve.databinding.ActivityCrearPreguntaBinding
import com.google.firebase.database.DatabaseReference
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream

class CrearPregunta : AppCompatActivity() {
    private lateinit var binding: ActivityCrearPreguntaBinding
    private lateinit var preguntaRef: DatabaseReference
    private var preguntaId: String? = null
    private lateinit var respuestaCorrecta: String

    private val requestGalleryPermissions = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
        when {
            permissions.all { it.value } -> {
                // Todos los permisos concedidos -> seleccionar una imagen de la galería
                seleccionarDeGaleria()
            }
            permissions.any { !it.value } -> {
                // Algún permiso fue denegado
                Toast.makeText(this, "Permisos de Galería denegados", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCrearPreguntaBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    override fun onResume() {
        super.onResume()
        binding.backButton.setOnClickListener {
            finish()
        }
        binding.crearPreguntaBtn.setOnClickListener {
            crearPregunta()
        }
        binding.cambiarFoto.setOnClickListener {
            pedirPermisosGaleria("Necesitamos el permiso de galeria para agregar una imagen de pregunta")
        }
        binding.radioOption1.setOnClickListener{
            binding.radioOption2.isChecked  = false
            binding.radioOption3.isChecked  = false
            binding.radioOption4.isChecked  = false
            if(binding.editOption1.text.toString().isEmpty()){
                Toast.makeText(this, "Ponga el texto antes de seleccionar", Toast.LENGTH_LONG).show()
                binding.radioOption1.isChecked  = false
                return@setOnClickListener
            }
            respuestaCorrecta = binding.editOption1.text.toString()
        }
        binding.radioOption2.setOnClickListener{
            binding.radioOption1.isChecked  = false
            binding.radioOption3.isChecked  = false
            binding.radioOption4.isChecked  = false
            if(binding.editOption2.text.toString().isEmpty()){
                Toast.makeText(this, "Ponga el texto antes de seleccionar", Toast.LENGTH_LONG).show()
                binding.radioOption2.isChecked  = false
                return@setOnClickListener
            }
            respuestaCorrecta = binding.editOption2.text.toString()
        }
        binding.radioOption3.setOnClickListener{
            binding.radioOption1.isChecked  = false
            binding.radioOption2.isChecked  = false
            binding.radioOption4.isChecked  = false
            if(binding.editOption3.text.toString().isEmpty()){
                Toast.makeText(this, "Ponga el texto antes de seleccionar", Toast.LENGTH_LONG).show()
                binding.radioOption3.isChecked  = false
                return@setOnClickListener
            }
            respuestaCorrecta = binding.editOption3.text.toString()
        }
        binding.radioOption4.setOnClickListener{
            binding.radioOption1.isChecked  = false
            binding.radioOption2.isChecked  = false
            binding.radioOption3.isChecked  = false
            if(binding.editOption4.text.toString().isEmpty()){
                Toast.makeText(this, "Ponga el texto antes de seleccionar", Toast.LENGTH_LONG).show()
                binding.radioOption4.isChecked  = false
                return@setOnClickListener
            }
            respuestaCorrecta = binding.editOption4.text.toString()
        }
    }

    private fun crearPregunta(){
        if(binding.enunciadoPregunta.text.isEmpty() || binding.editOption1.text.isEmpty() ||
            binding.editOption2.text.isEmpty() ||  binding.editOption3.text.isEmpty() ||
            binding.editOption4.text.isEmpty() || binding.imagenPregunta.drawable == null){
            Toast.makeText(this, "llene todos los campos antes de continuar", Toast.LENGTH_LONG).show()
            return
        }
        val drawable = binding.imagenPregunta.drawable
        if (drawable is BitmapDrawable) {
            val bitmap = drawable.bitmap

            // Guardar el Bitmap en un archivo temporal
            val file = File(cacheDir, "temp_image.jpg")
            try {
                val outputStream = FileOutputStream(file)
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
                outputStream.flush()
                outputStream.close()
            } catch (e: IOException) {
                e.printStackTrace()
                return
            }

            // Convertir el archivo a Uri
            val fileUri = Uri.fromFile(file)
            preguntaRef = database.reference.child(PATH_PREGUNTAS)
            preguntaId = preguntaRef.push().key // Genera el ID
            val imageRef =
                storage.reference.child("$PATH_PREGUNTAS/${preguntaId}.jpg")
            imageRef.putFile(fileUri)
                .addOnSuccessListener { taskSnapshot ->
                    // Obtener la URL de descarga después de que la imagen se suba con éxito
                    imageRef.downloadUrl.addOnSuccessListener { uri ->
                        // Guardar el URL en la base de datos o usarlo como desees
                        escribirPreguntaBD(uri.toString())
                    }.addOnFailureListener { exception ->
                        Toast.makeText(
                            this@CrearPregunta,
                            "No fue posible obtener la URL de descarga",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(
                        this@CrearPregunta,
                        "No fue posible subir la imagen",
                        Toast.LENGTH_LONG
                    ).show()
                }
        }
        else{
            Toast.makeText(this, "La imagen no es un BitmapDrawable", Toast.LENGTH_SHORT).show()
        }
    }

    private fun escribirPreguntaBD(imagenUrl: String) {
        val pregunta = preguntaId?.let {
            Pregunta(
                it,binding.enunciadoPregunta.text.toString(),
                listOf(binding.editOption1.text.toString(), binding.editOption2.text.toString(), binding.editOption3.text.toString(), binding.editOption4.text.toString()),
                respuestaCorrecta, imagenUrl
            )
        }
        preguntaRef.child(preguntaId!!).setValue(pregunta)
        startActivity(Intent(this, MainActivity::class.java))
    }

    private fun pedirPermisosGaleria(justificacion: String) {
        // Array de permisos a solicitar basado en la versión de Android del dispositivo
        val permisos = when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE -> arrayOf(
                android.Manifest.permission.READ_MEDIA_IMAGES,
                android.Manifest.permission.READ_MEDIA_VIDEO,
                android.Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED
            )
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> arrayOf(
                android.Manifest.permission.READ_MEDIA_IMAGES,
                android.Manifest.permission.READ_MEDIA_VIDEO
            )
            else -> arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE)
        }

        // Verificar si se debe mostrar una justificación para cualquiera de los permisos
        if (permisos.any { shouldShowRequestPermissionRationale(it) }) {
            mostrarJustificacion(
                justificacion
            ) {
                // Lanzar la solicitud de permisos después de la justificación
                requestGalleryPermissions.launch(permisos)
            }
        } else {
            // Lanzar la solicitud de permisos sin justificación
            requestGalleryPermissions.launch(permisos)
        }
    }

    private fun mostrarJustificacion(mensaje: String, onAccept: () -> Unit) {
        AlertDialog.Builder(this)
            .setTitle("Justificación de permisos")
            .setMessage(mensaje)
            .setPositiveButton("Aceptar") { dialog, _ ->
                onAccept()
                dialog.dismiss()
            }
            .setNegativeButton("Cancelar") { dialog, _ ->
                dialog.dismiss()
            }
            .create()
            .show()
    }

    private fun seleccionarDeGaleria() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        try {
            startActivityForResult(intent, MY_PERMISSION_REQUEST_GALLERY)
        } catch (e: ActivityNotFoundException) {
            e.message?. let{ Log.e("PERMISSION_APP",it) }
            Toast.makeText(this, "No es posible abrir la galería", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when(requestCode) {
            MY_PERMISSION_REQUEST_GALLERY -> {
                if (resultCode == RESULT_OK) {
                    try {
                        val imageUri = data?.data
                        val imageStream: InputStream? = contentResolver.openInputStream(imageUri!!)
                        val selectedImage = BitmapFactory.decodeStream(imageStream)
                        binding.imagenPregunta.setImageBitmap(selectedImage)
                        Toast.makeText(this, "Foto de pregunta actualizada", Toast.LENGTH_SHORT).show()
                    } catch (e: Exception) {
                        e.message?. let{ Log.e("PERMISSION_APP",it) }
                        Toast.makeText(this, "No fue posible seleccionar la imagen (exc.)", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }
}