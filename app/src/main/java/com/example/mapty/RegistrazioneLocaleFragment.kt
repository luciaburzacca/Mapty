package com.example.mapty

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Patterns
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class RegistrazioneLocaleFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    private lateinit var coordinateTextView: TextView
    private var selectedLatitude: Double? = null
    private var selectedLongitude: Double? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_registrazione_locale, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        val emailEditText = view.findViewById<EditText>(R.id.editTextEmailLocale)
        val passwordEditText = view.findViewById<EditText>(R.id.editTextPassLocale)
        val nomeLocaleEditText = view.findViewById<EditText>(R.id.editTextNomeLocale)
        coordinateTextView = view.findViewById(R.id.coordinateLocale)
        val numeroTelefonoEditText = view.findViewById<EditText>(R.id.editTextNumeroTelefono)
        val registerButton = view.findViewById<Button>(R.id.buttonSalvaRegistrazioneLocale)
        val cancelButton = view.findViewById<Button>(R.id.buttonAnnullaLocale)
        val buttonGoRegistrazioneUtente = view.findViewById<Button>(R.id.buttonGoRegistrazioneUtente)

        coordinateTextView.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, LocaleSelezionaMappaFragment())
                .addToBackStack(null)
                .commit()
        }

        parentFragmentManager.setFragmentResultListener("location_request", viewLifecycleOwner) { _, result ->
            selectedLatitude = result.getDouble("latitude")
            selectedLongitude = result.getDouble("longitude")
            coordinateTextView.text = "Latitudine: $selectedLatitude, Longitudine: $selectedLongitude"
        }

        registerButton.setOnClickListener {
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()
            val nomeLocale = nomeLocaleEditText.text.toString().trim()
            val numeroTelefono = numeroTelefonoEditText.text.toString().trim()

            if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                emailEditText.error = "Inserisci un'email valida"
                emailEditText.requestFocus()
                return@setOnClickListener
            }

            if (password.isEmpty() || password.length < 6) {
                passwordEditText.error = "Inserisci una password di almeno 6 caratteri"
                passwordEditText.requestFocus()
                return@setOnClickListener
            }

            if (nomeLocale.isEmpty()) {
                nomeLocaleEditText.error = "Inserisci il nome del locale"
                nomeLocaleEditText.requestFocus()
                return@setOnClickListener
            }

            if (numeroTelefono.isEmpty()) {
                numeroTelefonoEditText.error = "Inserisci un numero di telefono"
                numeroTelefonoEditText.requestFocus()
                return@setOnClickListener
            }

            if (selectedLatitude == null || selectedLongitude == null) {
                Toast.makeText(requireContext(), "Seleziona le coordinate", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val userId = auth.currentUser?.uid ?: ""
                    val locale = hashMapOf(
                        "email" to email,
                        "nomeLocale" to nomeLocale,
                        "latitude" to selectedLatitude,
                        "longitude" to selectedLongitude
                    )
                    db.collection("locali").document(userId).set(locale).addOnSuccessListener {
                        Toast.makeText(requireContext(), "Registrazione avvenuta con successo", Toast.LENGTH_SHORT).show()
                        // Naviga verso LocaleActivity
                        startActivity(Intent(requireContext(), LocaleActivity::class.java))
                    }.addOnFailureListener {
                        Toast.makeText(requireContext(), "Errore nel salvataggio dei dati", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(requireContext(), "Errore nella registrazione: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }

        cancelButton.setOnClickListener {
            startActivity(Intent(requireContext(), MainActivity::class.java))
        }

        buttonGoRegistrazioneUtente.setOnClickListener {
            // Naviga verso RegistrazioneLocaleFragment
            navigateToFragment(RegistrazioneUtenteFragment())
        }

    }

    private fun navigateToFragment(fragment: Fragment) {
        activity?.supportFragmentManager?.beginTransaction()
            ?.replace(R.id.fragment_container, fragment)
            ?.addToBackStack(null)
            ?.commit()
    }
}