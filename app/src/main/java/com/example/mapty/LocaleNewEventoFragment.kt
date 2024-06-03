package com.example.mapty

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import androidx.fragment.app.Fragment

class LocaleNewEventoFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_locale_new_evento, container, false)


    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val autoCompleteTextView: AutoCompleteTextView = view.findViewById(R.id.lista_item_locale)
        // Dati da visualizzare nel menu a discesa
        val items = arrayOf("Bar Party", "Beach Party", "Disco Party", "Films Night",
            "Karaoke", "Live Music", "Local Parties", "Raggaeton Party", "Slay Party", "Techno", "Thematic Party")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, items)
        autoCompleteTextView.setAdapter(adapter)
    }

}