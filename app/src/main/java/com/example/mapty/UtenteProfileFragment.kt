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
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mapty.recycler_components.AdapterLocali
import com.example.mapty.recycler_components.ItemLocale
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore

class UtenteProfileFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    private lateinit var textViewUsername: TextView
    private lateinit var textViewNomeCognome: TextView

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

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        val buttonLogoutUtente = view.findViewById<Button>(R.id.buttone_logout)

        buttonLogoutUtente.setOnClickListener {
            val intent = Intent(activity, MainActivity::class.java)
            startActivity(intent)
        }

        // Recupera l'ID dell'utente corrente
        val currentUser = auth.currentUser
        currentUser?.uid?.let { userId ->
            // Recupera i dati dell'utente dalla collezione 'utenti'
            firestore.collection("utenti").document(userId)
                .get()
                .addOnSuccessListener { document ->
                    if (document != null) {
                        val username = document.getString("username") ?: ""
                        val nome = document.getString("nome") ?: ""
                        val cognome = document.getString("cognome") ?: ""

                        // Aggiorna l'UI con i dati recuperati
                        textViewUsername.text = username
                        textViewNomeCognome.text = "$nome $cognome"
                    } else {
                        Log.d("UtenteProfileFragment", "No such document")
                    }
                }
                .addOnFailureListener { exception ->
                    Log.d("UtenteProfileFragment", "get failed with ", exception)
                    Toast.makeText(context, "Errore nel recupero dei dati utente", Toast.LENGTH_SHORT).show()
                }
        }
    }

    /*lateinit var recyclerView: RecyclerView
    lateinit var arrayList: ArrayList<ItemLocale>
    lateinit var nomiLocali: Array<String>

    override fun onViewCreated(view: View, savedInstanceState: Bundle?){
        super.onViewCreated(view, savedInstanceState)

        nomiLocali = arrayOf(
            "locale 1",
            "locale 2",
            "locale 3",
            "locale 4",
            "locale 5",
            "locale 6",
            "locale 7",
            "locale 8",
            "locale 9",
            "locale 10",
            "locale 11",
            "locale 12",
            "locale 13",
            "locale 14",
            "locale 15"
        )

        recyclerView = view.findViewById(R.id.recycler_view_locali_preferiti)
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.setHasFixedSize(true)

        arrayList = arrayListOf<ItemLocale>()
        getDataLocali()

        // Trova il bottone e imposta il listener del click
        val buttonProva = view.findViewById<Button>(R.id.prova)
        buttonProva.setOnClickListener {
            findNavController().navigate(R.id.action_utenteProfileFragment_to_utentePaginaLocaleFragment)
        }

        val buttonLogoutUtente = view.findViewById<Button>(R.id.buttone_logout)

        buttonLogoutUtente.setOnClickListener {
            val intent = Intent(activity, MainActivity::class.java)
            startActivity(intent)
        }
    }

    private fun getDataLocali() {
        for(i in nomiLocali.indices){
            val locale = ItemLocale(nomiLocali[i])
            arrayList.add(locale)
        }

        recyclerView.adapter = AdapterLocali(arrayList)
    }*/
}