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

class RecompensasAdapter(context: Context?, c: Cursor?, flags: Int ): CursorAdapter(context, c, flags) {
    private val IMAGEN = 1
    private val NOMBRE = 2
    private val LUGAR = 3
    private val FECHA = 4

    override fun newView(context: Context?, cursor: Cursor?, parent: ViewGroup?): View {
        return LayoutInflater.from(context)
            .inflate(R.layout.adapter_recompensas, parent, false)
    }

    override fun bindView(view: View?, context: Context?, cursor: Cursor?) {
        val imgRecompensa = view?.findViewById<ImageView>(R.id.imagenRecompensa)
        val nombreRecompensa = view?.findViewById<TextView>(R.id.nombreRecompensa)
        val lugarRecompensa = view?.findViewById<TextView>(R.id.lugarRecompensa)
        val fechaRecompensa = view?.findViewById<TextView>(R.id.fechaRecompensa)
        val imagen:Int = cursor?.getInt(IMAGEN)!!
        val nombre = cursor?.getString(NOMBRE)
        val lugar = cursor?.getString(LUGAR)
        val fecha = cursor?.getString(FECHA)
        imgRecompensa!!.setImageResource(imagen)
        nombreRecompensa!!.text = nombre
        lugarRecompensa!!.text = lugar
        fechaRecompensa!!.text = fecha
    }
}