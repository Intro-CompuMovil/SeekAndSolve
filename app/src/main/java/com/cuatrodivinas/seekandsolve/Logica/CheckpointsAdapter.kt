package com.cuatrodivinas.seekandsolve.Logica

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import com.cuatrodivinas.seekandsolve.Datos.Punto
import com.cuatrodivinas.seekandsolve.R

class CheckpointsAdapter(private val context: Context, private val checkpoints: MutableList<Punto>) : BaseAdapter() {

    override fun getCount(): Int = checkpoints.size

    override fun getItem(position: Int): Punto = checkpoints[position]

    override fun getItemId(position: Int): Long = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.adapter_checkpoints, parent, false)
        val checkpoint = getItem(position)
        val txtCheckpoint = view.findViewById<TextView>(R.id.txtCheckpoint)
        txtCheckpoint.text = "Lat: ${checkpoint.latitud}, Lng: ${checkpoint.longitud}"
        return view
    }
}
