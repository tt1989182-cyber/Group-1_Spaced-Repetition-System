package com.example.estudapp.data.model

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class FlashcardDTO(
    var id: String = "",
    var deckId: String = "",
    var type: String = FlashcardTypeEnum.FRENTE_VERSO.name,
    var userId: String = "",

    // Nội dung thẻ
    val frente: String? = null,
    val verso: String? = null,

    // Spaced Repetition (Lặp lại ngắt quãng)
    var nextReview: Long = System.currentTimeMillis(), // Ngày ôn tiếp theo (mặc định là ngay bây giờ)
    var reviewCount: Int = 0, // Số lần đã ôn

    // Các trường khác của bạn giữ nguyên
    val textoComLacunas: String? = null,
    val respostasCloze: Map<String, String>? = null,
    val pergunta: String? = null,
    val respostasValidas: List<String>? = null,
    val alternativas: List<AlternativaDTO>? = null,
    val respostaCorreta: AlternativaDTO? = null,
    
    // Media
    val perguntaImageUrl: String? = null,
    val perguntaAudioUrl: String? = null
) {
    constructor() : this(
        id = "",
        deckId = "",
        type = FlashcardTypeEnum.FRENTE_VERSO.name,
        userId = "",
        frente = null,
        verso = null,
        nextReview = System.currentTimeMillis(),
        reviewCount = 0
    )
}