package com.example.mapty

import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import android.location.Location.distanceBetween
import androidx.core.app.ActivityCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.mapty.recycler_components.ItemEvento
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import android.Manifest
import android.content.ContentValues.TAG
import android.health.connect.datatypes.ExerciseRoute
import android.location.Location
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import java.util.*

class VistaEventoFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    private lateinit var cuoreUtenteImageView: ImageView
    private lateinit var eventoId: String

    private var isPreferito = false
    private var isUtenteLocale = false

    private lateinit var textViewNomeEvento: TextView
    private lateinit var textViewTipoEvento: TextView
    private lateinit var textViewDataEvento: TextView
    private lateinit var textViewOraInizioEvento: TextView
    private lateinit var textViewPrezzoEvento: TextView
    private lateinit var textViewNomeLocaleEvento: TextView
    private lateinit var textViewNumeroTelefonoEvento: TextView
    private lateinit var textViewLuogoEvento: TextView
    private lateinit var textViewDescrizioneEvento: TextView

    private lateinit var fabCamera: FloatingActionButton
    private lateinit var tornaIndietroButton: Button

    private var userLocation: GeoPoint? = null

    private val REQUEST_LOCATION_PERMISSION = 1

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_vista_evento, container, false)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        cuoreUtenteImageView = view.findViewById(R.id.cuore_utente)
        fabCamera = view.findViewById(R.id.camera_fab)
        tornaIndietroButton = view.findViewById(R.id.torna_indietro)

        textViewNomeEvento = view.findViewById(R.id.textViewNomeEvento)
        textViewTipoEvento = view.findViewById(R.id.textViewTipoEvent)
        textViewDataEvento = view.findViewById(R.id.textViewDataEvento)
        textViewOraInizioEvento = view.findViewById(R.id.textViewOraInizioEvento)
        textViewPrezzoEvento = view.findViewById(R.id.textViewPrezzoEvento)
        textViewNomeLocaleEvento = view.findViewById(R.id.textViewNomeLocaleEvento)
        textViewNumeroTelefonoEvento = view.findViewById(R.id.textViewNumeroTelefonoEvento)
        textViewLuogoEvento = view.findViewById(R.id.textViewLuogoEvento)
        textViewDescrizioneEvento = view.findViewById(R.id.textViewDescrizioneEvento)

        eventoId = arguments?.getString("eventoId") ?: ""

        tornaIndietroButton.setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }

        textViewNomeLocaleEvento.setOnClickListener {
            val currentUser = auth.currentUser
            if (currentUser != null) {
                val emailUtente = currentUser.email
                if (emailUtente != null) {
                    db.collection("locali")
                        .whereEqualTo("email", emailUtente)
                        .get()
                        .addOnSuccessListener { documents ->
                            if (documents.isEmpty) {
                                val bundle = bundleOf("nomeLocale" to textViewNomeLocaleEvento.text.toString())
                                findNavController().navigate(R.id.action_vistaEventoFragment_to_utentePaginaLocaleFragment, bundle)
                            } else {
                                // Do nothing if the user is a locale
                            }
                        }
                        .addOnFailureListener { exception ->
                            Log.e(TAG, "Errore nel verificare il tipo di utente", exception)
                        }
                }
            }
        }

        caricaDettagliEvento { evento ->
            evento?.let {
                textViewNomeEvento.text = it.nomeEvento
                textViewTipoEvento.text = it.tipo
                textViewDataEvento.text = formatDate(it.data)
                textViewOraInizioEvento.text = formatTime(it.data)
                textViewPrezzoEvento.text = "${it.prezzo}€"
                textViewNomeLocaleEvento.text = it.nomeLocale
                textViewNumeroTelefonoEvento.text = it.numeroTelefono
                textViewDescrizioneEvento.text = it.descrizione

                val geoPoint = it.luogo ?: GeoPoint(0.0, 0.0)
                textViewLuogoEvento.text = "Latitudine: ${geoPoint.latitude}, \n" +
                        "Longitudine: ${geoPoint.longitude}"

                verificaUtenteLocale {
                    if (!isUtenteLocale) {
                        // Verifica la posizione e la data
                        getUserLocation {
                            checkProximityAndDate(it.data, it.dataFine, geoPoint)
                        }
                    }
                }
            }
        }

        cuoreUtenteImageView.setOnClickListener {
            if (!isUtenteLocale) {
                if (isPreferito) {
                    rimuoviEventoPreferito()
                } else {
                    aggiungiEventoPreferito()
                }
            }
        }

        return view
    }

    private fun verificaUtenteLocale(onComplete: () -> Unit) {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val emailUtente = currentUser.email
            if (emailUtente != null) {
                db.collection("locali")
                    .whereEqualTo("email", emailUtente)
                    .get()
                    .addOnSuccessListener { documents ->
                        isUtenteLocale = !documents.isEmpty
                        if (!isUtenteLocale) {
                            cuoreUtenteImageView.setImageResource(R.drawable.round_favorite_border)
                        }
                        onComplete()
                    }
                    .addOnFailureListener { exception ->
                        Log.e(TAG, "Errore nel verificare il tipo di utente", exception)
                        onComplete()
                    }
            } else {
                onComplete()
            }
        } else {
            onComplete()
        }
    }

    private fun caricaDettagliEvento(onEventoCaricato: (ItemEvento?) -> Unit) {
        GlobalScope.launch(Dispatchers.Main) {
            try {
                val document = db.collection("eventos").document(eventoId).get().await()

                if (document != null && document.exists()) {
                    val evento = document.toObject(ItemEvento::class.java)
                    if (evento != null) {
                        onEventoCaricato(evento)
                    } else {
                        Toast.makeText(requireContext(), "Evento non trovato", Toast.LENGTH_SHORT).show()
                        onEventoCaricato(null)
                    }
                } else {
                    Toast.makeText(requireContext(), "Documento non trovato", Toast.LENGTH_SHORT).show()
                    onEventoCaricato(null)
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Errore nel recupero dei dati: ${e.message}", Toast.LENGTH_SHORT).show()
                Log.e(TAG, "Errore nel recupero dei dati", e)
                onEventoCaricato(null)
            }
        }
    }

    private fun formatDate(timestamp: Long): String {
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val date = Date(timestamp)
        return sdf.format(date)
    }

    private fun formatTime(timestamp: Long): String {
        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
        val date = Date(timestamp)
        return sdf.format(date)
    }

    private fun aggiungiEventoPreferito() {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val userId = currentUser.uid

            db.collection("utenti")
                .document(userId)
                .collection("eventi_preferiti")
                .document(eventoId) // Use the eventId as the document ID for consistency
                .set(mapOf("idEvento" to eventoId))
                .addOnSuccessListener {
                    isPreferito = true
                    cuoreUtenteImageView.setImageResource(R.drawable.round_favorite)
                }
                .addOnFailureListener { exception ->
                    Log.e(TAG, "Errore nell'aggiungere l'evento ai preferiti", exception)
                }
        }
    }

    private fun rimuoviEventoPreferito() {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val userId = currentUser.uid

            db.collection("utenti")
                .document(userId)
                .collection("eventi_preferiti")
                .document(eventoId)
                .delete()
                .addOnSuccessListener {
                    isPreferito = false
                    cuoreUtenteImageView.setImageResource(R.drawable.round_favorite_border)
                }
                .addOnFailureListener { exception ->
                    Log.e(TAG, "Errore nella rimozione dell'evento dai preferiti", exception)
                }
        }
    }

    private fun getUserLocation(onLocationObtained: () -> Unit) {
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION),
                REQUEST_LOCATION_PERMISSION
            )
            return
        }
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location ->
                if (location != null) {
                    userLocation = GeoPoint(location.latitude, location.longitude)
                }
                onLocationObtained()
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Errore nell'ottenere la posizione dell'utente", exception)
                onLocationObtained()
            }
    }

    private fun calculateDistance(point1: GeoPoint, point2: GeoPoint): Float {
        val results = FloatArray(1)
        Location.distanceBetween(
            point1.latitude, point1.longitude,
            point2.latitude, point2.longitude,
            results
        )
        return results[0]
    }

    private fun checkProximityAndDate(eventStartDate: Long, eventEndDate: Long, eventLocation: GeoPoint) {
        val currentDate = System.currentTimeMillis()

        if (currentDate in eventStartDate..eventEndDate) {
            fabCamera.show()
        } else {
            fabCamera.hide()
        }

        userLocation?.let {
            val distance = calculateDistance(it, eventLocation)
            if (distance <= 5) {
            }
        }
    }
}

