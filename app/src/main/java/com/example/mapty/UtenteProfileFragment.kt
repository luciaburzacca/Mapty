package com.example.mapty

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mapty.recycler_components.AdapterLocali
import com.example.mapty.recycler_components.ItemEvento
import com.example.mapty.recycler_components.ItemLocale
import com.google.android.gms.tasks.Tasks
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.firestore

class UtenteProfileFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    private lateinit var textViewUsername: TextView
    private lateinit var textViewNomeCognome: TextView

    private lateinit var recyclerViewLocaliPreferiti: RecyclerView
    private lateinit var emptyView: TextView
    private lateinit var adapterLocali: AdapterLocali
    private var localiList: MutableList<ItemLocale> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()
        firestore = Firebase.firestore
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_utente_profile, container, false)

        textViewUsername = view.findViewById(R.id.profile_username)
        textViewNomeCognome = view.findViewById(R.id.profile_name)
        recyclerViewLocaliPreferiti = view.findViewById(R.id.recycler_view_locali_preferiti)
        recyclerViewLocaliPreferiti.layoutManager = LinearLayoutManager(context)
        adapterLocali = AdapterLocali(localiList) { itemLocale ->
            //onLocaleClicked(itemLocale)
        }
        recyclerViewLocaliPreferiti.adapter = adapterLocali
        emptyView = view.findViewById(R.id.empty_view_utente_profilo)

        return view
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val buttonLogoutUtente = view.findViewById<Button>(R.id.buttone_logout)

        buttonLogoutUtente.setOnClickListener {
            val intent = Intent(activity, MainActivity::class.java)
            startActivity(intent)
        }

        val currentUser = auth.currentUser
        currentUser?.uid?.let { userId ->
            firestore.collection("utenti").document(userId)
                .get()
                .addOnSuccessListener { document ->
                    if (document != null) {
                        val username = document.getString("username") ?: ""
                        val nome = document.getString("nome") ?: ""
                        val cognome = document.getString("cognome") ?: ""

                        textViewUsername.text = username
                        textViewNomeCognome.text = "$nome $cognome"
                    } else {
                        Log.d("UtenteProfileFragment", "No such document")
                    }
                }
                .addOnFailureListener { exception ->
                    Log.d("UtenteProfileFragment", "get failed with ", exception)
                    Toast.makeText(
                        context,
                        "Errore nel recupero dei dati utente",
                        Toast.LENGTH_SHORT
                    ).show()
                }

            firestore.collection("utenti").document(userId)
                .collection("locali_preferiti")
                .get()
                .addOnSuccessListener { documents ->
                    localiList.clear()
                    val tasks = documents.map { document ->
                        val localeRef = document.getDocumentReference("localeRef")
                        localeRef?.get() ?: Tasks.forResult(null)
                    }
                    /*Tasks.whenAllSuccess<DocumentSnapshot>(tasks)
                        .addOnSuccessListener { localeDocuments ->
                            for (localeDocument in localeDocuments) {
                                if (localeDocument != null) {
                                    val nomeLocale = localeDocument.getString("nomeLocale") ?: ""
                                    val posizioneLocale = localeDocument.getGeoPoint("posizioneLocale") ?: GeoPoint(0.0, 0.0)
                                    val localeId = localeDocument.id
                                    val itemLocale = ItemLocale(nomeLocale, posizioneLocale, localeId)
                                    localiList.add(itemLocale)
                                }
                            }
                            adapterLocali.notifyDataSetChanged()
                            updateEmptyViewVisibility()
                        }
                        .addOnFailureListener { exception ->
                            Log.d("UtenteProfileFragment", "Errore nel recupero dei locali preferiti", exception)
                            updateEmptyViewVisibility()
                        }*/
                }
                .addOnFailureListener { exception ->
                    Log.d("UtenteProfileFragment", "Errore nel recupero dei locali preferiti", exception)
                    updateEmptyViewVisibility()
                }
        }
    }

    private fun updateEmptyViewVisibility() {
        if (localiList.isEmpty()) {
            recyclerViewLocaliPreferiti.visibility = View.GONE
            emptyView.visibility = View.VISIBLE
        } else {
            recyclerViewLocaliPreferiti.visibility = View.VISIBLE
            emptyView.visibility = View.GONE
        }
    }

    private fun onLocaleClicked(itemLocale: ItemLocale) {
        val bundle = bundleOf("nomeLocale" to itemLocale.nomeLocale)
        findNavController().navigate(R.id.action_utenteProfileFragment_to_utentePaginaLocaleFragment, bundle)
    }
}


