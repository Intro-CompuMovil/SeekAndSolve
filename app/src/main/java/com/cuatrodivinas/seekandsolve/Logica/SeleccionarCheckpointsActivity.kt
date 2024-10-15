package com.cuatrodivinas.seekandsolve.Logica

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.cuatrodivinas.seekandsolve.Datos.Punto
import com.cuatrodivinas.seekandsolve.Datos.RetrofitOsmClient
import com.cuatrodivinas.seekandsolve.Datos.RetrofitUrls
import com.cuatrodivinas.seekandsolve.R
import com.cuatrodivinas.seekandsolve.databinding.ActivitySeleccionarCheckpointsBinding
import com.cuatrodivinas.seekandsolve.databinding.ActivitySeleccionarPuntoFinalBinding
import org.osmdroid.config.Configuration
import org.osmdroid.library.BuildConfig
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import okhttp3.ResponseBody
import org.json.JSONObject


class SeleccionarCheckpointsActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySeleccionarCheckpointsBinding
    private lateinit var puntoInicial: Punto
    private lateinit var puntoFinal: Punto
    private lateinit var map: MapView
    private lateinit var marcadorPuntoInicial: Marker
    private lateinit var marcadorPuntoFinal: Marker
    private lateinit var polyline: Polyline
    private val REQUEST_PERMISSIONS_REQUEST_CODE = 1
    private lateinit var retrofitUrls: RetrofitUrls
    private var trayectoRetrofit: RetrofitUrls
    private val marcadores = mutableListOf<GeoPoint>()


    init {
        val retrofit = RetrofitOsmClient.urlRuta()  // Usa el cliente adecuado para obtener rutas
        trayectoRetrofit = retrofit.create(RetrofitUrls::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        // Inflar correctamente el layout con el binding
        binding = ActivitySeleccionarCheckpointsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Retrieve the Bundle from the Intent
        val bundle = intent.getBundleExtra("bundle")

        // Retrieve the latitude and longitude of each point from the Bundle
        val latitudPuntoInicial = bundle?.getDouble("latitudPuntoInicial")
        val longitudPuntoInicial = bundle?.getDouble("longitudPuntoInicial")
        val latitudPuntoFinal = bundle?.getDouble("latitudPuntoFinal")
        val longitudPuntoFinal = bundle?.getDouble("longitudPuntoFinal")

        // Create the Punto objects
        puntoInicial = Punto(latitudPuntoInicial ?: 0.0, longitudPuntoInicial ?: 0.0)
        puntoFinal = Punto(latitudPuntoFinal ?: 0.0, longitudPuntoFinal ?: 0.0)

        // Configurar el mapa
        setupMap()

        // Mostrar los puntos en el mapa
        mostrarPuntoInicial(puntoInicial)
        mostrarPuntoFinal(puntoFinal)

        // Obtener la ruta real entre el punto inicial y el punto final
        val startPoint = GeoPoint(puntoInicial.latitud, puntoInicial.longitud)
        val endPoint = GeoPoint(puntoFinal.latitud, puntoFinal.longitud)
        getRoute(startPoint, endPoint)
        binding.agregarCheckpointCheck.setOnClickListener {
            // Create an Intent for returning the result
            val resultIntent = Intent().apply {
                putExtra("marcadores", marcadores.toTypedArray())
            }

            // Set the result and finish the activity
            setResult(RESULT_OK, resultIntent)
            finish()
        }
    }

    private fun getRoute(startPoint: GeoPoint, endPoint: GeoPoint) {
        val getRoute = trayectoRetrofit.getRoute(startPoint.longitude, startPoint.latitude, endPoint.longitude, endPoint.latitude)

        getRoute.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful && response.body() != null) {
                    val routeResponse = response.body()!!.string()
                    val routeJson = JSONObject(routeResponse)

                    // Acceder a la polilínea codificada dentro del JSON
                    val encodedPolyline = routeJson
                        .getJSONArray("routes")
                        .getJSONObject(0)
                        .getString("geometry")
                    drawRoute(encodedPolyline)
                } else {
                    println("Error en la respuesta: ${response.errorBody()}")
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                println("Error en la llamada: ${t.message}")
            }
        })
    }

    private fun drawRoute(encoded: String) {
        val path: List<GeoPoint> = decodePolyline(encoded)
        map.controller.setCenter(path[0])
        runOnUiThread {
            val polyline = Polyline()
            polyline.setPoints(path)
            polyline.color = Color.BLUE
            polyline.width = 10f
            map.overlays.add(polyline)
            map.invalidate()
        }
    }

    private fun decodePolyline(encoded: String): List<GeoPoint> {
        val poly = ArrayList<GeoPoint>()
        var index = 0
        val len = encoded.length
        var lat = 0
        var lng = 0

        while (index < len) {
            var b: Int
            var shift = 0
            var result = 0
            do {
                b = encoded[index++].toInt() - 63
                result = result or (b and 0x1f shl shift)
                shift += 5
            } while (b >= 0x20)

            val dlat = if (result and 1 != 0) (result shr 1).inv() else (result shr 1)
            lat += dlat

            shift = 0
            result = 0
            do {
                b = encoded[index++].toInt() - 63
                result = result or (b and 0x1f shl shift)
                shift += 5
            } while (b >= 0x20)

            val dlng = if (result and 1 != 0) (result shr 1).inv() else (result shr 1)
            lng += dlng

            val point = GeoPoint((lat / 1E5), (lng / 1E5))
            poly.add(point)
        }
        return poly
    }

    private fun setupMap() {
        Configuration.getInstance().userAgentValue = BuildConfig.APPLICATION_ID
        map = findViewById(R.id.map)
        map.setMultiTouchControls(true)

        // Inicializar la vista del mapa
        map.controller.setZoom(15.0)

        val mapEventsReceiver = object : org.osmdroid.events.MapEventsReceiver {
            override fun singleTapConfirmedHelper(p: GeoPoint?): Boolean {
                return false
            }

            override fun longPressHelper(p: GeoPoint?): Boolean {
                p?.let {
                    // Create a new marker
                    val newMarker = Marker(map).apply {
                        icon = ContextCompat.getDrawable(this@SeleccionarCheckpointsActivity, R.drawable.ic_location)
                        position = GeoPoint(it.latitude, it.longitude)
                        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                        title = "Posición seleccionada"
                    }

                    // Add the new marker to the map's overlays
                    map.overlays.add(newMarker)

                    // Add the new marker's position to the list
                    marcadores.add(newMarker.position)

                    // Update the map
                    map.invalidate()
                }
                return true
            }
        }

        // Create a MapEventsOverlay using the receiver and add it to the overlays
        val eventsOverlay = org.osmdroid.views.overlay.MapEventsOverlay(mapEventsReceiver)
        map.overlays.add(eventsOverlay)
    }

    private fun mostrarPuntoInicial(puntoInicial: Punto) {
        // Crear y mostrar el marcador del punto inicial en el mapa
        marcadorPuntoInicial = Marker(map).apply {
            icon = ContextCompat.getDrawable(this@SeleccionarCheckpointsActivity, R.drawable.ic_location)
            position = GeoPoint(puntoInicial.latitud, puntoInicial.longitud)
            title = "Punto inicial"
            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
        }
        map.overlays.add(marcadorPuntoInicial)

        // Centrar el mapa en el punto inicial
        map.controller.setCenter(marcadorPuntoInicial.position)
        map.controller.animateTo(marcadorPuntoInicial.position)
        map.invalidate()
    }

    private fun mostrarPuntoFinal(puntoFinal: Punto) {
        // Crear y mostrar el marcador del punto final en el mapa
        marcadorPuntoFinal = Marker(map).apply {
            icon = ContextCompat.getDrawable(this@SeleccionarCheckpointsActivity, R.drawable.ic_location)
            position = GeoPoint(puntoFinal.latitud, puntoFinal.longitud)
            title = "Punto final"
            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
        }
        map.overlays.add(marcadorPuntoFinal)

        // Centrar el mapa en el punto final
        map.controller.setCenter(marcadorPuntoFinal.position)
        map.controller.animateTo(marcadorPuntoFinal.position)
        map.invalidate()
    }


}