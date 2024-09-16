package com.cuatrodivinas.seekandsolve.Logica

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
    }
}