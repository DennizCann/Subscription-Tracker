package com.denizcan.substracktion.domain.model

enum class BillingPeriod(val label: String) {
    WEEKLY("Haftalik"),
    MONTHLY("Aylik"),
    YEARLY("Yillik");

    companion object {
        fun fromApiOrText(value: String?): BillingPeriod {
            if (value.isNullOrBlank()) return MONTHLY
            val v = value.trim().lowercase()
            return when {
                v in setOf("weekly", "week", "w", "haftalik", "hafta") -> WEEKLY
                v in setOf("yearly", "annual", "year", "y", "yillik", "yil") -> YEARLY
                v in setOf("monthly", "month", "m", "aylik", "ay") -> MONTHLY
                else -> MONTHLY
            }
        }
    }
}
