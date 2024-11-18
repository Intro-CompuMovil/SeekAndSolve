package com.cuatrodivinas.seekandsolve.Logica

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.cuatrodivinas.seekandsolve.Datos.Data.Companion.PATH_DESAFIOS
import com.cuatrodivinas.seekandsolve.Datos.Data.Companion.PERMISO_CAMARA
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

class CrearDesafioActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCrearDesafioBinding
    private lateinit var desafio: Desafio
    private lateinit var checkpointsAdapter: CheckpointsAdapter
    private lateinit var refImg: StorageReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCrearDesafioBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initDesafio()
        setupUI()
    }

    private fun initDesafio() {
        desafio = Desafio("", auth.currentUser!!.uid, "", "", "", Punto(0.0, 0.0), mutableListOf(), Punto(0.0, 0.0))
        checkpointsAdapter = CheckpointsAdapter(this, desafio.puntosIntermedios)
        binding.listaCheckpoints.adapter = checkpointsAdapter
    }

    private fun setupUI() {
        binding.cambiarFoto.setOnClickListener {
            pedirPermiso(this, android.Manifest.permission.CAMERA, "Necesitamos el permiso de cámara para cambiar tu foto de perfil", PERMISO_CAMARA)
        }
        binding.editarPuntoInicial.setOnClickListener { editarPunto("inicial", REQUEST_CODE_PUNTO_INICIAL) }
        binding.editarPuntoFinal.setOnClickListener { editarPunto("final", REQUEST_CODE_PUNTO_FINAL) }
        binding.agregarCheckpoint.setOnClickListener { editarPunto("checkpoint", REQUEST_CODE_CHECKPOINT) }
        binding.crearDesafio.setOnClickListener {
            crearDesafio()
            startActivity(Intent(this, VerDesafiosActivity::class.java))
        }
    }

    private fun editarPunto(tipoPunto: String, requestCode: Int) {
        val bundle = Bundle().apply {
            putString("tipoPunto", tipoPunto)
            putDouble("latitudPuntoInicial", desafio.puntoInicial.latitud)
            putDouble("longitudPuntoInicial", desafio.puntoInicial.longitud)
            putDouble("latitudPuntoFinal", desafio.puntoFinal.latitud)
            putDouble("longitudPuntoFinal", desafio.puntoFinal.longitud)
        }
        val intent = when (tipoPunto) {
            "inicial" -> Intent(this, SeleccionarPuntoActivity::class.java)
            "final" -> Intent(this, SeleccionarPuntoFinalActivity::class.java)
            else -> Intent(this, SeleccionarCheckpointsActivity::class.java)
        }
        startActivityForResult(intent.putExtra("bundle", bundle), requestCode)
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
    private fun pedirPermiso(context: Context, permiso: String, justificacion: String, idCode: Int) {
        if (ContextCompat.checkSelfPermission(context, permiso) != PackageManager.PERMISSION_GRANTED) {
            if (shouldShowRequestPermissionRationale(permiso)) {
                Toast.makeText(context, justificacion, Toast.LENGTH_SHORT).show()
            }
            requestPermissions(arrayOf(permiso), idCode)
        } else {
            Toast.makeText(context, "Permiso otorgado", Toast.LENGTH_SHORT).show()
            takePicture()
        }
    }

    // Cuando tenga el permiso, abrir la cámara y tomar una foto
    private fun takePicture() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        try {
            startActivityForResult(takePictureIntent, PERMISO_CAMARA)
        } catch (e: Exception) {
            Log.e("PERMISSION_APP", e.message.orEmpty())
            Toast.makeText(this, "No se puede abrir la cámara", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            val punto = data?.getSerializableExtra("punto") as? Punto
            punto?.let {
                when (requestCode) {
                    REQUEST_CODE_PUNTO_INICIAL -> actualizarPuntoInicial(it)
                    REQUEST_CODE_PUNTO_FINAL -> actualizarPuntoFinal(it)
                    REQUEST_CODE_CHECKPOINT -> agregarCheckpoint(it)
                }
            }
            if (requestCode == PERMISO_CAMARA) {
                val imageBitmap = data?.extras?.get("data") as? Bitmap
                imageBitmap?.let {
                    binding.imagenDesafio.setImageBitmap(it)
                    Toast.makeText(this, "Foto de perfil actualizada", Toast.LENGTH_SHORT).show()
                } ?: run {
                    Toast.makeText(this, "No se pudo obtener la foto de perfil", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun actualizarPuntoInicial(punto: Punto) {
        desafio.puntoInicial = punto
        Log.d("CrearDesafioActivity", "Nuevo punto inicial: ${punto.latitud}, ${punto.longitud}")
        desafio.puntosIntermedios.add(0, desafio.puntoInicial)
        checkpointsAdapter.notifyDataSetChanged()
    }

    private fun actualizarPuntoFinal(punto: Punto) {
        desafio.puntoFinal = punto
        Log.d("CrearDesafioActivity", "Nuevo punto final: ${punto.latitud}, ${punto.longitud}")
        desafio.puntosIntermedios.add(0, desafio.puntoInicial)
        checkpointsAdapter.notifyDataSetChanged()
    }

    private fun agregarCheckpoint(punto: Punto) {
        desafio.puntosIntermedios.add(punto)
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
    }
}
