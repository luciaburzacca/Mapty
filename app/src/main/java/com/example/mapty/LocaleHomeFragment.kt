package com.example.mapty

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mapty.recycler_components.AdapterEventi
import com.example.mapty.recycler_components.ItemEvento
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class LocaleHomeFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var recyclerView: RecyclerView
    private lateinit var eventiAdapter: AdapterEventi
    private var eventiList: MutableList<ItemEvento> = mutableListOf()
    private var nomeLocale: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_locale_home, container, false)
        recyclerView = view.findViewById(R.id.recycler_view_eventi_locale)
        recyclerView.layoutManager = LinearLayoutManager(context)
        eventiAdapter = AdapterEventi(eventiList)
        recyclerView.adapter = eventiAdapter
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // Recupera l'email dell'utente corrente
        val emailUtente = auth.currentUser?.email ?: return

        // Recupera il nome del locale associato all'utente
        db.collection("locali")
            .whereEqualTo("email", emailUtente)
            .get()
            .addOnSuccessListener { documents ->
                if (documents.isEmpty) {
                    Toast.makeText(context, "Nessun locale associato trovato", Toast.LENGTH_SHORT).show()
                    return@addOnSuccessListener
                }
                val localeDoc = documents.first()
                nomeLocale = localeDoc.getString("nomeLocale")

                // Carica gli eventi dopo aver recuperato il nome del locale
                caricaEventi()
            }
            .addOnFailureListener {
                Toast.makeText(context, "Errore nel recupero dei dati del locale", Toast.LENGTH_SHORT).show()
            }
    }

    private fun caricaEventi() {
        if (nomeLocale == null) return

        val currentTime = Date()

        db.collection("eventos")
            .whereEqualTo("nomeLocale", nomeLocale)
            .whereGreaterThan("dateTimeFine", currentTime)
            .get()
            .addOnSuccessListener { documents ->
                eventiList.clear()
                for (document in documents) {
                    val evento = document.toObject(ItemEvento::class.java)
                    eventiList.add(evento)
                }
                eventiAdapter.notifyDataSetChanged()
            }
            .addOnFailureListener { exception ->
                Toast.makeText(context, "Errore nel recupero dei dati", Toast.LENGTH_SHORT).show()
            }
    }
}