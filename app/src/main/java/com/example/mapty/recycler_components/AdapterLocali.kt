package com.example.mapty.recycler_components

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.mapty.R
import com.google.firebase.firestore.GeoPoint

class AdapterLocali(
    private val localiList: List<ItemLocale>,
    private val onItemClick: (ItemLocale) -> Unit
) : RecyclerView.Adapter<AdapterLocali.LocaleViewHolder>() {

    inner class LocaleViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nomeLocale: TextView = itemView.findViewById(R.id.locale_nome)
        val posizione: TextView = itemView.findViewById(R.id.locale_indirizzo)

        fun bind(locale: ItemLocale) {
            nomeLocale.text = locale.nomeLocale
            if (locale.posizioneLocale != null) {
                val latitudine = locale.posizioneLocale.latitude
                val longitudine = locale.posizioneLocale.longitude
                posizione.text = "Latitudine: $latitudine, Longitudine: $longitudine"
            } else {
                posizione.text = "Posizione non disponibile"
            }
            itemView.setOnClickListener {
                onItemClick(locale)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LocaleViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_locale, parent, false)
        return LocaleViewHolder(view)
    }

    override fun onBindViewHolder(holder: LocaleViewHolder, position: Int) {
        holder.bind(localiList[position])
    }

    override fun getItemCount(): Int {
        return localiList.size
    }
}



