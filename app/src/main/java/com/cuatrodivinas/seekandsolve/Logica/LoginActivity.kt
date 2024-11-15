package com.cuatrodivinas.seekandsolve.Logica

import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.graphics.Paint
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.cuatrodivinas.seekandsolve.Datos.Usuario
import com.cuatrodivinas.seekandsolve.R
import com.cuatrodivinas.seekandsolve.databinding.ActivityLoginBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import org.json.JSONArray
import org.json.JSONObject
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStream
import java.security.KeyStore
import java.util.UUID
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import kotlin.properties.Delegates

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding

    companion object {
        private const val RC_SIGN_IN = 9001
    }
    private lateinit var auth: FirebaseAuth
    private var signInType: String? = null

    private var externo by Delegates.notNull<Boolean>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        auth = FirebaseAuth.getInstance()
        setupUI()
    }

    private fun setupRegisterButton() {
        binding.registrateButtonText.paintFlags = binding.registrateButtonText.paintFlags or Paint.UNDERLINE_TEXT_FLAG

        binding.registrateButtonText.setOnClickListener {
            navigateToRegister()
        }
    }

    private fun setupUI() {
        val normalLoginButton: Button = findViewById(R.id.iniciarSesionButton)
        setupRegisterButton()


        normalLoginButton.setOnClickListener {

            if (binding.nombreUsuarioOCorreo.text.toString().isEmpty() ||
                binding.contrasenia.text.toString().isEmpty()) {
                showToast("Todos los campos deben estar llenos")
            } else {
                iniciarSesion()
                if (auth.currentUser != null) {
                    navigateToMain(auth.currentUser!!)
                } else {
                    showToast("Usuario no registrado :(")
                }
            }
        }
    }

    private fun iniciarSesion(){
        auth.signInWithEmailAndPassword(binding.nombreUsuarioOCorreo.text.toString(),binding.contrasenia.text.toString())
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "signInWithEmail:success:")
                    val intentMain = Intent(this, MainActivity::class.java)
                    val usuario = auth.currentUser
                    intentMain.putExtra("usuario", usuario)
                    startActivity(intentMain)
                } else {
                    Log.w(TAG, "signInWithEmail:failure", task.exception)
                    Toast.makeText(this, "Authentication failed.",
                        Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun navigateToMain(user: FirebaseUser) {
        val intent = Intent(this, MainActivity::class.java)
        val bundle = Bundle()
        val database = FirebaseDatabase.getInstance()
        val userRef = database.getReference("usuarios").child(user.uid)

        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // Convierte el snapshot a la clase User
                val usuario = snapshot.getValue(Usuario::class.java)

                bundle.putString("id", user.uid)
                bundle.putString("nombre", usuario?.nombre)
                bundle.putString("username", usuario?.nombreUsuario)
                bundle.putString("correo", usuario?.correo)
                bundle.putString("contrasena", usuario?.password)
                bundle.putString("fotoUrl", usuario?.imagenUrl)
                bundle.putString("fechaNacimiento", usuario?.fechaNacimiento)
                bundle.putString("externo", "false")
                intent.putExtras(bundle)
                startActivity(intent)
                finish()
            }
            override fun onCancelled(error: DatabaseError) {
                showToast("No se pudo obtener los datos del usuario")
            }
        })

    }

    private fun navigateToRegister() {
        val intent = Intent(this, RegisterActivity::class.java)
        startActivity(intent)
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}