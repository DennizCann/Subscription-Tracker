package com.example.substracktion.data.remote.groq

import com.google.gson.annotations.SerializedName

data class GroqPlansRootDto(
    @SerializedName("plans") val plans: List<GroqPlanItemDto>?,
    @SerializedName("disclaimer") val disclaimer: String?
)

data class GroqPlanItemDto(
    @SerializedName("name") val name: String?,
    @SerializedName("price") val price: Double?,
    @SerializedName("currency") val currency: String?,
    @SerializedName("billing_period") val billingPeriod: String?,
    @SerializedName("notes") val notes: String?,
    @SerializedName("billing") val billing: String?
)
