package com.cuatrodivinas.seekandsolve.Logica

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageView
import android.widget.ListView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.cuatrodivinas.seekandsolve.Datos.Desafio
import com.cuatrodivinas.seekandsolve.Datos.Pregunta
import com.cuatrodivinas.seekandsolve.Datos.Recompensa
import com.cuatrodivinas.seekandsolve.R
import com.squareup.picasso.Picasso

class DesafioTerminadoActivity : AppCompatActivity() {
    private lateinit var tituloDesafio: TextView
    private lateinit var imagenDesafio: ImageView

    private lateinit var imagenRecompensa: ImageView
    private lateinit var descripcionRecompensa: TextView

    private lateinit var botonEstadisticas: Button
    private lateinit var intent: Intent
    lateinit var desafio: Desafio

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_desafio_terminado)

        tituloDesafio = findViewById(R.id.tituloDesafio)
        imagenDesafio = findViewById(R.id.imagenDesafio)

        imagenRecompensa = findViewById(R.id.imagenRecompensa)
        descripcionRecompensa = findViewById(R.id.descripcionRecompensa)

        botonEstadisticas = findViewById(R.id.estadisticas)
        desafio = intent.getSerializableExtra("desafio") as Desafio
        intent = Intent(this, EstadisticasCarreraActivity::class.java)
        inicializarElementos()
    }

    private fun inicializarElementos(){
        //Obtener la pregunta random
        val recompensa: Recompensa = Recompensa("","Capitan Cuac Cuac")
        tituloDesafio.text = "Completaste ${desafio.nombre}"
        descripcionRecompensa.text = recompensa.nombre
        Picasso.get()
            .load(recompensa.foto) // URL de la imagen
            .placeholder(R.drawable.cargando) // Imagen de marcador de posición mientras se carga
            .error(R.drawable.error) // Imagen en caso de error
            .into(imagenRecompensa)
    }

    override fun onResume() {
        super.onResume()
        botonEstadisticas.setOnClickListener {
            startActivity(intent)
        }
    }
}