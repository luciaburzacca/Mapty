package com.example.mapty.recycler_components

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.mapty.R

class AdapterFoto(
    private val fotoList: List<ItemFoto>,
    private val onItemClick: (ItemFoto) -> Unit
) : RecyclerView.Adapter<AdapterFoto.FotoViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FotoViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_foto, parent, false)
        return FotoViewHolder(view)
    }

    override fun onBindViewHolder(holder: FotoViewHolder, position: Int) {
        val itemFoto = fotoList[position]

        Glide.with(holder.itemView.context)
            .load(itemFoto.url)
            .error(R.drawable.img_2 )
            .into(holder.imageView)

        holder.itemView.setOnClickListener {
            onItemClick(itemFoto)
        }
    }

    override fun getItemCount(): Int {
        return fotoList.size
    }

    inner class FotoViewHolder(itemView: android.view.View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.imageView)
    }
}
