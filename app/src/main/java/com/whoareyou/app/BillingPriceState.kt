package com.whoareyou.app

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

object BillingPriceState {
    var formattedPrice by mutableStateOf<String?>(null)
        private set

    fun update(price: String?) {
        formattedPrice = price
    }

    val isAvailable: Boolean
        get() = !formattedPrice.isNullOrBlank()
}
