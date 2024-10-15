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
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.cuatrodivinas.seekandsolve.R
import org.json.JSONArray
import org.osmdroid.config.Configuration
import org.osmdroid.library.BuildConfig
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import org.json.JSONException
import org.json.JSONObject
import kotlin.properties.Delegates
import org.osmdroid.api.IMapController
import java.io.IOException
import java.io.InputStream
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

class MainActivity : AppCompatActivity(), LocationListener {
    private lateinit var map: MapView
    private val REQUEST_PERMISSIONS_REQUEST_CODE = 1
    private lateinit var locationManager: LocationManager
    private lateinit var googleSignInClient: GoogleSignInClient
    private var isFirstLocation = true
    private lateinit var mapController: IMapController
    private lateinit var marcadores: MutableList<Marker>

    private var id by Delegates.notNull<Int>()
    private lateinit var nombre: String
    private lateinit var username: String
    private lateinit var correo: String
    private lateinit var contrasena: String
    private lateinit var fotoUrl: String
    private lateinit var fechaNacimiento: String


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.Base_Theme_SeekAndSolve)
        setContentView(R.layout.activity_main)
        val bundle = intent.extras
        if (bundle != null) {
            id = bundle.getInt("id")
            nombre = bundle.getString("nombre")!!
            username = bundle.getString("username")!!
            correo = bundle.getString("correo")!!
            contrasena = bundle.getString("contrasena")!!
            fotoUrl = bundle.getString("fotoUrl")?: ""
            fechaNacimiento = bundle.getString("fechaNacimiento")!!
        }
        else{
            val user = getLastRegisteredUser()
            if(user != null){
                id = 0
                username = user.getString("username")
                nombre = user.getString("name")
                contrasena = user.getString("password")
                correo = user.getString("email")
                fechaNacimiento = user.getString("fechaNacimiento")
                val sessionType = user.getString("signInType")
                fotoUrl = if (user.has("photoUrl") && !user.isNull("photoUrl")) user.getString("photoUrl") else ""
            }

        }
        marcadores = mutableListOf()
        setupGoogleSignInClient()
        checkSession()
        setupMap()
        setupLocationManager()

        setUsernameText()
        setupProfileLayout()

        setupVerDesafiosButton()
        setupCrearDesafioButton()
        setupRankingButton()
        setupAmigosButton()
    }

    private fun getLastRegisteredUser(): JSONObject? {
        try {
            val inputStream: InputStream = openFileInput("user_data.json")
            val size: Int = inputStream.available()
            val buffer = ByteArray(size)
            inputStream.read(buffer)
            inputStream.close()
            val json = String(buffer, Charsets.UTF_8)
            val jsonArray = JSONArray(json)
            if (jsonArray.length() > 0) {
                // Devuelve el último usuario registrado
                return jsonArray.getJSONObject(jsonArray.length() - 1)
            }
        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        return null
    }

    private fun setupAmigosButton() {
        val amigosButton: Button = findViewById(R.id.amigosButton)
        amigosButton.setOnClickListener {
            val intent = Intent(this, Amigos::class.java)
            val bundle = Bundle()
            bundle.putInt("id", id)
            bundle.putString("nombre", nombre)
            bundle.putString("username", username)
            bundle.putString("correo", correo)
            bundle.putString("contrasena", contrasena)
            bundle.putString("fotoUrl", fotoUrl)
            bundle.putString("fechaNacimiento", fechaNacimiento)
            intent.putExtras(bundle)
            startActivity(intent)
        }
    }

    private fun setupCrearDesafioButton() {
        val crearDesafioButton: Button = findViewById(R.id.crearDesafioButton)
        crearDesafioButton.setOnClickListener {
            val intent = Intent(this, CrearDesafioActivity::class.java)
            val bundle = Bundle()
            bundle.putInt("id", id)
            bundle.putString("nombre", nombre)
            bundle.putString("username", username)
            bundle.putString("correo", correo)
            bundle.putString("contrasena", contrasena)
            bundle.putString("fotoUrl", fotoUrl)
            bundle.putString("fechaNacimiento", fechaNacimiento)
            intent.putExtras(bundle)
            startActivity(intent)
        }
    }

    private fun setupVerDesafiosButton() {
        val verDesafiosButton: Button = findViewById(R.id.desafiosButton)
        verDesafiosButton.setOnClickListener {
            val intent = Intent(this, VerDesafiosActivity::class.java)
            val bundle = Bundle()
            bundle.putInt("id", id)
            bundle.putString("nombre", nombre)
            bundle.putString("username", username)
            bundle.putString("correo", correo)
            bundle.putString("contrasena", contrasena)
            bundle.putString("fotoUrl", fotoUrl)
            bundle.putString("fechaNacimiento", fechaNacimiento)
            intent.putExtras(bundle)
            startActivity(intent)
        }
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
                }
            }
        )
    }

    private fun setupGoogleSignInClient() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)
    }

    private fun setupProfileLayout() {
        val profileLayout: LinearLayout = findViewById(R.id.profileLayout)
        profileLayout.setOnClickListener {
            val intent = Intent(this, VerPerfil::class.java)
            val bundle = Bundle()
            bundle.putInt("id", id)
            bundle.putString("nombre", nombre)
            bundle.putString("username", username)
            bundle.putString("correo", correo)
            bundle.putString("contrasena", contrasena)
            bundle.putString("fotoUrl", fotoUrl)
            bundle.putString("fechaNacimiento", fechaNacimiento)
            intent.putExtras(bundle)
            startActivity(intent)
        }
    }

    private fun setupRankingButton() {
        val clasificacionesButton: Button = findViewById(R.id.clasificacionesButton)
        clasificacionesButton.setOnClickListener {
            val intent = Intent(this, RankingActivity::class.java)
            startActivity(intent)
        }
    }

    private fun checkSession() {
        val sharedPreferences = getSharedPreferences("user_session", Context.MODE_PRIVATE)
        val sessionId = sharedPreferences.getString("session_id", null)
        if (sessionId == null) {
            val intent = Intent(this, LandingActivity::class.java)
            startActivity(intent)
            finish()
        }
    }


    private fun setupMap() {
        Configuration.getInstance().userAgentValue = BuildConfig.APPLICATION_ID
        map = findViewById(R.id.map)
        map.setMultiTouchControls(true)
        mapController = map.controller
        mapController.setZoom(15.0)
        val marcador = Marker(map)
        marcador.icon = map.context.getDrawable(R.drawable.ic_location)
        marcador.position = GeoPoint(0.0, 0.0)
        marcador.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
        marcador.title = "Posición actual"
        marcadores.add(marcador)
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

    private fun setDesafiosCercanos(lalitud: Double, longitud: Double){
        val desafios = getDesafios()
        if (desafios != null) {
            for (i in 0 until desafios.length()){
                val desafio = desafios.getJSONObject(i)
                val puntoInicial = desafio.getJSONObject("puntoInicial")
                val latitudPunto = puntoInicial.getDouble("latitud")
                val longitudPunto = puntoInicial.getDouble("longitud")
                val distancia = calcularDistancia(lalitud, longitud, latitudPunto, longitudPunto)
                if(distancia <= 3000.0){
                    val marcadorDesafio = Marker(map)
                    marcadorDesafio.icon = map.context.getDrawable(R.drawable.desafio_marcador)
                    marcadorDesafio.position = GeoPoint(latitudPunto, longitudPunto)
                    marcadorDesafio.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                    marcadorDesafio.title = desafio.getString("nombre")
                    marcadores.add(marcadorDesafio)
                }
            }
        }
    }

    fun calcularDistancia(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val radioTierra = 6371000.0  // Radio de la Tierra en metros
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)

        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2) * sin(dLon / 2)

        val c = 2 * atan2(sqrt(a), sqrt(1 - a))

        return radioTierra * c  // Distancia en metros
    }

    private fun getDesafios(): JSONArray? {
        try {
            val inputStream: InputStream = assets.open("desafios.json")
            val size: Int = inputStream.available()
            val buffer = ByteArray(size)
            inputStream.read(buffer)
            inputStream.close()
            val json = String(buffer, Charsets.UTF_8)
            val desafios = JSONObject(json)
            val jsonArray = desafios.getJSONArray("desafios")
            return jsonArray
        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        return null
    }

    private fun setupLocationManager() {
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if (hasLocationPermissions()) {
            initializeMap()
        } else {
            requestLocationPermissions()
        }
    }

    private fun setUsernameText() {
        val usernameText: TextView = findViewById(R.id.usernameText)
        val profileImage: ImageView = findViewById(R.id.profileImage)
        usernameText.text = username
        if (fotoUrl != "") {
            Glide.with(this)
                .load(fotoUrl)
                .override(24, 24) // Establecer el tamaño de la imagen en 24x24 px
                .circleCrop() // Para hacer la imagen circular
                .into(profileImage) // Establecer la imagen en el ImageView
            profileImage.background = null
        } else {
            profileImage.imageTintList = ContextCompat.getColorStateList(this, R.color.primaryColor)
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