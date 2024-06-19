package com.example.mapty.recycler_components

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.mapty.R

class AdapterLocali(
    private val localiList: List<ItemLocale>,
    private val onItemClick: (String) -> Unit
) : RecyclerView.Adapter<AdapterLocali.LocaleViewHolder>() {

    inner class LocaleViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nomeLocale: TextView = itemView.findViewById(R.id.locale_nome)
        val Posizione: TextView = itemView.findViewById(R.id.locale_indirizzo)


        fun bind(locale: ItemLocale) {
            nomeLocale.text = locale.nomeLocale
            "Latitudine: ${locale.posizioneLocale?.latitude}, Longitudine: ${locale.posizioneLocale?.longitude}".also { Posizione.text = it }
            itemView.setOnClickListener {
                onItemClick(locale.localeId)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LocaleViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_locale, parent, false)
        return LocaleViewHolder(view)
    }

    override fun onBindViewHolder(holder: LocaleViewHolder, position: Int) {
        holder.bind(localiList[position])
    }

    override fun getItemCount(): Int {
        return localiList.size
    }
}


