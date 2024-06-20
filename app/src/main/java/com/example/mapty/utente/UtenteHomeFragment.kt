package com.example.mapty.utente

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
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
import androidx.core.os.bundleOf
import androidx.recyclerview.widget.RecyclerView
import com.example.mapty.R
import com.example.mapty.recycler_components.AdapterEventi
import com.example.mapty.recycler_components.ItemEvento
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.firestore.FirebaseFirestore
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.views.MapView
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.overlay.Marker


class UtenteHomeFragment : Fragment() {

    private lateinit var mapView: MapView
    private val REQUEST_PERMISSIONS_REQUEST_CODE = 1
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private lateinit var db: FirebaseFirestore
    private lateinit var recyclerView: RecyclerView
    private lateinit var eventiAdapter: AdapterEventi
    private var eventiList: MutableList<ItemEvento> = mutableListOf()

    // Definisce Piazza Cavour come centro del GeoPoint
    private val centerPoint = GeoPoint(43.6167, 13.5186)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Configuration.getInstance().load(
            requireActivity().applicationContext,
            androidx.preference.PreferenceManager.getDefaultSharedPreferences(requireActivity().applicationContext)
        )
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())


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


        mapView.controller.setCenter(centerPoint)
        mapView.controller.setZoom(15.0)
    }

    private fun loadEventMarkers() {
        db.collection("eventos")
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {

                    val dataFineMillis = document.getLong("dataFine")
                    if (dataFineMillis != null && dataFineMillis > System.currentTimeMillis()) {

                        val firebaseGeoPoint = document.getGeoPoint("luogo")
                        val nomeEvento = document.getString("nomeEvento") ?: ""
                        val eventoId = document.id

                        if (firebaseGeoPoint != null) {

                            val osmGeoPoint = org.osmdroid.util.GeoPoint(firebaseGeoPoint.latitude, firebaseGeoPoint.longitude)

                            addMarkerToMap(osmGeoPoint, nomeEvento, eventoId)
                        }
                    }
                }
            }
            .addOnFailureListener { exception ->
                Log.w("UtenteHomeFragment", "Error getting documents: ", exception)
            }
    }

    private fun getResizedBitmap(drawableId: Int, width: Int, height: Int): Bitmap {
        val drawable = ContextCompat.getDrawable(requireContext(), drawableId)
        val bitmap = (drawable as BitmapDrawable).bitmap
        return Bitmap.createScaledBitmap(bitmap, width, height, false)
    }

    private fun addMarkerToMap(geoPoint: GeoPoint, nomeEvento: String, eventoId: String) {
        val marker = Marker(mapView)
        marker.position = geoPoint
        marker.title = nomeEvento


        val resizedIcon = getResizedBitmap(R.drawable.marker, 64, 64)
        marker.icon = BitmapDrawable(resources, resizedIcon)

        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
        marker.setOnMarkerClickListener { _, _ ->
            val bundle = bundleOf("eventoId" to eventoId)
            findNavController().navigate(R.id.action_utenteHomeFragment_to_vistaEventoFragment, bundle)
            true
        }
        mapView.overlays.add(marker)
        mapView.invalidate()
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

        mapView.onDetach()
    }
}

