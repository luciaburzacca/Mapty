package com.example.mapty.recycler_components

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.mapty.R

class AdapterEventi(private val listaEventi: MutableList<ItemEvento>) :  RecyclerView.Adapter<AdapterEventi.EventoViewHolder>() {

        class EventoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val tvNomeEvento: TextView = itemView.findViewById(R.id.evento_nome)
            val tvDataOraInizio: TextView = itemView.findViewById(R.id.evento_data)
            val tvNomeLocale: TextView = itemView.findViewById(R.id.evento_locale)
            val tvTipoEvento: TextView = itemView.findViewById(R.id.evento_tag)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventoViewHolder {
            val itemView = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_evento, parent, false)
            return EventoViewHolder(itemView)
        }

        override fun onBindViewHolder(holder: EventoViewHolder, position: Int) {
            val evento = listaEventi[position]
            holder.tvNomeEvento.text = evento.nomeEvento
            //holder.tvDataOraInizio.text = evento.dateTimeInizio
            holder.tvNomeLocale.text = evento.nomeLocale
            holder.tvTipoEvento.text = evento.tipoEvento
        }

        override fun getItemCount() = listaEventi.size
    }
