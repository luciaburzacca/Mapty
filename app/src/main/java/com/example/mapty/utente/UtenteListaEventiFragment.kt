package com.example.mapty.utente

import android.os.Bundle
import android.view.LayoutInflater
import androidx.fragment.app.Fragment
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mapty.R
import com.example.mapty.recycler_components.AdapterEventi
import com.example.mapty.recycler_components.ItemEvento
import com.google.firebase.firestore.FirebaseFirestore

class UtenteListaEventiFragment : Fragment() {

    private lateinit var db: FirebaseFirestore
    private lateinit var recyclerView: RecyclerView
    private lateinit var eventiAdapter: AdapterEventi
    private var eventiList: MutableList<ItemEvento> = mutableListOf()
    private lateinit var emptyView: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_utente_lista_eventi, container, false)
        recyclerView = view.findViewById(R.id.recycler_view_eventi_utente)
        recyclerView.layoutManager = LinearLayoutManager(context)
        eventiAdapter = AdapterEventi(eventiList,
            onItemClick = { evento ->
                onEventoClicked(evento)
            }
        )
        recyclerView.adapter = eventiAdapter
        emptyView = view.findViewById(R.id.empty_view)

        view.findViewById<Button>(R.id.button_indietro).setOnClickListener {
            findNavController().navigate(R.id.action_utenteListaEventiFragment_to_utenteFiltroEventiFragment)
        }

        view.findViewById<Button>(R.id.button_home).setOnClickListener {
            findNavController().navigate(R.id.action_utenteListaEventiFragment_to_utenteHomeFragment)
        }
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        db = FirebaseFirestore.getInstance()

        val tipoEvento = arguments?.getString("tipo")

        caricaEventi(tipoEvento)
    }

    private fun caricaEventi(tipo: String?) {
        val currentTime = System.currentTimeMillis()
        val query = if (tipo.isNullOrEmpty() || tipo == " ") {
            db.collection("eventos")
                .whereGreaterThan("dataFine", currentTime)
        } else {
            db.collection("eventos")
                .whereEqualTo("tipo", tipo)
                .whereGreaterThan("dataFine", currentTime)
        }

        query.get()
            .addOnSuccessListener { documents ->
                eventiList.clear()
                for (document in documents) {
                    val evento = document.toObject(ItemEvento::class.java).apply {
                        id = document.id
                    }
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
        val bundle = bundleOf("eventoId" to evento.id)
        findNavController().navigate(R.id.action_utenteListaEventiFragment_to_vistaEventoFragment, bundle)
    }

}
