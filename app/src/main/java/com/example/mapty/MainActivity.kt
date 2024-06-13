package com.example.mapty

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

import android.widget.EditText
import android.widget.FrameLayout
import android.widget.Toast
import androidx.core.content.ContextCompat.startActivity
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore


class MainActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    private lateinit var editTextEmail: EditText
    private lateinit var editTextPassword: EditText
    private lateinit var buttonAccedi: Button
    private lateinit var buttonRegistrazione: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        editTextEmail = findViewById(R.id.editTextEmail)
        editTextPassword = findViewById(R.id.editTextPassword)
        buttonAccedi = findViewById(R.id.buttonAccedi)
        buttonRegistrazione = findViewById<Button>(R.id.buttonRegistrazione)

        buttonAccedi.setOnClickListener {
            val email = editTextEmail.text.toString().trim()
            val password = editTextPassword.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Svegliati! Non vedi lo spazio vuoto?", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Effettua il login con Firebase Authentication
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Login avvenuto con successo, verifica la raccolta dell'email
                        checkUserCollection(email)
                    } else {
                        // Gestione dell'errore di login
                        Toast.makeText(this, "SBAGLIATOOOO", Toast.LENGTH_SHORT).show()
                    }
                }
        }

        buttonRegistrazione.setOnClickListener {
            navigateToFragment(RegistrazioneUtenteFragment())
        }
    }

    private fun navigateToFragment(fragment: Fragment) {
        // Nascondi il contenuto principale
        findViewById<View>(R.id.main_content).visibility = View.GONE
        // Mostra il contenitore del frammento
        findViewById<FrameLayout>(R.id.fragment_container).visibility = View.VISIBLE

        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()
    }

    override fun onBackPressed() {
        if (supportFragmentManager.backStackEntryCount > 0) {
            supportFragmentManager.popBackStack()
            // Mostra il contenuto principale e nascondi il contenitore del frammento
            findViewById<View>(R.id.main_content).visibility = View.VISIBLE
            findViewById<FrameLayout>(R.id.fragment_container).visibility = View.GONE
        } else {
            super.onBackPressed()
        }
    }

    private fun checkUserCollection(email: String) {
        // Cerca l'email nella collezione "locali"
        db.collection("locali")
            .whereEqualTo("email", email)
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    // Trovato nella raccolta locali, apri LocaleActivity
                    startActivity(Intent(this, LocaleActivity::class.java))
                    finish()
                } else {
                    // Non trovato nella raccolta locali, cerca nella raccolta "utenti"
                    checkUtentiCollection(email)
                }
            }
            .addOnFailureListener { e ->
                // Gestione dell'errore nella query
                Toast.makeText(this, "Errore nella ricerca email: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun checkUtentiCollection(email: String) {
        // Cerca l'email nella collezione "utenti"
        db.collection("utenti")
            .whereEqualTo("email", email)
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    // Trovato nella raccolta utenti, apri UtenteActivity
                    startActivity(Intent(this, UtenteActivity::class.java))
                    finish()
                } else {
                    // Non trovato nella raccolta utenti
                    Toast.makeText(this, "Email non trovata", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                // Gestione dell'errore nella query
                Toast.makeText(this, "Errore nella ricerca email: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}


