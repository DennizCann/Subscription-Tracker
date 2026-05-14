package com.denizcan.substracktion.domain.model

data class SuggestedPlansResult(
    val plans: List<SuggestedPlan>,
    val disclaimer: String?
)
