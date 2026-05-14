package com.example.substracktion.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.substracktion.domain.catalog.Country
import com.example.substracktion.domain.catalog.CountryCatalog
import com.example.substracktion.domain.catalog.PopularService
import com.example.substracktion.domain.catalog.PopularServiceCatalog
import com.example.substracktion.domain.formatter.CurrencyFormatter
import com.example.substracktion.domain.model.BillingPeriod
import com.example.substracktion.domain.model.Subscription
import com.example.substracktion.domain.model.SuggestedPlan
import com.example.substracktion.ui.theme.SubstracktionTheme

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun PlanSuggestionScreen(
    country: Country,
    service: PopularService?,
    onBack: () -> Unit,
    onPlanChosen: (SuggestedPlan) -> Unit,
    modifier: Modifier = Modifier,
    editingSubscription: Subscription? = null,
    skipGroqLoad: Boolean = false,
    viewModel: PlanSuggestionViewModel = viewModel(factory = PlanSuggestionViewModel.Factory)
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val successPlansKey = when (val s = uiState) {
        is PlanSuggestionUiState.Success ->
            if (s.plans.isEmpty()) {
                "EMPTY"
            } else {
                s.plans.joinToString("|") { "${it.name}:${it.price}:${it.currencyCode}:${it.period}" }
            }
        else -> ""
    }
    var selectedPlan by remember(successPlansKey) { mutableStateOf<SuggestedPlan?>(null) }

    val editKey = editingSubscription?.id
    var manualMode by remember(editKey) { mutableStateOf(editingSubscription != null) }
    var manualPlanName by remember(editKey) {
        mutableStateOf(editingSubscription?.planName.orEmpty())
    }
    var manualPriceText by remember(editKey) {
        mutableStateOf(editingSubscription?.let { formatPriceForManualField(it.price) }.orEmpty())
    }
    var manualBillingPeriod by remember(editKey) {
        mutableStateOf(editingSubscription?.billingPeriod ?: BillingPeriod.MONTHLY)
    }
    var aiBillingPeriodOverride by remember { mutableStateOf(BillingPeriod.MONTHLY) }

    LaunchedEffect(uiState) {
        if (uiState is PlanSuggestionUiState.Loading) {
            selectedPlan = null
        }
    }

    LaunchedEffect(selectedPlan) {
        aiBillingPeriodOverride = selectedPlan?.period ?: BillingPeriod.MONTHLY
    }

    LaunchedEffect(successPlansKey, uiState, editKey) {
        val success = uiState as? PlanSuggestionUiState.Success ?: return@LaunchedEffect
        if (editingSubscription == null) {
            if (success.plans.isEmpty()) {
                manualMode = true
            } else {
                manualMode = false
            }
        }
    }

    LaunchedEffect(service?.id, country.code, editKey, skipGroqLoad) {
        if (service != null) {
            if (editingSubscription == null) {
                manualPlanName = ""
                manualPriceText = ""
                manualBillingPeriod = BillingPeriod.MONTHLY
            }
            if (skipGroqLoad) {
                viewModel.showManualOnly()
            } else {
                viewModel.load(service, country)
            }
        }
    }

    val hasAiPlans = when (val s = uiState) {
        is PlanSuggestionUiState.Success -> s.plans.isNotEmpty()
        else -> false
    }
    val showAiList = uiState is PlanSuggestionUiState.Success && hasAiPlans && !manualMode
    val showManualForm =
        uiState is PlanSuggestionUiState.Error ||
            (uiState is PlanSuggestionUiState.Success && (!hasAiPlans || manualMode))

    val parsedManualPrice = remember(manualPriceText) {
        manualPriceText.replace(',', '.').trim().toDoubleOrNull()
    }
    val canSubmitManual =
        manualPlanName.isNotBlank() &&
            parsedManualPrice != null

    val canSubmit = when {
        service == null -> false
        uiState is PlanSuggestionUiState.Loading -> false
        uiState is PlanSuggestionUiState.Idle -> false
        showAiList -> selectedPlan != null
        else -> canSubmitManual
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = when {
                                editingSubscription != null -> "Duzenle: ${service?.name ?: "Plan"}"
                                skipGroqLoad -> "Ozel: ${service?.name ?: "Plan"}"
                                else -> service?.name ?: "Plan"
                            },
                            style = MaterialTheme.typography.titleLarge
                        )
                        if (service != null) {
                            Text(
                                text = "${country.code} · ${country.displayName}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Geri"
                        )
                    }
                }
            )
        },
        bottomBar = {
            if (service != null &&
                (uiState is PlanSuggestionUiState.Success || uiState is PlanSuggestionUiState.Error)
            ) {
                Surface(shadowElevation = 6.dp) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        FilledTonalButton(
                                onClick = {
                                if (showAiList) {
                                    selectedPlan?.let { p ->
                                        onPlanChosen(p.copy(period = aiBillingPeriodOverride))
                                    }
                                } else {
                                    val price = parsedManualPrice ?: return@FilledTonalButton
                                    val cur = country.defaultCurrencyCode
                                    onPlanChosen(
                                        SuggestedPlan(
                                            name = manualPlanName.trim(),
                                            price = price,
                                            currencyCode = cur,
                                            period = manualBillingPeriod,
                                            detailNote = null
                                        )
                                    )
                                }
                            },
                            enabled = canSubmit,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                when {
                                    editingSubscription != null && showAiList ->
                                        "Secili AI planiyla guncelle"
                                    editingSubscription != null ->
                                        "Degisiklikleri kaydet"
                                    showAiList ->
                                        "Secili AI planini listeye ekle"
                                    skipGroqLoad ->
                                        "Ozel uyeligi listeye ekle"
                                    else ->
                                        "Plani listeye ekle"
                                }
                            )
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (service == null) {
                Text(
                    text = "Servis bulunamadi.",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(16.dp)
                )
            } else {
                if (!skipGroqLoad) {
                    TrustWarningBanner(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
                }

                when (val state = uiState) {
                    PlanSuggestionUiState.Idle -> {
                        if (skipGroqLoad) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator()
                            }
                        }
                    }
                    PlanSuggestionUiState.Loading -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }

                    is PlanSuggestionUiState.Success -> {
                        if (state.plans.isNotEmpty()) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 4.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                FilterChip(
                                    selected = !manualMode,
                                    onClick = {
                                        manualMode = false
                                    },
                                    label = { Text("AI onerileri") }
                                )
                                FilterChip(
                                    selected = manualMode,
                                    onClick = {
                                        manualMode = true
                                        selectedPlan = null
                                    },
                                    label = { Text("Manuel gir") }
                                )
                            }
                        }

                        if (showAiList) {
                            state.disclaimer?.let { d ->
                                Text(
                                    text = d,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                                )
                            }

                            Text(
                                text = "Planlar",
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                            )

                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .weight(1f, fill = true),
                                contentPadding = PaddingValues(
                                    start = 16.dp,
                                    end = 16.dp,
                                    top = 0.dp,
                                    bottom = 8.dp
                                ),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(
                                    items = state.plans,
                                    key = { plan -> "${plan.name}_${plan.price}_${plan.currencyCode}_${plan.period}" }
                                ) { plan ->
                                    SuggestedPlanCard(
                                        plan = plan,
                                        selected = plan == selectedPlan,
                                        onClick = {
                                            selectedPlan = plan
                                            manualMode = false
                                        }
                                    )
                                }
                            }

                            if (selectedPlan != null) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = "Odeme donemi (AI yanlis tahmin ettiyse degistirin)",
                                        style = MaterialTheme.typography.titleSmall,
                                        modifier = Modifier.padding(bottom = 8.dp)
                                    )
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .horizontalScroll(rememberScrollState()),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        BillingPeriod.entries.forEach { period ->
                                            FilterChip(
                                                selected = aiBillingPeriodOverride == period,
                                                onClick = { aiBillingPeriodOverride = period },
                                                label = { Text(period.label) }
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        if (showManualForm) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .weight(1f, fill = true)
                                    .verticalScroll(rememberScrollState())
                                    .padding(horizontal = 16.dp, vertical = 8.dp)
                            ) {
                                if (state.plans.isEmpty()) {
                                    Text(
                                        text = if (skipGroqLoad) {
                                            "Ozel servis: plan adini, odeme donemini ve tutari asagidan girin."
                                        } else {
                                            "AI plan listesi bos veya okunamadi; asagidan manuel girebilirsiniz."
                                        },
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(bottom = 8.dp)
                                    )
                                }
                                Text(
                                    text = "Manuel plan",
                                    style = MaterialTheme.typography.titleMedium,
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )
                                Text(
                                    text = "Guvenilir olmayan veya eksik AI bilgisinde plan adini ve fiyati kendiniz yazin. Virgul veya nokta ile ondalik kullanabilirsiniz.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(bottom = 12.dp)
                                )
                                ManualPlanFields(
                                    planName = manualPlanName,
                                    onPlanNameChange = { manualPlanName = it },
                                    priceText = manualPriceText,
                                    onPriceTextChange = { manualPriceText = it },
                                    currencyFromCountry = country.defaultCurrencyCode,
                                    billingPeriod = manualBillingPeriod,
                                    onBillingPeriodChange = { manualBillingPeriod = it }
                                )
                            }
                        }
                    }

                    is PlanSuggestionUiState.Error -> {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .weight(1f, fill = true)
                                .verticalScroll(rememberScrollState())
                                .padding(16.dp)
                        ) {
                            Text(
                                text = state.message,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.error
                            )
                            TextButton(
                                onClick = { viewModel.retry(service, country) },
                                modifier = Modifier.padding(top = 4.dp, bottom = 8.dp)
                            ) {
                                Text("Tekrar dene")
                            }
                            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                            Text(
                                text = "API calismadiysa plani asagidan manuel girebilirsiniz.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(bottom = 12.dp)
                            )
                            ManualPlanFields(
                                planName = manualPlanName,
                                onPlanNameChange = { manualPlanName = it },
                                priceText = manualPriceText,
                                onPriceTextChange = { manualPriceText = it },
                                currencyFromCountry = country.defaultCurrencyCode,
                                billingPeriod = manualBillingPeriod,
                                onBillingPeriodChange = { manualBillingPeriod = it }
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun formatPriceForManualField(price: Double): String {
    val asLong = price.toLong()
    return if (price == asLong.toDouble()) asLong.toString() else price.toString()
}

@Composable
private fun TrustWarningBanner(modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.45f))
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = Icons.Outlined.Info,
                contentDescription = null,
                modifier = Modifier
                    .padding(end = 10.dp)
                    .size(22.dp),
                tint = MaterialTheme.colorScheme.error
            )
            Column {
                Text(
                    text = "Guvenilirlik uyarisi",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Plan adlari ve fiyatlar yapay zeka tahminidir; guncel veya dogru olmayabilir. Onemliyse resmi site veya faturanizdan kontrol edin. Isterseniz \"Manuel gir\" ile kendi bilginizi kullanin.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.92f)
                )
            }
        }
    }
}

@Composable
private fun ManualPlanFields(
    planName: String,
    onPlanNameChange: (String) -> Unit,
    priceText: String,
    onPriceTextChange: (String) -> Unit,
    currencyFromCountry: String,
    billingPeriod: BillingPeriod,
    onBillingPeriodChange: (BillingPeriod) -> Unit
) {
    OutlinedTextField(
        value = planName,
        onValueChange = onPlanNameChange,
        modifier = Modifier.fillMaxWidth(),
        label = { Text("Plan adi") },
        singleLine = true,
        supportingText = {
            Text("Ornek: Premium Aile")
        }
    )
    Spacer(modifier = Modifier.height(16.dp))
    Text(
        text = "Odeme donemi",
        style = MaterialTheme.typography.titleSmall,
        modifier = Modifier.padding(bottom = 8.dp)
    )
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        BillingPeriod.entries.forEach { period ->
            FilterChip(
                selected = billingPeriod == period,
                onClick = { onBillingPeriodChange(period) },
                label = { Text(period.label) }
            )
        }
    }
    Spacer(modifier = Modifier.height(12.dp))
    OutlinedTextField(
        value = priceText,
        onValueChange = onPriceTextChange,
        modifier = Modifier.fillMaxWidth(),
        label = { Text("Tutar") },
        singleLine = true,
        supportingText = {
            Text(
                "Secilen doneme gore (${billingPeriod.label}): ornek 149,99"
            )
        },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
    )
    Text(
        text = "Para birimi: $currencyFromCountry (${CurrencyFormatter.symbolOrCode(currencyFromCountry)}) — ana ekranda sectiginiz ulkeye gore",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(top = 8.dp)
    )
}

@Composable
private fun SuggestedPlanCard(
    plan: SuggestedPlan,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val border = if (selected) {
        BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
    } else {
        BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    }
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        border = border,
        colors = CardDefaults.cardColors(
            containerColor = if (selected) {
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (selected) 6.dp else 2.dp
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = plan.name,
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = plan.period.label,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(top = 4.dp)
            )
            plan.detailNote?.let { note ->
                Text(
                    text = note,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = if (plan.price == 0.0) {
                    "${plan.period.label} tutar: belirtilmedi / tahmin yok"
                } else {
                    "${plan.period.label} tutar: ${CurrencyFormatter.formatAmount(plan.price, plan.currencyCode)}"
                },
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PlanSuggestionScreenPreview() {
    SubstracktionTheme {
        PlanSuggestionScreen(
            country = CountryCatalog.defaultCountry,
            service = PopularServiceCatalog.getById("netflix"),
            onBack = {},
            onPlanChosen = {}
        )
    }
}
