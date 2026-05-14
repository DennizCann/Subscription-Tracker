package com.denizcan.substracktion.data.repository

import com.denizcan.substracktion.data.remote.groq.GroqChatMessage
import com.denizcan.substracktion.data.remote.groq.GroqChatRequest
import com.denizcan.substracktion.data.remote.groq.GroqClient
import com.denizcan.substracktion.data.remote.groq.GroqPlansRootDto
import com.denizcan.substracktion.data.remote.groq.GroqResponseFormat
import com.denizcan.substracktion.domain.catalog.Country
import com.denizcan.substracktion.domain.model.BillingPeriod
import com.denizcan.substracktion.domain.model.SuggestedPlan
import com.denizcan.substracktion.domain.model.SuggestedPlansResult
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import retrofit2.HttpException

class GroqPlanRepository(
    private val apiKey: String
) {

    private val api by lazy { GroqClient.createGroqApi(apiKey) }
    private val gson = Gson()

    suspend fun fetchPlanSuggestions(serviceName: String, country: Country): Result<SuggestedPlansResult> {
        if (apiKey.isBlank()) {
            return Result.failure(
                IllegalStateException("Groq API anahtari yok. local.properties dosyasina groq.api.key=... ekleyin.")
            )
        }

        return runCatching {
            val userPrompt = buildUserPrompt(serviceName, country)
            val request = GroqChatRequest(
                model = MODEL,
                messages = listOf(
                    GroqChatMessage(
                        role = "system",
                        content = SYSTEM_PROMPT
                    ),
                    GroqChatMessage(
                        role = "user",
                        content = userPrompt
                    )
                ),
                temperature = 0.2,
                maxTokens = 1200,
                responseFormat = GroqResponseFormat(type = "json_object")
            )
            val response = api.createChatCompletion(request)
            val raw = response.choices
                ?.firstOrNull()
                ?.message
                ?.content
                ?.trim()
                .orEmpty()

            if (raw.isEmpty()) {
                error("Groq bos yanit dondurdu.")
            }

            val jsonPayload = extractJsonPayload(raw)
            val dto = try {
                gson.fromJson(jsonPayload, GroqPlansRootDto::class.java)
            } catch (e: JsonSyntaxException) {
                error("JSON cozumlenemedi: ${e.message}")
            }

            val plans = dto.plans.orEmpty().mapNotNull { item ->
                val name = item.name?.trim().orEmpty()
                if (name.isEmpty()) return@mapNotNull null
                val price = item.price ?: 0.0
                val currency = item.currency?.trim()?.ifEmpty { null }
                    ?: country.defaultCurrencyCode
                val periodSource = item.billingPeriod?.trim()?.takeIf { it.isNotEmpty() }
                    ?: item.billing?.trim()?.takeIf { it.isNotEmpty() }
                val period = BillingPeriod.fromApiOrText(periodSource)
                val detailNote = item.notes?.trim()?.takeIf { it.isNotEmpty() }
                SuggestedPlan(
                    name = name,
                    price = price,
                    currencyCode = currency,
                    period = period,
                    detailNote = detailNote
                )
            }

            if (plans.isEmpty()) {
                error("Plan icermeyen yanit.")
            }

            SuggestedPlansResult(
                plans = plans,
                disclaimer = dto.disclaimer?.trim()?.takeIf { it.isNotEmpty() }
            )
        }.mapFailure { e ->
            val message = when (e) {
                is HttpException -> {
                    val body = e.response()?.errorBody()?.string().orEmpty()
                    "HTTP ${e.code()}: ${e.message()} $body".trim()
                }
                else -> e.message ?: e::class.java.simpleName
            }
            IllegalStateException(message, e)
        }
    }

    private fun extractJsonPayload(raw: String): String {
        var s = raw.trim()
        if (s.startsWith("```")) {
            s = s.removePrefix("```json").removePrefix("```JSON").removePrefix("```")
            if (s.endsWith("```")) {
                s = s.removeSuffix("```").trim()
            }
        }
        return s.trim()
    }

    private fun buildUserPrompt(serviceName: String, country: Country): String = """
        "${serviceName}" servisi icin ${country.code} (${country.displayName}) bolgesindeki guncel tuketici abonelik planlarini ve fiyatlari JSON olarak ver.
        Para birimi olarak mumkunse ${country.defaultCurrencyCode} kullan.
        ONEMLI: Listeyi sade ve tek satirlik tut. Her plan icin TEK bir price ve TEK bir billing_period olsun.
        Ayni plan adini haftalik/aylik/yillik diye uc ayri kayit olarak VERME; haftalik veya yillik esdeger fiyat hesaplayip ek satir da ekleme.
        Tuketici aboneliklerinde fiyat genelde aylik verildigi icin mumkunse billing_period: "monthly" kullan ve price alanina yalnizca o aylik tutari yaz.
        Baska bir donem (or. yillik paket) gercekten ayri bir urun/plan ise o zaman ayri bir plan nesnesi olarak tek satirda ver; yine tek price ve o planin gercek donemi.
        Yaniti SADECE asagidaki JSON semasina uygun tek bir JSON nesnesi olarak dondur; baska metin, aciklama veya markdown kullanma.
        {
          "disclaimer": "Kisa bir uyari (tahmini bilgi olabilir)",
          "plans": [
            {
              "name": "Plan adi",
              "price": 99.99,
              "currency": "${country.defaultCurrencyCode}",
              "billing_period": "monthly",
              "notes": "Istege bagli ek aciklama"
            }
          ]
        }
        billing_period alani zorunlu; degerler yalnizca sunlardan biri olsun: weekly, monthly, yearly (kucuk harf, Ingilizce).
        price alaninda sayi kullan (bilinmiyorsa 0). plans dizisi en az 1 eleman icermeli; gereksiz tekrar veya donusum satirlari ekleme.
    """.trimIndent()

    companion object {
        private const val MODEL = "llama-3.1-8b-instant"

        private val SYSTEM_PROMPT = """
            Sen bir abonelik fiyat yardimcisisin. Yanitlarin yalnizca gecerli JSON olmalidir.
            json_object modunda calis; metin veya markdown ekleme.
            Ayni plan icin birden fazla donem fiyati uretme; listeyi kisa ve oz tut.
        """.trimIndent()
    }
}

private inline fun <T> Result<T>.mapFailure(transform: (Throwable) -> Throwable): Result<T> {
    return fold(
        onSuccess = { Result.success(it) },
        onFailure = { Result.failure(transform(it)) }
    )
}
