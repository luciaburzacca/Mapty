package com.example.mapty.recycler_components

import com.google.firebase.firestore.GeoPoint

data class ItemLocale(
    val localeId: String = "",
    var localeRef: String = null.toString(),
    val posizioneLocale: GeoPoint? = null,
    val nomeLocale: String = ""
)