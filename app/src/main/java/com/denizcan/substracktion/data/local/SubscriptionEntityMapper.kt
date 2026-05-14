package com.denizcan.substracktion.data.local

import com.denizcan.substracktion.domain.model.BillingPeriod
import com.denizcan.substracktion.domain.model.Subscription
import com.denizcan.substracktion.domain.model.SubscriptionCategory

fun SubscriptionEntity.toDomain(): Subscription {
    val cat = runCatching { SubscriptionCategory.valueOf(category) }
        .getOrDefault(SubscriptionCategory.OTHER)
    val period = runCatching { BillingPeriod.valueOf(billingPeriod) }
        .getOrDefault(BillingPeriod.MONTHLY)
    return Subscription(
        id = id,
        countryCode = countryCode,
        serviceName = serviceName,
        planName = planName,
        price = price,
        currencyCode = currencyCode,
        category = cat,
        billingPeriod = period
    )
}

fun Subscription.toNewEntity(): SubscriptionEntity = SubscriptionEntity(
    id = 0,
    countryCode = countryCode,
    serviceName = serviceName,
    planName = planName,
    price = price,
    currencyCode = currencyCode,
    category = category.name,
    billingPeriod = billingPeriod.name
)

fun Subscription.toEntity(): SubscriptionEntity = SubscriptionEntity(
    id = id,
    countryCode = countryCode,
    serviceName = serviceName,
    planName = planName,
    price = price,
    currencyCode = currencyCode,
    category = category.name,
    billingPeriod = billingPeriod.name
)
