package com.cuatrodivinas.seekandsolve.Logica

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.cuatrodivinas.seekandsolve.R
import com.cuatrodivinas.seekandsolve.databinding.ActivityTrayectoDesafioBinding
import org.osmdroid.config.Configuration
import org.osmdroid.library.BuildConfig
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

class TrayectoDesafioActivity : AppCompatActivity(), LocationListener {
    private lateinit var binding: ActivityTrayectoDesafioBinding
    private lateinit var map: MapView
    private val REQUEST_PERMISSIONS_REQUEST_CODE = 1
    private lateinit var locationManager: LocationManager
    private var isFirstLocation = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTrayectoDesafioBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val textoTitulo = "Ruta de " + intent.getBundleExtra("bundle")!!.getString("nombre")
        binding.tituloTrayecto.text = textoTitulo
        setupMap()
        setupLocationManager()
    }

    private fun setupMap() {
        Configuration.getInstance().userAgentValue = BuildConfig.APPLICATION_ID
        map = findViewById(R.id.map)
        map.setMultiTouchControls(true)
        val mapController = map.controller
        mapController.setZoom(17.0)
        val startPoint = GeoPoint(48.8583, 2.2944)
        mapController.setCenter(startPoint)
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
        if (::map.isInitialized && map.handler != null && map.controller != null) {
            val currentLocation = GeoPoint(location.latitude, location.longitude)
            if (isFirstLocation) {
                map.controller.setCenter(currentLocation)
                isFirstLocation = false
            }
            // Crea un marcador solo si el mapa está listo
            try {
                val marker = Marker(map)
                marker.position = currentLocation
                marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                val icon: Drawable = ContextCompat.getDrawable(this, R.drawable.ic_location)!!
                marker.icon = icon
                map.overlays.clear()
                map.overlays.add(marker)
                map.invalidate()
            } catch (e: Exception) {
                e.printStackTrace()
                println("Error al crear el marcador: ${e.message}")
            }
        } else {
            println("El mapa no está listo aún")
        }
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
        val intent = Intent(this, IniciarRutaActivity::class.java)
        super.onResume()

        binding.iniciarDesafio.setOnClickListener {
            val intentIniciarDesafio = Intent(this, ConfigurarCarreraActivity::class.java)
            intentIniciarDesafio.putExtra("bundle", intent.getBundleExtra("bundle"))
            startActivity(intentIniciarDesafio)
        }

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