package com.cuatrodivinas.seekandsolve.Logica

import android.Manifest
import android.app.UiModeManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.cuatrodivinas.seekandsolve.Datos.Desafio
import com.cuatrodivinas.seekandsolve.Datos.RetrofitOsmClient
import com.cuatrodivinas.seekandsolve.Datos.RetrofitUrls
import com.cuatrodivinas.seekandsolve.Datos.RouteResponse
import com.cuatrodivinas.seekandsolve.R
import com.cuatrodivinas.seekandsolve.databinding.ActivityTrayectoDesafioBinding
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

class TrayectoDesafioActivity : AppCompatActivity(), LocationListener {
    private lateinit var binding: ActivityTrayectoDesafioBinding
    private lateinit var map: MapView
    private val REQUEST_PERMISSIONS_REQUEST_CODE = 1
    private lateinit var locationManager: LocationManager
    private var retrofitUrls: RetrofitUrls
    private lateinit var desafio: Desafio
    private lateinit var marcadorActual: Marker
    private lateinit var listaElementos: MutableList<String>
    private var rutas: MutableList<Polyline>? = null
    private var isFirstLocation: Boolean = true

    init{
        val retrofit = RetrofitOsmClient.urlRuta()
        retrofitUrls = retrofit.create(RetrofitUrls::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTrayectoDesafioBinding.inflate(layoutInflater)
        setContentView(binding.root)
        desafio = intent.getSerializableExtra("desafio") as Desafio
        val textoTitulo = "Ruta de " + desafio.nombre
        binding.tituloTrayecto.text = textoTitulo
        rutas = mutableListOf()
        inicializarSpinners()
        setupMap()
        setupLocationManager()
        /*val puntoInicial = GeoPoint(desafio.puntoInicial.latitud, desafio.puntoInicial.longitud)
        var puntoAnterior = puntoInicial
        for(checkpoint in desafio.puntosIntermedios){
            val puntoSiguiente = GeoPoint(checkpoint.latitud, checkpoint.longitud)
            getRoute(puntoAnterior, puntoSiguiente, false, false)
            puntoAnterior = puntoSiguiente
        }
        val puntoFinal = GeoPoint(desafio.puntoFinal.latitud, desafio.puntoFinal.longitud)
        getRoute(puntoAnterior, puntoFinal, true, false)*/
        eventoRutaSpinners()
    }

    private fun inicializarSpinners(){
        listaElementos = mutableListOf()
        listaElementos.add("Punto inicial")
        listaElementos.addAll(desafio.puntosIntermedios!!.mapIndexed { index, punto ->
            "Checkpoint ${index+1}"
        }.toMutableList())
        listaElementos.add("Punto final")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, listaElementos)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.puntoInicial.adapter = adapter
        binding.puntoFinal.setSelection(0)
        val adapter2 = ArrayAdapter(this, android.R.layout.simple_spinner_item, listaElementos)
        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.puntoFinal.adapter = adapter2
        binding.puntoFinal.setSelection(1)
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

    private fun getRoute(startPoint: GeoPoint, endPoint: GeoPoint, adjustZoom: Boolean, borrarRutaAnterior: Boolean) {
        val getRoute = retrofitUrls.getRoute(startPoint.longitude, startPoint.latitude, endPoint.longitude, endPoint.latitude)

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
    
    private fun eventoRutaSpinners(){
        binding.puntoInicial.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
               eventoInicial(position)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Acción si no se selecciona nada
                Toast.makeText(applicationContext, "No seleccionaste nada", Toast.LENGTH_SHORT).show()
            }
        }

        binding.puntoFinal.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                eventoFinal(position)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Acción si no se selecciona nada
                Toast.makeText(applicationContext, "No seleccionaste nada", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun eventoInicial(position: Int){
        var positionFinal = binding.puntoFinal.selectedItemPosition
        if(positionFinal == position){
            Toast.makeText(this@TrayectoDesafioActivity, "No se puede hacer una ruta con mismo origen y destino", Toast.LENGTH_LONG).show()
            return
        }
        if(positionFinal-1 != position){
            Toast.makeText(this@TrayectoDesafioActivity, "Realiza la ruta entre dos puntos seguidos", Toast.LENGTH_LONG).show()
            return
        }
        var puntoOrigen: GeoPoint
        if(position == 0){
            puntoOrigen = GeoPoint(desafio.puntoInicial!!.latitud, desafio.puntoInicial!!.longitud)
        }
        else if (position == listaElementos.size - 1){
            puntoOrigen  = GeoPoint(desafio.puntoFinal!!.latitud, desafio.puntoFinal!!.longitud)
        }
        else{
            puntoOrigen  = GeoPoint(desafio.puntosIntermedios!![position-1].latitud, desafio.puntosIntermedios!![position-1].longitud)
        }
        var puntoDestino: GeoPoint

        if(positionFinal == 0){
            puntoDestino = GeoPoint(desafio.puntoInicial!!.latitud, desafio.puntoInicial!!.longitud)
        }
        else if (positionFinal == listaElementos.size - 1){
            puntoDestino  = GeoPoint(desafio.puntoFinal!!.latitud, desafio.puntoFinal!!.longitud)
        }
        else{
            puntoDestino  = GeoPoint(desafio.puntosIntermedios!![positionFinal-1].latitud, desafio.puntosIntermedios!![positionFinal-1].longitud)
        }
        getRoute(puntoOrigen, puntoDestino, true, true)
    }

    private fun eventoFinal(position: Int){
        var positionInicial = binding.puntoInicial.selectedItemPosition
        if(positionInicial == position){
            Toast.makeText(this@TrayectoDesafioActivity, "No se puede hacer una ruta con mismo origen y destino", Toast.LENGTH_LONG).show()
            return
        }
        if(positionInicial != position-1){
            Toast.makeText(this@TrayectoDesafioActivity, "Realiza la ruta entre dos puntos seguidos", Toast.LENGTH_LONG).show()
            return
        }
        var puntoDestino: GeoPoint
        if(position == 0){
            puntoDestino = GeoPoint(desafio.puntoInicial!!.latitud, desafio.puntoInicial!!.longitud)
        }
        else if (position == listaElementos.size - 1){
            puntoDestino  = GeoPoint(desafio.puntoFinal!!.latitud, desafio.puntoFinal!!.longitud)
        }
        else{
            puntoDestino  = GeoPoint(desafio.puntosIntermedios!![position-1].latitud, desafio.puntosIntermedios!![position-1].longitud)
        }
        var puntoOrigen: GeoPoint

        if(positionInicial == 0){
            puntoOrigen = GeoPoint(desafio.puntoInicial!!.latitud, desafio.puntoInicial!!.longitud)
        }
        else if (positionInicial == listaElementos.size - 1){
            puntoOrigen  = GeoPoint(desafio.puntoFinal!!.latitud, desafio.puntoFinal!!.longitud)
        }
        else{
            puntoOrigen  = GeoPoint(desafio.puntosIntermedios!![positionInicial-1].latitud, desafio.puntosIntermedios!![positionInicial-1].longitud)
        }
        getRoute(puntoOrigen, puntoDestino, true, true)
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
            setPoint(location.latitude, location.longitude)
            isFirstLocation = false
        }
    }

    private fun setPoint(latitud: Double, longitud: Double){
        //Obtener el GeoPoint
        val newPoint = GeoPoint(latitud, longitud)

        //cambiar Ubicacion
        marcadorActual.position = newPoint

        //Mover el mapa
        map.controller.animateTo(newPoint)

        //Refrescar cambios en el mapa
        map.invalidate()
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
            intentIniciarDesafio.putExtra("desafio", desafio)
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