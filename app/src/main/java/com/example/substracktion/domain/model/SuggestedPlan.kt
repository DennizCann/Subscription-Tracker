package com.example.substracktion.domain.model

data class SuggestedPlan(
    val name: String,
    val price: Double,
    val currencyCode: String,
    val period: BillingPeriod,
    val detailNote: String? = null
)
