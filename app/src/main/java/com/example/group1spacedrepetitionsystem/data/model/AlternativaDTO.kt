package com.example.estudapp.data.model

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class AlternativaDTO(
    val text: String? = null,
    val imageUrl: String? = null
   
) {
    constructor() : this(null, null)
}
