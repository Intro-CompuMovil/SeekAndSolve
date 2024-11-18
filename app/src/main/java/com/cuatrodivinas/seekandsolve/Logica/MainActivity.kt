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
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.cuatrodivinas.seekandsolve.Datos.Data.Companion.PATH_DESAFIOS
import com.cuatrodivinas.seekandsolve.Datos.Data.Companion.PATH_IMAGENES
import com.cuatrodivinas.seekandsolve.Datos.Data.Companion.PATH_USERS
import com.cuatrodivinas.seekandsolve.Datos.Data.Companion.auth
import com.cuatrodivinas.seekandsolve.Datos.Data.Companion.database
import com.cuatrodivinas.seekandsolve.Datos.Data.Companion.storage
import com.cuatrodivinas.seekandsolve.Datos.Desafio
import com.cuatrodivinas.seekandsolve.Datos.Punto
import com.cuatrodivinas.seekandsolve.Datos.Usuario
import com.cuatrodivinas.seekandsolve.R
import com.cuatrodivinas.seekandsolve.databinding.ActivityMainBinding
import org.osmdroid.config.Configuration
import org.osmdroid.library.BuildConfig
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import org.osmdroid.api.IMapController
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.views.overlay.TilesOverlay
import java.io.File
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

class MainActivity : AppCompatActivity(), LocationListener {
    private lateinit var binding: ActivityMainBinding
    private lateinit var desafiosRef: DatabaseReference
    private lateinit var desafiosListener: ValueEventListener
    private var desafiosList: MutableList<Desafio> = mutableListOf()
    private lateinit var map: MapView
    private val REQUEST_PERMISSIONS_REQUEST_CODE = 1
    private lateinit var locationManager: LocationManager
    private lateinit var mapController: IMapController
    private lateinit var marcadores: MutableList<Marker>
    private lateinit var username: String
    private lateinit var fotoUrl: String
    private lateinit var refImg: StorageReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.Base_Theme_SeekAndSolve)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Inicializar Firebase Auth y la database
        auth = Firebase.auth
        database = FirebaseDatabase.getInstance()
    }

    override fun onStart() {
        super.onStart()
        // Verificar si el usuario está logueado
        if(auth.currentUser == null){
            startActivity(Intent(this, LandingActivity::class.java))
            finish()
            return
        }
        marcadores = mutableListOf()
        // Si está logueado, obtener su información
        getUserInfo()
        getDesafios()
    }

    override fun onResume() {
        super.onResume()
        if (::map.isInitialized) {
            map.onResume()
        }
        setupMap()
        setupLocationManager()
        // Listeners de los botones de las opciones principales
        binding.profileLayout.setOnClickListener {
            startActivity(Intent(this, VerPerfil::class.java))
        }
        binding.desafiosButton.setOnClickListener {
            startActivity(Intent(this, VerDesafiosActivity::class.java))
        }
        binding.crearDesafioButton.setOnClickListener {
            startActivity(Intent(this, CrearDesafioActivity::class.java))
        }
        binding.clasificacionesButton.setOnClickListener {
            val intent = Intent(this, RankingActivity::class.java)
            startActivity(intent)
        }
        binding.amigosButton.setOnClickListener {
            startActivity(Intent(this, Amigos::class.java))
        }
    }

    override fun onPause() {
        super.onPause()
        // Remover la suscripción para evitar fugas de memoria
        if(::desafiosRef.isInitialized){
            desafiosRef.removeEventListener(desafiosListener)
        }
        if (::map.isInitialized) {
            map.onPause()
            if (::locationManager.isInitialized) {
                locationManager.removeUpdates(this)
            }
        }
    }

    // Con el UID del usuario, obtener su información
    private fun getUserInfo(){
        val userRef = database.getReference(PATH_USERS).child(auth.currentUser!!.uid)

        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // Convierte el snapshot a la clase User
                val usuario = snapshot.getValue(Usuario::class.java)
                username = usuario!!.nombreUsuario
                fotoUrl = usuario.imagenUrl
                // Inicializar el mapa y el LocationManager
                setupMap()
                setupLocationManager()
                // Establecer el nombre de usuario y la imagen de perfil
                setUsernameAndProfileImage()
            }
            override fun onCancelled(error: DatabaseError) {
                //showToast("No se pudo obtener los datos del usuario")
            }
        })
    }

    // Obtener los desafíos de la base de datos
    private fun getDesafios() {
        desafiosRef = database.getReference(PATH_DESAFIOS)

        // Crear e inicializar el listener a los cambios en los desafíos
        desafiosListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // Convertir el snapshot a una lista de Desafio (sin elementos nulos)
                desafiosList = snapshot.children.mapNotNull { it.getValue(Desafio::class.java) }.toMutableList()
                Log.d("Desafios", "Lista actualizada de desafíos: ${desafiosList.size}")
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("DatabaseError", "Error al suscribirse a los desafíos: ${error.message}")
            }
        }
        // Suscribirse a los cambios en los desafíos
        desafiosRef.addValueEventListener(desafiosListener)
    }

    // Inicializar el mapa y su controlador
    private fun setupMap() {
        Configuration.getInstance().userAgentValue = BuildConfig.APPLICATION_ID
        map = binding.map
        map.setMultiTouchControls(true)
        mapController = map.controller
        mapController.setZoom(15.0)
        val marcador = Marker(map)
        marcador.icon = map.context.getDrawable(R.drawable.ic_location)
        // Inicialmente ponemos el marcador en la ponti, luego se actualiza con la ubicación del usuario
        marcador.position = GeoPoint(4.6390956,-74.0720802)
        marcador.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
        marcador.title = "Posición actual"
        marcadores.add(marcador)
        map.setTileSource(TileSourceFactory.MAPNIK)
        val uiManager = getSystemService(Context.UI_MODE_SERVICE) as UiModeManager
        if(uiManager.nightMode == UiModeManager.MODE_NIGHT_YES){
            map.overlayManager.tilesOverlay.setColorFilter(TilesOverlay.INVERT_COLORS)
        }
    }

    // Si tiene permisos de ubicación, inicializar el LocationManager, sino pedirlos
    private fun setupLocationManager() {
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if (hasLocationPermissions()) {
            initializeMap()
        } else {
            requestLocationPermissions()
        }
    }

    // Verificar si la aplicación tiene permisos de ubicación
    private fun hasLocationPermissions(): Boolean {
        val fineLocationPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
        val coarseLocationPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
        return fineLocationPermission == PackageManager.PERMISSION_GRANTED && coarseLocationPermission == PackageManager.PERMISSION_GRANTED
    }

    // Solicitar permisos de ubicación
    private fun requestLocationPermissions() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION),
            REQUEST_PERMISSIONS_REQUEST_CODE
        )
    }

    // Se ejecuta cuando el usuario acepta o deniega los permisos de ubicación
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

    // Función que se suscribe a las actualizaciones de ubicación
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

    // Establecer el nombre de usuario y la imagen de perfil en la parte superior de la pantalla
    private fun setUsernameAndProfileImage() {
        val usernameText: TextView = binding.usernameText
        val profileImage: ImageView = binding.profileImage
        usernameText.text = username

        refImg = storage.getReference(PATH_IMAGENES).child("${auth.currentUser!!.uid}.jpg")
        refImg.downloadUrl.addOnSuccessListener { uri ->
            Glide.with(this)
                .load(uri) // Carga la imagen desde la URL
                .into(profileImage)
        }.addOnFailureListener { exception ->
            profileImage.imageTintList = ContextCompat.getColorStateList(this, R.color.primaryColor)
        }
    }

    // Se ejecuta cuando la ubicación del usuario cambia
    override fun onLocationChanged(location: Location) {
        setPoint(location.latitude, location.longitude)
    }

    // Actualiza todos los marcadores en el mapa (ubicación actual y desafíos cercanos)
    private fun setPoint(latitud: Double, longitud: Double){
        //Obtener el GeoPoint
        val newPoint = GeoPoint(latitud, longitud)

        //Crear los marcadores
        eliminarDesafiosAnteriores()
        marcadores[0].position = newPoint
        setDesafiosCercanos(latitud, longitud)
        agregarMarcadoresAlMapa()

        //Mover el mapa
        map.controller.animateTo(newPoint)

        //Refrescar cambios en el mapa
        map.invalidate()
    }

    // Elimina los marcadores de la lista y del mapa, excepto el marcador de la posición actual
    private fun eliminarDesafiosAnteriores(){
        map.overlays.clear()
        val marcadorPosicion = marcadores[0]
        marcadores.clear()
        marcadores.add(marcadorPosicion)
    }

    // Agrega marcadores a los desafíos cercanos
    private fun setDesafiosCercanos(latitud: Double, longitud: Double){
        for (desafio in desafiosList){
            val puntoInicialDesafio: Punto = desafio.puntoInicial
            val distancia = calcularDistancia(latitud, longitud, puntoInicialDesafio.latitud, puntoInicialDesafio.longitud)
            if (distancia < 1000){
                val marcador = Marker(map)
                marcador.icon = map.context.getDrawable(R.drawable.desafio_marcador)
                marcador.position = GeoPoint(puntoInicialDesafio.latitud, puntoInicialDesafio.longitud)
                marcador.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                marcador.title = desafio.nombre
                marcadores.add(marcador)
            }
        }
    }

    // Función para calcular la distancia en línea recta entre dos puntos
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

    // Agrega los marcadores al mapa
    private fun agregarMarcadoresAlMapa(){
        map.overlays.addAll(marcadores)
        Log.d("Marcadores", "Marcadores ${map.overlays.size}")
    }

    override fun onProviderEnabled(provider: String) {}
    override fun onProviderDisabled(provider: String) {}
    @Deprecated("Deprecated in Java")
    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
}