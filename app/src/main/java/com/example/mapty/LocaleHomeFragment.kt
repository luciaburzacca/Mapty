package com.example.mapty

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.navigation.fragment.findNavController
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
    private lateinit var bottoneFuturi: Button
    private lateinit var bottonePassati: Button
    private lateinit var emptyView: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_locale_home, container, false)
        recyclerView = view.findViewById(R.id.recycler_view_eventi_locale)
        recyclerView.layoutManager = LinearLayoutManager(context)
        eventiAdapter = AdapterEventi(eventiList) { evento ->
            onEventoClicked(evento)
        }
        recyclerView.adapter = eventiAdapter
        bottoneFuturi = view.findViewById(R.id.bottone_futuri)
        bottonePassati = view.findViewById(R.id.bottone_passati)
        emptyView = view.findViewById(R.id.empty_view)

        bottoneFuturi.setOnClickListener {
            caricaEventi(true)
        }

        bottonePassati.setOnClickListener {
            caricaEventi(false)
        }

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        val emailUtente = auth.currentUser?.email ?: return

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

                caricaEventi(true)
            }
            .addOnFailureListener {
                Toast.makeText(context, "Errore nel recupero dei dati del locale", Toast.LENGTH_SHORT).show()
            }
    }

    private fun caricaEventi(futuri: Boolean) {
        if (nomeLocale == null) return

        val currentTime = System.currentTimeMillis()
        val query = if (futuri) {
            db.collection("eventos")
                .whereEqualTo("nomeLocale", nomeLocale)
                .whereGreaterThan("dataFine", currentTime)

        } else {
            db.collection("eventos")
                .whereEqualTo("nomeLocale", nomeLocale)
                .whereLessThan("dataFine", currentTime)
        }

        query.get()
            .addOnSuccessListener { documents ->
                eventiList.clear()
                for (document in documents) {
                    val evento = document.toObject(ItemEvento::class.java)
                    eventiList.add(evento)
                }
                eventiAdapter.notifyDataSetChanged()
                updateEmptyView()
            }
            .addOnFailureListener { exception ->
                Toast.makeText(context, "Errore nel recupero dei dati", Toast.LENGTH_SHORT).show()
            }
    }

    private fun updateEmptyView() {
        if (eventiList.isEmpty()) {
            recyclerView.visibility = View.GONE
            emptyView.visibility = View.VISIBLE
        } else {
            recyclerView.visibility = View.VISIBLE
            emptyView.visibility = View.GONE
        }
    }

    private fun onEventoClicked(evento: ItemEvento) {
        val bundle = Bundle().apply {
            putString("nomeLocale", evento.nomeLocale)
            putLong("data", evento.data)
            putString("nomeEvento", evento.nomeEvento)
        }
        findNavController().navigate(R.id.action_localeHomeFragment_to_vistaEventoFragment, bundle)
    }
}

