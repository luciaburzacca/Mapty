package com.example.mapty

import android.os.Bundle
import android.util.Log
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
import com.google.android.gms.tasks.Tasks
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
class UtenteWishlistFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    private lateinit var recyclerView: RecyclerView
    private lateinit var eventiAdapter: AdapterEventi
    private var eventiList: MutableList<ItemEvento> = mutableListOf()
    private lateinit var emptyView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()
        firestore = Firebase.firestore
    }

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

        caricaEventiPreferiti()
    }

    private fun caricaEventiPreferiti() {
        val currentUser = auth.currentUser
        currentUser?.uid?.let { userId ->
            firestore.collection("utenti").document(userId)
                .collection("eventi_preferiti")
                .get()
                .addOnSuccessListener { preferitiDocuments ->
                    if (preferitiDocuments.isEmpty) {
                        updateEmptyView(true)
                        return@addOnSuccessListener
                    }

                    val eventiFutures = preferitiDocuments.documents.mapNotNull { document ->
                        val eventoRef = document.getDocumentReference("eventoRef")
                        eventoRef?.let {
                            it.get().addOnFailureListener { exception ->
                                Log.d("UtenteWishlistFragment", "Errore nel recupero dell'evento", exception)
                            }
                        }
                    }

                    if (eventiFutures.isEmpty()) {
                        updateEmptyView(true)
                        return@addOnSuccessListener
                    }

                    Tasks.whenAllSuccess<DocumentSnapshot>(eventiFutures)
                        .addOnSuccessListener { eventoDocuments ->
                            val currentTime = System.currentTimeMillis()
                            eventiList.clear()
                            for (eventoDocument in eventoDocuments) {
                                val evento = eventoDocument.toObject(ItemEvento::class.java)?.apply {
                                    id = eventoDocument.id
                                }
                                if (evento != null && evento.dataFine > currentTime) {
                                    eventiList.add(evento)
                                }
                            }
                            eventiAdapter.notifyDataSetChanged()
                            updateEmptyView(eventiList.isEmpty())
                        }
                        .addOnFailureListener { exception ->
                            Log.d("UtenteWishlistFragment", "Errore nel recupero degli eventi", exception)
                            Toast.makeText(context, "Errore nel recupero degli eventi", Toast.LENGTH_SHORT).show()
                            updateEmptyView(true)
                        }
                }
                .addOnFailureListener { exception ->
                    Log.d("UtenteWishlistFragment", "Errore nel recupero dei dati", exception)
                    Toast.makeText(context, "Errore nel recupero dei dati", Toast.LENGTH_SHORT).show()
                    updateEmptyView(true)
                }
        } ?: updateEmptyView(true)
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


