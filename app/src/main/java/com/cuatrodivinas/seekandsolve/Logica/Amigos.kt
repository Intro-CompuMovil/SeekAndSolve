package com.cuatrodivinas.seekandsolve.Logica

import android.content.Intent
import android.database.Cursor
import android.database.MatrixCursor
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.cuatrodivinas.seekandsolve.databinding.ActivityAmigosBinding
import org.json.JSONArray
import java.io.FileInputStream

class Amigos : AppCompatActivity() {
    private lateinit var binding: ActivityAmigosBinding
    private lateinit var amigosJsonArray: JSONArray

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAmigosBinding.inflate(layoutInflater)
        setContentView(binding.root)
        // Si hay algo en el intent, se actualiza la lista de amigos
        // El archivo user_friends.json se actualiza en AgregarAmigos.kt
        if (intent.hasExtra("amigosJsonArray")) {
            amigosJsonArray = JSONArray(intent.getStringExtra("amigosJsonArray"))
        } else {
            // Si no hay nada en el intent, se lee el archivo user_friends.json
            val friendsJson = readJsonFromMyWorldFile("user_friends.json")
            if (friendsJson != null) {
                amigosJsonArray = JSONArray(friendsJson)
            } else {
                amigosJsonArray = JSONArray()
            }
        }
        setListAmigos()
        eventoAgregarAmigos()
        eventoVolver()
    }

    private fun setListAmigos() {
        val columns = arrayOf("_id", "nombre")
        val matrixCursor = MatrixCursor(columns)
        for (i in 0 until amigosJsonArray.length()) {
            val jsonObject = amigosJsonArray.getJSONObject(i)
            val id = jsonObject.getInt("id")
            val nombre = jsonObject.getString("username")
            matrixCursor.addRow(arrayOf(id, nombre))
        }
        val cursor: Cursor = matrixCursor
        val amigosAdapter = AmigosAdapter(this, cursor, 0)
        binding.amigosLv.adapter = amigosAdapter
    }

    private fun readJsonFromMyWorldFile(fileName: String): String? {
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

    private fun eventoAgregarAmigos(){
        binding.agregarAmigosBtn.setOnClickListener {
            var intentAgregarAmigos = Intent(this, AgregarAmigos::class.java)
            intentAgregarAmigos.putExtra("amigosJsonArray", amigosJsonArray.toString())
            startActivity(intentAgregarAmigos)
        }
    }

    private fun eventoVolver(){
        binding.backButtonFriends.setOnClickListener {
            finish()
        }
    }
}