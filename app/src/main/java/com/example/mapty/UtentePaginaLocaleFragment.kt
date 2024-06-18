package com.example.mapty

import android.content.ContentValues.TAG
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController
import com.google.firebase.firestore.FirebaseFirestore


class UtentePaginaLocaleFragment : Fragment() {

    private lateinit var textViewNomeLocale: TextView
    private lateinit var textViewPosizione: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_utente_pagina_locale, container, false)

        textViewNomeLocale = view.findViewById(R.id.textNomeLocale)
        textViewPosizione = view.findViewById(R.id.textViewPosizione)

        // Recupera il nome del locale passato come argomento
        val nomeLocale = arguments?.getString("nomeLocale") ?: ""

        // Ricerca nel Firebase per il nomeLocale
        ricercaLocale(nomeLocale)

        // Click listener per il pulsante di ritorno indietro
        view.findViewById<Button>(R.id.buttonRitornaIndietro).setOnClickListener {
            findNavController().navigateUp()
        }

        return view
    }

    private fun ricercaLocale(nomeLocale: String) {
        val db = FirebaseFirestore.getInstance()
        db.collection("locali")
            .whereEqualTo("nomeLocale", nomeLocale)
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {

                    val locale = documents.documents[0]

                    // Aggiorna le view con i dati trovati
                    textViewNomeLocale.text = locale.getString("nomeLocale")
                    textViewPosizione.text = locale.getString("posizioneLocale")

                } else {
                    Toast.makeText(context, "Locale non trovato", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Errore nel recupero dei dati del locale", exception)
                Toast.makeText(context, "Errore nel recupero dei dati del locale", Toast.LENGTH_SHORT).show()
            }
    }
}
