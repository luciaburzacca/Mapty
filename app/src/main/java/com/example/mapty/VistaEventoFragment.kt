package com.example.mapty

import android.content.ContentValues.TAG
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.navigation.fragment.findNavController
import com.example.mapty.recycler_components.ItemEvento
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class VistaEventoFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    private lateinit var cuoreUtenteImageView: ImageView
    private lateinit var eventoId: String // L'ID dell'evento corrente

    private var isPreferito = false

    private lateinit var textViewNomeEvento: TextView
    private lateinit var textViewTipoEvento: TextView
    private lateinit var textViewDataEvento: TextView
    private lateinit var textViewOraInizioEvento: TextView
    private lateinit var textViewPrezzoEvento: TextView
    private lateinit var textViewNomeLocaleEvento: TextView
    private lateinit var textViewNumeroTelefonoEvento: TextView
    private lateinit var textViewLuogoEvento: TextView
    private lateinit var textViewDescrizioneEvento: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_vista_evento, container, false)

        // Inizializza Firebase
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // Inizializza views
        cuoreUtenteImageView = view.findViewById(R.id.cuore_utente)

        textViewNomeEvento = view.findViewById(R.id.textViewNomeEvento)
        textViewTipoEvento = view.findViewById(R.id.textViewTipoEvent)
        textViewDataEvento = view.findViewById(R.id.textViewDataEvento)
        textViewOraInizioEvento = view.findViewById(R.id.textViewOraInizioEvento)
        textViewPrezzoEvento = view.findViewById(R.id.textViewPrezzoEvento)
        textViewNomeLocaleEvento = view.findViewById(R.id.textViewNomeLocaleEvento)
        textViewNumeroTelefonoEvento = view.findViewById(R.id.textViewNumeroTelefonoEvento)
        textViewLuogoEvento = view.findViewById(R.id.textViewLuogoEvento)
        textViewDescrizioneEvento = view.findViewById(R.id.textViewDescrizioneEvento)

        // Recupera l'ID dell'evento corrente dai Safe Args
        eventoId = arguments?.getString("eventoId") ?: ""

        // Verifica se l'utente è autenticato
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val emailUtente = currentUser.email

            if (emailUtente != null) {
                // Verifica se l'utente è un locale
                db.collection("locali")
                    .whereEqualTo("email", emailUtente)
                    .get()
                    .addOnSuccessListener { documents ->
                        if (!documents.isEmpty) {
                            // L'utente è un locale, nascondi l'ImageView cuore_utente
                            cuoreUtenteImageView.visibility = View.GONE
                        } else {
                            // L'utente non è un locale, verifica se è un utente normale
                            verificaUtenteNormale(emailUtente)
                        }
                    }
                    .addOnFailureListener { exception ->
                        Log.e(TAG, "Errore nel recupero dei dati del locale", exception)
                        // Gestione dell'errore
                    }
            }
        }

        // Click listener per l'ImageView cuore_utente
        cuoreUtenteImageView.setOnClickListener {
            if (isPreferito) {
                // Rimuovi l'evento dai preferiti
                rimuoviEventoPreferito()
            } else {
                // Aggiungi l'evento ai preferiti
                aggiungiEventoPreferito()
            }
        }

        // Verifica se l'evento è già nei preferiti e aggiorna l'UI di conseguenza
        verificaEventoPreferito()

        // Carica i dettagli dell'evento
        caricaDettagliEvento()

        // Click listener per tornare indietro
        view.findViewById<Button>(R.id.torna_indietro).setOnClickListener {
            findNavController().navigateUp()
        }

        // Click listener per textViewNomeLocaleEvento
        textViewNomeLocaleEvento.setOnClickListener {
            val bundle = bundleOf("nomeLocale" to textViewNomeLocaleEvento.text.toString())
            findNavController().navigate(R.id.action_vistaEventoFragment_to_utentePaginaLocaleFragment, bundle)
        }

        return view
    }

    private fun verificaUtenteNormale(emailUtente: String) {
        db.collection("utenti")
            .whereEqualTo("email", emailUtente)
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    // L'utente è un utente normale, mostra l'ImageView cuore_utente con il bordo vuoto
                    cuoreUtenteImageView.setImageResource(R.drawable.round_favorite_border)
                    cuoreUtenteImageView.visibility = View.VISIBLE
                } else {
                    // L'utente non è neanche un utente normale, potrebbe essere gestito di conseguenza
                }
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Errore nel recupero dei dati dell'utente", exception)
                // Gestione dell'errore
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

                    if (isPreferito) {
                        // Mostra l'ImageView cuore_utente con il cuore pieno
                        cuoreUtenteImageView.setImageResource(R.drawable.round_favorite)
                    } else {
                        // Mostra l'ImageView cuore_utente con il bordo vuoto
                        cuoreUtenteImageView.setImageResource(R.drawable.round_favorite_border)
                    }
                    cuoreUtenteImageView.visibility = View.VISIBLE
                }
                .addOnFailureListener { exception ->
                    Log.e(TAG, "Errore nel verificare se l'evento è nei preferiti", exception)
                    // Gestione dell'errore
                }
        }
    }

    private fun aggiungiEventoPreferito() {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val userId = currentUser.uid

            db.collection("utenti")
                .document(userId)
                .collection("eventi_preferiti")
                .add(mapOf("idEvento" to eventoId))
                .addOnSuccessListener {
                    // Evento aggiunto con successo ai preferiti
                    isPreferito = true
                    cuoreUtenteImageView.setImageResource(R.drawable.round_favorite)
                }
                .addOnFailureListener { exception ->
                    Log.e(TAG, "Errore nell'aggiungere l'evento ai preferiti", exception)
                    // Gestione dell'errore
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
                    // Evento rimosso con successo dai preferiti
                    isPreferito = false
                    cuoreUtenteImageView.setImageResource(R.drawable.round_favorite_border)
                }
                .addOnFailureListener { exception ->
                    Log.e(TAG, "Errore nella rimozione dell'evento dai preferiti", exception)
                    // Gestione dell'errore
                }
        }
    }

    private fun caricaDettagliEvento() {
        db.collection("eventos").document(eventoId).get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    val evento = document.toObject(ItemEvento::class.java)
                    if (evento != null) {
                        textViewNomeEvento.text = evento.nomeEvento
                        textViewTipoEvento.text = evento.tipoEvento
                        textViewDataEvento.text = formatDate(evento.data)
                        textViewOraInizioEvento.text = formatTime(evento.data)
                        textViewPrezzoEvento.text = evento.prezzo
                        textViewNomeLocaleEvento.text = evento.nomeLocale
                        textViewNumeroTelefonoEvento.text = evento.numeroTelefono.toString()
                        evento.location?.let { geoPoint ->
                            textViewLuogoEvento.text = "${geoPoint.latitude}, ${geoPoint.longitude}"
                        } ?: run {
                            textViewLuogoEvento.text = "Posizione non disponibile"
                        }
                        textViewDescrizioneEvento.text = evento.descrizione
                    }
                } else {
                    Toast.makeText(context, "Evento non trovato", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(context, "Errore nel recupero dei dati", Toast.LENGTH_SHORT).show()
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

}



