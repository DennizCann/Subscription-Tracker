package com.example.substracktion.domain.catalog

import com.example.substracktion.domain.model.Subscription
import com.example.substracktion.domain.model.SubscriptionCategory

data class PopularService(
    val id: String,
    val name: String,
    val category: SubscriptionCategory
)

object PopularServiceCatalog {
    fun getById(id: String): PopularService? = all.find { it.id == id }

    /** Katalogda yoksa Groq icin sentetik servis (kayit servis adi korunur). */
    fun serviceForSubscription(subscription: Subscription): PopularService {
        val match = all.firstOrNull {
            it.name.trim().equals(subscription.serviceName.trim(), ignoreCase = true)
        }
        return match ?: PopularService(
            id = "custom_${subscription.id}",
            name = subscription.serviceName.trim(),
            category = subscription.category
        )
    }

    val all: List<PopularService> = listOf(
        PopularService("netflix", "Netflix", SubscriptionCategory.STREAMING),
        PopularService("spotify", "Spotify", SubscriptionCategory.MUSIC),
        PopularService("youtube_premium", "YouTube Premium", SubscriptionCategory.STREAMING),
        PopularService("apple_music", "Apple Music", SubscriptionCategory.MUSIC),
        PopularService("disney_plus", "Disney+", SubscriptionCategory.STREAMING),
        PopularService("amazon_prime", "Amazon Prime Video", SubscriptionCategory.STREAMING),
        PopularService("hbo_max", "Max (HBO Max)", SubscriptionCategory.STREAMING),
        PopularService("apple_tv", "Apple TV+", SubscriptionCategory.STREAMING),
        PopularService("paramount", "Paramount+", SubscriptionCategory.STREAMING),
        PopularService("deezer", "Deezer", SubscriptionCategory.MUSIC),
        PopularService("tidal", "Tidal", SubscriptionCategory.MUSIC),
        PopularService("audible", "Audible", SubscriptionCategory.EDUCATION),
        PopularService("duolingo", "Duolingo Plus", SubscriptionCategory.EDUCATION),
        PopularService("skillshare", "Skillshare", SubscriptionCategory.EDUCATION),
        PopularService("linkedin_learning", "LinkedIn Learning", SubscriptionCategory.EDUCATION),
        PopularService("notion", "Notion Plus", SubscriptionCategory.PRODUCTIVITY),
        PopularService("dropbox", "Dropbox", SubscriptionCategory.CLOUD_STORAGE),
        PopularService("google_one", "Google One", SubscriptionCategory.CLOUD_STORAGE),
        PopularService("icloud", "iCloud+", SubscriptionCategory.CLOUD_STORAGE),
        PopularService("onedrive", "Microsoft OneDrive", SubscriptionCategory.CLOUD_STORAGE),
        PopularService("xbox_game_pass", "Xbox Game Pass", SubscriptionCategory.GAMING),
        PopularService("playstation_plus", "PlayStation Plus", SubscriptionCategory.GAMING),
        PopularService("nyt", "The New York Times", SubscriptionCategory.NEWS),
        PopularService("medium", "Medium", SubscriptionCategory.NEWS),
        PopularService("strava", "Strava", SubscriptionCategory.FITNESS),
        PopularService("peloton", "Peloton", SubscriptionCategory.FITNESS),
        PopularService("figma", "Figma", SubscriptionCategory.PRODUCTIVITY),
        PopularService("chatgpt", "ChatGPT Plus", SubscriptionCategory.PRODUCTIVITY),
        PopularService("grammarly", "Grammarly", SubscriptionCategory.PRODUCTIVITY)
    )
}
