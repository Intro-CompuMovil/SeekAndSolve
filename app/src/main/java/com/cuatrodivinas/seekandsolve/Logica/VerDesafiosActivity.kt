package com.cuatrodivinas.seekandsolve.Logica

import android.database.Cursor
import android.database.MatrixCursor
import android.os.Bundle
import android.widget.ListView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.cuatrodivinas.seekandsolve.R
import com.google.android.gms.maps.MapView

class VerDesafiosActivity : AppCompatActivity() {
    lateinit var listaDesafios: ListView
    lateinit var mapa: MapView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ver_desafios)
        listaDesafios = findViewById(R.id.listaDesafios)
        mapa = findViewById(R.id.mapView)
        quemarDatos()
    }

    private fun quemarDatos() {
        val columns = arrayOf("_id", "imagen", "nombre")
        val matrixCursor = MatrixCursor(columns)
        matrixCursor.addRow(arrayOf(1, R.drawable.foto_ponti, "Descubre los QR por la Javeriana"))
        matrixCursor.addRow(arrayOf(2, R.drawable.foto_bandera, "Conquista la bandera"))
        matrixCursor.addRow(arrayOf(3, R.drawable.foto_rayo, "NirFix"))
        matrixCursor.addRow(arrayOf(4, R.drawable.foto_walle, "PackMyTrip"))
        matrixCursor.addRow(arrayOf(5, R.drawable.foto_legend_of_celda, "Aliento de lo salvaje"))
        val cursor: Cursor = matrixCursor
        val amigosAdapter = AmigosAdapter(this, cursor, 0)
        listaDesafios.adapter = amigosAdapter
    }
}