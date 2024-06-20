package com.example.mapty.utente

import android.content.ContentValues.TAG
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.mapty.R
import com.example.mapty.recycler_components.AdapterEventi
import com.example.mapty.recycler_components.AdapterFoto
import com.example.mapty.recycler_components.ItemEvento
import com.example.mapty.recycler_components.ItemFoto
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Locale


class UtentePaginaLocaleFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    private lateinit var textViewNomeLocale: TextView
    private lateinit var textViewPosizione: TextView
    private lateinit var ratingBar: RatingBar
    private lateinit var imageViewStella: ImageView
    private lateinit var textViewMediaStelle: TextView
    private lateinit var recyclerViewEventi: RecyclerView
    private lateinit var eventiAdapter: AdapterEventi
    private lateinit var buttonShowListaEventiLocale: TextView
    private lateinit var buttonShowFotoLocale: TextView
    private var eventiList: MutableList<ItemEvento> = mutableListOf()
    private var voto: Float = 0.0f
    private lateinit var localeId: String
    private var isPreferito = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_utente_pagina_locale, container, false)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        textViewNomeLocale = view.findViewById(R.id.textNomeLocale)
        textViewPosizione = view.findViewById(R.id.textViewPosizione)
        ratingBar = view.findViewById(R.id.ratingBar)
        imageViewStella = view.findViewById(R.id.imageViewStella)
        recyclerViewEventi = view.findViewById(R.id.recycler_view_locale)
        recyclerViewEventi.layoutManager = LinearLayoutManager(context)
        buttonShowListaEventiLocale = view.findViewById(R.id.buttonShowListaEventiLocale)
        buttonShowFotoLocale = view.findViewById(R.id.buttonShowFotoLocale)
        eventiAdapter = AdapterEventi(eventiList,
        onItemClick = { evento ->
            onEventoClicked(evento)
        }
        )
        recyclerViewEventi.adapter = eventiAdapter

        textViewMediaStelle = view.findViewById(R.id.textViewMediaStelle)

        val nomeLocale = arguments?.getString("nomeLocale") ?: ""

        ricercaLocale(nomeLocale)

        view.findViewById<Button>(R.id.buttonRitornaIndietro).setOnClickListener {
            findNavController().navigateUp()
        }

        ratingBar.setOnRatingBarChangeListener { _, rating, _ ->
            voto = rating
            salvaVotoLocale(voto)
        }

        imageViewStella.setOnClickListener {
            if (isPreferito) {
                rimuoviLocalePreferito()
            } else {
                aggiungiLocalePreferito()
            }
        }

        buttonShowFotoLocale.setOnClickListener {
            buttonShowFotoLocale.setBackgroundResource(R.color.lila)
            buttonShowListaEventiLocale.setBackgroundResource(R.color.dark_gray)
            caricaFotoLocale()
        }

        buttonShowListaEventiLocale.setOnClickListener {
            buttonShowListaEventiLocale.setBackgroundResource(android.R.color.background_light)
            buttonShowFotoLocale.setBackgroundResource(android.R.color.background_light)
            caricaEventiLocale()
        }


        return view
    }

    private fun ricercaLocale(nomeLocale: String) {
        db.collection("locali")
            .whereEqualTo("nomeLocale", nomeLocale)
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    val locale = documents.documents[0]
                    localeId = locale.id

                    val nomeLocale = locale.getString("nomeLocale")
                    textViewNomeLocale.text = nomeLocale

                    val posizioneLocale = locale.getGeoPoint("posizioneLocale")
                    if (posizioneLocale != null) {
                        val latitude = posizioneLocale.latitude
                        val longitude = posizioneLocale.longitude
                        textViewPosizione.text = "Latitudine: $latitude, Longitudine: $longitude"
                    } else {
                        textViewPosizione.text = "Posizione non disponibile"
                    }

                    verificaVotoUtente()
                    verificaLocalePreferito()
                    caricaEventiLocale()
                    calcolaMediaVotiUtente()

                } else {
                    Toast.makeText(context, "Locale non trovato", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Errore nel recupero dei dati del locale", exception)
                Toast.makeText(context, "Errore nel recupero dei dati del locale", Toast.LENGTH_SHORT).show()
            }
    }

    private fun salvaVotoLocale(valoreVoto: Float) {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val userId = currentUser.uid

            val votoUtente = hashMapOf(
                "voto" to valoreVoto.toDouble()
            )

            db.collection("locali")
                .document(localeId)
                .collection("voti_utenti")
                .document(userId)
                .set(votoUtente)
                .addOnSuccessListener {
                    ratingBar.rating = valoreVoto
                }
                .addOnFailureListener { exception ->
                    Log.e(TAG, "Errore nel salvare il voto dell'utente per il locale", exception)
                }
        }
    }

    private fun verificaLocalePreferito() {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val userId = currentUser.uid

            db.collection("utenti")
                .document(userId)
                .collection("locali_preferiti")
                .document(localeId)
                .get()
                .addOnSuccessListener { documentSnapshot ->
                    if (documentSnapshot.exists()) {
                        imageViewStella.setImageResource(R.drawable.round_star)
                        isPreferito = true
                    } else {
                        imageViewStella.setImageResource(R.drawable.round_star_border)
                        isPreferito = false
                    }
                    imageViewStella.visibility = View.VISIBLE
                }
                .addOnFailureListener { exception ->
                    Log.e(TAG, "Errore nel verificare se il locale è tra i preferiti dell'utente", exception)
                }
        }
    }


    private fun aggiungiLocalePreferito() {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val userId = currentUser.uid

            val localeRef = db.collection("locali").document(localeId)

            val localePreferito = hashMapOf(
                "localeRef" to localeRef
            )

            db.collection("utenti")
                .document(userId)
                .collection("locali_preferiti")
                .document(localeId)
                .set(localePreferito)
                .addOnSuccessListener {
                    imageViewStella.setImageResource(R.drawable.round_star)
                    isPreferito = true
                }
                .addOnFailureListener { exception ->
                    Log.e(TAG, "Errore nell'aggiungere il locale ai preferiti dell'utente", exception)
                }
        }
    }


    private fun rimuoviLocalePreferito() {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val userId = currentUser.uid

            db.collection("utenti")
                .document(userId)
                .collection("locali_preferiti")
                .document(localeId)
                .delete()
                .addOnSuccessListener {
                    imageViewStella.setImageResource(R.drawable.round_star_border)
                    isPreferito = false
                }
                .addOnFailureListener { exception ->
                    Log.e(TAG, "Errore nella rimozione del locale dai preferiti dell'utente", exception)
                }
        }
    }


    private fun verificaVotoUtente() {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val userId = currentUser.uid

            db.collection("locali")
                .document(localeId)
                .collection("voti_utenti")
                .document(userId)
                .get()
                .addOnSuccessListener { documentSnapshot ->
                    if (documentSnapshot.exists()) {
                        val valoreVoto = documentSnapshot.getDouble("valore")
                        ratingBar.rating = valoreVoto?.toFloat() ?: 0f
                    } else {
                        ratingBar.rating = 0f
                    }
                    ratingBar.visibility = View.VISIBLE
                }
                .addOnFailureListener { exception ->
                    Log.e(TAG, "Errore nel verificare il voto dell'utente per il locale", exception)
                }
        }
    }

    private fun caricaEventiLocale() {
        val currentTime = System.currentTimeMillis()
        db.collection("eventos")
            .whereEqualTo("nomeLocale", textViewNomeLocale.text.toString())
            .whereGreaterThan("dataFine", currentTime)
            .get()
            .addOnSuccessListener { documents ->
                eventiList.clear()
                for (document in documents) {
                    val evento = document.toObject(ItemEvento::class.java).apply {
                        id = document.id
                    }
                    eventiList.add(evento)
                }
                eventiAdapter.notifyDataSetChanged()

            }
            .addOnFailureListener { exception ->
                Toast.makeText(context, "Errore nel recupero degli eventi del locale", Toast.LENGTH_SHORT).show()
            }
    }

    private fun calcolaMediaVotiUtente() {
        db.collection("locali")
            .document(localeId)
            .collection("voti_utenti")
            .get()
            .addOnSuccessListener { documents ->
                if (documents.isEmpty) {
                    textViewMediaStelle.text = "Non ci sono ancora voti"
                } else {
                    var totaleVoti = 0.0
                    var conteggioVoti = 0

                    for (document in documents) {
                        val valoreVoto = document.getDouble("valore")
                        if (valoreVoto != null) {
                            totaleVoti += valoreVoto
                            conteggioVoti++
                        }
                    }

                    if (conteggioVoti > 0) {
                        val mediaVoti = totaleVoti / conteggioVoti
                        textViewMediaStelle.text = String.format(Locale.getDefault(), "%.1f media dei voti", mediaVoti)
                    } else {
                        textViewMediaStelle.text = "Non ci sono ancora voti"
                    }
                }
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Errore nel recupero dei voti degli utenti per il locale", exception)
                Toast.makeText(context, "Errore nel recupero dei voti degli utenti per il locale", Toast.LENGTH_SHORT).show()
            }
    }

    private fun onEventoClicked(evento: ItemEvento) {
        val bundle = bundleOf("eventoId" to evento.id)
        findNavController().navigate(R.id.action_utentePaginaLocaleFragment_to_vistaEventoFragment, bundle)
    }
    private fun caricaFotoLocale() {
        db.collection("locali")
            .document(localeId)
            .collection("foto")
            .get()
            .addOnSuccessListener { documents ->
                val fotoList = mutableListOf<ItemFoto>()
                for (document in documents) {
                    val url = document.getString("url") ?: ""
                    val itemFoto = ItemFoto(url, "Nome Utente") // Assicurati di ottenere il nome utente dal documento se necessario
                    fotoList.add(itemFoto)
                }

                if (fotoList.isEmpty()) {
                    Toast.makeText(context, "Non ci sono ancora foto per questo locale", Toast.LENGTH_SHORT).show()
                } else {
                    mostraFotoInRecyclerView(fotoList)
                }
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Errore nel recupero delle foto del locale", exception)
                Toast.makeText(context, "Errore nel recupero delle foto del locale", Toast.LENGTH_SHORT).show()
            }
    }

    private fun mostraFotoInRecyclerView(fotoList: List<ItemFoto>) {
        recyclerViewEventi.layoutManager = GridLayoutManager(context, 3)
        val adapterFoto = AdapterFoto(fotoList) { itemFoto ->
            mostraFotoSchermoIntero(itemFoto)
        }
        recyclerViewEventi.adapter = adapterFoto
    }


    private fun mostraFotoSchermoIntero(itemFoto: ItemFoto) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_foto_locale, null)
        val imageViewFullScreen = dialogView.findViewById<ImageView>(R.id.viewFoto)
        val usernameTextView = dialogView.findViewById<TextView>(R.id.textViewUsername)

        Glide.with(requireContext())
            .load(itemFoto.url)
            .into(imageViewFullScreen)

        usernameTextView.text = itemFoto.nomeUtente

        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()

        dialog.show()
    }

}

