package com.denizcan.substracktion.ui.navigation

import android.app.Application
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.denizcan.substracktion.domain.catalog.CountryCatalog
import com.denizcan.substracktion.domain.catalog.PopularServiceCatalog
import com.denizcan.substracktion.domain.model.Subscription
import com.denizcan.substracktion.ui.screens.AddSubscriptionScreen
import com.denizcan.substracktion.ui.screens.CustomSubscriptionFlowScreen
import com.denizcan.substracktion.ui.screens.PlanSuggestionScreen
import com.denizcan.substracktion.ui.screens.SubscriptionListScreen
import com.denizcan.substracktion.ui.viewmodel.SubstracktionViewModel

object SubstracktionRoutes {
    const val SubscriptionList = "subscription_list"
    const val AddSubscription = "add_subscription"
    const val PlanSuggestion = "plan_suggestion/{serviceId}"
    const val EditPlan = "edit_plan/{subscriptionId}"
    /** Ozel uyelik: tek rota, ic state (NavHost ile paylasilan taslak yok). */
    const val CustomSubscription = "custom_subscription"

    fun planSuggestion(serviceId: String): String = "plan_suggestion/$serviceId"

    fun editPlan(subscriptionId: Long): String = "edit_plan/$subscriptionId"
}

/**
 * Baslangic (liste) hedefi yigitta kalir; sadece ustteki ekranlar kapatilir.
 */
private fun NavController.popUntilSubscriptionList() {
    val startId = graph.startDestinationId
    repeat(24) {
        val entry = currentBackStackEntry ?: return
        if (entry.destination.id == startId) return
        if (!popBackStack()) return
    }
}

@Composable
fun SubstracktionNavHost() {
    val navController = rememberNavController()
    var selectedCountry by remember { mutableStateOf(CountryCatalog.defaultCountry) }
    val application = LocalContext.current.applicationContext as Application
    val appViewModel: SubstracktionViewModel = viewModel(
        factory = ViewModelProvider.AndroidViewModelFactory.getInstance(application)
    )
    val subscriptions by appViewModel.subscriptions.collectAsStateWithLifecycle()

    NavHost(
        navController = navController,
        startDestination = SubstracktionRoutes.SubscriptionList,
        modifier = Modifier.fillMaxSize()
    ) {
        composable(SubstracktionRoutes.SubscriptionList) {
            SubscriptionListScreen(
                subscriptions = subscriptions,
                selectedCountry = selectedCountry,
                onSelectedCountryChange = { selectedCountry = it },
                onAddClick = { navController.navigate(SubstracktionRoutes.AddSubscription) },
                onSubscriptionClick = { sub ->
                    navController.navigate(SubstracktionRoutes.editPlan(sub.id))
                },
                onSubscriptionDelete = { sub ->
                    appViewModel.deleteSubscription(sub.id)
                }
            )
        }
        composable(SubstracktionRoutes.AddSubscription) {
            AddSubscriptionScreen(
                onBack = { navController.popBackStack() },
                onServiceSelected = { service ->
                    navController.navigate(SubstracktionRoutes.planSuggestion(service.id))
                },
                onAddCustomSubscription = {
                    navController.navigate(SubstracktionRoutes.CustomSubscription)
                }
            )
        }
        composable(SubstracktionRoutes.CustomSubscription) {
            CustomSubscriptionFlowScreen(
                country = selectedCountry,
                onExitFlow = { navController.popBackStack() },
                onFinished = { newSubscription ->
                    appViewModel.addSubscription(newSubscription) {
                        navController.popUntilSubscriptionList()
                    }
                },
                modifier = Modifier.fillMaxSize()
            )
        }
        composable(
            route = SubstracktionRoutes.PlanSuggestion,
            arguments = listOf(
                navArgument("serviceId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val serviceId = backStackEntry.arguments?.getString("serviceId").orEmpty()
            val service = remember(serviceId) { PopularServiceCatalog.getById(serviceId) }
            PlanSuggestionScreen(
                country = selectedCountry,
                service = service,
                onBack = { navController.popBackStack() },
                onPlanChosen = { plan ->
                    service?.let { svc ->
                        val newSubscription = Subscription(
                            id = 0L,
                            countryCode = selectedCountry.code,
                            serviceName = svc.name,
                            planName = plan.name,
                            price = plan.price,
                            currencyCode = plan.currencyCode.ifBlank { selectedCountry.defaultCurrencyCode },
                            category = svc.category,
                            billingPeriod = plan.period
                        )
                        appViewModel.addSubscription(newSubscription) {
                            navController.popUntilSubscriptionList()
                        }
                    }
                }
            )
        }
        composable(
            route = SubstracktionRoutes.EditPlan,
            arguments = listOf(
                navArgument("subscriptionId") { type = NavType.LongType }
            )
        ) { backStackEntry ->
            val subscriptionId = backStackEntry.arguments?.getLong("subscriptionId") ?: -1L
            LaunchedEffect(subscriptionId) {
                if (subscriptionId <= 0L) {
                    navController.popBackStack()
                }
            }
            if (subscriptionId <= 0L) {
                return@composable
            }

            val subscription = subscriptions.find { it.id == subscriptionId }
            LaunchedEffect(subscriptionId, subscriptions) {
                if (subscriptions.isNotEmpty() && subscription == null) {
                    navController.popBackStack()
                }
            }

            if (subscription == null) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                val country = remember(subscription) {
                    CountryCatalog.countryForCode(subscription.countryCode)
                }
                val service = remember(subscription) {
                    PopularServiceCatalog.serviceForSubscription(subscription)
                }
                PlanSuggestionScreen(
                    country = country,
                    service = service,
                    editingSubscription = subscription,
                    onBack = { navController.popBackStack() },
                    onPlanChosen = { plan ->
                        val updated = subscription.copy(
                            planName = plan.name,
                            price = plan.price,
                            currencyCode = plan.currencyCode.ifBlank { country.defaultCurrencyCode },
                            billingPeriod = plan.period
                        )
                        appViewModel.updateSubscription(updated) {
                            navController.popUntilSubscriptionList()
                        }
                    }
                )
            }
        }
    }
}
