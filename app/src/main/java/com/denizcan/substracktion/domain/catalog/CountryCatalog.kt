package com.denizcan.substracktion.domain.catalog

data class Country(
    val code: String,
    val displayName: String,
    val defaultCurrencyCode: String
)

object CountryCatalog {
    val popularCountries = listOf(
        Country(code = "TR", displayName = "Turkiye", defaultCurrencyCode = "TRY"),
        Country(code = "US", displayName = "United States", defaultCurrencyCode = "USD"),
        Country(code = "DE", displayName = "Germany", defaultCurrencyCode = "EUR"),
        Country(code = "GB", displayName = "United Kingdom", defaultCurrencyCode = "GBP"),
        Country(code = "FR", displayName = "France", defaultCurrencyCode = "EUR"),
        Country(code = "IT", displayName = "Italy", defaultCurrencyCode = "EUR"),
        Country(code = "ES", displayName = "Spain", defaultCurrencyCode = "EUR"),
        Country(code = "NL", displayName = "Netherlands", defaultCurrencyCode = "EUR"),
        Country(code = "CA", displayName = "Canada", defaultCurrencyCode = "CAD"),
        Country(code = "AU", displayName = "Australia", defaultCurrencyCode = "AUD")
    )

    val defaultCountry: Country = popularCountries.first()

    fun countryForCode(code: String): Country =
        popularCountries.firstOrNull { it.code.equals(code, ignoreCase = true) } ?: defaultCountry
}
