package com.cuatrodivinas.seekandsolve.Logica

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.cuatrodivinas.seekandsolve.R
import com.google.android.gms.maps.MapView

class SeleccionarPuntoActivity : AppCompatActivity() {
    lateinit var punto: MapView
    lateinit var btnAgregar: Button
    lateinit var intent: Intent
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_seleccionar_punto)
        punto = findViewById(R.id.mapView)
        btnAgregar = findViewById(R.id.agregarCheckpoint)
        intent = Intent(this, CrearDesafioActivity::class.java)
    }

    override fun onResume() {
        super.onResume()
        btnAgregar.setOnClickListener {
            startActivity(intent)
        }
    }
}