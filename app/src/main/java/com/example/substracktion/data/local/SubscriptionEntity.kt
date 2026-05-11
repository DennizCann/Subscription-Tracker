package com.example.substracktion.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "subscriptions")
data class SubscriptionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val countryCode: String,
    val serviceName: String,
    val planName: String,
    val price: Double,
    val currencyCode: String,
    val category: String,
    val billingPeriod: String
)
