package com.cuatrodivinas.seekandsolve.Logica

import android.content.Intent
import android.database.Cursor
import android.database.MatrixCursor
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.cuatrodivinas.seekandsolve.Datos.Data.Companion.PATH_USERS
import com.cuatrodivinas.seekandsolve.Datos.Data.Companion.auth
import com.cuatrodivinas.seekandsolve.Datos.Data.Companion.database
import com.cuatrodivinas.seekandsolve.Datos.Usuario
import com.cuatrodivinas.seekandsolve.databinding.ActivityAmigosBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import org.json.JSONArray
import java.io.FileInputStream

class Amigos : AppCompatActivity() {
    private lateinit var binding: ActivityAmigosBinding
    private var amigos: MutableMap<String, String> = mutableMapOf()
    private var amigosArray: MutableList<String> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAmigosBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setListAmigos()
        eventoAgregarAmigos()
        eventoVolver()
    }

    private fun setListAmigos() {
        //obtener amigos del usuario actual de firebase
        val refAmigos = database.getReference("usuarios/${auth.currentUser?.uid}/amigos/")
        refAmigos.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val amigosUids = snapshot.children.mapNotNull { it.value as? String }
                fetchAmigosInfo(amigosUids)
            }
            override fun onCancelled(error: DatabaseError) {
                // Handle error
            }
        })
    }

    private fun fetchAmigosInfo(amigosUids: List<String>) {
        amigosUids.forEach { uid ->
            val amigoRef = database.getReference("$PATH_USERS/$uid")
            amigoRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val amigo = snapshot.getValue(Usuario::class.java)
                    amigo?.let {
                        amigos.put(uid, amigo.nombreUsuario)
                        //insertar uid en amigosArray
                        amigosArray.add(uid)
                        val columns = arrayOf("_id", "idUser", "nombre")
                        val matrixCursor = MatrixCursor(columns)
                        var idCounter = 1L
                        amigos.forEach { (id, name) ->
                            matrixCursor.addRow(arrayOf(idCounter, id, name))
                            idCounter++
                        }
                        val cursor: Cursor = matrixCursor
                        val amigosAdapter = AmigosAdapter(this@Amigos, cursor, 0)
                        binding.amigosLv.adapter = amigosAdapter
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    // Handle error
                }
            })
        }
    }

    private fun eventoAgregarAmigos(){
        binding.agregarAmigosBtn.setOnClickListener {
            val intentAgregarAmigos = Intent(this, AgregarAmigos::class.java)
            intentAgregarAmigos.putStringArrayListExtra("amigos", ArrayList(amigosArray))
            startActivity(intentAgregarAmigos)
        }
    }

    private fun eventoVolver(){
        binding.backButtonFriends.setOnClickListener {
            finish()
        }
    }
}