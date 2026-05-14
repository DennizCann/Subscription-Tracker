package com.denizcan.substracktion.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.denizcan.substracktion.data.local.SubstracktionDatabase
import com.denizcan.substracktion.data.local.toDomain
import com.denizcan.substracktion.data.local.toEntity
import com.denizcan.substracktion.data.local.toNewEntity
import com.denizcan.substracktion.domain.model.Subscription
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SubstracktionViewModel(application: Application) : AndroidViewModel(application) {

    private val dao = SubstracktionDatabase.getInstance(application).subscriptionDao()

    val subscriptions = dao.observeAll()
        .map { list -> list.map { it.toDomain() } }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = emptyList()
        )

    fun addSubscription(subscription: Subscription, onInserted: (() -> Unit)? = null) {
        viewModelScope.launch {
            dao.insert(subscription.toNewEntity())
            onInserted?.let { done ->
                withContext(Dispatchers.Main.immediate) { done() }
            }
        }
    }

    fun updateSubscription(subscription: Subscription, onUpdated: (() -> Unit)? = null) {
        viewModelScope.launch {
            dao.update(subscription.toEntity())
            onUpdated?.let { done ->
                withContext(Dispatchers.Main.immediate) { done() }
            }
        }
    }

    fun deleteSubscription(id: Long) {
        viewModelScope.launch {
            dao.deleteById(id)
        }
    }
}
