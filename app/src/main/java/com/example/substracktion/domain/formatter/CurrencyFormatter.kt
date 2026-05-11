package com.example.substracktion.domain.formatter

import java.util.Currency
import java.util.Locale

object CurrencyFormatter {
    fun symbolOrCode(currencyCode: String, locale: Locale = Locale.getDefault()): String {
        val symbol = runCatching {
            Currency.getInstance(currencyCode).getSymbol(locale)
        }.getOrElse {
            currencyCode
        }

        // Some locales may return the code ("TRY") instead of symbol.
        if (currencyCode == "TRY" && symbol == "TRY") return "₺"

        return symbol
    }

    fun formatAmount(amount: Double, currencyCode: String, locale: Locale = Locale.getDefault()): String {
        if (amount == 0.0) {
            return "${symbolOrCode(currencyCode, locale)} —"
        }
        val formatted = String.format(Locale.US, "%.2f", amount)
        return "${symbolOrCode(currencyCode, locale)} $formatted"
    }
}
