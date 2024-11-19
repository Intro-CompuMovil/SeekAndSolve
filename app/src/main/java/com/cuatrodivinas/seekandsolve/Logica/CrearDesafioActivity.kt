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
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.cuatrodivinas.seekandsolve.Datos.Data.Companion.MY_PERMISSION_REQUEST_GALLERY
import com.cuatrodivinas.seekandsolve.Datos.Data.Companion.PATH_DESAFIOS
import com.cuatrodivinas.seekandsolve.Datos.Data.Companion.auth
import com.cuatrodivinas.seekandsolve.Datos.Data.Companion.database
import com.cuatrodivinas.seekandsolve.Datos.Data.Companion.storage
import com.cuatrodivinas.seekandsolve.Datos.Desafio
import com.cuatrodivinas.seekandsolve.Datos.Punto
import com.cuatrodivinas.seekandsolve.databinding.ActivityCrearDesafioBinding
import com.google.firebase.database.DatabaseReference
import com.google.firebase.storage.StorageReference
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream

class CrearDesafioActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCrearDesafioBinding
    private lateinit var desafio: Desafio
    private lateinit var checkpointsAdapter: CheckpointsAdapter
    private lateinit var refImg: StorageReference
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
        binding = ActivityCrearDesafioBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initDesafio()
        setupUI()
    }

    private fun initDesafio() {
        desafio = Desafio("", auth.currentUser!!.uid, "", "", "", Punto(0.0, 0.0), mutableListOf(), Punto(0.0, 0.0))
        checkpointsAdapter = CheckpointsAdapter(this, desafio.puntosIntermedios!!, this)
        binding.listaCheckpoints.adapter = checkpointsAdapter
    }

    private fun setupUI() {
        binding.cambiarFoto.setOnClickListener {
            pedirPermisosGaleria("Necesitamos el permiso de galeria para agregar una imagen de desafio")
        }
        binding.editarPuntoInicial.setOnClickListener { editarPunto("inicial", REQUEST_CODE_PUNTO_INICIAL) }
        binding.editarPuntoFinal.setOnClickListener { editarPunto("final", REQUEST_CODE_PUNTO_FINAL) }
        binding.agregarCheckpoint.setOnClickListener { editarPunto("checkpoint", REQUEST_CODE_CHECKPOINT) }
        binding.crearDesafio.setOnClickListener {
            if (validarInformacionDesafio()) {
                crearDesafio()
                startActivity(Intent(this, VerDesafiosActivity::class.java))
                finish()
            }
        }
        binding.backButton.setOnClickListener {
            finish()
        }
    }

    private fun editarPunto(tipoPunto: String, requestCode: Int) {
        val bundle = Bundle().apply {
            putString("tipoPunto", tipoPunto)
            putDouble("latitudPuntoInicial", desafio.puntoInicial!!.latitud)
            putDouble("longitudPuntoInicial", desafio.puntoInicial!!.longitud)
            putDouble("latitudPuntoFinal", desafio.puntoFinal!!.latitud)
            putDouble("longitudPuntoFinal", desafio.puntoFinal!!.longitud)
        }
        val intent = when (tipoPunto) {
            "inicial" -> Intent(this, SeleccionarPuntoActivity::class.java)
            "final" -> Intent(this, SeleccionarPuntoFinalActivity::class.java)
            else -> Intent(this, SeleccionarCheckpointsActivity::class.java)
        }
        startActivityForResult(intent.putExtra("bundle", bundle), requestCode)
    }

    private fun validarInformacionDesafio(): Boolean {
        val nombre = binding.nombreDesafio.text.toString().trim()
        val descripcion = binding.descripcionDesafio.text.toString().trim()

        if (nombre.isEmpty()) {
            binding.nombreDesafio.error = "El nombre es obligatorio"
            return false
        }

        if (descripcion.isEmpty()) {
            binding.descripcionDesafio.error = "La descripción es obligatoria"
            return false
        }

        // Update the Desafio object with the new information
        desafio.nombre = nombre
        desafio.descripcion = descripcion

        return true
    }

    private fun crearDesafio() {
        val drawable = binding.imagenDesafio.drawable
        if (drawable is BitmapDrawable) {
            val bitmap = drawable.bitmap
            val fileUri = saveBitmapToFile(bitmap)
            if (fileUri != null) {
                val desafiosRef = database.reference.child(PATH_DESAFIOS)
                val desafioId = desafiosRef.push().key // Genera el ID del desafío
                desafio.id = desafioId!!
                desafio.puntosIntermedios = checkpointsAdapter.checkpoints
                guardarImagenYDesafio(fileUri, desafioId, desafiosRef)
            }
        } else {
            Toast.makeText(this, "La imagen no es un BitmapDrawable", Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveBitmapToFile(bitmap: Bitmap): Uri? {
        val file = File(cacheDir, "temp_image.jpg")
        return try {
            val outputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
            outputStream.flush()
            outputStream.close()
            Uri.fromFile(file)
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }

    private fun guardarImagenYDesafio(fileUri: Uri, desafioId: String, desafiosRef: DatabaseReference) {
        val imageRef = storage.reference.child("$PATH_DESAFIOS/${desafioId}.jpg")
        imageRef.putFile(fileUri)
            .addOnSuccessListener {
                imageRef.downloadUrl.addOnSuccessListener { uri ->
                    desafio.imagenUrl = uri.toString()
                    escribirDesafioDB(desafioId, desafiosRef)
                }.addOnFailureListener {
                    showToast("No fue posible obtener la URL de descarga")
                }
            }
            .addOnFailureListener {
                showToast("No fue posible subir la imagen")
            }
    }

    private fun escribirDesafioDB(desafioId: String, desafiosRef: DatabaseReference) {
        desafiosRef.child(desafioId).setValue(desafio.toMap())
    }

    // Pedir permiso para acceder a la cámara
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
        if (resultCode == RESULT_OK) {
            try {
                when (requestCode) {
                    REQUEST_CODE_PUNTO_INICIAL, REQUEST_CODE_PUNTO_FINAL -> {
                        // Obtener el punto actualizado
                        val punto = data?.getSerializableExtra("punto") as? Punto
                        punto?.let {
                            when (requestCode) {
                                REQUEST_CODE_PUNTO_INICIAL -> actualizarPuntoInicial(it)
                                REQUEST_CODE_PUNTO_FINAL -> actualizarPuntoFinal(it)
                            }
                        }
                    }

                    REQUEST_CODE_CHECKPOINT -> {
                        // Obtener la lista de puntos actualizada
                        val puntosArray: Array<Punto>? = data?.getSerializableExtra("puntos") as? Array<Punto>
                        puntosArray?.let {
                            agregarCheckpoints(it.toMutableList())
                        }
                    }

                    REQUEST_CODE_EDITAR_CHEKCPOINT -> {
                        // Obtener la lista de puntos actualizada
                        val punto = data?.getSerializableExtra("punto") as Punto
                        val posicion = data.getIntExtra("posicion", 0)
                        desafio.puntosIntermedios[posicion] = punto
                        checkpointsAdapter.notifyDataSetChanged()
                    }

                    MY_PERMISSION_REQUEST_GALLERY -> {
                        // Manejo de selección de imagen desde la galería
                        val imageUri = data?.data
                        if (imageUri != null) {
                            val imageStream: InputStream? = contentResolver.openInputStream(imageUri)
                            val selectedImage = BitmapFactory.decodeStream(imageStream)
                            binding.imagenDesafio.setImageBitmap(selectedImage)
                            Toast.makeText(this, "Foto de desafío actualizada", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(this, "No se seleccionó ninguna imagen", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("PERMISSION_APP", e.message ?: "Error desconocido")
                Toast.makeText(this, "Ocurrió un error al procesar la solicitud", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "Operación cancelada", Toast.LENGTH_SHORT).show()
        }
    }

    private fun actualizarPuntoInicial(punto: Punto) {
        desafio.puntoInicial = punto
        checkpointsAdapter.notifyDataSetChanged()
        binding.txtPuntoInicial.text = "Lat: ${punto.latitud}, Lng: ${punto.longitud}"
        Log.d("CrearDesafioActivity", "Nuevo punto inicial: ${punto.latitud}, ${punto.longitud}")
//        desafio.puntosIntermedios!!.add(0, desafio.puntoInicial!!)
//        checkpointsAdapter.notifyDataSetChanged()
    }

    private fun actualizarPuntoFinal(punto: Punto) {
        desafio.puntoFinal = punto
        checkpointsAdapter.notifyDataSetChanged()
        binding.txtPuntoFinal.text = "Lat: ${punto.latitud}, Lng: ${punto.longitud}"
        Log.d("CrearDesafioActivity", "Nuevo punto final: ${punto.latitud}, ${punto.longitud}")
//        desafio.puntosIntermedios!!.add(0, desafio.puntoInicial!!)
//        checkpointsAdapter.notifyDataSetChanged()
    }

    private fun agregarCheckpoints(puntos: MutableList<Punto>) {
        desafio.puntosIntermedios.addAll(puntos)
        checkpointsAdapter.notifyDataSetChanged()
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    companion object {
        private const val REQUEST_CODE_PUNTO_INICIAL = 1001
        private const val REQUEST_CODE_PUNTO_FINAL = 1002
        private const val REQUEST_CODE_CHECKPOINT = 1003
        private const val REQUEST_CODE_MARCADORES_ADICIONALES = 1004
        private const val REQUEST_CODE_EDITAR_CHEKCPOINT = 1005
    }
}
