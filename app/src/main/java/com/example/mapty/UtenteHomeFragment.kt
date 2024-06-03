package com.example.mapty

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.navigation.fragment.findNavController

class UtenteHomeFragment : Fragment() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {

        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.fragment_utente_home, container, false)
        //mapView = view.findViewById(R.id.mapView)

        view.findViewById<Button>(R.id.button_filtro_eventi).setOnClickListener {
            findNavController().navigate(R.id.action_utenteHomeFragment_to_utenteFiltroEventiFragment)
        }
        return view

    }


}