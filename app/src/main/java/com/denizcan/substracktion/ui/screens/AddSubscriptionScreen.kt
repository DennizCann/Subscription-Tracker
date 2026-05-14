package com.denizcan.substracktion.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.PlaylistAdd
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.denizcan.substracktion.domain.catalog.PopularService
import com.denizcan.substracktion.domain.catalog.PopularServiceCatalog
import com.denizcan.substracktion.domain.model.SubscriptionCategory
import com.denizcan.substracktion.ui.theme.SubstracktionTheme

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun AddSubscriptionScreen(
    onBack: () -> Unit,
    onServiceSelected: (PopularService) -> Unit,
    onAddCustomSubscription: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedCategory by remember { mutableStateOf<SubscriptionCategory?>(null) }
    var sortAscending by remember { mutableStateOf(true) }

    val visibleServices = remember(selectedCategory, sortAscending) {
        PopularServiceCatalog.all
            .filter { service ->
                selectedCategory == null || service.category == selectedCategory
            }
            .let { list ->
                if (sortAscending) {
                    list.sortedBy { it.name.lowercase() }
                } else {
                    list.sortedByDescending { it.name.lowercase() }
                }
            }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(text = "Uyelik ekle") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Geri"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onAddCustomSubscription) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.PlaylistAdd,
                            contentDescription = "Listede olmayan servis ekle"
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
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                FilterChip(
                    selected = selectedCategory == null,
                    onClick = { selectedCategory = null },
                    label = { Text("Tumu") }
                )
                SubscriptionCategory.entries.forEach { category ->
                    FilterChip(
                        selected = selectedCategory == category,
                        onClick = {
                            selectedCategory = if (selectedCategory == category) null else category
                        },
                        label = { Text(category.label) }
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Isim sirasi",
                    style = MaterialTheme.typography.titleSmall
                )
                TextButton(onClick = { sortAscending = !sortAscending }) {
                    Text(if (sortAscending) "A > Z" else "Z > A")
                }
            }

            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(
                    items = visibleServices,
                    key = { it.id }
                ) { service ->
                    PopularServiceRow(
                        service = service,
                        onClick = { onServiceSelected(service) }
                    )
                    HorizontalDivider()
                }
            }
        }
    }
}

@Composable
private fun PopularServiceRow(
    service: PopularService,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Text(
            text = service.name,
            style = MaterialTheme.typography.titleMedium
        )
        Text(
            text = service.category.label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun AddSubscriptionScreenPreview() {
    SubstracktionTheme {
        AddSubscriptionScreen(
            onBack = {},
            onServiceSelected = {},
            onAddCustomSubscription = {}
        )
    }
}
