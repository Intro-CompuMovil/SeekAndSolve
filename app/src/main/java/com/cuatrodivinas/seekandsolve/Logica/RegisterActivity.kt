package com.cuatrodivinas.seekandsolve.Logica

import android.app.DatePickerDialog
import android.content.Intent
import android.graphics.Paint
import android.icu.util.Calendar
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
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
        setupPasswordVisibility()
        setupFechaNacimiento()
        setupIniciaSesionButton()

        googleLoginButton.setOnClickListener {
            signIn()
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
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
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
                    Log.d("registerActivity", "signInWithCredential:success, user: ${user?.displayName}")
                } else {
                    Log.w("registerActivity", "signInWithCredential:failure", task.exception)
                }
            }
    }
}