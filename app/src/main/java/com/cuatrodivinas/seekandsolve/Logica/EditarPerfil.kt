package com.cuatrodivinas.seekandsolve.Logica

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.VectorDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.text.InputType
import android.util.Base64
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.cuatrodivinas.seekandsolve.Datos.Data.Companion.PERMISO_CAMARA
import com.cuatrodivinas.seekandsolve.Datos.Data.Companion.PERMISO_GALERIA
//import com.cuatrodivinas.seekandsolve.Datos.Data.Companion.SELECCIONAR_IMAGEN
import com.cuatrodivinas.seekandsolve.R
import com.cuatrodivinas.seekandsolve.databinding.ActivityEditarPerfilBinding
import com.google.firebase.crashlytics.buildtools.reloc.org.apache.commons.io.output.ByteArrayOutputStream
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.security.KeyStore
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import kotlin.properties.Delegates

class EditarPerfil : AppCompatActivity() {
    private lateinit var binding: ActivityEditarPerfilBinding
    private var contraseniaVisible = false
    private lateinit var photoUri: Uri

    private var id by Delegates.notNull<Int>()
    private lateinit var nombre: String
    private lateinit var username: String
    private lateinit var correo: String
    private lateinit var contrasena: String
    private lateinit var fotoUrl: String
    private lateinit var fechaNacimiento: String

    private lateinit var localizacionFoto : File

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
        if (fotoUrl.isNotEmpty()) {
            if (fotoUrl.startsWith("/")) {
                // Cargar imagen desde el archivo
                val file = File(fotoUrl)
                if (file.exists()) {
                    Glide.with(this)
                        .load(file)
                        .override(24, 24) // Establecer el tamaño de la imagen en 24x24 px
                        .circleCrop() // Hacer la imagen circular
                        .into(binding.imagenPerfil) // Establecer la imagen en el ImageView
                } else {
                    // Caso: archivo no existe, cargar imagen por defecto
                    binding.imagenPerfil.imageTintList = ContextCompat.getColorStateList(this, R.color.primaryColor)
                }
            } else {
                // Caso: cargar imagen por defecto si la fotoUrl no es un archivo válido
                binding.imagenPerfil.imageTintList = ContextCompat.getColorStateList(this, R.color.primaryColor)
                Toast.makeText(this, "Error al cargar la imagen", Toast.LENGTH_SHORT).show()
            }
        } else {
            // Caso: fotoUrl está vacía, cargar imagen por defecto
            binding.imagenPerfil.imageTintList = ContextCompat.getColorStateList(this, R.color.primaryColor)
        }
        binding.nombreETxt.setText(nombre)
        binding.corrreoETxt.setText(correo)
        binding.FechaETxt.setText(fechaNacimiento)
        binding.nombreUsuarioETxt.setText(username)
        binding.contraETxt.setText(descifrarPassword(contrasena))
        /*if (sessionType == "Google") {
            binding.nombreUsuarioETxtLayout.visibility = View.GONE
            binding.contraETxtLayout.visibility = View.GONE
        } else {
            binding.nombreUsuarioETxt.isEnabled = true
            binding.contraETxt.isEnabled = true
        } */
    }

    fun isBase64(string: String): Boolean {
        return try {
            Base64.decode(string, Base64.DEFAULT)
            true
        } catch (e: IllegalArgumentException) {
            false
        }
    }

    private fun descifrarPassword(storedPasswordHash: String): String {
        val keyStore = KeyStore.getInstance("AndroidKeyStore")
        keyStore.load(null)
        val secretKey = keyStore.getKey("MyKeyAlias", null) as SecretKey

        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        val ivAndCipherText = Base64.decode(storedPasswordHash, Base64.DEFAULT)
        val iv = ivAndCipherText.copyOfRange(0, 12)
        val cipherText = ivAndCipherText.copyOfRange(12, ivAndCipherText.size)
        val spec = GCMParameterSpec(128, iv)
        cipher.init(Cipher.DECRYPT_MODE, secretKey, spec)
        return String(cipher.doFinal(cipherText), Charsets.UTF_8)
    }

    private fun eventoCambiarFoto(){
        binding.cambiarFotoBtn.setOnClickListener {
            val opciones = arrayOf("Tomar foto", "Seleccionar desde galería")
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Cambiar foto de perfil")
            builder.setItems(opciones) { _, which ->
                when (which) {
                    0 -> {
                        // Opción para tomar una foto
                        pedirPermiso(this, android.Manifest.permission.CAMERA,
                            "Necesitamos el permiso de cámara para cambiar tu foto de perfil", PERMISO_CAMARA)
                    }
                    1 -> {
                        pedirPermisosGaleria("Necesitamos acceder a la galería para seleccionar y mostrar una imagen en la app")
                    }
                }
            }
            builder.show()
        }
    }

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
        androidx.appcompat.app.AlertDialog.Builder(this)
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
            startActivityForResult(intent, PERMISO_GALERIA)
        } catch (e: ActivityNotFoundException) {
            e.message?. let{ Log.e("PERMISSION_APP",it) }
            Toast.makeText(this, "No es posible abrir la galería", Toast.LENGTH_SHORT).show()
        }
    }

    private fun pedirPermiso(context: Context, permiso: String, justificacion: String,
                             idCode: Int){
        if(ContextCompat.checkSelfPermission(context, permiso) !=
            PackageManager.PERMISSION_GRANTED){
            if (shouldShowRequestPermissionRationale(permiso)) {
                // Explicar al usuario por qué necesitamos el permiso
                mostrarJustificacion(justificacion) {
                    requestPermissions(arrayOf(permiso), idCode)
                }
            } else {
                requestPermissions(arrayOf(permiso), idCode)
            }
        }
        else{
            // Permiso ya concedido, tomar la foto
            takePicture()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            PERMISO_CAMARA -> {
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    // Permiso concedido, tomar la foto
                    takePicture()
                } else {
                    Toast.makeText(this, "Funcionalidades reducidas", Toast.LENGTH_LONG).show()
                }
                return
            }
        }
    }

    private fun takePicture() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        try {
            localizacionFoto = createImageFile()
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
            startActivityForResult(takePictureIntent, PERMISO_CAMARA)
        } catch (e: ActivityNotFoundException) {
            e.message?. let{ Log.e("PERMISSION_APP",it) }
            Toast.makeText(this, "No es posible abrir la cámara", Toast.LENGTH_SHORT).show()
        }
    }

    private fun createImageFile(): File {
        // El timestamp se usa para que el nombre del archivo sea único
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        // El directorio donde se guardará la imagen
        val storageDir: File = getExternalFilesDir(Environment.DIRECTORY_PICTURES)!!
        // Crear el archivo con el nombre "JPEG_YYYYMMDD_HHMMSS.jpg" en el directorio storageDir
        return File.createTempFile(
            "JPEG_${timestamp}_", /* prefix */
            ".jpg", /* suffix */
            storageDir /* directory */
        ).apply {
            // Guardar la URI del archivo para usarla en el Intent de la cámara
            photoUri = FileProvider.getUriForFile(this@EditarPerfil, "com.cuatrodivinas.seekandsolve.fileprovider", this)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            PERMISO_CAMARA -> {
                if (resultCode == RESULT_OK) {
                    binding.imagenPerfil.setImageURI(photoUri)
                    binding.imagenPerfil.imageTintList = null
                    // Guardar la ruta de la imagen en la variable
                    localizacionFoto = File(photoUri.path!!)
                }
            }
            PERMISO_GALERIA -> {
                if (resultCode == RESULT_OK) {
                    data?.data?.let { imageUri ->
                        binding.imagenPerfil.setImageURI(imageUri)
                        binding.imagenPerfil.imageTintList = null
                        // Guardar la ruta de la imagen seleccionada
                        localizacionFoto = saveImageToFile(imageUri)
                    }
                }
            }
        }
    }

    private fun saveImageToFile(imageUri: Uri): File {
        // Crear un archivo temporal para almacenar la imagen
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir: File = getExternalFilesDir(Environment.DIRECTORY_PICTURES)!!
        val tempFile = File.createTempFile("IMG_${timestamp}_", ".jpg", storageDir)

        // Copiar los datos de la imagen desde el InputStream hacia el archivo
        val inputStream: InputStream? = contentResolver.openInputStream(imageUri)
        val outputStream = FileOutputStream(tempFile)
        inputStream?.copyTo(outputStream)
        outputStream.close()
        inputStream?.close()
        return tempFile // Devolver el archivo
    }

    private fun eventoVolver() {
        binding.backButtonEditarPerfil.setOnClickListener {
            finish()
        }
    }

    fun eventoAplicarCambios() {
        binding.aplicarCambiosBtn.setOnClickListener {
            if (localizacionFoto != null){
                actualizarInfo(id, binding.nombreETxt.text.toString(), binding.nombreUsuarioETxt.text.toString(), binding.corrreoETxt.text.toString(),
                    binding.contraETxt.text.toString(), localizacionFoto.absolutePath)
            }else{
                actualizarInfo(id, binding.nombreETxt.text.toString(), binding.nombreUsuarioETxt.text.toString(), binding.corrreoETxt.text.toString(),
                    binding.contraETxt.text.toString(), saveDrawableToFileAsJPG(R.drawable.profile_user_svgrepo_com).absolutePath)
            }

        }
    }

    private fun saveDrawableToFileAsJPG(drawableResId: Int): File {
        // Cargar el recurso de la imagen desde drawable
        val drawable = ContextCompat.getDrawable(this, drawableResId)

        // Crear un bitmap a partir del drawable
        val bitmap = drawableToBitmap(drawable!!)

        // Crear un archivo temporal para almacenar la imagen
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir: File = getExternalFilesDir(Environment.DIRECTORY_PICTURES)!!
        val tempFile = File.createTempFile("IMG_${timestamp}_", ".jpg", storageDir)

        // Guardar el bitmap en el archivo como JPEG
        val outputStream = FileOutputStream(tempFile)
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
        outputStream.flush()
        outputStream.close()

        return tempFile // Devolver el archivo
    }

    private fun drawableToBitmap(drawable: Drawable): Bitmap {
        if (drawable is BitmapDrawable) {
            return drawable.bitmap
        }

        // Si el drawable no es un BitmapDrawable, creamos un Bitmap
        val bitmap = Bitmap.createBitmap(
            drawable.intrinsicWidth,
            drawable.intrinsicHeight,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        return bitmap
    }

    private fun hashPassword(password: String): String {
        val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")
        val keyGenParameterSpec = KeyGenParameterSpec.Builder(
            "MyKeyAlias",
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        ).setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .build()
        keyGenerator.init(keyGenParameterSpec)
        val secretKey = keyGenerator.generateKey()

        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, secretKey)
        val iv = cipher.iv
        val encryption = cipher.doFinal(password.toByteArray(Charsets.UTF_8))
        return Base64.encodeToString(iv + encryption, Base64.DEFAULT)
    }

    private fun actualizarInfo(id: Int, nombre: String, username: String, correo: String, contrasena: String, imagenPerfil: String) {
        val userData = JSONObject()
        userData.put("name", nombre)
        userData.put("username", username)
        userData.put("email", correo)
        val hashPassword = hashPassword(contrasena)
        userData.put("password", hashPassword)
        userData.put("fotoUrl", imagenPerfil)
        userData.put("fechaNacimiento", fechaNacimiento)
        userData.put("signInType", "Normal")

        deleteFile("user_data.json")

        val usersArray = JSONArray()
        usersArray.put(userData)
        saveJsonToFile(usersArray)
        println(userData)
        // Generate and save session_id
        val sessionId = generateSessionId()
        saveSessionId(sessionId)
        val intent = Intent(this, VerPerfil::class.java)
        val bundle = Bundle()
        bundle.putInt("id", id)
        bundle.putString("nombre", nombre)
        bundle.putString("username", username)
        bundle.putString("correo", correo)
        bundle.putString("contrasena", hashPassword)
        bundle.putString("fotoUrl", imagenPerfil)
        bundle.putString("fechaNacimiento", fechaNacimiento)
        intent.putExtras(bundle)
        startActivity(intent)
    }

    private fun generateSessionId(): String {
        return java.util.UUID.randomUUID().toString()
    }

    private fun saveSessionId(sessionId: String) {
        val sharedPreferences = getSharedPreferences("user_session", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString("session_id", sessionId)
        editor.apply()
    }

    private fun saveJsonToFile(usersArray: JSONArray) {
        val fileName = "user_data.json"
        try {
            val fileOutputStream: FileOutputStream = openFileOutput(fileName, Context.MODE_PRIVATE)
            fileOutputStream.write(usersArray.toString().toByteArray())
            fileOutputStream.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}