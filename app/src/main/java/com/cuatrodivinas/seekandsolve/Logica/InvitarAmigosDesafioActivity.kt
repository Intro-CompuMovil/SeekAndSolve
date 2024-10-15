package com.cuatrodivinas.seekandsolve.Logica

import android.content.Intent
import android.database.Cursor
import android.database.MatrixCursor
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.cuatrodivinas.seekandsolve.Datos.Desafio
import com.cuatrodivinas.seekandsolve.databinding.ActivityInvitarAmigosDesafioBinding
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStream

class InvitarAmigosDesafioActivity : AppCompatActivity(), AdapterView.OnItemSelectedListener {
    private lateinit var binding: ActivityInvitarAmigosDesafioBinding
    private var lastRegisteredUser: JSONObject? = null
    private var amigosInvitadosJsonArray: JSONArray? = null
    private var amigosNoInvitadosJsonArray: JSONArray = JSONArray()
    private lateinit var desafio: Desafio
    lateinit var global:Any

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityInvitarAmigosDesafioBinding.inflate(layoutInflater)
        setContentView(binding.root)
        lastRegisteredUser = getLastRegisteredUser()

        // Llenar la lista de amigos invitados y no invitados con el intent anterior
        amigosInvitadosJsonArray = JSONArray(intent.getStringExtra("amigosInvitadosJsonArray"))
        amigosNoInvitadosJsonArray = JSONArray(intent.getStringExtra("amigosNoInvitadosJsonArray"))
        desafio = intent.getSerializableExtra("desafio") as Desafio

        // Si las listas están vacías, leemos el archivo user_friends.json para llenar los no invitados
        if (amigosInvitadosJsonArray!!.length() == 0 && amigosNoInvitadosJsonArray!!.length() == 0) {
            val friendsJson = readJsonFromMyWorldFile("user_friends.json")
            if (friendsJson != null) {
                amigosNoInvitadosJsonArray = JSONArray(friendsJson)
            }
        }

        setListAmigos()
    }

    override fun onResume() {
        super.onResume()
        binding.backButtonInvite.setOnClickListener {
            finish()
        }

        binding.invitarAmigos.visibility = View.GONE
        /*binding.invitarAmigos.setOnClickListener {
            val intentConfigurar = Intent(this, ConfigurarCarreraActivity::class.java)
            intentConfigurar.putExtra("bundle", intent.getBundleExtra("bundle"))
            startActivity(intentConfigurar)
            finish()
        }*/

        // Poner un listener para cada elemento de la lista de amigos y poder enviar ese amigo al presionar el botón invitar amigos
        binding.listaAmigos.setOnItemClickListener { parent, view, position, id ->
            val cursor = parent.adapter.getItem(position) as Cursor
            // El nuevo amigo a invitar
            val newFriend = JSONObject()
            newFriend.put("id", cursor.getInt(0))
            newFriend.put("fotoUrl", cursor.getString(1))
            newFriend.put("username", cursor.getString(2))
            // Hacer el intent hacia intentConfigurar, mandando el amigo seleccionado
            val intentConfigurar = Intent(this, ConfigurarCarreraActivity::class.java)
            // Eliminar el amigo de la lista de amigos no invitados
            for (i in 0 until amigosNoInvitadosJsonArray.length()) {
                val jsonObject = amigosNoInvitadosJsonArray.getJSONObject(i)
                if (jsonObject.getInt("id") == newFriend.getInt("id")) {
                    amigosNoInvitadosJsonArray.remove(i)
                    break
                }
            }
            // Agregar el amigo a la lista de amigos invitados
            amigosInvitadosJsonArray!!.put(newFriend)
            // Envía la lista de amigos invitados y no invitados
            intentConfigurar.putExtra("amigosInvitadosJsonArray", amigosInvitadosJsonArray.toString())
            intentConfigurar.putExtra("amigosNoInvitadosJsonArray", amigosNoInvitadosJsonArray.toString())
            // Enviar también el desafío
            intentConfigurar.putExtra("desafio", desafio)

            startActivity(intentConfigurar)
            finish()
        }
    }

    override fun onItemSelected(parent: AdapterView<*>, view: View?, pos: Int, id: Long) {
        // An item is selected. You can retrieve the selected item using
        // parent.getItemAtPosition(pos).
        global = parent.selectedItem.toString()
        Toast.makeText(this, global as String, Toast.LENGTH_SHORT).show()
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {
        // Another interface callback.
    }

    private fun setListAmigos() {
        val columns = arrayOf("_id", "imagen", "nombre")
        val matrixCursor = MatrixCursor(columns)
        // Llenar la lista (visual) de amigos no invitados
        for (i in 0 until amigosNoInvitadosJsonArray.length()) {
            val jsonObject = amigosNoInvitadosJsonArray.getJSONObject(i)
            val id = jsonObject.getInt("id")
            val imagen = jsonObject.getString("fotoUrl")
            val nombre = jsonObject.getString("username")
            matrixCursor.addRow(arrayOf(id, imagen, nombre))
        }
        val cursor: Cursor = matrixCursor
        val amigosAdapter = AmigosAdapter(this, cursor, 0)
        binding.listaAmigos.adapter = amigosAdapter
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

    private fun getLastRegisteredUser(): JSONObject? {
        try {
            val inputStream: InputStream = openFileInput("user_data.json")
            val size: Int = inputStream.available()
            val buffer = ByteArray(size)
            inputStream.read(buffer)
            inputStream.close()
            val json = String(buffer, Charsets.UTF_8)
            val jsonArray = JSONArray(json)
            if (jsonArray.length() > 0) {
                // Devuelve el último usuario registrado
                return jsonArray.getJSONObject(jsonArray.length() - 1)
            }
        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        return null
    }
}