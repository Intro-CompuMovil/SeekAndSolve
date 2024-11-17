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
import com.cuatrodivinas.seekandsolve.R
import com.bumptech.glide.Glide
import com.cuatrodivinas.seekandsolve.Datos.Data.Companion.PATH_IMAGENES
import com.cuatrodivinas.seekandsolve.Datos.Data.Companion.storage
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

class AmigosAdapter(context: Context?, c: Cursor?, flags: Int): CursorAdapter(context, c, flags) {
    private val ID = 0
    private val NOMBRE = 1

    override fun newView(context: Context?, cursor: Cursor?, parent: ViewGroup?): View {
        return LayoutInflater.from(context)
            .inflate(R.layout.adapter_amigos, parent, false)
    }

    override fun bindView(view: View?, context: Context?, cursor: Cursor?) {
        val imagenAmigo = view?.findViewById<ImageView>(R.id.imagenAmigo)
        val nombreAmigo = view?.findViewById<TextView>(R.id.nombreAmigo)

        val idAmigo = cursor?.getString(ID) // Obtén el ID del amigo desde el cursor
        val nombre = cursor?.getString(NOMBRE)

        val refImg = storage.reference.child("${PATH_IMAGENES}/$idAmigo.jpg")
        refImg.downloadUrl.addOnSuccessListener { uri ->
            Glide.with(context!!)
                .load(uri) // Carga la imagen desde la URL
                .into(imagenAmigo!!)
        }.addOnFailureListener { exception ->
            imagenAmigo?.imageTintList = ContextCompat.getColorStateList(context!!, R.color.primaryColor)
        }

        nombreAmigo!!.text = nombre
    }
}
