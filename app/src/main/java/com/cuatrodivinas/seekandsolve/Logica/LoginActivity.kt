package com.cuatrodivinas.seekandsolve.Logica

import android.content.ContentValues.TAG
import android.content.Intent
import android.graphics.Paint
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.cuatrodivinas.seekandsolve.Datos.Data.Companion.auth
import com.cuatrodivinas.seekandsolve.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    override fun onStart() {
        super.onStart()
        setupUI()
    }

    private fun setupUI() {
        // Subrayar el texto de registro
        binding.registrateButtonText.paintFlags =
            binding.registrateButtonText.paintFlags or Paint.UNDERLINE_TEXT_FLAG
        // Listeners de botones
        binding.registrateButtonText.setOnClickListener { startActivity(Intent(this, RegisterActivity::class.java)) }
        binding.iniciarSesionButton.setOnClickListener { validateAndLogin() }
    }

    // Valida el formulario e inicia sesión
    private fun validateAndLogin() {
        // Obtener los valores de los campos sin espacios
        val emailOrUsername = binding.nombreUsuarioOCorreo.text.toString().trim()
        val password = binding.contrasenia.text.toString().trim()

        if (emailOrUsername.isEmpty() || password.isEmpty()) {
            showToast("Todos los campos deben estar llenos")
            return
        }

        if (!isValidEmail(emailOrUsername)) {
            showToast("Por favor, ingresa un correo electrónico válido")
            return
        }

        if (password.length < 6) {
            showToast("La contraseña debe tener al menos 6 caracteres")
            return
        }

        iniciarSesion(emailOrUsername, password)
    }

    private fun isValidEmail(email: String): Boolean {
        val emailPattern = Patterns.EMAIL_ADDRESS
        return emailPattern.matcher(email).matches()
    }

    // Inicia sesión en Firebase Authentication
    private fun iniciarSesion(emailOrUsername: String, password: String){
        auth.signInWithEmailAndPassword(emailOrUsername, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "signInWithEmail:success")
                    navigateToMain()
                } else {
                    Log.w(TAG, "signInWithEmail:failure", task.exception)
                    showToast("Error al iniciar sesión. Revisa las credenciales.")
                }
            }
    }

    private fun navigateToMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}