package com.denizcan.substracktion.ui.screens

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.Spacer
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.denizcan.substracktion.domain.catalog.Country
import com.denizcan.substracktion.domain.catalog.PopularService
import com.denizcan.substracktion.domain.model.CustomSubscriptionDraft
import com.denizcan.substracktion.domain.model.Subscription

private enum class CustomFlowStep {
    Details,
    Plan
}

/**
 * Listede olmayan servis: servis bilgisi + plan tek Nav rotasinda, taslak NavHost disinda tutulur.
 * Boylece rotalar arasi paylasilan state ve draft==null dallari ortadan kalkar.
 */
@Composable
fun CustomSubscriptionFlowScreen(
    country: Country,
    onExitFlow: () -> Unit,
    onFinished: (Subscription) -> Unit,
    modifier: Modifier = Modifier
) {
    var step by remember { mutableStateOf(CustomFlowStep.Details) }
    var draft by remember { mutableStateOf<CustomSubscriptionDraft?>(null) }

    when (step) {
        CustomFlowStep.Details -> {
            CustomSubscriptionEntryScreen(
                initialDraft = draft,
                onBack = onExitFlow,
                onContinue = { d ->
                    draft = d
                    step = CustomFlowStep.Plan
                },
                modifier = modifier.fillMaxSize()
            )
        }

        CustomFlowStep.Plan -> {
            val d = draft
            if (d == null) {
                LaunchedEffect(Unit) {
                    step = CustomFlowStep.Details
                }
                Spacer(modifier = Modifier)
            } else {
                val service = remember(d.serviceName, d.category) {
                    PopularService(
                        id = "custom_flow",
                        name = d.serviceName,
                        category = d.category
                    )
                }
                PlanSuggestionScreen(
                    country = country,
                    service = service,
                    skipGroqLoad = true,
                    onBack = { step = CustomFlowStep.Details },
                    onPlanChosen = { plan ->
                        onFinished(
                            Subscription(
                                id = 0L,
                                countryCode = country.code,
                                serviceName = d.serviceName,
                                planName = plan.name,
                                price = plan.price,
                                currencyCode = plan.currencyCode.ifBlank { country.defaultCurrencyCode },
                                category = d.category,
                                billingPeriod = plan.period
                            )
                        )
                    },
                    modifier = modifier.fillMaxSize()
                )
            }
        }
    }
}
