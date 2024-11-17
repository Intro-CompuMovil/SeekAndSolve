package com.cuatrodivinas.seekandsolve.Logica

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.cuatrodivinas.seekandsolve.Datos.Data.Companion.PATH_DESAFIOS
import com.cuatrodivinas.seekandsolve.Datos.Data.Companion.PATH_IMAGENES
import com.cuatrodivinas.seekandsolve.Datos.Data.Companion.PERMISO_CAMARA
import com.cuatrodivinas.seekandsolve.Datos.Data.Companion.auth
import com.cuatrodivinas.seekandsolve.Datos.Data.Companion.storage
import com.cuatrodivinas.seekandsolve.Datos.Desafio
import com.cuatrodivinas.seekandsolve.Datos.Punto
import com.cuatrodivinas.seekandsolve.databinding.ActivityCrearDesafioBinding
import com.google.firebase.storage.StorageReference
import org.json.JSONArray
import org.osmdroid.util.GeoPoint
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStreamWriter
import java.util.UUID

class CrearDesafioActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCrearDesafioBinding
    private lateinit var desafio: Desafio
    private lateinit var checkpointsAdapter: CheckpointsAdapter
    private lateinit var refImg: StorageReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCrearDesafioBinding.inflate(layoutInflater)
        setContentView(binding.root)

        desafio = Desafio(auth.currentUser!!.uid, "", "", "", Punto(0.0, 0.0), mutableListOf(), Punto(0.0, 0.0))
        checkpointsAdapter = CheckpointsAdapter(this, desafio.puntosIntermedios)
        binding.listaCheckpoints.adapter = checkpointsAdapter

        // Cuando se edita el punto inicial
        binding.editarPuntoInicial.setOnClickListener {
            val bundle = Bundle().apply {
                putString("tipoPunto", "inicial")
                putSerializable("desafio", desafio)
            }
            startActivityForResult(Intent(this, SeleccionarPuntoActivity::class.java).putExtra("bundle", bundle), REQUEST_CODE_PUNTO_INICIAL)
        }

// Cuando se edita el punto final (con punto inicial ya presente)
        binding.editarPuntoFinal.setOnClickListener {
            val bundle = Bundle().apply {
                putString("tipoPunto", "final")
                putSerializable("desafio", desafio)
                // Asegurar que se pasen las coordenadas del punto inicial al bundle
                putDouble("latitudPuntoInicial", desafio.puntoInicial.latitud)
                putDouble("longitudPuntoInicial", desafio.puntoInicial.longitud)
                val latitudInicial = desafio.puntoInicial.latitud
                val longitudInicial = desafio.puntoInicial.longitud

                Log.d("SeleccionarPunto", "Latitud inicial: $latitudInicial, Longitud inicial: $longitudInicial")

            }
            startActivityForResult(Intent(this, SeleccionarPuntoFinalActivity::class.java).putExtra("bundle", bundle), REQUEST_CODE_PUNTO_FINAL)
        }

        // Agregar checkpoint
        binding.agregarCheckpoint.setOnClickListener {
            val bundle = Bundle().apply {
                putString("tipoPunto", "checkpoint")
                putSerializable("desafio", desafio)
                // Pass the coordinates of the initial and final points to the bundle
                putDouble("latitudPuntoInicial", desafio.puntoInicial.latitud)
                putDouble("longitudPuntoInicial", desafio.puntoInicial.longitud)
                putDouble("latitudPuntoFinal", desafio.puntoFinal.latitud)
                putDouble("longitudPuntoFinal", desafio.puntoFinal.longitud)
                var latitudInicial = desafio.puntoInicial.latitud
                var longitudInicial = desafio.puntoInicial.longitud
                var latitudFinal = desafio.puntoFinal.latitud
                var longitudFinal = desafio.puntoFinal.longitud

                Log.d("Check", "Latitud inicial: $latitudInicial, Longitud inicial: $longitudInicial, Latitud final: $latitudFinal, Longitud final: $longitudFinal")
            }
            startActivityForResult(Intent(this, SeleccionarCheckpointsActivity::class.java).putExtra("bundle", bundle), REQUEST_CODE_CHECKPOINT)
        }

        // Crear el desafío
        binding.crearDesafio.setOnClickListener {
            startActivity(Intent(this, VerDesafiosActivity::class.java))
        }

        // Escuchar si hay datos nuevos de SeleccionarPuntoActivity (checkpoint o punto final)
        val puntoSeleccionado = intent.getSerializableExtra("checkpoint") as? Punto
        val puntoFinalSeleccionado = intent.getSerializableExtra("puntoFinal") as? Punto

        puntoSeleccionado?.let {
            desafio.puntosIntermedios.add(it)
            checkpointsAdapter.notifyDataSetChanged()  // Actualizar la lista
        }

        puntoFinalSeleccionado?.let {
            desafio.puntoFinal = it
            // Actualiza también el ListView si es necesario
            checkpointsAdapter.notifyDataSetChanged()
        }

        eventoCambiarFoto()
        // Llamar a saveData() en el lugar apropiado, por ejemplo, cuando se hace clic en un botón
        binding.crearDesafio.setOnClickListener {
            saveData()
            startActivity(Intent(this, VerDesafiosActivity::class.java))
        }
    }

    private fun saveData() {
        // Crear un objeto JSONObject
        val jsonObject = JSONObject()

        //Crear id para el desafio
        val idDesafio = UUID.randomUUID().toString()

        // Agregar la información al objeto JSONObject
        jsonObject.put("nombre", binding.nombreDesafio.text.toString())
        jsonObject.put("descripcion", binding.descripcionDesafio.text.toString())
        jsonObject.put("puntoInicial", desafio.puntoInicial.toJSON())
        jsonObject.put("puntoFinal", desafio.puntoFinal.toJSON())
        jsonObject.put("puntosIntermedios", desafio.puntosIntermedios.toJSONArray())

        // Convertir el objeto JSONObject a una cadena
        val jsonString = jsonObject.toString()

        // Abrir un archivo en la memoria interna de la aplicación
        val fileOutputStream = openFileOutput("desafio.json", Context.MODE_PRIVATE)
        val outputStreamWriter = OutputStreamWriter(fileOutputStream)

        // Escribir la cadena en el archivo
        outputStreamWriter.use { it.write(jsonString) }

        // Cerrar el archivo
        outputStreamWriter.close()
        fileOutputStream.close()

        //guardar imagen en storage
        val imagen = binding.imagenDesafio.drawable
        refImg = storage.getReference("$PATH_DESAFIOS/$idDesafio.jpg")
        refImg.putFile(toUri(imagen) ?: return)
    }

    private fun toUri(imagen: Drawable?): Uri? {
        // Verificar si la imagen es null
        if (imagen == null) return null

        // Convertir el Drawable a un Bitmap
        val bitmap = (imagen as BitmapDrawable).bitmap

        // Crear un archivo temporal en el cache del contexto
        val file = File(this.cacheDir, "imagen_temp.jpg")
        try {
            // Crear un flujo de salida para guardar el Bitmap como archivo
            val fos = FileOutputStream(file)
            // Comprimir el Bitmap en formato JPEG y guardarlo en el archivo temporal
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos)
            fos.flush()
            fos.close()

            // Retornar el Uri del archivo temporal
            return Uri.fromFile(file)
        } catch (e: IOException) {
            e.printStackTrace()
            return null
        }
    }

    fun Punto.toJSON(): JSONObject {
        val jsonObject = JSONObject()
        jsonObject.put("latitud", this.latitud)
        jsonObject.put("longitud", this.longitud)
        return jsonObject
    }

    fun List<Punto>.toJSONArray(): JSONArray {
        val jsonArray = JSONArray()
        this.forEach { jsonArray.put(it.toJSON()) }
        return jsonArray
    }

    private fun eventoCambiarFoto() {
        binding.cambiarFoto.setOnClickListener {
            pedirPermiso(this, android.Manifest.permission.CAMERA, "Necesitamos el permiso de cámara para cambiar tu foto de perfil", PERMISO_CAMARA)
        }
    }

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
        if (requestCode == REQUEST_CODE_PUNTO_INICIAL && resultCode == RESULT_OK) {
            val puntoInicial = data?.getSerializableExtra("puntoInicial") as? Punto
            puntoInicial?.let {
                desafio.puntoInicial = it  // Actualizar el desafío con el punto inicial
                Log.d("CrearDesafioActivity", "Nuevo punto inicial: ${it.latitud}, ${it.longitud}")
                desafio.puntosIntermedios.add(0, desafio.puntoInicial)
                checkpointsAdapter.notifyDataSetChanged()  // Actualizar la lista
            }
        } else if (requestCode == REQUEST_CODE_PUNTO_FINAL && resultCode == RESULT_OK) {
            val puntoFinal = data?.getSerializableExtra("puntoFinal") as? Punto
            puntoFinal?.let {
                desafio.puntoFinal = it  // Actualizar el desafío con el punto final
                Log.d("CrearDesafioActivity", "Nuevo punto final: ${it.latitud}, ${it.longitud}")
                desafio.puntosIntermedios.add(0, desafio.puntoInicial)
                checkpointsAdapter.notifyDataSetChanged()  // Actualizar la lista
            }
        } else if (requestCode == REQUEST_CODE_MARCADORES_ADICIONALES && resultCode == RESULT_OK) {
            val marcadoresAdicionales = data?.getParcelableArrayExtra("marcadores")
            marcadoresAdicionales?.map { it as GeoPoint }?.forEach { punto ->
                desafio.puntosIntermedios.add(Punto(punto.latitude, punto.longitude))  // Agregar cada marcador adicional al desafío
            }
            checkpointsAdapter.notifyDataSetChanged()  // Actualizar la lista
        } else if (requestCode == PERMISO_CAMARA && resultCode == RESULT_OK) {
            val imageBitmap = data?.extras?.get("data") as? Bitmap
            imageBitmap?.let {
                binding.imagenDesafio.setImageBitmap(it)
                Toast.makeText(this, "Foto de perfil actualizada", Toast.LENGTH_SHORT).show()
            } ?: run {
                Toast.makeText(this, "No se pudo obtener la foto de perfil", Toast.LENGTH_SHORT).show()
            }
        }
    }
    companion object {
        private const val REQUEST_CODE_PUNTO_INICIAL = 1001
        private const val REQUEST_CODE_PUNTO_FINAL = 1002
        private const val REQUEST_CODE_CHECKPOINT = 1003
        private const val REQUEST_CODE_MARCADORES_ADICIONALES = 1004
    }


}
