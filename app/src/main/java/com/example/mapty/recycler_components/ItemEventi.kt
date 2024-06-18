package com.example.mapty.recycler_components

import com.google.firebase.firestore.GeoPoint
import com.google.type.Date

data class ItemEvento(
    var id: String = "",
    val nomeEvento: String = "",
    val descrizione: String = "",
    val tipoEvento: String = "",
    val prezzo: String = "0.0",
    val location: GeoPoint? = null,
    val data: Long = 0L,
    val dataFine: Long = 0L,
    val nomeLocale: String? = "",
    val numeroTelefono: String = "",
) {

}