package com.example.mapty

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.navigation.fragment.findNavController

class UtenteFiltroEventiFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.fragment_utente_filtro_eventi, container, false)
        view.findViewById<Button>(R.id.buttonTornaHome).setOnClickListener {
            findNavController().navigate(R.id.action_utenteFiltroEventiFragment_to_utenteHomeFragment)
        }
        return view
    }


}