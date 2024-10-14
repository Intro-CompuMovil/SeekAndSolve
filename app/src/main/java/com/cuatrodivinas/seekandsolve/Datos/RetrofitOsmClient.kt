package com.cuatrodivinas.seekandsolve.Datos

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class RetrofitOsmClient {
    companion object{
        private lateinit var retrofit : Retrofit
        private val url = "https://nominatim.openstreetmap.org/"
        private val urlRuta = "http://router.project-osrm.org/"

        fun conectarBackURL() : Retrofit {
            try{
                retrofit = Retrofit.Builder()
                    .baseUrl(url)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
            }catch(e : Exception){
                println("ERROR: ${e.message}")
            }
            return retrofit
        }

        fun urlRuta() : Retrofit {
            try{
                retrofit = Retrofit.Builder()
                    .baseUrl(urlRuta)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
            }catch(e : Exception){
                println("ERROR: ${e.message}")
            }
            return retrofit
        }
    }
}