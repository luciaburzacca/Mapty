package com.example.mapty.recycler_components

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.mapty.R

class AdapterLocali(private val localiList: List<ItemLocale>) :
    RecyclerView.Adapter<AdapterLocali.LocaleViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LocaleViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_locale, parent, false)
        return LocaleViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: LocaleViewHolder, position: Int) {
        val locale = localiList[position]
        holder.bind(locale)
    }

    override fun getItemCount(): Int {
        return localiList.size
    }

    class LocaleViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val textViewNomeLocale: TextView = itemView.findViewById(R.id.locale_nome)
        private val textViewPosizione: TextView = itemView.findViewById(R.id.locale_indirizzo)

        fun bind(locale: ItemLocale) {
            textViewNomeLocale.text = locale.nomeLocale
            textViewPosizione.text =
                "Latitudine: ${locale.posizioneLocale?.latitude}, Longitudine: ${locale.posizioneLocale?.longitude}"
        }
    }
}

