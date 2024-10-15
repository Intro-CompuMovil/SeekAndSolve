package com.cuatrodivinas.seekandsolve.Logica

import android.content.Context
import android.content.Intent
import android.graphics.Paint
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.cuatrodivinas.seekandsolve.R
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
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

    companion object {
        private const val RC_SIGN_IN = 9001
    }
    private lateinit var auth: FirebaseAuth
    private var signInType: String? = null

    private var externo by Delegates.notNull<Boolean>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        auth = FirebaseAuth.getInstance()
        setupUI()
    }

    private fun setupRegisterButton() {
        val iniciaSesionButton: Button = findViewById(R.id.registrateButtonText)
        iniciaSesionButton.paintFlags = iniciaSesionButton.paintFlags or Paint.UNDERLINE_TEXT_FLAG

        iniciaSesionButton.setOnClickListener {
            navigateToRegister()
        }
    }

    private fun setupUI() {
        val googleLoginButton: Button = findViewById(R.id.googleLoginButton)
        val normalLoginButton: Button = findViewById(R.id.iniciarSesionButton)
        setupRegisterButton()

        googleLoginButton.setOnClickListener {
            if (signInType == "Normal") {
                showToast("Sesion iniciada en la app. Usa el inicio de sesión normal.")
            } else {
                signIn()
            }
        }

        normalLoginButton.setOnClickListener {
            val usernameOrEmail = findViewById<TextInputEditText>(R.id.nombreUsuarioOCorreo).text.toString()
            val password = findViewById<TextInputEditText>(R.id.contrasenia).text.toString()

            if (usernameOrEmail.isEmpty() || password.isEmpty()) {
                showToast("Todos los campos deben estar llenos")
            } else {
                val user = getUserByUsernameOrEmail(usernameOrEmail)
                if (user != null) {
                    signInType = user.getString("signInType")
                    if (signInType == "Google") {
                        showToast("Sesion iniciada con Google. Usa el inicio de sesión con Google.")
                    } else if (validateNormalLogin(usernameOrEmail, password)) {
                        saveSessionId()
                        navigateToMain(user)
                    } else {
                        showToast("Credenciales incorrectas")
                    }
                } else {
                    showToast("Usuario no registrado")
                }
            }
        }
    }

    private fun getUserByUsernameOrEmail(usernameOrEmail: String): JSONObject? {
        val usersArray = readUserDataFromJson() ?: return null
        for (i in 0 until usersArray.length()) {
            val user = usersArray.getJSONObject(i)
            val storedUsername = user.getString("username")
            val storedEmail = user.getString("email")
            if (usernameOrEmail == storedUsername || usernameOrEmail == storedEmail) {
                externo = true
                return user
            }
        }
        val json = JSONObject(loadJSONFromAsset("usuarios.json"))
        val usuariosJson = json.getJSONArray("usuarios")
        for (i in 0 until usuariosJson.length()) {
            val user = usuariosJson.getJSONObject(i)
            val storedUsername = user.getString("username")
            val storedEmail = user.getString("correo")
            if (usernameOrEmail == storedUsername || usernameOrEmail == storedEmail) {
                externo = false
                return user
            }
        }
        return null
    }

    private fun validateNormalLogin(usernameOrEmail: String, password: String): Boolean {
        val usersArray = readUserDataFromJson() ?: return false
        for (i in 0 until usersArray.length()) {
            val user = usersArray.getJSONObject(i)
            val storedUsername = user.getString("username")
            val storedEmail = user.getString("email")
            val storedPasswordHash = user.getString("password")
            if ((usernameOrEmail == storedUsername || usernameOrEmail == storedEmail) && verifyPassword(password, storedPasswordHash)) {
                signInType = user.getString("signInType")
                return true
            }
        }
        val json = JSONObject(loadJSONFromAsset("usuarios.json"))
        val usuariosJson = json.getJSONArray("usuarios")
        for (i in 0 until usuariosJson.length()) {
            val user = usuariosJson.getJSONObject(i)
            val storedUsername = user.getString("username")
            val storedEmail = user.getString("correo")
            val storedPasswordHash = user.getString("contrasena")
            if ((usernameOrEmail == storedUsername || usernameOrEmail == storedEmail) && verifyPassword(password, storedPasswordHash)) {
                signInType = user.getString("signInType")
                return true
            }
        }

        return false
    }

    private fun loadJSONFromAsset(filename: String): String? {
        var json: String? = null
        try {
            val isStream: InputStream = assets.open(filename)
            val size:Int = isStream.available()
            val buffer = ByteArray(size)
            isStream.read(buffer)
            isStream.close()
            json = String(buffer, Charsets.UTF_8)
        } catch (ex: IOException) {
            ex.printStackTrace()
            return null
        }
        return json
    }

    private fun verifyPassword(password: String, storedPasswordHash: String): Boolean {
        if(password == storedPasswordHash){
            return true
        }
        val keyStore = KeyStore.getInstance("AndroidKeyStore")
        keyStore.load(null)
        val secretKey = keyStore.getKey("MyKeyAlias", null) as SecretKey

        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        val ivAndCipherText = Base64.decode(storedPasswordHash, Base64.DEFAULT)
        val iv = ivAndCipherText.copyOfRange(0, 12)
        val cipherText = ivAndCipherText.copyOfRange(12, ivAndCipherText.size)
        val spec = GCMParameterSpec(128, iv)
        cipher.init(Cipher.DECRYPT_MODE, secretKey, spec)
        val decryptedPassword = String(cipher.doFinal(cipherText), Charsets.UTF_8)
        return password == decryptedPassword
    }

    private fun readUserDataFromJson(): JSONArray? {
        return try {
            val fileInputStream: FileInputStream = openFileInput("user_data.json")
            val bytes = fileInputStream.readBytes()
            fileInputStream.close()
            JSONArray(String(bytes))
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun navigateToMain(user: JSONObject? = null) {
        val intent = Intent(this, MainActivity::class.java)
        val bundle = Bundle()
        if(externo) {
            bundle.putInt("id", user?.getInt("id") ?: -1)
            bundle.putString("nombre", user?.getString("name") ?: "")
            bundle.putString("username", user?.getString("username") ?: "")
            bundle.putString("correo", user?.getString("email") ?: "")
            bundle.putString("contrasena", user?.getString("password") ?: "")
            bundle.putString("fotoUrl", user?.getString("photoUrl") ?: "")
            bundle.putString("fechaNacimiento", user?.getString("fechaNacimiento") ?: "")
            intent.putExtras(bundle)
        }else{
            bundle.putInt("id", user?.getInt("id") ?: -1)
            bundle.putString("nombre", user?.getString("nombre") ?: "")
            bundle.putString("username", user?.getString("username") ?: "")
            bundle.putString("correo", user?.getString("correo") ?: "")
            bundle.putString("contrasena", user?.getString("contrasena") ?: "")
            bundle.putString("fotoUrl", user?.getString("fotoUrl") ?: "")
            bundle.putString("fechaNacimiento", user?.getString("fechaNacimiento") ?: "")
            intent.putExtras(bundle)
        }
        startActivity(intent)
        finish()
    }

    private fun navigateToRegister() {
        val intent = Intent(this, RegisterActivity::class.java)
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
                Log.w("LoginActivity", "Google sign in failed", e)
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
                    if (username != null && isUsernameRegistered(username)) {
                        Log.d("LoginActivity", "signInWithCredential:success, user: ${user.displayName}")
                        saveSessionId()
                        navigateToMain(searchUserByUsername(username))
                    } else {
                        showToast("Usuario no registrado")
                        signOutFromGoogle()
                    }
                    Log.d("LoginActivity", "signInWithCredential:success, user: ${user?.displayName}")
                } else {
                    Log.w("LoginActivity", "signInWithCredential:failure", task.exception)
                }
            }
    }

    private fun searchUserByUsername(username: String): JSONObject? {
        val usersArray = readUserDataFromJson() ?: return null
        for (i in 0 until usersArray.length()) {
            val user = usersArray.getJSONObject(i)
            if (user.getString("username") == username) {
                return user
            }
        }
        return null
    }

    private fun signOutFromGoogle() {
        val googleSignInClient = GoogleSignIn.getClient(this, GoogleSignInOptions.DEFAULT_SIGN_IN)
        googleSignInClient.signOut().addOnCompleteListener(this) {}
    }

    private fun isUsernameRegistered(username: String): Boolean {
        val usersArray = readUserDataFromJson() ?: return false
        for (i in 0 until usersArray.length()) {
            val user = usersArray.getJSONObject(i)
            if (user.getString("username") == username) {
                signInType = user.getString("signInType")
                return true
            }
        }
        return false
    }

    private fun saveSessionId() {
        val sessionId = UUID.randomUUID().toString()
        val sharedPreferences = getSharedPreferences("user_session", Context.MODE_PRIVATE)
        with(sharedPreferences.edit()) {
            putString("session_id", sessionId)
            apply()
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}