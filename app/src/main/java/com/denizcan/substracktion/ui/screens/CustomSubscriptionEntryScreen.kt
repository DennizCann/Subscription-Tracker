package com.denizcan.substracktion.ui.screens

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.denizcan.substracktion.domain.model.CustomSubscriptionDraft
import com.denizcan.substracktion.domain.model.SubscriptionCategory
import com.denizcan.substracktion.ui.theme.SubstracktionTheme

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun CustomSubscriptionEntryScreen(
    initialDraft: CustomSubscriptionDraft?,
    onBack: () -> Unit,
    onContinue: (CustomSubscriptionDraft) -> Unit,
    modifier: Modifier = Modifier
) {
    var serviceName by remember { mutableStateOf("") }
    var category by remember { mutableStateOf(SubscriptionCategory.OTHER) }

    LaunchedEffect(initialDraft) {
        val d = initialDraft ?: return@LaunchedEffect
        serviceName = d.serviceName
        category = d.category
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("Listede olmayan servis") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Geri"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = "Servis adini ve kategoriyi secin; sonraki adimda plan bilgisini manuel gireceksiniz.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp, bottom = 16.dp)
            )
            OutlinedTextField(
                value = serviceName,
                onValueChange = { serviceName = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Servis / uygulama adi") },
                singleLine = true,
                supportingText = { Text("Ornek: Dijital gazete, yerel spor salonu") }
            )
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = "Kategori",
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SubscriptionCategory.entries.forEach { cat ->
                    FilterChip(
                        selected = category == cat,
                        onClick = { category = cat },
                        label = { Text(cat.label) }
                    )
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
            FilledTonalButton(
                onClick = {
                    onContinue(
                        CustomSubscriptionDraft(
                            serviceName = serviceName.trim(),
                            category = category
                        )
                    )
                },
                enabled = serviceName.isNotBlank(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Plani girmeye devam et")
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun CustomSubscriptionEntryScreenPreview() {
    SubstracktionTheme {
        CustomSubscriptionEntryScreen(
            initialDraft = null,
            onBack = {},
            onContinue = {}
        )
    }
}
