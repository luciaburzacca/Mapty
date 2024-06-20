package com.example.mapty.utente

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import android.widget.Button
import android.widget.ImageButton

import androidx.navigation.fragment.findNavController
import com.example.mapty.R


class UtenteFiltroEventiFragment : Fragment() {


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_utente_filtro_eventi, container, false)

        view.findViewById<Button>(R.id.buttonTornaHome).setOnClickListener {
            findNavController().navigate(R.id.action_utenteFiltroEventiFragment_to_utenteHomeFragment)
        }

        val imageButtonAll = view.findViewById<ImageButton>(R.id.imageButtonAll)
        val imageButtonBar = view.findViewById<ImageButton>(R.id.imageButtonBar)
        val imageButtonBeach = view.findViewById<ImageButton>(R.id.imageButtonBeach)
        val imageButtonDisco = view.findViewById<ImageButton>(R.id.imageButtonDisco)
        val imageButtonFilm = view.findViewById<ImageButton>(R.id.imageButtonFilm)
        val imageButtonKaraoke = view.findViewById<ImageButton>(R.id.imageButtonKaraoke)
        val imageButtonLive = view.findViewById<ImageButton>(R.id.imageButtonLive)
        val imageButtonLocal = view.findViewById<ImageButton>(R.id.imageButtonLocal)
        val imageButtonRaggaeton = view.findViewById<ImageButton>(R.id.imageButtonReggaeton)
        val imageButtonSlay = view.findViewById<ImageButton>(R.id.imageButtonSlay)
        val imageButtonTechno = view.findViewById<ImageButton>(R.id.imageButtonTechno)
        val imageButtonthematic = view.findViewById<ImageButton>(R.id.imageButtonThematic)

        imageButtonAll.setOnClickListener {
            val action = UtenteFiltroEventiFragmentDirections.actionUtenteFiltroEventiFragmentToUtenteListaEventiFragment(" ")
            findNavController().navigate(action)
        }

        imageButtonBar.setOnClickListener {
            val action = UtenteFiltroEventiFragmentDirections.actionUtenteFiltroEventiFragmentToUtenteListaEventiFragment("Bar Party")
            findNavController().navigate(action)
        }

        imageButtonBeach.setOnClickListener {
            val action = UtenteFiltroEventiFragmentDirections.actionUtenteFiltroEventiFragmentToUtenteListaEventiFragment("Beach Party")
            findNavController().navigate(action)
        }

        imageButtonDisco.setOnClickListener {
            val action = UtenteFiltroEventiFragmentDirections.actionUtenteFiltroEventiFragmentToUtenteListaEventiFragment("Disco Party")
            findNavController().navigate(action)
        }

        imageButtonFilm.setOnClickListener {
            val action = UtenteFiltroEventiFragmentDirections.actionUtenteFiltroEventiFragmentToUtenteListaEventiFragment("Films Party")
            findNavController().navigate(action)
        }

        imageButtonKaraoke.setOnClickListener {
            val action = UtenteFiltroEventiFragmentDirections.actionUtenteFiltroEventiFragmentToUtenteListaEventiFragment("Karaoke")
            findNavController().navigate(action)
        }

        imageButtonLive.setOnClickListener {
            val action = UtenteFiltroEventiFragmentDirections.actionUtenteFiltroEventiFragmentToUtenteListaEventiFragment("Live Music")
            findNavController().navigate(action)
        }

        imageButtonLocal.setOnClickListener {
            val action = UtenteFiltroEventiFragmentDirections.actionUtenteFiltroEventiFragmentToUtenteListaEventiFragment("Local Parties")
            findNavController().navigate(action)
        }

        imageButtonRaggaeton.setOnClickListener {
            val action = UtenteFiltroEventiFragmentDirections.actionUtenteFiltroEventiFragmentToUtenteListaEventiFragment("Raggaeton Party")
            findNavController().navigate(action)
        }

        imageButtonSlay.setOnClickListener {
            val action = UtenteFiltroEventiFragmentDirections.actionUtenteFiltroEventiFragmentToUtenteListaEventiFragment("Slay Party")
            findNavController().navigate(action)
        }

        imageButtonTechno.setOnClickListener {
            val action = UtenteFiltroEventiFragmentDirections.actionUtenteFiltroEventiFragmentToUtenteListaEventiFragment("Techno")
            findNavController().navigate(action)
        }

        imageButtonthematic.setOnClickListener {
            val action = UtenteFiltroEventiFragmentDirections.actionUtenteFiltroEventiFragmentToUtenteListaEventiFragment("Thematic")
            findNavController().navigate(action)
        }



        return view
    }


}