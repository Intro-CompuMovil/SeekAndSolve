package com.cuatrodivinas.seekandsolve.Logica

import android.Manifest
import android.app.UiModeManager
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.cuatrodivinas.seekandsolve.Datos.CarreraActual
import com.cuatrodivinas.seekandsolve.Datos.Data.Companion.MY_PERMISSION_REQUEST_CAMERA
import com.cuatrodivinas.seekandsolve.Datos.Desafio
import com.cuatrodivinas.seekandsolve.Datos.Punto
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
import org.osmdroid.views.overlay.TilesOverlay
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.util.Date
import java.util.Locale
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

class IniciarRutaActivity : AppCompatActivity(), LocationListener {
    private lateinit var binding: ActivityIniciarRutaBinding
    private lateinit var map: MapView
    private val REQUEST_PERMISSIONS_REQUEST_CODE = 1
    private lateinit var locationManager: LocationManager
    private var trayectoRetrofit: RetrofitUrls
    private var geocoderRetrofit: RetrofitUrls
    private lateinit var desafio: Desafio
    private lateinit var carreraActual: CarreraActual
    private lateinit var photoUri: Uri
    private lateinit var marcadorActual: Marker
    private var rutas: MutableList<Polyline>? = null
    private lateinit var lastKnownLocation: Location
    private var puntoFinal = false
    private lateinit var punto: Punto
    private lateinit var puntoInicial: Punto
    private var isFirstLocation = true

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

        // Obtener el desafío y la carrera de la actividad anterior
        desafio = intent.getSerializableExtra("desafio") as Desafio
        carreraActual = intent.getSerializableExtra("carreraActual") as CarreraActual
        rutas = mutableListOf()
        inicializarTextos()
        setupMap()
        setupLocationManager()
    }

    private fun inicializarTextos(){
        binding.tituloRuta.text = "Estas jugando " + desafio.nombre + "!"
        binding.destinoActual.text = "Dirígete al punto inicial"
    }

    private fun setupMap() {
        Configuration.getInstance().userAgentValue = BuildConfig.APPLICATION_ID
        map = binding.map
        map.setMultiTouchControls(true)
        val mapController = map.controller
        mapController.setZoom(17.0)
        val startPoint = GeoPoint(0.0, 0.0)
        marcadorActual = Marker(map)
        marcadorActual.position = startPoint
        marcadorActual.icon = map.context.getDrawable(R.drawable.ic_location)
        marcadorActual.title = "Posición actual"
        map.overlays.add(marcadorActual)
        val uiManager = getSystemService(Context.UI_MODE_SERVICE) as UiModeManager
        if(uiManager.nightMode == UiModeManager.MODE_NIGHT_YES){
            map.overlayManager.tilesOverlay.setColorFilter(TilesOverlay.INVERT_COLORS)
        }
    }

    private fun setPoint(lalitud: Double, longitud: Double){
        //Obtener el GeoPoint
        val newPoint = GeoPoint(lalitud, longitud)

        //Crear los marcadores
        marcadorActual.position = newPoint

        //Mover el mapa
        map.controller.animateTo(newPoint)

        //Refrescar cambios en el mapa
        map.invalidate()
    }

    private fun getRoute(startPoint: GeoPoint, endPoint: GeoPoint, adjustZoom: Boolean, borrarRutaAnterior: Boolean) {
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

                    val path: List<GeoPoint> = decodePolyline(encodedPolyline)
                    drawRoute(path, borrarRutaAnterior)
                    if(adjustZoom){
                        adjustZoomToRoute(path)
                        adjustCenter(path)
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

    private fun drawRoute(path: List<GeoPoint>, borrarRutaAnterior: Boolean) {
        map.controller.setCenter(path[0])
        runOnUiThread {
            if (rutas!!.size > 0 != null && borrarRutaAnterior) {
                for(ruta in rutas!!){
                    map.overlayManager.remove(ruta)  // Eliminar la ruta anterior
                }
            }
            val polyline = Polyline()
            polyline.setPoints(path)
            polyline.color = Color.BLUE
            polyline.width = 10f
            map.overlays.add(polyline)
            rutas!!.add(polyline)
            map.invalidate()
        }
    }

    private fun adjustZoomToRoute(path: List<GeoPoint>) {
        if (path.isEmpty()) return

        // Determinar los límites de latitud y longitud
        val minLat = path.minOf { it.latitude }
        val maxLat = path.maxOf { it.latitude }
        val minLon = path.minOf { it.longitude }
        val maxLon = path.maxOf { it.longitude }

        // Añadir un margen a los límites
        val margin = 0.006 // Margen en grados (~1km dependiendo de la ubicación)
        val adjustedBoundingBox = org.osmdroid.util.BoundingBox(
            maxLat + margin, // Añadir margen al norte
            maxLon + margin, // Añadir margen al este
            minLat - margin, // Añadir margen al sur
            minLon - margin  // Añadir margen al oeste
        )

        // Ajustar el mapa para que enfoque la ruta
        map.zoomToBoundingBox(adjustedBoundingBox, true)
    }

    private fun adjustCenter(path: List<GeoPoint>){
        var totalLat = 0.0
        var totalLon = 0.0
        path.forEach {
            totalLat += it.latitude
            totalLon += it.longitude
        }

        val centerLat = totalLat / path.size
        val centerLon = totalLon / path.size

        // Crear un GeoPoint para el centro de la ruta
        val routeCenter = GeoPoint(centerLat, centerLon)
        map.controller.setCenter(routeCenter)
        // Asegurarse de que el mapa se haya actualizado correctamente
        map.invalidate()
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
        if(isFirstLocation){
            puntoInicial = Punto(location.latitude, location.longitude)
            isFirstLocation = false
        }
        if(!shouldChangeLocation(location.latitude, location.longitude)){
            return
        }
        setPoint(location.latitude, location.longitude)
        val puntoActual = GeoPoint(location.latitude, location.longitude)
        var puntoDestino: GeoPoint? = null
        if(carreraActual.puntosCompletados.isEmpty()){
            puntoDestino = GeoPoint(desafio.puntoInicial.latitud, desafio.puntoInicial.longitud)
        }
        else{
            for(checkpoint in desafio.puntosIntermedios){
                if(!containsLatitudLongitud(carreraActual.puntosCompletados, checkpoint)){
                    puntoDestino = GeoPoint(checkpoint.latitud, checkpoint.longitud)
                    break
                }
            }
            if(puntoDestino == null){
                puntoDestino = GeoPoint(desafio.puntoFinal.latitud, desafio.puntoFinal.longitud)
            }
        }
        getRoute(puntoActual, puntoDestino, true, true)
    }

    private fun shouldChangeLocation(latitud: Double, longitud: Double): Boolean{
        var cambiar = false
        if(::lastKnownLocation.isInitialized && calcularDistancia(latitud, longitud, lastKnownLocation.latitude, lastKnownLocation.longitude) > 2){
            cambiar = true
        }
        else if(!::lastKnownLocation.isInitialized){
            cambiar = true
        }
        lastKnownLocation = Location("")
        lastKnownLocation.latitude = latitud
        lastKnownLocation.longitude = longitud
        return cambiar
    }

    /*override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_PERMISSIONS_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                initializeMap()
            } else {
                // Permisos denegados, no mostrar el mapa
            }
        }
    }*/

    private fun containsLatitudLongitud(puntos: List<Punto>, punto: Punto): Boolean{
        for(point in puntos){
            if(calcularDistancia(point.latitud, point.longitud, punto.latitud, punto.longitud) <= 100){
                return true
            }
        }
        return false
    }

    override fun onResume() {
        super.onResume()
        if (::map.isInitialized) {
            map.onResume()
        }
        var checkpointEncontrado = false
        if(!carreraActual.puntosCompletados.isEmpty()){
            var numero = 1
            for(checkpoint in desafio.puntosIntermedios){
                if(!containsLatitudLongitud(carreraActual.puntosCompletados,checkpoint)){
                    binding.destinoActual.text = "Dirigete al checkpoint $numero!"
                    checkpointEncontrado = true
                    break
                }
                numero++
            }
        }
        if(!checkpointEncontrado  && !carreraActual.puntosCompletados.isEmpty()){
            binding.destinoActual.text = "Dirigete al punto final!!!"
        }
        binding.abandonar.setOnClickListener {
            startActivity(Intent(this, VerDesafiosActivity::class.java))
            finish()
        }
        binding.resolverAcertijo.setOnClickListener {
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                val distanciaInicio = calcularDistancia(desafio.puntoInicial.latitud, desafio.puntoInicial.longitud, lastKnownLocation.latitude, lastKnownLocation.longitude)
                if(distanciaInicio <= 100 && !containsLatitudLongitud(carreraActual.puntosCompletados,desafio.puntoInicial)){
                    carreraActual.puntosCompletados.add(desafio.puntoInicial)
                    Toast.makeText(this, "Comienza tu aventura en el desafio ${desafio.nombre}!", Toast.LENGTH_LONG).show()
                    val puntoInicial = GeoPoint(desafio.puntoInicial.latitud, desafio.puntoInicial.longitud)
                    /*if (!desafio.puntosIntermedios.isEmpty()) {
                        val puntoDestino = GeoPoint(desafio.puntosIntermedios[0].latitud, desafio.puntosIntermedios[0].longitud)
                        getRoute(puntoInicial, puntoDestino, true, true)
                        binding.destinoActual.text = "Dirigete al Checkpoint 1!"
                        carreraActual.distanciaRecorrida += calcularDistancia(this.puntoInicial.latitud, this.puntoInicial.longitud, lastKnownLocation.latitude, lastKnownLocation.longitude)
                        return@setOnClickListener
                    }*/
                    punto = desafio.puntoInicial
                    carreraActual.distanciaRecorrida += calcularDistancia(this.puntoInicial.latitud, this.puntoInicial.longitud, lastKnownLocation.latitude, lastKnownLocation.longitude)

                    pedirPermiso(this, android.Manifest.permission.CAMERA,
                        "Necesitamos acceder a la cámara para tomar la foto y continuar con la experiencia", MY_PERMISSION_REQUEST_CAMERA
                    )
                    return@setOnClickListener
                }
                var checkpointAnterior: Punto? = null
                var puntoActual = Punto(lastKnownLocation.latitude, lastKnownLocation.longitude)
                for(checkpoint in desafio.puntosIntermedios){

                    if(containsLatitudLongitud(carreraActual.puntosCompletados,puntoActual)){
                        Toast.makeText(this, "Este checkpoint ya ha sido completado!", Toast.LENGTH_LONG).show()
                        return@setOnClickListener
                    }
                    if(!containsLatitudLongitud(carreraActual.puntosCompletados,desafio.puntoInicial) && calcularDistancia(checkpoint.latitud, checkpoint.longitud, lastKnownLocation.latitude, lastKnownLocation.longitude) <= 100.0){
                        Toast.makeText(this, "Llega al punto inicial antes de completar este checkpoint!", Toast.LENGTH_LONG).show()
                        return@setOnClickListener
                    }
                    if(!containsLatitudLongitud(carreraActual.puntosCompletados,checkpoint) && calcularDistancia(desafio.puntoFinal.latitud, desafio.puntoFinal.longitud, lastKnownLocation.latitude, lastKnownLocation.longitude) <= 100.0){
                        Toast.makeText(this, "Completa todos los checkpoints antes de finalizar el juego!", Toast.LENGTH_LONG).show()
                        return@setOnClickListener
                    }
                    if(checkpointAnterior != null && !containsLatitudLongitud(carreraActual.puntosCompletados,checkpointAnterior)  && calcularDistancia(checkpoint.latitud, checkpoint.longitud, lastKnownLocation.latitude, lastKnownLocation.longitude) <= 100.0){
                        Toast.makeText(this, "Completa el checkpoint anterior antes de jugar este!", Toast.LENGTH_LONG).show()
                        return@setOnClickListener
                    }
                    if(calcularDistancia(checkpoint.latitud, checkpoint.longitud, lastKnownLocation.latitude, lastKnownLocation.longitude) <= 100.0){
                        punto = checkpoint
                        carreraActual.distanciaRecorrida += calcularDistancia(puntoInicial.latitud, puntoInicial.longitud, lastKnownLocation.latitude, lastKnownLocation.longitude)
                        break
                    }
                    checkpointAnterior = checkpoint
                }
                if(!::punto.isInitialized && calcularDistancia(desafio.puntoFinal.latitud, desafio.puntoFinal.longitud, lastKnownLocation.latitude, lastKnownLocation.longitude) <= 100.0){
                    puntoFinal = true
                    carreraActual.distanciaRecorrida += calcularDistancia(puntoInicial.latitud, puntoInicial.longitud, lastKnownLocation.latitude, lastKnownLocation.longitude)
                    punto = desafio.puntoFinal
                }
                if(puntoFinal && !containsLatitudLongitud(carreraActual.puntosCompletados,desafio.puntosIntermedios.last())){
                    Toast.makeText(this, "Completa el checkpoint anterior antes de jugar el final!", Toast.LENGTH_LONG).show()
                    return@setOnClickListener
                }
            }
            if(!::punto.isInitialized){
                Toast.makeText(this, "No te encuentras en un checkpoint", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }
            pedirPermiso(this, android.Manifest.permission.CAMERA,
                "Necesitamos acceder a la cámara para tomar la foto y continuar con la experiencia", MY_PERMISSION_REQUEST_CAMERA
            )
        }
    }

    private fun pedirPermiso(context: Context, permiso: String, justificacion: String,
                             idCode: Int){
        if(ContextCompat.checkSelfPermission(context, permiso) !=
            PackageManager.PERMISSION_GRANTED){
            if (shouldShowRequestPermissionRationale(permiso)) {
                // Explicar al usuario por qué necesitamos el permiso
                mostrarJustificacion(justificacion) {
                    requestPermissions(arrayOf(permiso), idCode)
                }
            } else {
                requestPermissions(arrayOf(permiso), idCode)
            }
        }
        else{
            // Permiso ya concedido, tomar la foto
            takePicture()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when(requestCode){
            MY_PERMISSION_REQUEST_CAMERA -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    takePicture()
                } else {
                    Toast.makeText(this, "No se puede tomar la foto sin el permiso", Toast.LENGTH_SHORT).show()
                }
            }

            REQUEST_PERMISSIONS_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                    initializeMap()
                } else {
                    // Permisos denegados, no mostrar el mapa
                }
            }
            else -> super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }

    // Función para mostrar la justificación con un diálogo y volver a solicitar el permiso
    private fun mostrarJustificacion(mensaje: String, onAccept: () -> Unit) {
        AlertDialog.Builder(this)
            .setTitle("Justificación de permisos")
            .setMessage(mensaje)
            .setPositiveButton("Aceptar") { dialog, _ ->
                onAccept()
                dialog.dismiss()
            }
            .setNegativeButton("Cancelar") { dialog, _ ->
                dialog.dismiss()
            }
            .create()
            .show()
    }

    private fun takePicture() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        try {
            val photoFile: File = createImageFile()
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
            startActivityForResult(takePictureIntent, MY_PERMISSION_REQUEST_CAMERA)
        } catch (e: ActivityNotFoundException) {
            e.message?. let{ Log.e("PERMISSION_APP",it) }
            Toast.makeText(this, "No es posible abrir la cámara", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            val intent = Intent(this, ResolverAcertijoActivity::class.java)
            intent.putExtra("desafio", desafio)
            intent.putExtra("puntoFinal", puntoFinal)
            intent.putExtra("punto", punto)
            intent.putExtra("carreraActual", carreraActual)
            startActivity(intent)
        } else {
            Toast.makeText(this, "No se pudo tomar la foto", Toast.LENGTH_SHORT).show()
        }
    }

    private fun createImageFile(): File {
        // El timestamp se usa para que el nombre del archivo sea único
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        // El directorio donde se guardará la imagen
        val storageDir: File = getExternalFilesDir(Environment.DIRECTORY_PICTURES)!!
        // Crear el archivo con el nombre "JPEG_YYYYMMDD_HHMMSS.jpg" en el directorio storageDir
        return File.createTempFile(
            "JPEG_${timestamp}_", /* prefix */
            ".jpg", /* suffix */
            storageDir /* directory */
        ).apply {
            // Guardar la URI del archivo para usarla en el Intent de la cámara
            photoUri = FileProvider.getUriForFile(this@IniciarRutaActivity, "com.cuatrodivinas.seekandsolve.fileprovider", this)
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

    override fun onPause() {
        super.onPause()
        if (::map.isInitialized) {
            map.onPause()
        }
    }

    override fun onStop() {
        super.onStop()
        if (::map.isInitialized) {
            map.onDetach()
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