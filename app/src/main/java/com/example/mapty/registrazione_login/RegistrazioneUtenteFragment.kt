package com.example.mapty.registrazione_login

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.example.mapty.R
import com.example.mapty.utente.UtenteActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await


class RegistrazioneUtenteFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_registrazione_utente, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        val emailEditText = view.findViewById<EditText>(R.id.editTextEmailUtente)
        val usernameEditText = view.findViewById<EditText>(R.id.editTextUsername)
        val nomeEditText = view.findViewById<EditText>(R.id.editTextNomeUtente)
        val cognomeEditText = view.findViewById<EditText>(R.id.editTextCognomeUtente)
        val passwordEditText = view.findViewById<EditText>(R.id.editTextPassUtente)
        val registerButton = view.findViewById<Button>(R.id.buttonSalvaRegistrazioneUtente)
        val cancelButton = view.findViewById<Button>(R.id.buttonAnnullaUtente)
        val buttonGoRegistrazioneLocale = view.findViewById<Button>(R.id.buttonGoRegistrazioneLocale)


        registerButton.setOnClickListener {
            val email = emailEditText.text.toString().trim()
            val username = usernameEditText.text.toString().trim()
            val nome = nomeEditText.text.toString().trim()
            val cognome = cognomeEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()

            if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                emailEditText.error = "Inserisci un'email valida"
                emailEditText.requestFocus()
                return@setOnClickListener
            }

            if (username.isEmpty() || username.contains(" ")) {
                usernameEditText.error = "Il nome utente deve essere unico e senza spazi"
                usernameEditText.requestFocus()
                return@setOnClickListener
            }

            if (nome.isEmpty()) {
                nomeEditText.error = "Inserisci il nome"
                nomeEditText.requestFocus()
                return@setOnClickListener
            }

            if (cognome.isEmpty()) {
                cognomeEditText.error = "Inserisci il cognome"
                cognomeEditText.requestFocus()
                return@setOnClickListener
            }

            if (password.isEmpty() || password.length < 6) {
                passwordEditText.error = "La password deve essere di almeno 6 caratteri"
                passwordEditText.requestFocus()
                return@setOnClickListener
            }

            // Verifica se il nome utente è unico usando Coroutine per operazioni asincrone
            GlobalScope.launch(Dispatchers.Main) {
                try {
                    val result = db.collection("utenti").whereEqualTo("username", username).get().await()

                    if (!result.isEmpty) {
                        usernameEditText.error = "Sei arrivat* tardi, Nome utente già in uso"
                        usernameEditText.requestFocus()
                    } else {
                        registerUser(email, password, username, nome, cognome)
                    }
                } catch (e: Exception) {
                    Toast.makeText(requireContext(), "Errore: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }


        cancelButton.setOnClickListener {
            startActivity(Intent(requireContext(), MainActivity::class.java))
        }

        buttonGoRegistrazioneLocale.setOnClickListener {
            // Naviga verso RegistrazioneLocaleFragment
            navigateToFragment(RegistrazioneLocaleFragment())
        }
    }

    private fun navigateToFragment(fragment: Fragment) {
        activity?.supportFragmentManager?.beginTransaction()
            ?.replace(R.id.fragment_container, fragment)
            ?.addToBackStack(null)
            ?.commit()
    }

    private fun registerUser(email: String, password: String, username: String, nome: String, cognome: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(requireActivity()) { task ->
                if (task.isSuccessful) {
                    val user = hashMapOf(
                        "email" to email,
                        "username" to username,
                        "nome" to nome,
                        "cognome" to cognome
                    )

                    db.collection("utenti").document(auth.currentUser!!.uid)
                        .set(user)
                        .addOnSuccessListener {
                            Toast.makeText(requireContext(), "Registrazione avvenuta con successo", Toast.LENGTH_SHORT).show()
                            // Naviga verso UtenteActivity
                            val intent = Intent(activity, UtenteActivity::class.java)
                            startActivity(intent)
                            activity?.finish()
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(requireContext(), "Errore nel salvataggio dei dati: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                } else {
                    Toast.makeText(requireContext(), "Errore nella registrazione: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }
}