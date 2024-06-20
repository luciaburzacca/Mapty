package com.example.mapty.evento

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
import androidx.core.app.ActivityCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.mapty.recycler_components.ItemEvento
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import android.Manifest
import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.provider.MediaStore
import androidx.lifecycle.lifecycleScope
import com.example.mapty.R
import com.google.android.gms.location.LocationServices
import com.google.firebase.storage.FirebaseStorage
import java.io.ByteArrayOutputStream
import java.util.*

class VistaEventoFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var storage: FirebaseStorage

    private lateinit var eventoId: String

    private var isPreferito = false
    private var isUtenteLocale = false

    private lateinit var userLocation: GeoPoint
    private var fotoCaricata = false

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
                updateUI(it)
                verificaUtenteLocale()
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
        textViewLuogoEvento.text = "Latitudine: ${geoPoint.latitude}, \n" +
                "Longitudine: ${geoPoint.longitude}"
    }

    private fun verificaUtenteLocale() {
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
    }

    private fun caricaDettagliEvento(onEventoCaricato: (ItemEvento?) -> Unit) {
        db.collection("eventos").document(eventoId)
            .get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val evento = document.toObject(ItemEvento::class.java)
                    onEventoCaricato(evento)
                    verificaEventoPreferito()
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
                .collection("eventi_preferiti")
                .document(eventoId)
                .set(mapOf("idEvento" to eventoId))
                .addOnSuccessListener {
                    isPreferito = true
                    cuoreUtenteImageView.setImageResource(R.drawable.round_favorite)
                    Log.d(TAG, "Evento aggiunto ai preferiti con successo")
                }
                .addOnFailureListener { exception ->
                    Log.e(TAG, "Errore nell'aggiungere l'evento ai preferiti", exception)
                }
        } else {
            Log.e(TAG, "Utente non autenticato")
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

    private fun verificaEventoPreferito() {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val userId = currentUser.uid

            db.collection("utenti")
                .document(userId)
                .collection("eventi_preferiti")
                .document(eventoId)
                .get()
                .addOnSuccessListener { documentSnapshot ->
                    isPreferito = documentSnapshot.exists()
                    updateCuoreUtente()
                }
                .addOnFailureListener { exception ->
                    Log.e(TAG, "Errore nel verificare se l'evento è nei preferiti dell'utente", exception)
                }
        }
    }

    private fun updateCuoreUtente() {
        if (isPreferito) {
            cuoreUtenteImageView.setImageResource(R.drawable.round_favorite)
        } else {
            cuoreUtenteImageView.setImageResource(R.drawable.round_favorite_border)
        }
    }

    class VistaEventoFragment : Fragment() {

        private lateinit var auth: FirebaseAuth
        private lateinit var db: FirebaseFirestore
        private lateinit var storage: FirebaseStorage

        private lateinit var eventoId: String

        private var isPreferito = false
        private var isUtenteLocale = false

        private lateinit var userLocation: GeoPoint
        private var fotoCaricata = false

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
                    updateUI(it)
                    verificaUtenteLocale()
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
            textViewLuogoEvento.text = "Latitudine: ${geoPoint.latitude}, \n" +
                    "Longitudine: ${geoPoint.longitude}"
        }

        private fun verificaUtenteLocale() {
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
                    .collection("eventi_preferiti")
                    .document(eventoId)
                    .set(mapOf("idEvento" to eventoId))
                    .addOnSuccessListener {
                        isPreferito = true
                        cuoreUtenteImageView.setImageResource(R.drawable.round_favorite)
                        Log.d(TAG, "Evento aggiunto ai preferiti con successo")
                    }
                    .addOnFailureListener { exception ->
                        Log.e(TAG, "Errore nell'aggiungere l'evento ai preferiti", exception)
                    }
            } else {
                Log.e(TAG, "Utente non autenticato")
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

        private fun openCamera() {
            val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            if (takePictureIntent.resolveActivity(requireActivity().packageManager) != null) {
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
            }
        }

        override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
            super.onActivityResult(requestCode, resultCode, data)
            if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK) {
                val imageBitmap = data?.extras?.get("data") as? Bitmap
                if (imageBitmap != null) {
                    viewLifecycleOwner.lifecycleScope.launch {
                        if (::storage.isInitialized) {
                            getUsernameAndUploadImage(imageBitmap)
                        } else {
                            Toast.makeText(requireContext(), "Errore: Firebase Storage non è inizializzato", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    Toast.makeText(requireContext(), "Errore: Immagine non catturata correttamente", Toast.LENGTH_SHORT).show()
                }
            }
        }

        private fun getUsernameAndUploadImage(bitmap: Bitmap) {
            getUsernameFromFirestore { username ->
                if (username != null) {
                    uploadImageToFirebase(bitmap, username)
                } else {
                    Toast.makeText(requireContext(), "Username non disponibile", Toast.LENGTH_SHORT).show()
                }
            }
        }

        private fun getUsernameFromFirestore(onUsernameObtained: (String?) -> Unit) {
            val currentUser = auth.currentUser
            if (currentUser != null) {
                val emailUtente = currentUser.email
                if (emailUtente != null) {
                    db.collection("utenti")
                        .whereEqualTo("email", emailUtente)
                        .get()
                        .addOnSuccessListener { documents ->
                            if (!documents.isEmpty) {
                                val username = documents.documents[0].getString("username")
                                onUsernameObtained(username)
                            } else {
                                Log.e(TAG, "Documento utente non trovato per l'utente autenticato")
                                onUsernameObtained(null)
                            }
                        }
                        .addOnFailureListener { exception ->
                            Log.e(TAG, "Errore nel cercare l'utente", exception)
                            onUsernameObtained(null)
                        }
                } else {
                    Log.e(TAG, "Email utente non disponibile")
                    onUsernameObtained(null)
                }
            } else {
                Log.e(TAG, "Utente non autenticato")
                onUsernameObtained(null)
            }
        }

        private fun uploadImageToFirebase(bitmap: Bitmap, username: String) {
            val baos = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
            val data = baos.toByteArray()

            val storageRef = storage.reference
            val imageRef = storageRef.child("images/${UUID.randomUUID()}.jpg")

            val uploadTask = imageRef.putBytes(data)
            uploadTask.addOnSuccessListener {
                imageRef.downloadUrl.addOnSuccessListener { uri ->
                    saveImageReference(uri.toString(), username)
                }
            }.addOnFailureListener { exception ->
                Log.e(TAG, "Errore nel caricamento della foto", exception)
                Toast.makeText(requireContext(), "Errore nel caricamento della foto", Toast.LENGTH_SHORT).show()
            }
        }

        private fun saveImageReference(imageUrl: String, username: String) {
            val nomeLocale = textViewNomeLocaleEvento.text.toString()

            val photoData = hashMapOf(
                "url" to imageUrl,
                "nomeUtente" to username
            )

            db.collection("locali")
                .whereEqualTo("nomeLocale", nomeLocale)
                .get()
                .addOnSuccessListener { localeDocuments ->
                    if (!localeDocuments.isEmpty) {
                        val localeDocument = localeDocuments.documents[0]
                        db.collection("locali")
                            .document(localeDocument.id)
                            .collection("foto")
                            .add(photoData)
                            .addOnSuccessListener {
                                Toast.makeText(requireContext(), "Foto caricata con successo", Toast.LENGTH_SHORT).show()
                                fotoCaricata = true
                                fabCamera.hide()
                            }
                            .addOnFailureListener { exception ->
                                Log.e(TAG, "Errore nel salvataggio del riferimento della foto", exception)
                                Toast.makeText(requireContext(), "Errore nel salvataggio del riferimento della foto", Toast.LENGTH_SHORT).show()
                            }
                    } else {
                        Log.e(TAG, "Locale non trovato")
                        Toast.makeText(requireContext(), "Locale non trovato", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener { exception ->
                    Log.e(TAG, "Errore nel cercare il locale", exception)
                    Toast.makeText(requireContext(), "Errore nel cercare il locale", Toast.LENGTH_SHORT).show()
                }
        }

        private fun loadFabState(): Boolean {
            val prefs = requireActivity().getSharedPreferences("FAB_PREFS", Activity.MODE_PRIVATE)
            return prefs.getBoolean("fab_visible_$eventoId", true)
        }

        override fun onRequestPermissionsResult(
            requestCode: Int,
            permissions: Array<out String>,
            grantResults: IntArray
        ) {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults)
            when (requestCode) {
                REQUEST_LOCATION_PERMISSION -> {
                    if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                        getUserLocation {}
                    }
                }
                REQUEST_CAMERA_PERMISSION -> {
                    if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                        openCamera()
                    } else {
                        Toast.makeText(requireContext(), "Permesso per la fotocamera negato", Toast.LENGTH_SHORT).show()
                    }
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
        companion object {
            private const val TAG = "VistaEventoFragment"
        }
    }

    private fun openCamera() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (takePictureIntent.resolveActivity(requireActivity().packageManager) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK) {
            val imageBitmap = data?.extras?.get("data") as? Bitmap
            if (imageBitmap != null) {
                viewLifecycleOwner.lifecycleScope.launch {
                    if (::storage.isInitialized) {
                        getUsernameAndUploadImage(imageBitmap)
                    } else {
                        Toast.makeText(requireContext(), "Errore: Firebase Storage non è inizializzato", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                Toast.makeText(requireContext(), "Errore: Immagine non catturata correttamente", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun getUsernameAndUploadImage(bitmap: Bitmap) {
        getUsernameFromFirestore { username ->
            if (username != null) {
                uploadImageToFirebase(bitmap, username)
            } else {
                Toast.makeText(requireContext(), "Username non disponibile", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun getUsernameFromFirestore(onUsernameObtained: (String?) -> Unit) {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val emailUtente = currentUser.email
            if (emailUtente != null) {
                db.collection("utenti")
                    .whereEqualTo("email", emailUtente)
                    .get()
                    .addOnSuccessListener { documents ->
                        if (!documents.isEmpty) {
                            val username = documents.documents[0].getString("username")
                            onUsernameObtained(username)
                        } else {
                            Log.e(TAG, "Documento utente non trovato per l'utente autenticato")
                            onUsernameObtained(null)
                        }
                    }
                    .addOnFailureListener { exception ->
                        Log.e(TAG, "Errore nel cercare l'utente", exception)
                        onUsernameObtained(null)
                    }
            } else {
                Log.e(TAG, "Email utente non disponibile")
                onUsernameObtained(null)
            }
        } else {
            Log.e(TAG, "Utente non autenticato")
            onUsernameObtained(null)
        }
    }

    private fun uploadImageToFirebase(bitmap: Bitmap, username: String) {
        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val data = baos.toByteArray()

        val storageRef = storage.reference
        val imageRef = storageRef.child("images/${UUID.randomUUID()}.jpg")

        val uploadTask = imageRef.putBytes(data)
        uploadTask.addOnSuccessListener {
            imageRef.downloadUrl.addOnSuccessListener { uri ->
                saveImageReference(uri.toString(), username)
            }
        }.addOnFailureListener { exception ->
            Log.e(TAG, "Errore nel caricamento della foto", exception)
            Toast.makeText(requireContext(), "Errore nel caricamento della foto", Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveImageReference(imageUrl: String, username: String) {
        val nomeLocale = textViewNomeLocaleEvento.text.toString()

        val photoData = hashMapOf(
            "url" to imageUrl,
            "nomeUtente" to username
        )

        db.collection("locali")
            .whereEqualTo("nomeLocale", nomeLocale)
            .get()
            .addOnSuccessListener { localeDocuments ->
                if (!localeDocuments.isEmpty) {
                    val localeDocument = localeDocuments.documents[0]
                    db.collection("locali")
                        .document(localeDocument.id)
                        .collection("foto")
                        .add(photoData)
                        .addOnSuccessListener {
                            Toast.makeText(requireContext(), "Foto caricata con successo", Toast.LENGTH_SHORT).show()
                            fotoCaricata = true
                            fabCamera.hide()
                        }
                        .addOnFailureListener { exception ->
                            Log.e(TAG, "Errore nel salvataggio del riferimento della foto", exception)
                            Toast.makeText(requireContext(), "Errore nel salvataggio del riferimento della foto", Toast.LENGTH_SHORT).show()
                        }
                } else {
                    Log.e(TAG, "Locale non trovato")
                    Toast.makeText(requireContext(), "Locale non trovato", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Errore nel cercare il locale", exception)
                Toast.makeText(requireContext(), "Errore nel cercare il locale", Toast.LENGTH_SHORT).show()
            }
    }

    private fun loadFabState(): Boolean {
        val prefs = requireActivity().getSharedPreferences("FAB_PREFS", Activity.MODE_PRIVATE)
        return prefs.getBoolean("fab_visible_$eventoId", true)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_LOCATION_PERMISSION -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getUserLocation {}
                }
            }
            REQUEST_CAMERA_PERMISSION -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    openCamera()
                } else {
                    Toast.makeText(requireContext(), "Permesso per la fotocamera negato", Toast.LENGTH_SHORT).show()
                }
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
    companion object {
        private const val TAG = "VistaEventoFragment"
    }
}


