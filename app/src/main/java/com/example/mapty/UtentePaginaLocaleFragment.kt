package com.example.mapty

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import android.widget.Button
import android.widget.LinearLayout
import androidx.core.content.ContextCompat


class UtentePaginaLocaleFragment : Fragment() {

    private lateinit var buttonShowListaEventiLocale: Button
    private lateinit var buttonShowFotoLocale: Button
    private lateinit var fragmentLayout: LinearLayout

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_utente_pagina_locale, container, false)
    }
    /*override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        buttonShowListaEventiLocale = view.findViewById(R.id.buttonShowListaEventiLocale)
        buttonShowFotoLocale = view.findViewById(R.id.buttonShowFotoLocale)
        fragmentLayout = view.findViewById(/* id = */ R.id.sfondoRecycler)

        // Imposta i colori iniziali
        buttonShowListaEventiLocale.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.viola))
        buttonShowFotoLocale.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.dark_gray))
        fragmentLayout.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.lila))

        // Imposta i listener per i click dei pulsanti
        buttonShowListaEventiLocale.setOnClickListener {
            setButtonColorsAndBackground(buttonShowListaEventiLocale, buttonShowFotoLocale, R.color.lila)
        }

        buttonShowFotoLocale.setOnClickListener {
            setButtonColorsAndBackground(buttonShowFotoLocale, buttonShowListaEventiLocale, R.color.black)
        }
    }

    private fun setButtonColorsAndBackground(activeButton: Button, inactiveButton: Button, backgroundColor: Int) {
        activeButton.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.viola))
        inactiveButton.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.dark_gray))
        fragmentLayout.setBackgroundColor(ContextCompat.getColor(requireContext(), backgroundColor))
    }*/
}