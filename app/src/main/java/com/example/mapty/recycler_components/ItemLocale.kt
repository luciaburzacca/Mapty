package com.example.mapty.recycler_components

import com.google.firebase.firestore.GeoPoint

data class ItemLocale(
    var localeId: String,
    val nomeLocale: String,
    val posizioneLocale: GeoPoint,
)