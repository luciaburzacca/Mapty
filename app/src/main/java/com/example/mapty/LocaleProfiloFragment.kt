package com.example.mapty

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore


class LocaleProfiloFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var textNomeLocale: TextView
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_locale_profilo, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val buttonLogoutLocale = view.findViewById<Button>(R.id.bottom_logout_locale)

        buttonLogoutLocale.setOnClickListener {
            val intent = Intent(activity, MainActivity::class.java)
            startActivity(intent)
        }

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // Trova il TextView con l'ID textNomeLocale
        textNomeLocale = view.findViewById(R.id.textNomeLocale)

        // Recupera l'email dell'utente autenticato
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val userEmail = currentUser.email
            if (userEmail != null) {
                getNomeLocale(userEmail)
            } else {
                Toast.makeText(context, "Errore: Email utente non trovata.", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(context, "Errore: Utente non autenticato.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun getNomeLocale(email: String) {
        // Cerca nella raccolta "locali" il documento con l'email dell'utente
        db.collection("locali").whereEqualTo("email", email).get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    val nomeLocale = document.getString("nomeLocale") ?: ""
                    textNomeLocale.text = nomeLocale
                    break // Assume che ci sia solo un documento corrispondente
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(context, "Errore nel recupero del nome del locale: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }
}