package com.example.mapty

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class VistaEventoFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    private lateinit var cuoreUtenteImageView: ImageView
    private lateinit var eventoId: String // L'ID dell'evento corrente

    private var isPreferito = false

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

        // Recupera l'ID dell'evento corrente (potrebbe essere passato tramite Safe Args)
        eventoId = "ID_DEL_TUO_EVENTO"

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
                .document(eventoId)
                .set(mapOf("idEvento" to eventoId))
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

    companion object {
        private const val TAG = "VistaEventoFragment"
    }
}