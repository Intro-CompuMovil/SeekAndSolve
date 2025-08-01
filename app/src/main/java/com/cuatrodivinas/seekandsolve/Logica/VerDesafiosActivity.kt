package com.cuatrodivinas.seekandsolve.Logica

import android.Manifest
import android.app.UiModeManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.database.MatrixCursor
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.cuatrodivinas.seekandsolve.Datos.Data.Companion.PATH_DESAFIOS
import com.cuatrodivinas.seekandsolve.Datos.Data.Companion.database
import com.cuatrodivinas.seekandsolve.Datos.Desafio
import com.cuatrodivinas.seekandsolve.R
import com.cuatrodivinas.seekandsolve.databinding.ActivityVerDesafiosBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import org.osmdroid.config.Configuration
import org.osmdroid.library.BuildConfig
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.TilesOverlay
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

class VerDesafiosActivity : AppCompatActivity(), LocationListener {
    private lateinit var binding: ActivityVerDesafiosBinding
    private lateinit var map: MapView
    private val REQUEST_PERMISSIONS_REQUEST_CODE = 1
    private lateinit var locationManager: LocationManager
    private var desafiosList: MutableList<Desafio> = mutableListOf()
    private lateinit var marcadores: MutableList<Marker>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVerDesafiosBinding.inflate(layoutInflater)
        setContentView(binding.root)

        marcadores = mutableListOf()
        inicializarDesafios()
        setupMap()
        eventoVolver()
        verDesafio()
    }

    private fun verDesafio() {
        binding.listaDesafios.setOnItemClickListener { parent, view, position, id ->
            val desafio = desafiosList[position]
            val intent = Intent(this, VerDesafioActivity::class.java)
            intent.putExtra("desafio", desafio)
            startActivity(intent)
        }
    }

    private fun inicializarDesafios() {
        val desafiosRef = database.getReference(PATH_DESAFIOS)

        desafiosRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val columns = arrayOf("_id", "desafio_id", "nombre")
                val matrixCursor = MatrixCursor(columns)
                var idCounter = 1L
                desafiosList.clear()

                for (desafioSnapshot in dataSnapshot.children) {
                    val desafio = desafioSnapshot.getValue(Desafio::class.java)
                    desafio?.let {
                        // Asigna el ID manualmente si no viene en el modelo
                        it.id = desafioSnapshot.key ?: ""
                        if (it.id.isEmpty()) {
                            Log.e("Realtime Database", "Desafío sin id: ${it.nombre}")
                        }
                        desafiosList.add(it)
                        matrixCursor.addRow(arrayOf(idCounter, it.id, it.nombre))
                        idCounter++
                    }
                }

                val cursor: Cursor = matrixCursor
                val desafiosAdapter = DesafiosAdapter(this@VerDesafiosActivity, cursor, 0)
                binding.listaDesafios.adapter = desafiosAdapter
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e("Firebase", "Error al leer los desafíos", databaseError.toException())
            }
        })
    }

    private fun setupMap() {
        Configuration.getInstance().userAgentValue = BuildConfig.APPLICATION_ID
        map = findViewById(R.id.map)
        map.setMultiTouchControls(true)
        val mapController = map.controller
        mapController.setZoom(15.0)
        val marcador = Marker(map)
        marcador.icon = map.context.getDrawable(R.drawable.ic_location)
        marcador.position = GeoPoint(0.0, 0.0)
        marcador.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
        marcador.title = "Posición actual"
        marcadores.add(marcador)
        val uiManager = getSystemService(Context.UI_MODE_SERVICE) as UiModeManager
        if(uiManager.nightMode == UiModeManager.MODE_NIGHT_YES){
            map.overlayManager.tilesOverlay.setColorFilter(TilesOverlay.INVERT_COLORS)
        }
    }

    private fun setPoint(lalitud: Double, longitud: Double){
        //Obtener el GeoPoint
        val newPoint = GeoPoint(lalitud, longitud)

        //Crear los marcadores
        eliminarDesafiosAnteriores()
        marcadores[0].position = newPoint
        setDesafiosCercanos(lalitud, longitud)
        agregarMarcadores()

        //Mover el mapa
        map.controller.animateTo(newPoint)

        //Refrescar cambios en el mapa
        map.invalidate()
    }

    private fun eliminarDesafiosAnteriores(){
        map.overlays.clear()
        val marcadorPosicion = marcadores[0]
        marcadores.clear()
        marcadores.add(marcadorPosicion)
    }

    private fun agregarMarcadores(){
        map.overlays.addAll(marcadores)
        Log.d("Marcadores", "Marcadores ${map.overlays.size}")
    }

    private fun setDesafiosCercanos(latitud: Double, longitud: Double){
        for (desafio in desafiosList) {
            val latitudPunto = desafio.puntoInicial.latitud
            val longitudPunto = desafio.puntoInicial.longitud
            val distancia = calcularDistancia(latitud, longitud, latitudPunto, longitudPunto)
            if (distancia <= 10000.0) {
                val marcadorDesafio = Marker(map)
                marcadorDesafio.icon = map.context.getDrawable(R.drawable.desafio_marcador)
                marcadorDesafio.position = GeoPoint(latitudPunto, longitudPunto)
                marcadorDesafio.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                marcadorDesafio.title = desafio.nombre
                marcadores.add(marcadorDesafio)
            }
        }
    }

    private fun calcularDistancia(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val radioTierra = 6371000.0  // Radio de la Tierra en metros
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)

        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2) * sin(dLon / 2)

        val c = 2 * atan2(sqrt(a), sqrt(1 - a))

        return radioTierra * c  // Distancia en metros
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
            if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0L, 0f, this)
            }
        } else {
            println("Permisos de ubicación no concedidos")
        }
    }

    override fun onLocationChanged(location: Location) {
        setPoint(location.latitude, location.longitude)
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
            setupLocationManager()
        }
    }

    override fun onPause() {
        super.onPause()
        if (::map.isInitialized) {
            map.onPause()
            if (::locationManager.isInitialized) {
                locationManager.removeUpdates(this)
            }
        }
    }

    override fun onProviderEnabled(provider: String) {}
    override fun onProviderDisabled(provider: String) {}
    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}


    private fun eventoVolver(){
        binding.backButtonChallenges.setOnClickListener {
            finish()
        }
    }
}