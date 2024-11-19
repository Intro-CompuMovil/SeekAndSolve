package com.cuatrodivinas.seekandsolve.Logica

import android.content.Context
import android.database.Cursor
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CursorAdapter
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.cuatrodivinas.seekandsolve.Datos.Data.Companion.PATH_DESAFIOS
import com.cuatrodivinas.seekandsolve.Datos.Data.Companion.storage
import com.cuatrodivinas.seekandsolve.R
import com.squareup.picasso.Picasso
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

class DesafiosAdapter(context: Context?, c: Cursor?, flags: Int) : CursorAdapter(context, c, flags) {
    private val ID = 1
    private val NOMBRE = 2

    override fun newView(context: Context?, cursor: Cursor?, parent: ViewGroup?): View {
        return LayoutInflater.from(context)
            .inflate(R.layout.adapter_amigos, parent, false)
    }

    override fun bindView(view: View?, context: Context?, cursor: Cursor?) {
        val imagenAmigo = view?.findViewById<ImageView>(R.id.imagenAmigo)
        val nombreAmigo = view?.findViewById<TextView>(R.id.nombreAmigo)

        // Obtener el ID de la imagen (en este caso es una referencia al archivo en Firebase Storage)
        val id: String = cursor?.getString(ID) ?: "" // Si no hay ID, se usa una cadena vacía
        val nombre = cursor?.getString(NOMBRE)

        // Obtener la referencia de la imagen en Firebase Storage
        val refImg: StorageReference = storage.reference.child("${PATH_DESAFIOS}/$id.jpg")
        refImg.downloadUrl.addOnSuccessListener { uri ->
            Glide.with(context!!)
                .load(uri) // Carga la imagen desde la URL
                .into(imagenAmigo!!)
        }.addOnFailureListener { exception ->
            imagenAmigo?.setImageResource(R.drawable.foto_bandera)
        }
        nombreAmigo!!.text = nombre
    }
}