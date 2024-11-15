package com.cuatrodivinas.seekandsolve.Logica

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.graphics.Paint
import android.icu.util.Calendar
import android.os.Bundle
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.text.InputType
import android.util.Base64
import android.util.Log
import android.widget.Button
import android.widget.DatePicker
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.cuatrodivinas.seekandsolve.Datos.Data.Companion.PATH_USERS
import com.cuatrodivinas.seekandsolve.Datos.Usuario
import com.cuatrodivinas.seekandsolve.R
import com.cuatrodivinas.seekandsolve.databinding.ActivityRegisterBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import org.json.JSONArray
import org.json.JSONObject
import java.io.FileInputStream
import java.io.FileOutputStream

class RegisterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterBinding
    private val database = FirebaseDatabase.getInstance()
    private lateinit var myRef: DatabaseReference

    companion object {
        private const val RC_SIGN_IN = 9001
    }

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)
        auth = Firebase.auth
        setupPasswordVisibility()
        setupFechaNacimiento()
        setupIniciaSesionButton()
        binding.registrarse.setOnClickListener {
            registerUser()
        }
    }

    private fun setupPasswordVisibility() {
        val passwordLayout: TextInputLayout = findViewById(R.id.contraseniaLayout)
        val passwordEditText: TextInputEditText = findViewById(R.id.contrasenia)
        val confirmPasswordEditText: TextInputEditText = findViewById(R.id.confirmarContrasenia)

        passwordLayout.setEndIconOnClickListener {
            val isPasswordVisible = passwordEditText.inputType == InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            val newInputType = if (isPasswordVisible) {
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            } else {
                InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            }
            passwordEditText.inputType = newInputType
            confirmPasswordEditText.inputType = newInputType
            passwordEditText.setSelection(passwordEditText.text?.length ?: 0)
            confirmPasswordEditText.setSelection(confirmPasswordEditText.text?.length ?: 0)
        }
    }

    private fun setupFechaNacimiento() {
        val fechaNacimientoTextView: TextView = findViewById(R.id.fechaNacimiento)
        fechaNacimientoTextView.setOnClickListener {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)
            val datePickerDialog = DatePickerDialog(this, { _, selectedYear, selectedMonth, selectedDay ->
                val selectedDate = "$selectedDay/${selectedMonth + 1}/$selectedYear"
                fechaNacimientoTextView.text = selectedDate
            }, year, month, day)
            datePickerDialog.show()
        }
    }

    private fun setupIniciaSesionButton() {
        val iniciaSesionButton: Button = findViewById(R.id.iniciaSesionButtonText)
        iniciaSesionButton.paintFlags = iniciaSesionButton.paintFlags or Paint.UNDERLINE_TEXT_FLAG

        iniciaSesionButton.setOnClickListener {
            navigateToLogin()
        }
    }

    private fun navigateToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
    }


    private fun registerUser() {
        if (binding.nombreEditText.text.toString().isNotEmpty() &&
            binding.nombreUsuarioEditText.text.toString().isNotEmpty() &&
            binding.correoEditText.text.toString().isNotEmpty() &&
            binding.contrasenia.text.toString().isNotEmpty() &&
            binding.confirmarContrasenia.text.toString().isNotEmpty() &&
            binding.fechaNacimiento.text.toString().isNotEmpty()) {
            if (binding.contrasenia.text.toString().equals(binding.confirmarContrasenia.text.toString())) {
                crearUsuario()
                navigateToMain()
            } else {
                Log.e("registerActivity", "Las contraseñas no coinciden")
                showToast("Las contraseñas no coinciden")
            }
        } else {
            showToast("Todos los campos deben estar llenos")
        }
    }

    private fun crearUsuario(){
        auth.createUserWithEmailAndPassword(binding.correoEditText.text.toString(), binding.contrasenia.text.toString())
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "createUserWithEmail:onComplete:" + task.isSuccessful)
                    val user = auth.currentUser
                    if (user != null) {
                        escribirUsuarioBD()
                    }
                } else {
                    Toast.makeText(this, "createUserWithEmail:Failure: " + task.exception.toString(),
                        Toast.LENGTH_SHORT).show()
                    task.exception?.message?.let { Log.e(TAG, it) }
                }
            }
    }

    private fun escribirUsuarioBD() {
        val usuario = Usuario(
            binding.nombreEditText.text.toString(),
            binding.nombreUsuarioEditText.text.toString(),
            binding.correoEditText.text.toString(),
            binding.contrasenia.text.toString(),
            "",
            binding.fechaNacimiento.text.toString()
        )
        myRef = database.getReference(PATH_USERS + auth.currentUser!!.uid)
        myRef.setValue(usuario)
        Toast.makeText(
            this@RegisterActivity, "usuario creado con éxito!",
            Toast.LENGTH_SHORT
        ).show()
        saveUserDataToJson(usuario.nombreUsuario, usuario.correo, usuario.password,usuario.imagenUrl, usuario.imagenUrl, "normal", usuario.fechaNacimiento)
    }

    private fun saveUserDataToJson(name: String?, email: String?, contrasena: String, username: String?, photoUrl: String, signInType: String, birthDate: String) {
        // Eliminar el archivo JSON existente
        deleteFile("user_data.json")
        // Crear un nuevo JSONArray y agregar el nuevo usuario
        val userDataJson = readJsonFromFile("user_data.json")
        var usersArray = JSONArray()
        if (userDataJson != null && userDataJson.isNotEmpty()) {
            usersArray = JSONArray(userDataJson)
        }
        val userData = JSONObject()
        userData.put("id", userData.length() + 1)
        userData.put("name", name)
        userData.put("email", email)
        userData.put("password", contrasena)
        userData.put("username", username)
        userData.put("photoUrl", photoUrl)
        userData.put("fechaNacimiento", birthDate)
        userData.put("signInType", signInType)
        usersArray.put(userData)
        // Eliminar el archivo JSON existente
        deleteFile("user_data.json")
        usersArray.put(userData)
        saveJsonToFile(usersArray)
        println(userData)
        // Generate and save session_id
        val sessionId = generateSessionId()
        saveSessionId(sessionId)
        saveJsonToFile(usersArray)
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

    private fun readJsonFromFile(fileName: String): String? {
        return try {
            val fileInputStream: FileInputStream = openFileInput(fileName)
            val bytes = fileInputStream.readBytes()
            fileInputStream.close()
            String(bytes)
        } catch (e: Exception) {
            e.printStackTrace()
            ""
        }
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

    private fun showToast(message: String) {
        val toast = Toast.makeText(this, message, Toast.LENGTH_SHORT)
        val view = toast.view
        view?.setBackgroundColor(ContextCompat.getColor(this, android.R.color.holo_red_dark))
        toast.show()
    }

    private fun navigateToMain() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}