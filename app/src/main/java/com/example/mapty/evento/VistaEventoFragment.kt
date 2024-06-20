package com.example.mapty.evento

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.mapty.R
import com.example.mapty.recycler_components.ItemEvento
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.storage.FirebaseStorage
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.*

class VistaEventoFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var storage: FirebaseStorage

    private lateinit var eventoId: String

    private var isPreferito = false
    private var isUtenteLocale = false

    private lateinit var cuoreUtenteImageView: ImageView
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

    private val REQUEST_LOCATION_PERMISSION = 1
    private val REQUEST_IMAGE_CAPTURE = 2
    private val REQUEST_CAMERA_PERMISSION = 3

    private var userLocation: GeoPoint? = null
    private var fotoCaricata = false

    private var locationManager: LocationManager? = null
    private var currentLocation: Location? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_vista_evento, container, false)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        storage = FirebaseStorage.getInstance()

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
                                // Gestione quando l'utente è un locale
                            }
                        }
                        .addOnFailureListener { exception ->
                            Log.e(TAG, "Errore nel verificare il tipo di utente", exception)
                        }
                }
            }
        }

        locationManager = requireActivity().getSystemService(Context.LOCATION_SERVICE) as LocationManager

        caricaDettagliEvento { evento ->
            evento?.let {
                updateUI(it)
                verificaTipoUtente()
                verificaEventoPreferito()
                verificaDataEPosizione()
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

        if (!loadFabState()) {
            fabCamera.hide()
        }

        fabCamera.setOnClickListener {
            if (ActivityCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.CAMERA
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    requireActivity(),
                    arrayOf(Manifest.permission.CAMERA),
                    REQUEST_CAMERA_PERMISSION
                )
            } else {
                openCamera()
            }
        }

        return view
    }

    private fun updateUI(evento: ItemEvento) {
        textViewNomeEvento.text = evento.nomeEvento
        textViewTipoEvento.text = evento.tipo
        textViewDataEvento.text = formatDate(evento.data)
        textViewOraInizioEvento.text = formatTime(evento.data)
        "${evento.prezzo}€".also { textViewPrezzoEvento.text = it }
        textViewNomeLocaleEvento.text = evento.nomeLocale
        textViewNumeroTelefonoEvento.text = evento.numeroTelefono
        textViewDescrizioneEvento.text = evento.descrizione

        val geoPoint = evento.luogo ?: GeoPoint(0.0, 0.0)
        textViewLuogoEvento.text = "Latitudine: ${geoPoint.latitude}, \nLongitudine: ${geoPoint.longitude}"

        userLocation = geoPoint
    }

    private fun verificaTipoUtente() {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val emailUtente = currentUser.email
            if (emailUtente != null) {
                db.collection("locali")
                    .whereEqualTo("email", emailUtente)
                    .get()
                    .addOnSuccessListener { documents ->
                        isUtenteLocale = !documents.isEmpty
                        updateCuoreUtenteImageView()
                    }
                    .addOnFailureListener { exception ->
                        Log.e(TAG, "Errore nel verificare il tipo di utente", exception)
                    }
            }
        }
    }

    private fun updateCuoreUtenteImageView() {
        if (isUtenteLocale) {
            cuoreUtenteImageView.visibility = View.GONE
        }
        else {
            if (isPreferito) {
                cuoreUtenteImageView.setImageResource(R.drawable.round_favorite)
            } else {
                cuoreUtenteImageView.setImageResource(R.drawable.round_favorite_border)
            }
        }
    }

    private fun caricaDettagliEvento(onEventoCaricato: (ItemEvento?) -> Unit) {
        db.collection("eventos").document(eventoId)
            .get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val evento = document.toObject(ItemEvento::class.java)
                    onEventoCaricato(evento)
                } else {
                    Log.d(TAG, "Documento non trovato")
                    onEventoCaricato(null)
                }
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Errore nel caricamento dell'evento", exception)
                onEventoCaricato(null)
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
                .collection("locali_preferiti")
                .document(eventoId)
                .set(mapOf("idEvento" to eventoId))
                .addOnSuccessListener {
                    isPreferito = true
                    cuoreUtenteImageView.setImageResource(R.drawable.round_favorite)
                }
                .addOnFailureListener { exception ->
                    Log.e(TAG, "Errore nell'aggiunta dell'evento ai preferiti", exception)
                }
        }
    }

    private fun rimuoviEventoPreferito() {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val userId = currentUser.uid

            db.collection("utenti")
                .document(userId)
                .collection("locali_preferiti")
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

    private fun verificaEventoPreferito() {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val userId = currentUser.uid

            db.collection("utenti")
                .document(userId)
                .collection("locali_preferiti")
                .document(eventoId)
                .get()
                .addOnSuccessListener { documentSnapshot ->
                    isPreferito = documentSnapshot.exists()
                    updateCuoreUtenteImageView()
                }
                .addOnFailureListener { exception ->
                    Log.e(TAG, "Errore nel verificare se l'evento è nei preferiti dell'utente", exception)
                }
        }
    }

    private fun saveFabState(isVisible: Boolean) {
        val sharedPreferences = requireContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putBoolean("fab_visible", isVisible)
        editor.apply()
    }

    private fun loadFabState(): Boolean {
        val sharedPreferences = requireContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        return sharedPreferences.getBoolean("fab_visible", false)
    }

    private fun openCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (intent.resolveActivity(requireActivity().packageManager) != null) {
            startActivityForResult(intent, REQUEST_IMAGE_CAPTURE)
        } else {
            Log.e(TAG, "Nessuna attività trovata per gestire l'intent di acquisizione immagine")
        }
    }

    private fun uploadImageToFirebase(imageBitmap: Bitmap) {
        val storageRef = storage.reference
        val imagesRef = storageRef.child("images/${UUID.randomUUID()}.jpg")
        val baos = ByteArrayOutputStream()
        imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val data = baos.toByteArray()

        val uploadTask = imagesRef.putBytes(data)
        uploadTask.addOnSuccessListener { taskSnapshot ->
            imagesRef.downloadUrl.addOnSuccessListener { uri ->
                saveImageUriToFirestore(uri.toString())
                // Aggiorna il flag dopo il caricamento dell'immagine
                fotoCaricata = true
            }
        }.addOnFailureListener { exception ->
            Log.e(TAG, "Errore durante il caricamento dell'immagine", exception)
        }
    }

    private fun saveImageUriToFirestore(imageUri: String) {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val userId = currentUser.uid
            val eventoRef = db.collection("eventos").document(eventoId)
            eventoRef.update("imageUri", imageUri)
                .addOnSuccessListener {
                    Log.d(TAG, "URL immagine salvato nel database con successo")
                }
                .addOnFailureListener { exception ->
                    Log.e(TAG, "Errore nel salvare l'URL dell'immagine nel database", exception)
                }
        }
    }

    private fun requestLocationPermission() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_LOCATION_PERMISSION
            )
        } else {
        }
    }

    private fun verificaDataEPosizione() {
        val cal = Calendar.getInstance()
        val currentTimestamp = cal.timeInMillis

        db.collection("eventos").document(eventoId)
            .get()
            .addOnSuccessListener { documentSnapshot ->
                val dataInizio = documentSnapshot.getLong("data")
                val dataFine = documentSnapshot.getLong("dataFine")

                if (dataInizio != null && dataFine != null) {
                    if (currentTimestamp > dataInizio && currentTimestamp < dataFine) {
                        if (ContextCompat.checkSelfPermission(
                                requireContext(),
                                Manifest.permission.ACCESS_FINE_LOCATION
                            ) == PackageManager.PERMISSION_GRANTED
                        ) {
                            currentLocation = locationManager?.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                            currentLocation?.let {
                                if (userLocation != null && isUserWithinRadius(it.latitude, it.longitude, userLocation!!)) {
                                    fabCamera.show()
                                    saveFabState(true)
                                }
                            } ?: requestLocationPermission()
                        } else {
                            requestLocationPermission()
                        }
                    }
                }
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Errore nel caricamento dell'evento per la verifica della data e posizione", exception)
            }
    }

    private fun isUserWithinRadius(userLat: Double, userLng: Double, eventLocation: GeoPoint): Boolean {
        val eventLat = eventLocation.latitude
        val eventLng = eventLocation.longitude
        val distance = FloatArray(1)
        Location.distanceBetween(userLat, userLng, eventLat, eventLng, distance)
        return distance[0] <= 5
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == AppCompatActivity.RESULT_OK) {
            val imageBitmap = data?.extras?.get("data") as Bitmap
            uploadImageToFirebase(imageBitmap)
        }
    }

    companion object {
        private const val TAG = "VistaEventoFragment"
    }
}


