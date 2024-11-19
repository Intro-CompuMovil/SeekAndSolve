package com.cuatrodivinas.seekandsolve.Logica

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Button
import android.widget.TextView
import com.cuatrodivinas.seekandsolve.Datos.Punto
import com.cuatrodivinas.seekandsolve.R

class CheckpointsAdapter(private val context: Context, val checkpoints: MutableList<Punto>, private  val activity: Activity) : BaseAdapter() {

    override fun getCount(): Int = checkpoints.size

    override fun getItem(position: Int): Punto = checkpoints[position]

    override fun getItemId(position: Int): Long = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.adapter_checkpoints, parent, false)
        val checkpoint = getItem(position)
        val txtCheckpoint = view.findViewById<TextView>(R.id.txtCheckpoint)
        txtCheckpoint.text = "Lat: ${checkpoint.latitud}, Lng: ${checkpoint.longitud}"
        val btnEliminar = view.findViewById<Button>(R.id.btnEliminar)
        val btnEditar = view.findViewById<Button>(R.id.btnEditar)
        btnEliminar.setOnClickListener {
            checkpoints.removeAt(position)
            notifyDataSetChanged()
        }
        btnEditar.setOnClickListener{
            var intent = Intent(context, EditarCheckpointActivity::class.java)
            var bundle = Bundle()
            intent.putExtra("punto", checkpoint)
            intent.putExtra("posicion", position)
            activity.startActivityForResult(intent, 1005)
        }
        return view
    }
}
