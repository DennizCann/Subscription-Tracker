package com.example.substracktion.ui.screens

import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.substracktion.domain.catalog.Country
import com.example.substracktion.domain.catalog.CountryCatalog
import com.example.substracktion.domain.formatter.CurrencyFormatter
import com.example.substracktion.domain.model.Subscription
import com.example.substracktion.ui.theme.SubstracktionTheme

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun SubscriptionListScreen(
    subscriptions: List<Subscription>,
    selectedCountry: Country,
    onSelectedCountryChange: (Country) -> Unit,
    onAddClick: () -> Unit,
    onSubscriptionClick: (Subscription) -> Unit,
    onSubscriptionDelete: (Subscription) -> Unit,
    modifier: Modifier = Modifier
) {
    val isInPreview = LocalInspectionMode.current
    var isCountryMenuExpanded by remember { mutableStateOf(false) }
    var pendingDelete by remember { mutableStateOf<Subscription?>(null) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(text = "Uyeliklerim") },
                actions = {
                    Box {
                        TextButton(onClick = { isCountryMenuExpanded = true }) {
                            Text(
                                text = "${selectedCountry.code} (${CurrencyFormatter.symbolOrCode(selectedCountry.defaultCurrencyCode)})"
                            )
                        }

                        if (!isInPreview) {
                            DropdownMenu(
                                expanded = isCountryMenuExpanded,
                                onDismissRequest = { isCountryMenuExpanded = false }
                            ) {
                                CountryCatalog.popularCountries.forEach { country ->
                                    DropdownMenuItem(
                                        text = { Text(text = "${country.code} - ${country.displayName}") },
                                        onClick = {
                                            onSelectedCountryChange(country)
                                            isCountryMenuExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddClick) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Uyelik ekle"
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (subscriptions.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Henuz uyelik eklemedin.\nSag alttaki + butonuna basarak basla.",
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "Para birimi: ${selectedCountry.defaultCurrencyCode} (${CurrencyFormatter.symbolOrCode(selectedCountry.defaultCurrencyCode)})",
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp, 16.dp, 16.dp, 88.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(
                        items = subscriptions,
                        key = { it.id }
                    ) { subscription ->
                        SubscriptionRowCard(
                            subscription = subscription,
                            onOpenClick = { onSubscriptionClick(subscription) },
                            onDeleteClick = { pendingDelete = subscription }
                        )
                    }
                }
            }
            pendingDelete?.let { target ->
                AlertDialog(
                    onDismissRequest = { pendingDelete = null },
                    title = { Text("Uyeligi sil?") },
                    text = {
                        Text(
                            "${target.serviceName} kaydini kalici olarak sileceksiniz."
                        )
                    },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                onSubscriptionDelete(target)
                                pendingDelete = null
                            }
                        ) {
                            Text(
                                "Sil",
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { pendingDelete = null }) {
                            Text("Vazgec")
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun SubscriptionRowCard(
    subscription: Subscription,
    onOpenClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .combinedClickable(
                    onClick = onOpenClick,
                    onLongClick = onDeleteClick
                )
                .padding(16.dp)
        ) {
            Text(
                text = subscription.serviceName,
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = subscription.planName,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp)
            )
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 10.dp),
                color = MaterialTheme.colorScheme.outlineVariant
            )
            Text(
                text = CurrencyFormatter.formatAmount(subscription.price, subscription.currencyCode),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Odeme: ${subscription.billingPeriod.label}",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.padding(top = 6.dp)
            )
            Text(
                text = "${subscription.countryCode} · ${subscription.category.label}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 6.dp)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SubscriptionListScreenPreview() {
    SubstracktionTheme {
        SubscriptionListScreen(
            subscriptions = emptyList(),
            selectedCountry = CountryCatalog.defaultCountry,
            onSelectedCountryChange = {},
            onAddClick = {},
            onSubscriptionClick = {},
            onSubscriptionDelete = {}
        )
    }
}
