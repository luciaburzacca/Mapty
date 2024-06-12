package com.example.mapty.recycler_components

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.mapty.R

class AdapterLocali(private val listalocali: ArrayList<ItemLocale>) : RecyclerView.Adapter<AdapterLocali.MyViewHolder>() {

    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nomeLoc: TextView = itemView.findViewById(R.id.locale_nome)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_locale, parent, false)
        return MyViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val currentItem = listalocali[position]
        holder.nomeLoc.text = currentItem.nomeLocale
    }

    override fun getItemCount(): Int {
        return listalocali.size

    }
}