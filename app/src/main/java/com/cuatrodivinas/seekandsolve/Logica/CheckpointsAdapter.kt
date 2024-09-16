package com.cuatrodivinas.seekandsolve.Logica

import android.content.Context
import android.database.Cursor
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CursorAdapter
import android.widget.ImageView
import android.widget.TextView
import com.cuatrodivinas.seekandsolve.R

class CheckpointsAdapter(context: Context?, c: Cursor?, flags: Int ): CursorAdapter(context, c, flags) {
    private val TXTCHECKPOINT = 1

    override fun newView(context: Context?, cursor: Cursor?, parent: ViewGroup?): View {
        return LayoutInflater.from(context)
            .inflate(R.layout.adapter_checkpoints, parent, false)
    }

    override fun bindView(view: View?, context: Context?, cursor: Cursor?) {
        val txtCheckpoint = view?.findViewById<TextView>(R.id.txtCheckpoint)
        val texto = cursor?.getString(TXTCHECKPOINT)
        txtCheckpoint!!.text = texto
    }
}