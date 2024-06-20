package com.example.mapty.recycler_components

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.mapty.R

class AdapterFoto(private val fotoList: List<ItemFoto>, private val onItemClick: (ItemFoto) -> Unit) :
    RecyclerView.Adapter<AdapterFoto.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imageViewFoto: ImageView = itemView.findViewById(R.id.imageView)

        fun bind(itemFoto: ItemFoto) {
            Glide.with(itemView.context)
                .load(itemFoto.url)
                .centerCrop()
                .into(imageViewFoto)

            itemView.setOnClickListener {
                onItemClick(itemFoto)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_foto, parent, false)
        val size = parent.measuredWidth / 3 // Divide per 3 per una griglia a 3 colonne
        view.layoutParams.width = size
        view.layoutParams.height = size
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val itemFoto = fotoList[position]
        holder.bind(itemFoto)
    }

    override fun getItemCount(): Int {
        return fotoList.size
    }
}

