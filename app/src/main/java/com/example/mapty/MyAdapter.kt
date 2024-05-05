package com.example.mapty

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class MyAdapter(private val dataList: ArrayList<ItemLocale>) : RecyclerView.Adapter<MyAdapter.MyViewHolder>() {

    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nomeLoc: TextView = itemView.findViewById(R.id.nomeLocale)
        val indirizzoLoc: TextView = itemView.findViewById(R.id.indirizzoLocale)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView =
            LayoutInflater.from(parent.context).inflate(R.layout.item_locale, parent, false)
        return MyViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val currentItem = dataList[position]
        holder.nomeLoc.text = currentItem.nomeLocale
        holder.indirizzoLoc.text = currentItem.indirizzoLocale
    }

    override fun getItemCount(): Int = dataList.size
}