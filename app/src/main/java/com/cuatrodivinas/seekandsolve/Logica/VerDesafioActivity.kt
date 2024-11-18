package com.cuatrodivinas.seekandsolve.Logica

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.cuatrodivinas.seekandsolve.Datos.Carrera
import com.cuatrodivinas.seekandsolve.Datos.Data.Companion.PATH_DESAFIOS
import com.cuatrodivinas.seekandsolve.Datos.Data.Companion.PATH_IMAGENES
import com.cuatrodivinas.seekandsolve.Datos.Data.Companion.auth
import com.cuatrodivinas.seekandsolve.Datos.Data.Companion.storage
import com.cuatrodivinas.seekandsolve.Datos.Desafio
import com.cuatrodivinas.seekandsolve.Datos.Punto
import com.cuatrodivinas.seekandsolve.Datos.RetrofitOsmClient
import com.cuatrodivinas.seekandsolve.Datos.RetrofitUrls
import com.cuatrodivinas.seekandsolve.R
import com.cuatrodivinas.seekandsolve.databinding.ActivityVerDesafioBinding
import com.google.firebase.crashlytics.buildtools.reloc.com.google.common.reflect.TypeToken
import com.google.firebase.storage.StorageReference
import com.google.gson.Gson
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class VerDesafioActivity : AppCompatActivity() {
    private lateinit var binding: ActivityVerDesafioBinding
    private lateinit var refImg: StorageReference
    private lateinit var desafio: Desafio
    private var retrofitUrls: RetrofitUrls

    init {
        val retrofit = RetrofitOsmClient.conectarBackURL()
        val retrofit2 = RetrofitOsmClient.urlRuta()
        retrofitUrls = retrofit.create(RetrofitUrls::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVerDesafioBinding.inflate(layoutInflater)
        setContentView(binding.root)

        desafio = intent.getSerializableExtra("desafio") as Desafio

        inicializarElementos()
        setupListeners()
    }

    private fun inicializarElementos() {
        binding.tituloDesafio.text = desafio.nombre
        binding.descripcionDesafio.text = desafio.descripcion
        actualizarDireccionesPuntoInicial()
        actualizarDireccionesPuntoFinal()

        refImg = storage.getReference(PATH_DESAFIOS).child("${desafio.id}.jpg")
        refImg.downloadUrl.addOnSuccessListener { uri ->
            Glide.with(this)
                .load(uri)
                .into(binding.imagenDesafio)
        }.addOnFailureListener {
            binding.imagenDesafio.setImageResource(R.drawable.foto_bandera)
        }
    }

    private fun actualizarDireccionesPuntoInicial() {
        val reverseGeocode = retrofitUrls.reverseGeocode(desafio.puntoInicial.latitud, desafio.puntoInicial.longitud)
        reverseGeocode.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful && response.body() != null) {
                    try {
                        val direction = response.body()!!.string()
                        val jsonDirection = JSONObject(direction)
                        binding.puntoInicial.text = "Punto inicial: " + peluquearDireccion(jsonDirection.getString("display_name"))
                    } catch (e: Exception) {
                        Log.e("Geocode", "Error parsing JSON response", e)
                    }
                } else {
                    Log.e("Geocode", "Error en la respuesta: ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Log.e("Geocode", "Error en la llamada: ${t.message}", t)
            }
        })
    }

    private fun peluquearDireccion(direccion: String):String{
        val partes = direccion.split(",")

        // Si hay más de 3 partes, unir las primeras 3 con comas y devolverlo
        return if (partes.size > 3) {
            partes.take(3).joinToString(",")
        } else {
            // Si no hay más de 3 comas, devolver el string original
            direccion
        }
    }

    private fun actualizarDireccionesPuntoFinal() {
        val reverseGeocode = retrofitUrls.reverseGeocode(desafio.puntoFinal.latitud, desafio.puntoFinal.longitud)
        reverseGeocode.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful && response.body() != null) {
                    try {
                        val direction = response.body()!!.string()
                        val jsonDirection = JSONObject(direction)
                        binding.puntoFinal.text = "Punto Final: " + peluquearDireccion(jsonDirection.getString("display_name"))
                    } catch (e: Exception) {
                        Log.e("Geocode", "Error parsing JSON response", e)
                    }
                } else {
                    Log.e("Geocode", "Error en la respuesta: ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Log.e("Geocode", "Error en la llamada: ${t.message}", t)
            }
        })
    }


    private fun setupListeners() {
        binding.iniciarDesafio.setOnClickListener {
            val intent = Intent(this, ConfigurarCarreraActivity::class.java)
            intent.putExtra("desafio", desafio)
            startActivity(intent)
        }

        binding.revisarTrayecto.setOnClickListener {
            val intent = Intent(this, TrayectoDesafioActivity::class.java)
            intent.putExtra("desafio", desafio)
            startActivity(intent)
        }

        binding.backButtonChallenge.setOnClickListener {
            finish()
        }
    }
}