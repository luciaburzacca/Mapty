package com.example.mapty.locale

import android.content.ContentValues
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.mapty.MainActivity
import com.example.mapty.R
import com.example.mapty.recycler_components.AdapterFoto
import com.example.mapty.recycler_components.ItemFoto
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Locale


class LocaleProfiloFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var textNomeLocale: TextView
    private lateinit var textViewMediaStelleLocale: TextView
    private lateinit var recyclerViewFoto: RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_locale_profilo, container, false)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        textNomeLocale = view.findViewById(R.id.textNomeLocale)
        textViewMediaStelleLocale = view.findViewById(R.id.textViewMediaStelleLocale)
        recyclerViewFoto = view.findViewById(R.id.recycler_view_foto_locale)

        val buttonLogoutLocale = view.findViewById<Button>(R.id.bottom_logout_locale)

        buttonLogoutLocale.setOnClickListener {
            auth.signOut()
            val intent = Intent(activity, MainActivity::class.java)
            startActivity(intent)
            activity?.finish()
        }

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

        return view
    }

    private fun getNomeLocale(email: String) {
        db.collection("locali")
            .whereEqualTo("email", email)
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    val nomeLocale = document.getString("nomeLocale") ?: ""
                    textNomeLocale.text = nomeLocale

                    val localeId = document.id

                    calcolaMediaVoti(localeId)
                    caricaFotoLocale(localeId) // Call to load photos as well
                    break
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(context, "Errore nel recupero del nome del locale: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun calcolaMediaVoti(localeId: String) {
        db.collection("locali")
            .document(localeId)
            .collection("voti_utenti")
            .get()
            .addOnSuccessListener { documents ->
                var totaleVoti = 0.0
                var conteggioVoti = 0

                for (document in documents) {
                    val valoreVoto = document.getDouble("valore")
                    if (valoreVoto != null) {
                        totaleVoti += valoreVoto
                        conteggioVoti++
                    }
                }

                if (conteggioVoti > 0) {
                    val mediaVoti = totaleVoti / conteggioVoti
                    textViewMediaStelleLocale.text =
                        String.format(Locale.getDefault(), "%.1f media dei voti", mediaVoti)
                } else {
                    textViewMediaStelleLocale.text = "Non ci sono ancora voti"
                }
            }
            .addOnFailureListener { exception ->
                Log.e(
                    ContentValues.TAG,
                    "Errore nel recupero dei voti degli utenti per il locale",
                    exception
                )
                Toast.makeText(
                    context,
                    "Errore nel recupero dei voti degli utenti per il locale",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    private fun caricaFotoLocale(localeId: String) {
        db.collection("locali")
            .document(localeId)
            .collection("foto")
            .get()
            .addOnSuccessListener { documents ->
                val fotoList = mutableListOf<ItemFoto>()
                for (document in documents) {
                    val url = document.getString("url") ?: ""
                    val itemFoto = ItemFoto(url, "Nome Utente") // Replace with actual user name retrieval if needed
                    fotoList.add(itemFoto)
                }

                if (fotoList.isEmpty()) {
                    Toast.makeText(context, "Non ci sono ancora foto per questo locale", Toast.LENGTH_SHORT).show()
                } else {
                    mostraFotoInRecyclerView(fotoList)
                }
            }
            .addOnFailureListener { exception ->
                Log.e(ContentValues.TAG, "Errore nel recupero delle foto del locale", exception)
                Toast.makeText(context, "Errore nel recupero delle foto del locale", Toast.LENGTH_SHORT).show()
            }
    }

    private fun mostraFotoInRecyclerView(fotoList: List<ItemFoto>) {
        recyclerViewFoto.layoutManager = GridLayoutManager(context, 3)
        val adapterFoto = AdapterFoto(fotoList) { itemFoto ->
            mostraFotoSchermoIntero(itemFoto)
        }
        recyclerViewFoto.adapter = adapterFoto
    }

    private fun mostraFotoSchermoIntero(itemFoto: ItemFoto) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_foto_locale, null)
        val imageViewFullScreen = dialogView.findViewById<ImageView>(R.id.viewFoto)
        val usernameTextView = dialogView.findViewById<TextView>(R.id.textViewUsername)

        Glide.with(requireContext())
            .load(itemFoto.url)
            .into(imageViewFullScreen)

        usernameTextView.text = itemFoto.nomeUtente // Replace with actual username if available

        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()

        dialog.show()
    }
}