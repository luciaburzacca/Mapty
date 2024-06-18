package com.example.mapty.recycler_components

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.mapty.R
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AdapterEventi(private val eventiList: List<ItemEvento>, private val onItemClick: (ItemEvento) -> Unit) : RecyclerView.Adapter<AdapterEventi.EventoViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventoViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_evento, parent, false)
        return EventoViewHolder(view)
    }

    override fun onBindViewHolder(holder: EventoViewHolder, position: Int) {
        val evento = eventiList[position]
        holder.bind(evento)
        holder.itemView.setOnClickListener {
            onItemClick(evento)
        }
    }

    override fun getItemCount(): Int = eventiList.size

    class EventoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nomeEventoTextView: TextView = itemView.findViewById(R.id.evento_nome)
        private val tipoEventoTextView: TextView = itemView.findViewById(R.id.evento_tag)
        private val nomeLocaleTextView: TextView = itemView.findViewById(R.id.evento_locale)
        private val dataEventoTextView: TextView = itemView.findViewById(R.id.evento_data)

        fun bind(evento: ItemEvento) {
            nomeEventoTextView.text = evento.nomeEvento
            tipoEventoTextView.text = evento.tipo
            nomeLocaleTextView.text = evento.nomeLocale
            val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
            val data = Date(evento.data)
            dataEventoTextView.text = dateFormat.format(data)
        }
    }
}
