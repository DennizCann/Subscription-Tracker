package com.denizcan.substracktion.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.denizcan.substracktion.BuildConfig
import com.denizcan.substracktion.data.repository.GroqPlanRepository
import com.denizcan.substracktion.domain.catalog.Country
import com.denizcan.substracktion.domain.catalog.PopularService
import com.denizcan.substracktion.domain.model.SuggestedPlan
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface PlanSuggestionUiState {
    data object Idle : PlanSuggestionUiState
    data object Loading : PlanSuggestionUiState
    data class Success(
        val plans: List<SuggestedPlan>,
        val disclaimer: String?
    ) : PlanSuggestionUiState

    data class Error(val message: String) : PlanSuggestionUiState
}

class PlanSuggestionViewModel(
    private val repository: GroqPlanRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<PlanSuggestionUiState>(PlanSuggestionUiState.Idle)
    val uiState: StateFlow<PlanSuggestionUiState> = _uiState.asStateFlow()

    fun load(service: PopularService, country: Country) {
        viewModelScope.launch {
            _uiState.value = PlanSuggestionUiState.Loading
            val result = repository.fetchPlanSuggestions(
                serviceName = service.name,
                country = country
            )
            _uiState.value = result.fold(
                onSuccess = { r ->
                    PlanSuggestionUiState.Success(
                        plans = r.plans,
                        disclaimer = r.disclaimer
                    )
                },
                onFailure = { PlanSuggestionUiState.Error(it.message ?: "Bilinmeyen hata") }
            )
        }
    }

    fun retry(service: PopularService, country: Country) {
        load(service, country)
    }

    /** Groq cagrilmadan sadece manuel plan formu (ozel servis akisi). */
    fun showManualOnly() {
        _uiState.value = PlanSuggestionUiState.Success(
            plans = emptyList(),
            disclaimer = null
        )
    }

    companion object {
        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val repository = GroqPlanRepository(apiKey = BuildConfig.GROQ_API_KEY)
                return PlanSuggestionViewModel(repository) as T
            }
        }
    }
}
