package com.cuatrodivinas.seekandsolve.Logica

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.cuatrodivinas.seekandsolve.Datos.Desafio
import com.cuatrodivinas.seekandsolve.Datos.RetrofitOsmClient
import com.cuatrodivinas.seekandsolve.Datos.RetrofitUrls
import com.cuatrodivinas.seekandsolve.R
import com.cuatrodivinas.seekandsolve.databinding.ActivityIniciarRutaBinding
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

class IniciarRutaActivity : AppCompatActivity(), LocationListener {
    private lateinit var binding: ActivityIniciarRutaBinding
    private lateinit var map: MapView
    private val REQUEST_PERMISSIONS_REQUEST_CODE = 1
    private lateinit var locationManager: LocationManager
    private var isFirstLocation = true
    private var trayectoRetrofit: RetrofitUrls
    private var geocoderRetrofit: RetrofitUrls
    private lateinit var desafio: Desafio
    private lateinit var marcador: Marker

    init{
        val retrofit = RetrofitOsmClient.urlRuta()
        val retrofit2 = RetrofitOsmClient.conectarBackURL()
        trayectoRetrofit = retrofit.create(RetrofitUrls::class.java)
        geocoderRetrofit = retrofit2.create(RetrofitUrls::class.java)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityIniciarRutaBinding.inflate(layoutInflater)
        setContentView(binding.root)
        desafio = intent.getSerializableExtra("desafio") as Desafio
        setupMap()
        setupLocationManager()
        val puntoInicial = GeoPoint(desafio.puntoInicial.latitud, desafio.puntoInicial.longitud)
        val puntoFinal = GeoPoint(desafio.puntoFinal.latitud, desafio.puntoFinal.longitud)
        buscarDireccion(puntoInicial.latitude, puntoInicial.longitude, puntoFinal.latitude,
                        puntoFinal.longitude)
        getRoute(puntoInicial, puntoFinal)
        startLocationUpdates()
    }

    private fun startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1)
            return
        }
        locationManager.requestLocationUpdates(
            LocationManager.GPS_PROVIDER,
            1000L,
            1f,
            object: LocationListener {
                override fun onLocationChanged(location: Location) {
                    Log.d("Marcadores", "Marcadores ${map.overlays.size}")
                    setPoint(location.latitude, location.longitude)
                    val puntoInicial = GeoPoint(location.latitude, location.longitude)
                    val puntoFinal = GeoPoint(desafio.puntoFinal.latitud, desafio.puntoFinal.longitud)
                    getRoute(puntoInicial, puntoFinal)
                }
            }
        )
    }

    private fun setupMap() {
        Configuration.getInstance().userAgentValue = BuildConfig.APPLICATION_ID
        map = findViewById(R.id.map)
        map.setMultiTouchControls(true)
        marcador = Marker(map)
        marcador.icon = getDrawable(R.drawable.ic_location)
        val mapController = map.controller
        mapController.setZoom(17.0)
        val startPoint = GeoPoint(0.0, 0.0)
        mapController.setCenter(startPoint)
        map.overlays.add(marcador)
    }

    private fun setPoint(lalitud: Double, longitud: Double){
        //Obtener el GeoPoint
        val newPoint = GeoPoint(lalitud, longitud)

        //Crear los marcadores
        map.overlays.clear()
        marcador.position = newPoint
        map.overlays.add(marcador)

        //Mover el mapa
        map.controller.animateTo(newPoint)

        //Refrescar cambios en el mapa
        map.invalidate()
    }

    private fun getRoute(startPoint: GeoPoint, endPoint: GeoPoint) {
        val getRoute = trayectoRetrofit.getRoute(startPoint.longitude, startPoint.latitude, endPoint.longitude, endPoint.latitude)

        getRoute.enqueue(object :
            Callback<ResponseBody> {
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

    private fun buscarDireccion(latitudIni: Double, longitudIni: Double,
                                latitudFin: Double, longitudFin: Double){
        // Realizar la solicitud a la API
        val reverseGeocode = geocoderRetrofit.reverseGeocode(latitudIni, longitudIni)

        reverseGeocode.enqueue(object :
            Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful && response.body() != null) {
                    val direction = response.body()!!.string()
                    val jsonDirection = JSONObject(direction)
                    val reverseGeocode2 = geocoderRetrofit.reverseGeocode(latitudFin, longitudFin)
                    reverseGeocode2.enqueue(object :
                    Callback<ResponseBody> {
                        override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                            if (response.isSuccessful && response.body() != null) {
                                val direction2 = response.body()!!.string()
                                val jsonDirection2 = JSONObject(direction2)
                                binding.tituloRuta.text = "De " + jsonDirection.getString("name") + " a " +  jsonDirection2.getString("name")
                            } else {
                                println("Error en la respuesta: ${response.errorBody()}")
                            }
                        }

                        override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                            println("Error en la llamada: ${t.message}")
                        }
                    })
                    binding.tituloRuta.text
                } else {
                    println("Error en la respuesta: ${response.errorBody()}")
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                println("Error en la llamada: ${t.message}")
            }
        })
    }

    private fun setupLocationManager() {
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if (hasLocationPermissions()) {
            initializeMap()
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

    private fun initializeMap() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0L, 0f, this)
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0L, 0f, this)
        } else {
            println("Permisos de ubicación no concedidos")
        }
    }

    override fun onLocationChanged(location: Location) {
        setPoint(location.latitude, location.longitude)
        val puntoInicial = GeoPoint(location.latitude, location.longitude)
        val puntoFinal = GeoPoint(desafio.puntoFinal.latitud, desafio.puntoFinal.longitud)
        getRoute(puntoInicial, puntoFinal)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_PERMISSIONS_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                initializeMap()
            } else {
                // Permisos denegados, no mostrar el mapa
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (::map.isInitialized) {
            map.onResume()
        }

        binding.abandonar.setOnClickListener {
            startActivity(Intent(this, VerDesafiosActivity::class.java))
            finish()
        }

        binding.resolverAcertijo.setOnClickListener {
            startActivity(Intent(this, ResolverAcertijoActivity::class.java))
        }
    }

    override fun onPause() {
        super.onPause()
        if (::map.isInitialized) {
            map.onPause()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::map.isInitialized) {
            map.onDetach()
        }
    }

    override fun onProviderEnabled(provider: String) {}
    override fun onProviderDisabled(provider: String) {}
    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
}