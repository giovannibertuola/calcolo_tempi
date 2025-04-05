package com.example.geocal

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.geocal.databinding.ActivityMainBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.PolylineOptions
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity(), OnMapReadyCallback {
    private lateinit var binding: ActivityMainBinding
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var googleMap: GoogleMap
    private var lastLocation: Location? = null
    private val locationPoints = mutableListOf<LatLng>()
    private val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        
        // Inizializza la mappa
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.mapView) as SupportMapFragment
        mapFragment.getMapAsync(this)

        setupButtons()
        checkLocationPermission()
    }

    private fun setupButtons() {
        binding.btnInizioLavoro.setOnClickListener {
            recordLocation("Inizio Lavoro")
        }

        binding.btnArrivatoPaziente.setOnClickListener {
            recordLocation("Arrivato dal Paziente")
        }

        binding.btnPartenzaPaziente.setOnClickListener {
            recordLocation("Partenza dal Paziente")
        }
    }

    private fun checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        } else {
            startLocationUpdates()
        }
    }

    private fun startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                location?.let {
                    lastLocation = it
                    val currentLatLng = LatLng(it.latitude, it.longitude)
                    locationPoints.add(currentLatLng)
                    updateMap()
                }
            }
        }
    }

    private fun recordLocation(eventType: String) {
        if (lastLocation != null) {
            val currentTime = dateFormat.format(Date())
            val locationInfo = """
                Evento: $eventType
                Data/Ora: $currentTime
                Coordinate: ${lastLocation!!.latitude}, ${lastLocation!!.longitude}
            """.trimIndent()
            
            binding.tvStatus.text = locationInfo
            
            // Qui aggiungeremo la logica per salvare i dati su Supabase
            saveToSupabase(eventType, currentTime, lastLocation!!)
        } else {
            Toast.makeText(this, "Posizione non disponibile", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateMap() {
        if (locationPoints.isNotEmpty()) {
            val currentPoint = locationPoints.last()
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentPoint, 15f))
            
            if (locationPoints.size > 1) {
                val polylineOptions = PolylineOptions()
                    .addAll(locationPoints)
                    .color(ContextCompat.getColor(this, android.R.color.holo_blue_dark))
                    .width(10f)
                googleMap.addPolyline(polylineOptions)
            }
        }
    }

    private fun saveToSupabase(eventType: String, timestamp: String, location: Location) {
        // Implementare la logica di salvataggio su Supabase
        // TODO: Implementare la connessione a Supabase
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            googleMap.isMyLocationEnabled = true
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startLocationUpdates()
            } else {
                Toast.makeText(this, "Permesso di localizzazione negato", Toast.LENGTH_SHORT).show()
            }
        }
    }
} 