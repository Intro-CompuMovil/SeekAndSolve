package com.cuatrodivinas.seekandsolve.Logica

import android.app.AlertDialog
import android.app.DatePickerDialog
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
import com.cuatrodivinas.seekandsolve.R
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import org.json.JSONArray
import org.json.JSONObject
import java.io.FileInputStream
import java.io.FileOutputStream
import javax.crypto.Cipher
import javax.crypto.KeyGenerator

class RegisterActivity : AppCompatActivity() {

    companion object {
        private const val RC_SIGN_IN = 9001
    }

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
        auth = Firebase.auth
        val googleLoginButton: Button = findViewById(R.id.googleLoginButton)
        val registerButton: Button = findViewById(R.id.registrarse)
        setupPasswordVisibility()
        setupFechaNacimiento()
        setupIniciaSesionButton()
        googleLoginButton.setOnClickListener {
            signIn()
        }
        registerButton.setOnClickListener {
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

    private fun signIn() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        val googleSignInClient = GoogleSignIn.getClient(this, gso)
        val account = GoogleSignIn.getLastSignedInAccount(this)

        if (account != null) {
            // El usuario ya ha iniciado sesión previamente, proceder con la autenticación
            firebaseAuthWithGoogle(account.idToken!!)
        } else {
            // El usuario no ha iniciado sesión previamente, iniciar el flujo de inicio de sesión
            val signInIntent = googleSignInClient.signInIntent
            startActivityForResult(signInIntent, RC_SIGN_IN)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                Log.w("registerActivity", "Google sign in failed", e)
            }
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    val username = user?.displayName
                    val email = user?.email
                    val photoUrl = user?.photoUrl.toString() // Obtener la URL de la foto de perfil

                    if (isEmailRegistered(email)) {
                        showToast("Correo ya registrado, por favor inicie sesión")
                        navigateToLogin()
                    } else {
                        // Mostrar un cuadro de diálogo para editar el nombre y seleccionar la fecha de nacimiento
                        val editText = EditText(this)
                        editText.setText(username)
                        val birthDateEditText = EditText(this)
                        birthDateEditText.hint = "Fecha de nacimiento"
                        birthDateEditText.isFocusable = false
                        birthDateEditText.setOnClickListener {
                            val calendar = Calendar.getInstance()
                            val year = calendar.get(Calendar.YEAR)
                            val month = calendar.get(Calendar.MONTH)
                            val day = calendar.get(Calendar.DAY_OF_MONTH)
                            val datePickerDialog = DatePickerDialog(this, { _, selectedYear, selectedMonth, selectedDay ->
                                val selectedDate = "$selectedDay/${selectedMonth + 1}/$selectedYear"
                                birthDateEditText.setText(selectedDate)
                            }, year, month, day)
                            datePickerDialog.show()
                        }
                        val layout = LinearLayout(this)
                        layout.orientation = LinearLayout.VERTICAL
                        layout.addView(editText)
                        layout.addView(birthDateEditText)
                        // Agregar un margen alrededor de los campos de texto
                        val margin = 20
                        layout.setPadding(margin, margin, margin, margin)
                        AlertDialog.Builder(this)
                            .setTitle("Confirmar nombre y fecha de nacimiento")
                            .setView(layout)
                            .setPositiveButton("Confirmar") { _, _ ->
                                val editedName = editText.text.toString()
                                val birthDate = birthDateEditText.text.toString()
                                saveUserDataToJson(editedName, email, username, photoUrl, "Google", birthDate)
                                // Generate and save session_id
                                val sessionId = generateSessionId()
                                saveSessionId(sessionId)
                                // Redirigir a MainActivity
                                navigateToMain()
                            }
                            .setNegativeButton("Cancelar", null)
                            .show()
                    }
                } else {
                    Log.w("registerActivity", "signInWithCredential:failure", task.exception)
                }
            }
    }

    private fun isEmailRegistered(email: String?): Boolean {
        val userDataJson = readJsonFromFile("user_data.json")
        if (userDataJson != null) {
            val usersArray = JSONArray(userDataJson)
            for (i in 0 until usersArray.length()) {
                val user = usersArray.getJSONObject(i)
                if (user.getString("email") == email) {
                    return true
                }
            }
        }
        return false
    }

    private fun readJsonFromFile(fileName: String): String? {
        return try {
            val fileInputStream: FileInputStream = openFileInput(fileName)
            val bytes = fileInputStream.readBytes()
            fileInputStream.close()
            String(bytes)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
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

    private fun saveSessionId(sessionId: String) {
        val sharedPreferences = getSharedPreferences("user_session", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString("session_id", sessionId)
        editor.apply()
    }

    private fun generateSessionId(): String {
        return java.util.UUID.randomUUID().toString()
    }

    private fun registerUser() {
        val nameEditText: TextInputEditText = findViewById(R.id.nombreEditText)
        val usernameEditText: TextInputEditText = findViewById(R.id.nombreUsuarioEditText)
        val emailEditText: TextInputEditText = findViewById(R.id.correoEditText)
        val passwordEditText: TextInputEditText = findViewById(R.id.contrasenia)
        val confirmPasswordEditText: TextInputEditText = findViewById(R.id.confirmarContrasenia)
        val fechaNacimientoTextView: TextView = findViewById(R.id.fechaNacimiento)

        val name = nameEditText.text.toString()
        val username = usernameEditText.text.toString()
        val email = emailEditText.text.toString()
        val password = passwordEditText.text.toString()
        val confirmPassword = confirmPasswordEditText.text.toString()
        val fechaNacimiento = fechaNacimientoTextView.text.toString()

        if (name.isNotEmpty() && username.isNotEmpty() && email.isNotEmpty() && password.isNotEmpty() && confirmPassword.isNotEmpty() && fechaNacimiento.isNotEmpty()) {
            if (password == confirmPassword) {
                val hashedPassword = hashPassword(password)
                if (isEmailRegistered(email)) {
                    showToast("Correo ya registrado, por favor inicie sesión")
                    navigateToLogin()
                } else {
                    val userData = JSONObject()
                    userData.put("id", userData.length() + 1)
                    userData.put("name", name)
                    userData.put("username", username)
                    userData.put("email", email)
                    userData.put("password", hashedPassword)
                    userData.put("fechaNacimiento", fechaNacimiento)
                    userData.put("signInType", "Normal")

                    val userDataJson = readJsonFromFile("user_data.json")
                    var usersArray = JSONArray()
                    if (userDataJson != null) {
                        usersArray = JSONArray(userDataJson)
                    }
                    // Eliminar el archivo JSON existente
                    deleteFile("user_data.json")
                    usersArray.put(userData)

                    saveJsonToFile(usersArray)
                    println(userData)
                    // Generate and save session_id
                    val sessionId = generateSessionId()
                    saveSessionId(sessionId)
                    // Redirigir a MainActivity
                    navigateToMain()
                }
            } else {
                Log.e("registerActivity", "Las contraseñas no coinciden")
                showToast("Las contraseñas no coinciden")
            }
        } else {
            showToast("Todos los campos deben estar llenos")
        }
    }

    private fun showToast(message: String) {
        val toast = Toast.makeText(this, message, Toast.LENGTH_SHORT)
        val view = toast.view
        view?.setBackgroundColor(ContextCompat.getColor(this, android.R.color.holo_red_dark))
        toast.show()
    }

    private fun saveUserDataToJson(name: String?, email: String?, username: String?, photoUrl: String, signInType: String, birthDate: String) {
        // Eliminar el archivo JSON existente
        deleteFile("user_data.json")
        // Crear un nuevo JSONArray y agregar el nuevo usuario
        val usersArray = JSONArray()
        val userData = JSONObject()
        userData.put("name", name)
        userData.put("email", email)
        userData.put("username", username)
        userData.put("photoUrl", photoUrl)
        userData.put("fechaNacimiento", birthDate)
        userData.put("signInType", signInType)
        usersArray.put(userData)

        saveJsonToFile(usersArray)
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

    private fun navigateToMain() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}