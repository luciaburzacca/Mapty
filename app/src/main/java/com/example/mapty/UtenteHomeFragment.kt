package com.example.mapty

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
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
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_utente_home, container, false)

        val bottoneListaEventi: Button = view.findViewById(R.id.button_visualizza_eventi)

        bottoneListaEventi.setOnClickListener{
            findNavController().navigate(R.id.action_utenteHomeFragment_to_utenteListaEventiFragment)
        }
    }

}