package com.denizcan.substracktion.domain.model

data class Subscription(
    val id: Long,
    val countryCode: String,
    val serviceName: String,
    val planName: String,
    val price: Double,
    val currencyCode: String,
    val category: SubscriptionCategory,
    val billingPeriod: BillingPeriod
)
