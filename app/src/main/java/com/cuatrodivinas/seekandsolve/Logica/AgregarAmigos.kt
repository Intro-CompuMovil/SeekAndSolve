package com.cuatrodivinas.seekandsolve.Logica

import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.database.MatrixCursor
import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.AdapterView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.cuatrodivinas.seekandsolve.databinding.ActivityAgregarAmigosBinding
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream

class AgregarAmigos : AppCompatActivity(), AdapterView.OnItemSelectedListener {
    private lateinit var binding: ActivityAgregarAmigosBinding
    private var lastRegisteredUser: JSONObject? = null
    private var amigosJsonArray: JSONArray? = null
    lateinit var global:Any

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAgregarAmigosBinding.inflate(layoutInflater)
        setContentView(binding.root)
        lastRegisteredUser = getLastRegisteredUser()
        // Obtener la lista de amigos del usuario del intent anterior
        amigosJsonArray = JSONArray(intent.getStringExtra("amigosJsonArray"))
    }

    override fun onResume() {
        super.onResume()
        binding.busquedaEt.setOnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                //Cuando se realice la busqueda se actualizan los resultados en la lista
                actualizarResultados(binding.busquedaEt.text.toString())
            }
            false
        }

        // Poner un listener para cada elemento de la lista de amigos y poder enviar ese amigo a AmigosActivity al presionar el botón invitar amigos
        binding.amigosLv.setOnItemClickListener { parent, view, position, id ->
            val cursor = parent.adapter.getItem(position) as Cursor
            val newFriend = JSONObject()
            newFriend.put("id", cursor.getInt(0))
            newFriend.put("username", cursor.getString(1))
            // Escribir de nuevo el archivo user_friends.json con el nuevo amigo
            amigosJsonArray = agregarAmigoAMiMundo(newFriend)
            // Hacer el intent hacia AmigosActivity, mandando la lista de amigos actualizada
            val intentAmigos = Intent(this, Amigos::class.java)
            intentAmigos.putExtra("amigosJsonArray", amigosJsonArray.toString())
            startActivity(intentAmigos)
            finish()
        }

        binding.backButtonAddFriends.setOnClickListener {
            finish()
        }
    }

    private fun agregarAmigoAMiMundo(newFriend: JSONObject): JSONArray {
        val userFriendsJson = readJsonFromMyWorldFile("user_friends.json")
        var usersFriendsArray = JSONArray()
        if (userFriendsJson != null) {
            usersFriendsArray = JSONArray(userFriendsJson)
        }
        // Eliminar el archivo JSON existente
        deleteFile("user_friends.json")
        // Agregar el nuevo amigo al arreglo de amigos
        usersFriendsArray.put(newFriend)
        // Guardar el nuevo arreglo en el archivo JSON
        saveJsonToMyWorldFile(usersFriendsArray, "user_friends.json")
        return usersFriendsArray
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

    private fun saveJsonToMyWorldFile(usersArray: JSONArray, fileName: String) {
        try {
            val fileOutputStream: FileOutputStream = openFileOutput(fileName, Context.MODE_PRIVATE)
            fileOutputStream.write(usersArray.toString().toByteArray())
            fileOutputStream.close()
        } catch (e: Exception) {
            e.printStackTrace()
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

    private fun actualizarResultados(busqueda: String) {
        val columns = arrayOf("_id", "nombre")
        val matrixCursor = MatrixCursor(columns)
        buscarUsuarios(matrixCursor, busqueda)
        val cursor: Cursor = matrixCursor
        val amigosAdapter = AmigosAdapter(this, cursor, 0)
        binding.amigosLv.adapter = amigosAdapter
    }

    private fun buscarUsuarios(matrixCursor: MatrixCursor, busqueda: String) {
        val json = JSONObject(loadJSONFromAssetExternalWorld("usuarios.json"))
        val usuariosJson = json.getJSONArray("usuarios")
        // Buscar dentro del arreglo todos los usuarios que tengan en alguna parte del username o el correo la busqueda
        for (i in 0 until usuariosJson.length()) {
            val jsonObject = usuariosJson.getJSONObject(i)
            val username = jsonObject.getString("username")
            val correo = jsonObject.getString("correo")
            if (username.contains(busqueda) || correo.contains(busqueda)) {
                val id = jsonObject.getString("id")
                // Si el usuario encontrado no es él mismo, se verifica si ya está en la lista de amigos
                if (lastRegisteredUser!!.getString("id") != id) {
                    var encontrado = false
                    for (j in 0 until amigosJsonArray!!.length()) {
                        val amigo = amigosJsonArray!!.getJSONObject(j)
                        if (amigo.getString("id") == id) {
                            encontrado = true
                            break
                        }
                    }
                    if (!encontrado) {
                        // Si no está en la lista de amigos, se agrega al cursor
                        matrixCursor.addRow(arrayOf(id, jsonObject.getString("username")))
                    }
                }
            }
        }
    }

    private fun loadJSONFromAssetExternalWorld(filename: String): String? {
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