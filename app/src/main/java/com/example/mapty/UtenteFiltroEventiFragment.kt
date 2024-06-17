package com.example.mapty

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import android.widget.SearchView
import androidx.navigation.fragment.findNavController
import com.google.firebase.firestore.FirebaseFirestore

class UtenteFiltroEventiFragment : Fragment() {

    private lateinit var searchView: SearchView
    private lateinit var suggestionsListView: ListView
    private lateinit var db: FirebaseFirestore
    private lateinit var searchAdapter: ArrayAdapter<String>
    private val suggestions: MutableList<String> = mutableListOf()
    private val suggestionsType: MutableList<String> = mutableListOf()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_utente_filtro_eventi, container, false)
        view.findViewById<Button>(R.id.buttonTornaHome).setOnClickListener {
            findNavController().navigate(R.id.action_utenteFiltroEventiFragment_to_utenteHomeFragment)
        }


        searchView = view.findViewById(R.id.search_view_filtro)
        suggestionsListView = view.findViewById(R.id.suggestions_list_view)
        db = FirebaseFirestore.getInstance()

        searchAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, suggestions)
        suggestionsListView.adapter = searchAdapter

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                if (query != null) {
                    performSearch(query)
                }
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if (newText != null) {
                    fetchSuggestions(newText)
                }
                return false
            }
        })

        suggestionsListView.setOnItemClickListener { parent, view, position, id ->
            val selectedSuggestion = suggestions[position]
            val selectedType = suggestionsType[position]
            if (selectedType == "locale") {
                //val action = SearchFragmentDirections.actionSearchFragmentToUtentePaginaLocaleFragment(selectedSuggestion)
                //findNavController().navigate(action)
            } else if (selectedType == "evento") {
                //val action = SearchFragmentDirections.actionSearchFragmentToUtenteVistaEventoFragment(selectedSuggestion)
                //findNavController().navigate(action)
            }
        }

        return view
    }

    private fun performSearch(query: String) {
        // Perform search if needed
    }

    private fun fetchSuggestions(query: String) {
        suggestions.clear()
        suggestionsType.clear()

        // Search in 'locali' collection
        db.collection("locali")
            .whereGreaterThanOrEqualTo("nomeLocale", query)
            .whereLessThanOrEqualTo("nomeLocale", query + '\uf8ff')
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    val nomeLocale = document.getString("nomeLocale")
                    if (nomeLocale != null) {
                        suggestions.add(nomeLocale)
                        suggestionsType.add("locale")
                    }
                }
                searchAdapter.notifyDataSetChanged()
            }

        // Search in 'eventos' collection
        db.collection("eventos")
            .whereGreaterThanOrEqualTo("nome", query)
            .whereLessThanOrEqualTo("nome", query + '\uf8ff')
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    val nomeEvento = document.getString("nome")
                    if (nomeEvento != null) {
                        suggestions.add(nomeEvento)
                        suggestionsType.add("evento")
                    }
                }
                searchAdapter.notifyDataSetChanged()
            }
    }
}