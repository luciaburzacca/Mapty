package com.example.mapty

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mapty.recycler_components.AdapterEventi
import com.example.mapty.recycler_components.AdapterLocali
import com.example.mapty.recycler_components.ItemEvento
import com.example.mapty.recycler_components.ItemLocale

class UtenteListaEventiFragment : Fragment(R.layout.fragment_utente_lista_eventi) {

    lateinit var recyclerView: RecyclerView
    lateinit var arrayList: ArrayList<ItemEvento>
    lateinit var nomiEventi: Array<String>


    override fun onViewCreated(view: View, savedInstanceState: Bundle?){
        super.onViewCreated(view, savedInstanceState)

        nomiEventi = arrayOf(
            "evento 1",
            "evento 2",
            "evento 3",
            "evento 4",
            "evento 5",
            "evento 6",
            "evento 7",
            "evento 8",
            "evento 9",
            "evento 10",
            "evento 11",
            "evento 12",
            "evento 13",
            "evento 14",
            "evento 15"
        )

        recyclerView = view.findViewById(R.id.recycler_view_eventi_utente)
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.setHasFixedSize(true)

        arrayList = arrayListOf<ItemEvento>()
        getDataEventi()
    }

    private fun getDataEventi() {
        for(i in nomiEventi.indices){
            val evento = ItemEvento(nomiEventi[i])
            arrayList.add(evento)
        }

        recyclerView.adapter = AdapterEventi(arrayList)
    }

}