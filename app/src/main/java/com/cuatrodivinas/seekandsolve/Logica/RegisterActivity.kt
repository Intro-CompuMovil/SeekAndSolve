package com.cuatrodivinas.seekandsolve.Logica

import android.app.DatePickerDialog
import android.content.ContentValues.TAG
import android.content.Intent
import android.graphics.Paint
import android.icu.util.Calendar
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.cuatrodivinas.seekandsolve.Datos.Data.Companion.PATH_USERS
import com.cuatrodivinas.seekandsolve.Datos.Data.Companion.auth
import com.cuatrodivinas.seekandsolve.Datos.Data.Companion.database
import com.cuatrodivinas.seekandsolve.Datos.Usuario
import com.cuatrodivinas.seekandsolve.databinding.ActivityRegisterBinding
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.database.FirebaseDatabase
import java.text.SimpleDateFormat
import java.util.Locale

class RegisterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)
        // Inicializar Firebase Auth y la database
        auth = Firebase.auth
        database = FirebaseDatabase.getInstance()
    }

    override fun onResume() {
        super.onResume()
        setupUI()
    }

    private fun setupUI() {
        // Mostrar/ocultar la contraseña
        binding.contraseniaLayout.setEndIconOnClickListener {
            togglePasswordVisibility(binding.contrasenia, binding.confirmarContrasenia)
        }
        // DatePickerDialog para seleccionar la fecha de nacimiento
        binding.fechaNacimiento.setOnClickListener {
            showDatePickerDialog()
        }
        // Subrayar el texto de Iniciar sesión y llevar al usuario al Login cuando se haga clic
        binding.iniciaSesionButtonText.apply {
            paintFlags = paintFlags or Paint.UNDERLINE_TEXT_FLAG
            setOnClickListener { navigateToLogin() }
        }
        // Registrar al usuario cuando haga clic en el botón de registro
        binding.registrarse.setOnClickListener { validateAndRegisterUser() }
    }

    // Muestra/oculta la contraseña en los campos de contraseña y confirmar contraseña
    // vararg permite recibir un número variable de argumentos
    private fun togglePasswordVisibility(vararg fields: TextInputEditText) {
        fields.forEach { field ->
            val isPasswordVisible = field.inputType == InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            // Cambiar el tipo de input para mostrar/ocultar la contraseña
            field.inputType = if (isPasswordVisible) {
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            } else {
                InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            }
            // Mueve el cursor al final del texto del input
            field.setSelection(field.text?.length ?: 0)
        }
    }

    // Crea y muestra el selector de fecha
    private fun showDatePickerDialog() {
        val calendar = Calendar.getInstance()
        val datePickerDialog = DatePickerDialog(
            this,
            { _, year, month, day ->
                binding.fechaNacimiento.text = "$day/${month + 1}/$year"
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePickerDialog.show()
    }

    // Valida los campos y registra al usuario
    private fun validateAndRegisterUser() {
        if (!isFormValid()) return

        if (binding.contrasenia.text.toString() == binding.confirmarContrasenia.text.toString()) {
            crearUsuario()
        } else {
            showToast("Las contraseñas no coinciden")
        }
    }

    // Valida que los campos no estén vacíos y que el correo sea válido
    private fun isFormValid(): Boolean {
        return when {
            binding.nombreEditText.text.isNullOrEmpty() -> {
                showToast("El nombre no puede estar vacío")
                false
            }
            binding.nombreUsuarioEditText.text.isNullOrEmpty() -> {
                showToast("El nombre de usuario no puede estar vacío")
                false
            }
            !isEmailValid(binding.correoEditText.text.toString()) -> {
                showToast("El correo no es válido")
                false
            }
            binding.contrasenia.text.isNullOrEmpty() || binding.contrasenia.text.toString().length < 6 -> {
                showToast("La contraseña debe tener al menos 6 caracteres")
                false
            }
            binding.fechaNacimiento.text.isNullOrEmpty() -> {
                showToast("La fecha de nacimiento no puede estar vacía")
                false
            }
            !isAgeValid(binding.fechaNacimiento.text.toString()) -> {
                showToast("Debes tener al menos 10 años para usar SeekAndSolve")
                false
            }
            else -> true
        }
    }

    // Valida que el usuario tenga al menos 10 años
    private fun isAgeValid(fechaNacimiento: String): Boolean {
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val birthDate = dateFormat.parse(fechaNacimiento)
        val calendar = Calendar.getInstance()
        calendar.time = birthDate
        val birthYear = calendar.get(Calendar.YEAR)
        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
        val age = currentYear - birthYear
        return age >= 10
    }

    private fun isEmailValid(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    // Crea un nuevo usuario en Firebase Authentication
    private fun crearUsuario(){
        auth.createUserWithEmailAndPassword(
            binding.correoEditText.text.toString(),
            binding.contrasenia.text.toString()
        ).addOnCompleteListener(this) { task ->
            if (task.isSuccessful) {
                escribirUsuarioBD()
            } else {
                Log.e(TAG, "Error al crear usuario: ${task.exception?.message}")
                showToast("Error al crear usuario: ${task.exception?.message}")
            }
        }
    }

    // Escribe el usuario en el Realtime Database
    private fun escribirUsuarioBD() {
        val usuario = Usuario(
            binding.nombreEditText.text.toString(),
            binding.nombreUsuarioEditText.text.toString(),
            binding.correoEditText.text.toString(),
            binding.contrasenia.text.toString(),
            "",
            binding.fechaNacimiento.text.toString(),
            mutableListOf()
        )
        val myRef = database.getReference("$PATH_USERS/${auth.currentUser!!.uid}")
        myRef.setValue(usuario).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                showToast("Usuario registrado con éxito")
                navigateToMain()
            } else {
                Log.e(TAG, "Error al escribir usuario en BD: ${task.exception?.message}")
                showToast("Error al escribir usuario en BD: ${task.exception?.message}")
            }
        }
    }

    private fun navigateToLogin() {
        startActivity(Intent(this, LoginActivity::class.java))
    }

    private fun navigateToMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}