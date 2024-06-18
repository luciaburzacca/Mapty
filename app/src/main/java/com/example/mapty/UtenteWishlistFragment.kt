package com.example.mapty

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import android.widget.TextView
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mapty.recycler_components.AdapterEventi
import com.example.mapty.recycler_components.ItemEvento
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore

class UtenteWishlistFragment : Fragment() {

    private lateinit var db: FirebaseFirestore
    private lateinit var recyclerView: RecyclerView
    private lateinit var eventiAdapter: AdapterEventi
    private var eventiList: MutableList<ItemEvento> = mutableListOf()
    private lateinit var emptyView: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_utente_wishlist, container, false)
        recyclerView = view.findViewById(R.id.recycler_view_eventi_wishlist)
        recyclerView.layoutManager = LinearLayoutManager(context)
        eventiAdapter = AdapterEventi(eventiList) { evento ->
            onEventoClicked(evento)
        }
        recyclerView.adapter = eventiAdapter
        emptyView = view.findViewById(R.id.empty_view_wishlist)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        db = FirebaseFirestore.getInstance()

        caricaEventiPreferiti()
    }

    private fun caricaEventiPreferiti() {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            val userEmail = currentUser.email

            if (userEmail != null) {
                db.collection("utenti")
                    .whereEqualTo("email", userEmail)
                    .get()
                    .addOnSuccessListener { documents ->
                        if (documents.isEmpty) {
                            updateEmptyView(true)
                        } else {
                            val userId = documents.first().id
                            db.collection("utenti")
                                .document(userId)
                                .collection("Eventi_preferiti")
                                .get()
                                .addOnSuccessListener { preferitiDocuments ->
                                    val preferitiIds = preferitiDocuments.map { it.getString("idEvento") }

                                    if (preferitiIds.isEmpty()) {
                                        updateEmptyView(true)
                                    } else {
                                        caricaEventi(preferitiIds)
                                    }
                                }
                                .addOnFailureListener { exception ->
                                    Toast.makeText(context, "Errore nel recupero dei dati", Toast.LENGTH_SHORT).show()
                                }
                        }
                    }
                    .addOnFailureListener { exception ->
                        Toast.makeText(context, "Errore nel recupero dei dati", Toast.LENGTH_SHORT).show()
                    }
            } else {
                updateEmptyView(true)
            }
        } else {
            updateEmptyView(true)
        }
    }

    private fun caricaEventi(preferitiIds: List<String?>) {
        val currentTime = System.currentTimeMillis()
        db.collection("eventos")
            .whereIn(FieldPath.documentId(), preferitiIds.filterNotNull())
            .whereGreaterThan("data", currentTime)
            .get()
            .addOnSuccessListener { documents ->
                eventiList.clear()
                for (document in documents) {
                    val evento = document.toObject(ItemEvento::class.java).apply {
                        id = document.id  // Set the document ID
                    }
                    eventiList.add(evento)
                }
                eventiAdapter.notifyDataSetChanged()
                updateEmptyView(eventiList.isEmpty())
            }
            .addOnFailureListener { exception ->
                Toast.makeText(context, "Errore nel recupero dei dati", Toast.LENGTH_SHORT).show()
            }
    }

    private fun updateEmptyView(isEmpty: Boolean) {
        if (isEmpty) {
            recyclerView.visibility = View.GONE
            emptyView.visibility = View.VISIBLE
        } else {
            recyclerView.visibility = View.VISIBLE
            emptyView.visibility = View.GONE
        }
    }

    private fun onEventoClicked(evento: ItemEvento) {
        val bundle = bundleOf("eventoId" to evento.id)
        findNavController().navigate(R.id.action_utenteWishlistFragment_to_vistaEventoFragment, bundle)
    }
}


