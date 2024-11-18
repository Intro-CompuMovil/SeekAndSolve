package com.cuatrodivinas.seekandsolve.Logica

import android.Manifest
import android.app.UiModeManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.cuatrodivinas.seekandsolve.Datos.Punto
import com.cuatrodivinas.seekandsolve.Datos.RetrofitOsmClient
import com.cuatrodivinas.seekandsolve.Datos.RetrofitUrls
import com.cuatrodivinas.seekandsolve.databinding.ActivitySeleccionarPuntoBinding
import okhttp3.ResponseBody
import org.json.JSONArray
import org.json.JSONObject
import org.osmdroid.config.Configuration
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.TilesOverlay
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SeleccionarPuntoActivity : AppCompatActivity(), LocationListener {
    private lateinit var binding: ActivitySeleccionarPuntoBinding
    private lateinit var map: MapView
    private val REQUEST_PERMISSIONS_REQUEST_CODE = 1
    private lateinit var locationManager: LocationManager
    private lateinit var tipoPunto: String
    private lateinit var marcador: Marker
    private var retrofitUrls: RetrofitUrls
    private var retrofitUrls2: RetrofitUrls

    init {
        val retrofit = RetrofitOsmClient.conectarBackURL()
        val retrofit2 = RetrofitOsmClient.urlRuta()
        retrofitUrls = retrofit.create(RetrofitUrls::class.java)
        retrofitUrls2 = retrofit2.create(RetrofitUrls::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySeleccionarPuntoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Obtener los datos del Intent
        val bundle = intent.getBundleExtra("bundle")
        tipoPunto = bundle!!.getString("tipoPunto") ?: "checkpoint"

        setupMap()
        setupLocationManager()
        startLocationUpdates()
        eventoEnviarTexto()

        binding.agregarCheckpoint.setOnClickListener {
            val punto = Punto(marcador.position.latitude, marcador.position.longitude)

            // Crear un Intent para devolver el resultado
            val resultIntent = Intent().apply {
                when (tipoPunto) {
                    "inicial" -> putExtra("puntoInicial", punto)
                    "final" -> putExtra("puntoFinal", punto)
                    "checkpoint" -> putExtra("checkpoint", punto)
                }
            }

// Devolver el resultado a CrearDesafioActivity
            setResult(RESULT_OK, resultIntent)
            finish()
        }

    }

    private fun setupMap() {
        map = binding.map
        map.setMultiTouchControls(true)

        // Configurar el marcador
        marcador = Marker(map)
        marcador.icon = ContextCompat.getDrawable(this, com.cuatrodivinas.seekandsolve.R.drawable.ic_location)
        marcador.position = GeoPoint(0.0, 0.0)
        marcador.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
        marcador.title = "Posición seleccionada"
        map.overlays.add(marcador)

        // Inicializamos la vista del mapa
        map.controller.setZoom(15.0)

        // Configurar eventos del mapa (long click para agregar puntos)
        val mapEventsReceiver = object : org.osmdroid.events.MapEventsReceiver {
            override fun singleTapConfirmedHelper(p: GeoPoint?): Boolean {
                return false
            }

            override fun longPressHelper(p: GeoPoint?): Boolean {
                p?.let {
                    setPoint(it.latitude, it.longitude)
                }
                return true
            }
        }
        val eventsOverlay = org.osmdroid.views.overlay.MapEventsOverlay(mapEventsReceiver)
        map.overlays.add(eventsOverlay)
        val uiManager = getSystemService(Context.UI_MODE_SERVICE) as UiModeManager
        if(uiManager.nightMode == UiModeManager.MODE_NIGHT_YES){
            map.overlayManager.tilesOverlay.setColorFilter(TilesOverlay.INVERT_COLORS)
        }
    }

    private fun setPoint(latitude: Double, longitude: Double) {
        val newPoint = GeoPoint(latitude, longitude)
        marcador.position = newPoint
        map.controller.animateTo(newPoint)
        map.invalidate()
        actualizarDireccionPunto(latitude, longitude)
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
            this
        )
    }

    private fun setupLocationManager() {
        locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
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
            Log.e("SeleccionarPunto", "Permisos de ubicación no concedidos")
        }
    }

    override fun onLocationChanged(location: Location) {
        setPoint(location.latitude, location.longitude)
    }

    private fun eventoEnviarTexto(){
        binding.buscar.setOnEditorActionListener { _, actionId, event ->
            // Verifica si se ha presionado el botón "Send"
            if (actionId == EditorInfo.IME_ACTION_SEND ||
                (event != null && event.keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_DOWN)) {
                val mensaje = binding.buscar.text.toString()
                obtenerPuntoPorDireccion(mensaje)
                true // Indica que el evento ha sido manejado
            } else {
                false // No se maneja otro evento
            }
        }
    }

    private fun obtenerPuntoPorDireccion(direccion: String){
        val geocode = retrofitUrls.geocode(direccion)

        geocode.enqueue(object: Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful && response.body() != null) {
                    val direction = response.body()!!.string()
                    val jsonDirections = JSONArray(direction)
                    if(jsonDirections.length() > 0){
                        val jsonObject = jsonDirections.getJSONObject(0) // Obtener el primer objeto
                        val latitude = jsonObject.getString("lat").toDouble() // Obtener latitud
                        val longitude = jsonObject.getString("lon").toDouble()  // Obtener longitud
                        setPoint(latitude, longitude)
                        binding.direccion.text = direccion
                    }
                } else {
                    println("Error en la respuesta: ${response.errorBody()}")
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                println("Error en la llamada: ${t.message}")
            }
        })
    }

    private fun actualizarDireccionPunto(latitud: Double, longitud: Double) {
        val reverseGeocode = retrofitUrls.reverseGeocode(latitud, longitud)
        reverseGeocode.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful && response.body() != null) {
                    try {
                        val direction = response.body()!!.string()
                        val jsonDirection = JSONObject(direction)
                        binding.direccion.text = jsonDirection.getString("display_name")
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

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_PERMISSIONS_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                initializeMap()
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

    override fun onStop() {
        super.onStop()
        if (::map.isInitialized) {
            map.onDetach()
        }
    }
}
