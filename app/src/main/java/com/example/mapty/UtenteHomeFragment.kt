package com.example.mapty

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.health.connect.datatypes.ExerciseRoute
import android.location.Location
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.navigation.fragment.findNavController
import androidx.navigation.findNavController

import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.mapty.recycler_components.AdapterEventi
import com.example.mapty.recycler_components.ItemEvento
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.FirebaseFirestore
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.views.MapView
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay



class UtenteHomeFragment : Fragment() {

    private lateinit var mapView: MapView
    private val REQUEST_PERMISSIONS_REQUEST_CODE = 1
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private lateinit var db: FirebaseFirestore
    private lateinit var recyclerView: RecyclerView
    private lateinit var eventiAdapter: AdapterEventi
    private var eventiList: MutableList<ItemEvento> = mutableListOf()

    // Define the center GeoPoint for Piazza Cavour, Ancona
    private val centerPoint = GeoPoint(43.6167, 13.5186) // Replace with actual coordinates of Piazza Cavour

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Initialize osmdroid configuration
        Configuration.getInstance().load(
            requireActivity().applicationContext,
            androidx.preference.PreferenceManager.getDefaultSharedPreferences(requireActivity().applicationContext)
        )
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        // Initialize Firebase Firestore
        db = FirebaseFirestore.getInstance()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_utente_home, container, false)
        mapView = view.findViewById(R.id.map)

        view.findViewById<Button>(R.id.button_filtro_eventi).setOnClickListener {
            findNavController().navigate(R.id.action_utenteHomeFragment_to_utenteFiltroEventiFragment)
        }
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        checkLocationPermission()
    }

    private fun checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION), REQUEST_PERMISSIONS_REQUEST_CODE)
        } else {
            initializeMap()
            loadEventMarkers()
        }
    }

    private fun initializeMap() {
        mapView.setTileSource(TileSourceFactory.MAPNIK)
        mapView.setMultiTouchControls(true)

        // Set the view of the map and zoom to Piazza Cavour
        mapView.controller.setCenter(centerPoint)
        mapView.controller.setZoom(15.0)
    }

    private fun loadEventMarkers() {
        db.collection("eventos")
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    // Check if the end date of the event has not passed
                    val dataFineTimestamp = document.getTimestamp("dataFine")
                    if (dataFineTimestamp != null && dataFineTimestamp.seconds > System.currentTimeMillis() / 1000) {
                        // Event end date has not passed, read GeoPoint of the location
                        val firebaseGeoPoint = document.getGeoPoint("luogo")
                        if (firebaseGeoPoint != null) {
                            // Convert Firebase GeoPoint to OSMDroid GeoPoint
                            val osmGeoPoint = org.osmdroid.util.GeoPoint(firebaseGeoPoint.latitude, firebaseGeoPoint.longitude)
                            // Add a marker to the map
                            addMarkerToMap(osmGeoPoint)
                        }
                    }
                }
            }
            .addOnFailureListener { exception ->
                Log.w("UtenteHomeFragment", "Error getting documents: ", exception)
            }
    }

    private fun addMarkerToMap(geoPoint: GeoPoint) {
        val marker = Marker(mapView)
        marker.position = geoPoint
        marker.title = "Event Location"
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
        mapView.overlays.add(marker)
        mapView.invalidate() // Refresh the map view
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_PERMISSIONS_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                initializeMap()
                loadEventMarkers()
            } else {
                Snackbar.make(requireView(), "Location permission denied", Snackbar.LENGTH_LONG).show()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Release resources when the fragment is destroyed
        mapView.onDetach()
    }
}