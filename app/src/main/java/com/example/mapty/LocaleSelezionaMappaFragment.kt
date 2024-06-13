package com.example.mapty

import android.graphics.Canvas
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import org.osmdroid.api.IMapController
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.events.MapListener
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Overlay

class LocaleSelezionaMappaFragment : Fragment() {

    private lateinit var mapView: MapView
    private lateinit var confirmButton: Button
    private lateinit var selectedLocation: GeoPoint
    private lateinit var marker: Marker

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Configuration.getInstance().load(requireContext(), requireActivity().getSharedPreferences("osmdroid", 0))
        return inflater.inflate(R.layout.fragment_locale_seleziona_mappa, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mapView = view.findViewById(R.id.map)
        confirmButton = view.findViewById(R.id.saveButton)

        mapView.setTileSource(org.osmdroid.tileprovider.tilesource.TileSourceFactory.MAPNIK)
        mapView.setBuiltInZoomControls(true)
        mapView.setMultiTouchControls(true)

        val mapController = mapView.controller
        mapController.setZoom(15.0)
        val initialLocation = GeoPoint(43.6158, 13.5189) // Centro di Ancona
        mapController.setCenter(initialLocation)

        selectedLocation = initialLocation

        marker = Marker(mapView)
        marker.position = initialLocation
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

        confirmButton.setOnClickListener {
            val result = Bundle().apply {
                putDouble("latitude", selectedLocation.latitude)
                putDouble("longitude", selectedLocation.longitude)
            }
            parentFragmentManager.setFragmentResult("location_request", result)
            parentFragmentManager.popBackStack()
        }
    }

    private fun updateMarkerPosition() {
        selectedLocation = mapView.mapCenter as GeoPoint
        marker.position = selectedLocation
        mapView.invalidate()
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