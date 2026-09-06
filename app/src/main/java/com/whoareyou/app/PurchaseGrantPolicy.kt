package com.whoareyou.app

enum class PurchaseGrantSource {
    LIVE_PURCHASE,
    RESTORE
}

object PurchaseGrantPolicy {
    fun shouldLogPurchaseSuccess(source: PurchaseGrantSource): Boolean =
        source == PurchaseGrantSource.LIVE_PURCHASE
}
