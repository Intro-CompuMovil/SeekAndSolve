package com.cuatrodivinas.seekandsolve.Logica

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.cuatrodivinas.seekandsolve.Datos.Desafio
import com.cuatrodivinas.seekandsolve.Datos.Punto
import com.cuatrodivinas.seekandsolve.Datos.RetrofitOsmClient
import com.cuatrodivinas.seekandsolve.Datos.RetrofitUrls
import com.cuatrodivinas.seekandsolve.R
import com.cuatrodivinas.seekandsolve.databinding.ActivitySeleccionarPuntoFinalBinding
import okhttp3.ResponseBody
import org.json.JSONObject
import org.osmdroid.config.Configuration
import org.osmdroid.library.BuildConfig
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SeleccionarPuntoFinalActivity : AppCompatActivity(), LocationListener {
    private lateinit var binding: ActivitySeleccionarPuntoFinalBinding
    private lateinit var map: MapView
    private lateinit var locationManager: LocationManager
    private lateinit var desafio: Desafio
    private lateinit var marcadorPuntoInicial: Marker
    private lateinit var marcadorPuntoFinal: Marker
    private lateinit var polyline: Polyline
    private val REQUEST_PERMISSIONS_REQUEST_CODE = 1
    private lateinit var retrofitUrls: RetrofitUrls
    private var trayectoRetrofit: RetrofitUrls
    private var geocoderRetrofit: RetrofitUrls

    init {
        val retrofit = RetrofitOsmClient.urlRuta()  // Usa el cliente adecuado para obtener rutas
        val retrofit2 = RetrofitOsmClient.conectarBackURL()
        trayectoRetrofit = retrofit.create(RetrofitUrls::class.java)
        geocoderRetrofit = retrofit2.create(RetrofitUrls::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySeleccionarPuntoFinalBinding.inflate(layoutInflater)
        setContentView(binding.root)
        // Initialize retrofitUrls here
        val retrofit = RetrofitOsmClient.conectarBackURL()
        retrofitUrls = retrofit.create(RetrofitUrls::class.java)


        // Obtener los datos del Intent (incluyendo el punto inicial)
        val bundle = intent.getBundleExtra("bundle")
        desafio = bundle?.getSerializable("desafio") as Desafio

        // Verificar si el punto inicial tiene coordenadas válidas
        val latitudInicial = desafio.puntoInicial.latitud
        val longitudInicial = desafio.puntoInicial.longitud

        // Log para depurar si el punto inicial fue pasado correctamente
        Log.d("SeleccionarPuntoFinal", "Latitud inicial: $latitudInicial, Longitud inicial: $longitudInicial")

        setupMap()
        setupLocationManager()

        // Mostrar el punto inicial en el mapa si existe
        if (latitudInicial != 0.0 && longitudInicial != 0.0) {
            mostrarPuntoInicial(desafio.puntoInicial)
        } else {
            Log.e("SeleccionarPuntoFinal", "El punto inicial no está definido o es inválido.")
        }

        binding.agregarCheckpointFinal.setOnClickListener {
            val puntoFinal = Punto(marcadorPuntoFinal.position.latitude, marcadorPuntoFinal.position.longitude)

            // Guardar el punto final en el desafío
            desafio.puntoFinal = puntoFinal

            // Obtener la ruta real entre el punto inicial y el punto final
            val startPoint = GeoPoint(desafio.puntoInicial.latitud, desafio.puntoInicial.longitud)
            val endPoint = GeoPoint(puntoFinal.latitud, puntoFinal.longitud)
            getRoute(startPoint, endPoint)

            // Crear un Intent para devolver el resultado
            val resultIntent = Intent().apply {
                putExtra("puntoFinal", puntoFinal)
            }

            // Devolver el resultado a CrearDesafioActivity
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
        map = binding.map
        map.setMultiTouchControls(true)

        // Inicializar la vista del mapa
        map.controller.setZoom(15.0)

        // Configurar eventos del mapa (long click para agregar el punto final)
        val mapEventsReceiver = object : org.osmdroid.events.MapEventsReceiver {
            override fun singleTapConfirmedHelper(p: GeoPoint?): Boolean {
                return false
            }

            override fun longPressHelper(p: GeoPoint?): Boolean {
                p?.let {
                    setPuntoFinal(it.latitude, it.longitude)
                }
                return true
            }
        }
        val eventsOverlay = org.osmdroid.views.overlay.MapEventsOverlay(mapEventsReceiver)
        map.overlays.add(eventsOverlay)
    }

    private fun setPuntoFinal(latitude: Double, longitude: Double) {
        // Crear o actualizar el marcador para el punto final
        if (!::marcadorPuntoFinal.isInitialized) {
            marcadorPuntoFinal = Marker(map)
            marcadorPuntoFinal.icon = ContextCompat.getDrawable(this, com.cuatrodivinas.seekandsolve.R.drawable.ic_location)
            marcadorPuntoFinal.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
            marcadorPuntoFinal.title = "Punto final"
            map.overlays.add(marcadorPuntoFinal)
        }
        marcadorPuntoFinal.position = GeoPoint(latitude, longitude)
        map.controller.animateTo(marcadorPuntoFinal.position)
        map.invalidate()
        actualizarDireccionPunto(latitude, longitude)

        // Obtener la ruta entre el punto inicial y el punto final
        val puntoInicial = GeoPoint(desafio.puntoInicial.latitud, desafio.puntoInicial.longitud)
        val puntoFinal = GeoPoint(latitude, longitude)
        getRoute(puntoInicial, puntoFinal)
    }

    private fun mostrarPuntoInicial(puntoInicial: Punto) {
        // Crear y mostrar el marcador del punto inicial en el mapa
        marcadorPuntoInicial = Marker(map).apply {
            icon = ContextCompat.getDrawable(this@SeleccionarPuntoFinalActivity, com.cuatrodivinas.seekandsolve.R.drawable.ic_location)
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


    private fun dibujarRuta(puntoInicial: Punto, puntoFinal: Punto) {
        val geoPuntoInicial = GeoPoint(puntoInicial.latitud, puntoInicial.longitud)
        val geoPuntoFinal = GeoPoint(puntoFinal.latitud, puntoFinal.longitud)

        polyline = Polyline().apply {
            addPoint(geoPuntoInicial)
            addPoint(geoPuntoFinal)
            color = ContextCompat.getColor(this@SeleccionarPuntoFinalActivity, R.color.primaryColor)
            width = 5.0f
        }
        map.overlays.add(polyline)
        map.invalidate()
    }

    private fun startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), REQUEST_PERMISSIONS_REQUEST_CODE)
            return
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000L, 1f, this)
    }

    private fun setupLocationManager() {
        locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
        if (hasLocationPermissions()) {
            startLocationUpdates()
        } else {
            requestLocationPermissions()
        }
    }

    private fun hasLocationPermissions(): Boolean {
        val fineLocationPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
        val coarseLocationPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
        return fineLocationPermission == PackageManager.PERMISSION_GRANTED && coarseLocationPermission == PackageManager.PERMISSION_GRANTED
    }

    private fun requestLocationPermissions() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION),
            REQUEST_PERMISSIONS_REQUEST_CODE
        )
    }

    override fun onLocationChanged(location: Location) {
        // Si no hay un punto inicial, centrar el mapa en la ubicación actual
        if (!::marcadorPuntoInicial.isInitialized) {
            setPuntoFinal(location.latitude, location.longitude)
        }
    }

    private fun actualizarDireccionPunto(latitud: Double, longitud: Double) {
        val reverseGeocode = retrofitUrls.reverseGeocode(latitud, longitud)
        reverseGeocode.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful && response.body() != null) {
                    val direction = response.body()!!.string()
                    val jsonDirection = JSONObject(direction)
                    binding.direccionFinal.text = jsonDirection.getString("display_name")
                } else {
                    Log.e("Geocode", "Error en la respuesta: ${response.errorBody()}")
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Log.e("Geocode", "Error en la llamada: ${t.message}")
            }
        })
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_PERMISSIONS_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                startLocationUpdates()
            } else {
                Toast.makeText(this, "Permisos de ubicación denegados", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onPause() {
        super.onPause()
        if (::map.isInitialized) {
            map.onPause()
        }
    }

    override fun onResume() {
        super.onResume()
        if (::map.isInitialized) {
            map.onResume()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::map.isInitialized) {
            map.onDetach()
        }
    }
}
