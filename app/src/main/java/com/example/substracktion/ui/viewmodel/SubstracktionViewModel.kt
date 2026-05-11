package com.example.substracktion.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.substracktion.data.local.SubstracktionDatabase
import com.example.substracktion.data.local.toDomain
import com.example.substracktion.data.local.toEntity
import com.example.substracktion.data.local.toNewEntity
import com.example.substracktion.domain.model.Subscription
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SubstracktionViewModel(application: Application) : AndroidViewModel(application) {

    private val dao = SubstracktionDatabase.getInstance(application).subscriptionDao()

    val subscriptions = dao.observeAll()
        .map { list -> list.map { it.toDomain() } }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    fun addSubscription(subscription: Subscription) {
        viewModelScope.launch {
            dao.insert(subscription.toNewEntity())
        }
    }

    fun updateSubscription(subscription: Subscription) {
        viewModelScope.launch {
            dao.update(subscription.toEntity())
        }
    }

    fun deleteSubscription(id: Long) {
        viewModelScope.launch {
            dao.deleteById(id)
        }
    }
}
