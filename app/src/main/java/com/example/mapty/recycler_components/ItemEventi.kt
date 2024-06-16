package com.example.mapty.recycler_components

import com.google.firebase.firestore.GeoPoint
import com.google.type.Date

data class ItemEvento(
    val nomeEvento: String = "",
    val descrizione: String = "",
    val tipoEvento: String = "",
    val prezzo: Double = 0.0,
    val location: GeoPoint? = null,
    val dateTimeInizio: Date? = null,
    val dateTimeFine: Date? = null,
    val nomeLocale: String = "",
)