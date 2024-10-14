package com.cuatrodivinas.seekandsolve.Datos

import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface RetrofitUrls {
    @GET("search")
    fun geocode(
        @Query("q") address: String,
        @Query("format") format: String = "json"
    ): Call<ResponseBody>

    @GET("reverse")
    fun reverseGeocode(
        @Query("lat") latitude: Double,
        @Query("lon") longitude: Double,
        @Query("format") format: String = "json"
    ): Call<ResponseBody>

    @GET("route/v1/driving/{startLng},{startLat};{endLng},{endLat}")
    fun getRoute(
        @Path("startLng") startLng: Double,
        @Path("startLat") startLat: Double,
        @Path("endLng") endLng: Double,
        @Path("endLat") endLat: Double,
        @Query("overview") overview: String = "full"
    ): Call<ResponseBody>
}