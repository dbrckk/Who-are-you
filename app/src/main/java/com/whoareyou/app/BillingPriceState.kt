package com.whoareyou.app

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

enum class BillingPriceLoadState {
    LOADING,
    AVAILABLE,
    UNAVAILABLE
}

object BillingPriceState {
    var formattedPrice by mutableStateOf<String?>(null)
        private set

    var loadState by mutableStateOf(BillingPriceLoadState.LOADING)
        private set

    fun markLoading() {
        formattedPrice = null
        loadState = BillingPriceLoadState.LOADING
    }

    fun update(price: String?) {
        formattedPrice = price?.takeIf { it.isNotBlank() }
        loadState = if (formattedPrice == null) {
            BillingPriceLoadState.UNAVAILABLE
        } else {
            BillingPriceLoadState.AVAILABLE
        }
    }

    val isAvailable: Boolean
        get() = loadState == BillingPriceLoadState.AVAILABLE && formattedPrice != null
}
