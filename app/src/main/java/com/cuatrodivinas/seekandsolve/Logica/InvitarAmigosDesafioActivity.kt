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
    private var amigosInvitados: HashMap<String, String> = hashMapOf()
    private var amigosNoInvitados: HashMap<String, String> = hashMapOf()
    lateinit var global: Any

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityInvitarAmigosDesafioBinding.inflate(layoutInflater)
        setContentView(binding.root)

        amigosInvitados = intent.getSerializableExtra("amigosInvitados") as HashMap<String, String>
        amigosNoInvitados = intent.getSerializableExtra("amigosNoInvitados") as HashMap<String, String>

        setListAmigos()
    }

    override fun onResume() {
        super.onResume()
        binding.backButtonInvite.setOnClickListener {
            actualizarAmigos()
        }

        binding.listaAmigos.setOnItemClickListener { parent, view, position, id ->
            val cursor = parent.adapter.getItem(position) as Cursor
            val friendId = cursor.getString(cursor.getColumnIndexOrThrow("idUser"))
            val friendName = cursor.getString(cursor.getColumnIndexOrThrow("nombre"))

            amigosNoInvitados.remove(friendId)
            amigosInvitados[friendId] = friendName

            setListAmigos()
        }

        binding.invitarAmigos.setOnClickListener {
            actualizarAmigos()
        }
    }

    override fun onItemSelected(parent: AdapterView<*>, view: View?, pos: Int, id: Long) {
        global = parent.selectedItem.toString()
        Toast.makeText(this, global as String, Toast.LENGTH_SHORT).show()
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {
        // Another interface callback.
    }

    private fun setListAmigos() {
        val columns = arrayOf("_id", "idUser", "nombre")
        val matrixCursor = MatrixCursor(columns)
        var idCounter = 1L
        amigosNoInvitados.forEach { (id, name) ->
            matrixCursor.addRow(arrayOf(idCounter, id, name))
            idCounter++
        }
        val cursor: Cursor = matrixCursor
        val amigosAdapter = AmigosAdapter(this, cursor, 0)
        binding.listaAmigos.adapter = amigosAdapter
    }

    private fun actualizarAmigos() {
        val resultIntent = Intent()
        resultIntent.putExtra("amigosInvitados", amigosInvitados)
        resultIntent.putExtra("amigosNoInvitados", amigosNoInvitados)
        setResult(RESULT_OK, resultIntent)
        finish()
    }
}