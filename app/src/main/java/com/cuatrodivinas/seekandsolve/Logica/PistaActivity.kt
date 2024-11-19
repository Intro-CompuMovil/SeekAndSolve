package com.cuatrodivinas.seekandsolve.Logica

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.cuatrodivinas.seekandsolve.R
import kotlin.random.Random

class PistaActivity : AppCompatActivity(), SensorEventListener {

    private lateinit var subTituloPista: TextView
    private lateinit var textoPista: TextView
    private lateinit var btnVolver: Button

    private lateinit var sensorManager: SensorManager
    private var selectedSensor: Sensor? = null

    private val sensorsList = listOf(Sensor.TYPE_ACCELEROMETER, Sensor.TYPE_GYROSCOPE, Sensor.TYPE_PROXIMITY, Sensor.TYPE_LIGHT)
    private var sensorActivated = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pista)

        subTituloPista = findViewById(R.id.acertijo)
        textoPista = findViewById(R.id.pista)
        btnVolver = findViewById(R.id.volver)

        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager

        // Seleccionar sensor aleatorio y mostrar el acertijo correspondiente
        selectRandomSensor()

        btnVolver.setOnClickListener {
            finish()
        }
    }

    private fun selectRandomSensor() {
        // Elegir un sensor aleatorio de la lista
        val randomSensorType = sensorsList.random()
        selectedSensor = sensorManager.getDefaultSensor(randomSensorType)

        if (selectedSensor == null) {
            Toast.makeText(this, "El sensor no está disponible", Toast.LENGTH_SHORT).show()
            return
        }

        // Mostrar el mensaje según el sensor seleccionado
        when (randomSensorType) {
            Sensor.TYPE_ACCELEROMETER -> subTituloPista.text = "Agita el teléfono para revelar la pista."
            Sensor.TYPE_GYROSCOPE -> subTituloPista.text = "Gira el teléfono como si desenrollaras un pergamino."
            Sensor.TYPE_PROXIMITY -> subTituloPista.text = "Acerca tu mano al sensor de proximidad."
            Sensor.TYPE_LIGHT -> subTituloPista.text = "Lleva el teléfono a un lugar con más luz."
        }
    }

    override fun onResume() {
        super.onResume()
        selectedSensor?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null || sensorActivated) return

        when (event.sensor.type) {
            Sensor.TYPE_ACCELEROMETER -> {
                val acceleration = Math.sqrt(
                    (event.values[0] * event.values[0] +
                            event.values[1] * event.values[1] +
                            event.values[2] * event.values[2]).toDouble()
                )
                if (acceleration > 15) { // Umbral de agitación
                    revealPista()
                }
            }
            Sensor.TYPE_GYROSCOPE -> {
                if (event.values[2] > 3) { // Detección de giro
                    revealPista()
                }
            }
            Sensor.TYPE_PROXIMITY -> {
                if (event.values[0] < event.sensor.maximumRange) { // Mano cerca
                    revealPista()
                }
            }
            Sensor.TYPE_LIGHT -> {
                if (event.values[0] > 50) { // Más luz
                    revealPista()
                }
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // No es necesario manejar cambios de precisión para este caso
    }

    private fun revealPista() {
        sensorActivated = true
        textoPista.text = "¡Pista revelada! Papas con mayonesa."
        Toast.makeText(this, "¡Has completado el acertijo!", Toast.LENGTH_SHORT).show()
        sensorManager.unregisterListener(this) // Detener el sensor
    }
}

