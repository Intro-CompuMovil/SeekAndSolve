package com.cuatrodivinas.seekandsolve.Logica

import android.database.Cursor
import android.database.MatrixCursor
import android.os.Bundle
import android.util.Log
import android.view.animation.AnimationUtils
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.cuatrodivinas.seekandsolve.Datos.Data.Companion.PATH_RECOMPENSAS
import com.cuatrodivinas.seekandsolve.Datos.Data.Companion.PATH_USERS
import com.cuatrodivinas.seekandsolve.Datos.Data.Companion.auth
import com.cuatrodivinas.seekandsolve.Datos.Data.Companion.database
import com.cuatrodivinas.seekandsolve.Datos.InfoRecompensa
import com.cuatrodivinas.seekandsolve.R
import com.cuatrodivinas.seekandsolve.databinding.ActivityRecompensasCarreraBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

class RecompensasCarrera : AppCompatActivity() {
    private lateinit var binding: ActivityRecompensasCarreraBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRecompensasCarreraBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Si se envió un uid en el intent, usarlo, sino usar el uid del usuario autenticado
        val uid = intent.getStringExtra("uid") ?: auth.currentUser?.uid ?: ""
        cargarRecompensas(uid)

        eventoVolver()
        findViewById<ImageView>(R.id.medalImageView).startAnimation(AnimationUtils.loadAnimation(this, R.anim.blink))
    }

    private fun cargarRecompensas(uid: String) {
        val recompensasRef = database.reference.child(PATH_USERS).child(uid).child(PATH_RECOMPENSAS)

        recompensasRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val columns = arrayOf("_id", "nombre", "lugar", "fecha")
                val matrixCursor = MatrixCursor(columns)

                for (recompensaSnapshot in dataSnapshot.children) {
                    val recompensa = recompensaSnapshot.getValue(InfoRecompensa::class.java)
                    recompensa?.let {
                        matrixCursor.addRow(arrayOf(it.idRecompensa, it.nombreRecompensa, it.nombreDesafio, it.fecha))
                    }
                }

                val cursor: Cursor = matrixCursor
                val recompensasAdapter = RecompensasAdapter(this@RecompensasCarrera, cursor, 0)
                binding.recompensasLv.adapter = recompensasAdapter
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e("Firebase", "Error al leer las recompensas", databaseError.toException())
            }
        })
    }

    private fun eventoVolver(){
        binding.backButtonRewards.setOnClickListener {
            finish()
        }
    }
}