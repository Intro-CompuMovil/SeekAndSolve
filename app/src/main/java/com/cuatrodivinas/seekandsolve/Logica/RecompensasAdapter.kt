package com.cuatrodivinas.seekandsolve.Logica

import android.content.Context
import android.database.Cursor
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CursorAdapter
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.cuatrodivinas.seekandsolve.Datos.Data.Companion.PATH_IMAGENES
import com.cuatrodivinas.seekandsolve.Datos.Data.Companion.PATH_RECOMPENSAS
import com.cuatrodivinas.seekandsolve.Datos.Data.Companion.auth
import com.cuatrodivinas.seekandsolve.Datos.Data.Companion.storage
import com.cuatrodivinas.seekandsolve.R

class RecompensasAdapter(context: Context?, c: Cursor?, flags: Int) : CursorAdapter(context, c, flags) {
    private val _ID = 0
    private val NOMBRE = 1
    private val LUGAR = 2
    private val FECHA = 3

    override fun newView(context: Context?, cursor: Cursor?, parent: ViewGroup?): View {
        return LayoutInflater.from(context)
            .inflate(R.layout.adapter_recompensas, parent, false)
    }

    override fun bindView(view: View?, context: Context?, cursor: Cursor?) {
        val imgRecompensa = view?.findViewById<ImageView>(R.id.imagenRecompensa)
        val nombreRecompensa = view?.findViewById<TextView>(R.id.nombreRecompensa)
        val lugarRecompensa = view?.findViewById<TextView>(R.id.lugarRecompensa)
        val fechaRecompensa = view?.findViewById<TextView>(R.id.fechaRecompensa)

        val id = cursor?.getInt(_ID)
        val nombre = cursor?.getString(NOMBRE)
        val lugar = cursor?.getString(LUGAR)
        val fecha = cursor?.getString(FECHA)

        // Establecer la imagen con Glide
        cargarImagen(id, imgRecompensa)

        nombreRecompensa!!.text = nombre
        lugarRecompensa!!.text = lugar
        fechaRecompensa!!.text = fecha
    }

    private fun cargarImagen(id: Int?, imgRecompensa: ImageView?) {
        if (id != null) {
            val refImg = storage.getReference(PATH_RECOMPENSAS).child("${id}.jpg")

            refImg.downloadUrl.addOnSuccessListener { uri ->
                if (imgRecompensa != null) {
                    Glide.with(imgRecompensa.context)
                        .load(uri) // Carga la imagen desde la URL
                        .into(imgRecompensa)
                } // Establece la imagen en el ImageView
            }.addOnFailureListener { exception ->
                imgRecompensa?.imageTintList =
                    imgRecompensa?.context?.let { ContextCompat.getColorStateList(it, R.color.primaryColor) }
            }
        }
    }
}
