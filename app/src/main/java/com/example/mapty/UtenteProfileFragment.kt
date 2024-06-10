package com.example.mapty

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class UtenteProfileFragment : Fragment(R.layout.fragment_utente_profile){

    lateinit var recyclerView: RecyclerView
    lateinit var arrayList: ArrayList<ItemLocale>
    lateinit var nomiLocali: Array<String>

    override fun onViewCreated(view: View, savedInstanceState: Bundle?){
        super.onViewCreated(view, savedInstanceState)

        nomiLocali = arrayOf(
            "locale 1",
            "locale 2",
            "locale 3",
            "locale 4",
            "locale 5",
            "locale 6",
            "locale 7",
            "locale 8",
            "locale 9",
            "locale 10",
            "locale 11",
            "locale 12",
            "locale 13",
            "locale 14",
            "locale 15"
        )

        recyclerView = view.findViewById(R.id.recycler_view_locali_preferiti)
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.setHasFixedSize(true)

        arrayList = arrayListOf<ItemLocale>()
        getDataLocali()

        // Trova il bottone e imposta il listener del click
        val buttonProva = view.findViewById<Button>(R.id.prova)
        buttonProva.setOnClickListener {
            findNavController().navigate(R.id.action_utenteProfileFragment_to_utentePaginaLocaleFragment)
        }
    }

    private fun getDataLocali() {
        for(i in nomiLocali.indices){
            val locale = ItemLocale(nomiLocali[i])
            arrayList.add(locale)
        }

        recyclerView.adapter = MyAdapter(arrayList)
    }
}