package com.example.mapty.locale

import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.mapty.R
import com.google.firebase.firestore.FirebaseFirestore
import org.osmdroid.config.Configuration
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

class LocaleSelezionaMappaFragment : Fragment() {

    private lateinit var mapView: MapView
    private lateinit var confirmButton: Button
    private lateinit var selectedLocation: GeoPoint
    private lateinit var marker: Marker
    private lateinit var db: FirebaseFirestore

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Configuration.getInstance().load(requireContext(), requireActivity().getSharedPreferences("osmdroid", 0))
        return inflater.inflate(R.layout.fragment_locale_seleziona_mappa, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        db = FirebaseFirestore.getInstance()

        mapView = view.findViewById(R.id.map)
        confirmButton = view.findViewById(R.id.saveButton)

        mapView.setTileSource(org.osmdroid.tileprovider.tilesource.TileSourceFactory.MAPNIK)
        mapView.setBuiltInZoomControls(true)
        mapView.setMultiTouchControls(true)

        val mapController = mapView.controller
        mapController.setZoom(15.0)

        val localeName = arguments?.getString("localeName")

        if (localeName != null) {
            getLocaleCoordinates(localeName) { geoPoint ->
                setInitialLocation(geoPoint)
            }
        } else {
            val initialLocation = GeoPoint(43.6158, 13.5189)
            setInitialLocation(initialLocation)
        }

        confirmButton.setOnClickListener {
            val result = Bundle().apply {
                putDouble("latitude", selectedLocation.latitude)
                putDouble("longitude", selectedLocation.longitude)
            }
            parentFragmentManager.setFragmentResult("location_request", result)
            parentFragmentManager.popBackStack()
        }

    }

    private fun setInitialLocation(geoPoint: GeoPoint) {
        val mapController = mapView.controller
        mapController.setCenter(geoPoint)
        selectedLocation = geoPoint

        marker = Marker(mapView)
        marker.position = geoPoint

        val resizedIcon = getResizedBitmap(R.drawable.marker, 64, 64)
        marker.icon = BitmapDrawable(resources, resizedIcon)

        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
        mapView.overlays.add(marker)

        mapView.setMapListener(object : org.osmdroid.events.MapListener {
            override fun onScroll(event: org.osmdroid.events.ScrollEvent?): Boolean {
                updateMarkerPosition()
                return true
            }

            override fun onZoom(event: org.osmdroid.events.ZoomEvent?): Boolean {
                updateMarkerPosition()
                return true
            }
        })
    }

    private fun getLocaleCoordinates(localeName: String, callback: (GeoPoint) -> Unit) {
        db.collection("locali").whereEqualTo("nomeLocale", localeName).get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    val latitude = document.getDouble("latitude")
                    val longitude = document.getDouble("longitude")
                    if (latitude != null && longitude != null) {
                        callback(GeoPoint(latitude, longitude))
                        return@addOnSuccessListener
                    }
                }
                callback(GeoPoint(43.6158, 13.5189)) // Piazza Cavour
            }
            .addOnFailureListener {
                callback(GeoPoint(43.6158, 13.5189))
            }
    }

    private fun updateMarkerPosition() {
        selectedLocation = mapView.mapCenter as GeoPoint
        marker.position = selectedLocation
        mapView.invalidate()
    }

    private fun getResizedBitmap(drawableId: Int, width: Int, height: Int): Bitmap {
        val drawable = ContextCompat.getDrawable(requireContext(), drawableId)
        val bitmap = (drawable as BitmapDrawable).bitmap
        return Bitmap.createScaledBitmap(bitmap, width, height, false)
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

}
