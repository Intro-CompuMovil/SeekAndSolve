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

class EstadisticasAdapter(context: Context?, c: Cursor?, flags: Int ): CursorAdapter(context, c, flags) {
    private val IMAGEN = 1
    private val NOMBRE = 2
    private val DATO = 3

    override fun newView(context: Context?, cursor: Cursor?, parent: ViewGroup?): View {
        return LayoutInflater.from(context)
            .inflate(R.layout.adapter_estadisticas, parent, false)
    }

    override fun bindView(view: View?, context: Context?, cursor: Cursor?) {
        val imagenEstadistica = view?.findViewById<ImageView>(R.id.imagenEstadistica)
        val nombreEstadistica = view?.findViewById<TextView>(R.id.nombreEstadistica)
        val datoEstadistica = view?.findViewById<TextView>(R.id.datoEstadistica)
        val imagen:Int = cursor?.getInt(IMAGEN)!!
        val nombre = cursor?.getString(NOMBRE)
        val dato = cursor?.getString(DATO)
        imagenEstadistica!!.setImageResource(imagen)
        nombreEstadistica!!.text = nombre
        datoEstadistica!!.text = dato
    }
}