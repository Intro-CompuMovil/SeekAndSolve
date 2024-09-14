package com.cuatrodivinas.seekandsolve.Logica

import android.content.Context
import android.database.Cursor
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CursorAdapter
import android.widget.ImageView
import android.widget.TextView
import com.cuatrodivinas.seekandsolve.R

class AmigosAdapter(context: Context?, c: Cursor?, flags: Int ): CursorAdapter(context, c, flags) {
    private val IMAGEN = 1
    private val NOMBRE = 2

    override fun newView(context: Context?, cursor: Cursor?, parent: ViewGroup?): View {
        return LayoutInflater.from(context)
            .inflate(R.layout.adapter_amigos, parent, false)
    }

    override fun bindView(view: View?, context: Context?, cursor: Cursor?) {
        val imagenAmigo = view?.findViewById<ImageView>(R.id.imagenAmigo)
        val nombreAmigo = view?.findViewById<TextView>(R.id.nombreAmigo)
        val imagen:Int = cursor?.getInt(IMAGEN)!!
        val nombre = cursor?.getString(NOMBRE)
        imagenAmigo!!.setImageResource(imagen)
        nombreAmigo!!.text = nombre
    }
}