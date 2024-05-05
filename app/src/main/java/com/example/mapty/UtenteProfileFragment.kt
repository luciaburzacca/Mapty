package com.example.mapty

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class UtenteProfileFragment : Fragment(R.layout.fragment_utente_profile){

    lateinit var listaLocali: RecyclerView
    lateinit var dataList: ArrayList<ItemLocale>
    lateinit var nameList: Array<String>
    lateinit var addressList: Array<String>

    override fun onViewCreated(view: View, savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)

        nameList = arrayOf(
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

        addressList = arrayOf(
            "indirizzo 1",
            "indirizzo 2",
            "indirizzo 3",
            "indirizzo 4",
            "indirizzo 5",
            "indirizzo 6",
            "indirizzo 7",
            "indirizzo 8",
            "indirizzo 9",
            "indirizzo 10",
            "indirizzo 11",
            "indirizzo 12",
            "indirizzo 13",
            "indirizzo 14",
            "indirizzo 15"
        )

        listaLocali = findViewById(R.id.recyclerViewLocali)
        listaLocali.layoutManager = LinearLayoutManager(this)
        listaLocali.setHasFixedSize(true)

        dataList = arrayListOf<ItemLocale>()
        getData()

    }

    fun getData(){
        for (i in nameList.indices){
            val dataClass = ItemLocale(nameList[i], addressList[i])
            dataList.add(dataClass)
        }
        listaLocali.adapter = MyAdapter(dataList)
    }

}