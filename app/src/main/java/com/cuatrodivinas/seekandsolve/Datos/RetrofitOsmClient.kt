package com.cuatrodivinas.seekandsolve.Datos

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class RetrofitOsmClient {
    companion object{
        private lateinit var retrofit : Retrofit
        private val url = "https://nominatim.openstreetmap.org/"
        private val urlRuta = "http://router.project-osrm.org/"

        val cliente = OkHttpClient.Builder()
            .addInterceptor { chain ->
                val request = chain.request().newBuilder()
                    .header("User-Agent", "SeekAndSolve/1.0 (edgarjuliangonzalezsierra167@gmail.com)")
                    .build()
                chain.proceed(request)
            }
            .build()

        fun conectarBackURL() : Retrofit {
            try{
                retrofit = Retrofit.Builder()
                    .baseUrl(url)
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(cliente)
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
                    .client(cliente)
                    .build()
            }catch(e : Exception){
                println("ERROR: ${e.message}")
            }
            return retrofit
        }
    }
}