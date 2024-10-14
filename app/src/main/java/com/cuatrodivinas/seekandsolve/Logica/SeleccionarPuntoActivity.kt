package com.cuatrodivinas.seekandsolve.Logica

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.cuatrodivinas.seekandsolve.Datos.Desafio
import com.cuatrodivinas.seekandsolve.Datos.Punto
import com.cuatrodivinas.seekandsolve.Datos.RetrofitOsmClient
import com.cuatrodivinas.seekandsolve.Datos.RetrofitUrls
import com.cuatrodivinas.seekandsolve.R
import com.cuatrodivinas.seekandsolve.databinding.ActivitySeleccionarPuntoBinding
import okhttp3.ResponseBody
import org.json.JSONArray
import org.json.JSONObject
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.library.BuildConfig
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SeleccionarPuntoActivity : AppCompatActivity(), LocationListener {
    private lateinit var binding: ActivitySeleccionarPuntoBinding
    private lateinit var map: MapView
    private val REQUEST_PERMISSIONS_REQUEST_CODE = 1
    private lateinit var locationManager: LocationManager
    private var isFirstLocation = true
    private lateinit var desafio: Desafio
    private lateinit var tipoPunto: String
    private lateinit var marcador: Marker
    private var retrofitUrls: RetrofitUrls

    init{
        val retrofit = RetrofitOsmClient.conectarBackURL()
        retrofitUrls = retrofit.create(RetrofitUrls::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySeleccionarPuntoBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val bundle = intent.getBundleExtra("bundle")
        map = binding.map
        desafio = bundle!!.getSerializable("desafio") as Desafio
        tipoPunto = bundle.getString("tipoPunto")!!
        setupMap()
        setupLocationManager()
        startLocationUpdates()
        eventoEnviarTexto()
        eventoAgregarPunto()
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
                    actualizarDireccionPunto()
                }
            }
        )
    }

    private fun setupMap() {
        Configuration.getInstance().userAgentValue = BuildConfig.APPLICATION_ID
        map = findViewById(R.id.map)
        map.setMultiTouchControls(true)
        val mapController = map.controller
        mapController.setZoom(15.0)
        marcador = Marker(map)
        val icon: Drawable = ContextCompat.getDrawable(this, R.drawable.ic_location)!!
        marcador.icon = icon
        marcador.position = GeoPoint(0.0, 0.0)
        marcador.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
        marcador.title = "Posición actual"
        map.overlays.add(marcador)
        eventoMapa()
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun eventoMapa(){
        /*val eventosOverlay = MapEventsOverlay(object : MapEventsReceiver {
            override fun singleTapConfirmedHelper(punto: GeoPoint): Boolean {
                // Actualizar la posición del marcador
                setPoint(punto)
                return true
            }

            override fun longPressHelper(punto: GeoPoint): Boolean {
                return false
            }
        })
        map.overlays.add(eventosOverlay)*/
        map.setOnTouchListener{ _, event ->
            if (event.action == android.view.MotionEvent.ACTION_UP) {
                val geoPoint = map.projection.fromPixels(event.x.toInt(), event.y.toInt()) as GeoPoint
                setPoint(geoPoint)
                actualizarDireccionPunto()
            }
            true
        }
    }

    private fun setPoint(lalitud: Double, longitud: Double){
        //Obtener el GeoPoint
        val newPoint = GeoPoint(lalitud, longitud)

        //Crear los marcadores
        marcador.position = newPoint

        //Mover el mapa
        map.controller.animateTo(newPoint)

        //Refrescar cambios en el mapa
        map.invalidate()
    }

    private fun setPoint(newPoint: GeoPoint){
        //Crear los marcadores
        marcador.position = newPoint

        //Mover el mapa
        map.controller.animateTo(newPoint)

        //Refrescar cambios en el mapa
        map.invalidate()
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

    private fun actualizarDireccionPunto(){
        // Realizar la solicitud a la API
        val reverseGeocode = retrofitUrls.reverseGeocode(marcador.position.latitude, marcador.position.longitude)

        reverseGeocode.enqueue(object :
            Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful && response.body() != null) {
                    val direction = response.body()!!.string()
                    val jsonDirection = JSONObject(direction)
                    binding.direccion.text = jsonDirection.getString("display_name")
                } else {
                    println("Error en la respuesta: ${response.errorBody()}")
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                println("Error en la llamada: ${t.message}")
            }
        })
    }

    private fun eventoAgregarPunto() {
        binding.agregarCheckpoint.setOnClickListener {
            if(tipoPunto.equals("inicial")){
                desafio.puntoInicial.longitud = marcador.position.longitude
                desafio.puntoInicial.latitud = marcador.position.latitude
            }
            else if(tipoPunto.equals("final")){
                desafio.puntoFinal.longitud = marcador.position.longitude
                desafio.puntoFinal.latitud = marcador.position.latitude
            }
            else if(tipoPunto.equals("checkpoint")){
                var punto = Punto(marcador.position.latitude, marcador.position.longitude)
                desafio.puntosIntermedios.add(punto)
            }
            startActivity(Intent(this, CrearDesafioActivity::class.java).putExtra("desafio", desafio))
            finish()
        }
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
            //locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0L, 0f, this)
        } else {
            println("Permisos de ubicación no concedidos")
        }
    }

   override fun onLocationChanged(location: Location) {
        /*if (::map.isInitialized && map.handler != null && map.controller != null) {
            val currentLocation = GeoPoint(location.latitude, location.longitude)
            if (isFirstLocation) {
                map.controller.setCenter(currentLocation)
                isFirstLocation = false
            }
            // Crea un marcador solo si el mapa está listo
            try {
                marcador = Marker(map)
                marcador.position = currentLocation
                marcador.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                val icon: Drawable = ContextCompat.getDrawable(this, R.drawable.ic_location)!!
                marcador.icon = icon
                map.overlays.clear()
                map.overlays.add(marcador)
                map.invalidate()
            } catch (e: Exception) {
                e.printStackTrace()
                println("Error al crear el marcador: ${e.message}")
            }
        } else {
            println("El mapa no está listo aún")
        }*/
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
        val intent = Intent(this, CrearDesafioActivity::class.java)
        super.onResume()
        if (::map.isInitialized) {
            map.onResume()
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