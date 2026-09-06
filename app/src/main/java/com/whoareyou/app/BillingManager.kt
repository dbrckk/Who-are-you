package com.whoareyou.app

import android.app.Activity
import android.content.Context
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class BillingManager(
    private val context: Context,
    private val onPremiumChanged: (Boolean) -> Unit,
    private val onPriceChanged: (String?) -> Unit = {}
) {
    companion object {
        const val REMOVE_ADS_PRODUCT_ID = "remove_ads_lifetime"
    }

    private var removeAdsProduct: ProductDetails? = null
    private var removeAdsOfferToken: String? = null
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private var reconnectJob: Job? = null
    private var reconnectAttempt = 0
    private var closed = false
    private val loggedLivePurchaseTokens = mutableSetOf<String>()

    private val billingClient: BillingClient = BillingClient.newBuilder(context)
        .enablePendingPurchases(
            PendingPurchasesParams.newBuilder()
                .enableOneTimeProducts()
                .build()
        )
        .setListener { result, purchases ->
            if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                handlePurchases(purchases.orEmpty(), PurchaseGrantSource.LIVE_PURCHASE)
            } else if (result.responseCode != BillingClient.BillingResponseCode.USER_CANCELED) {
                recordBillingError("purchase_update", result)
            }
        }
        .build()

    fun start() {
        closed = false
        connect()
    }

    fun launchPurchase(activity: Activity) {
        val product = removeAdsProduct ?: return
        val productBuilder = BillingFlowParams.ProductDetailsParams.newBuilder()
            .setProductDetails(product)
        removeAdsOfferToken?.let(productBuilder::setOfferToken)

        val params = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(listOf(productBuilder.build()))
            .build()
        AppEvents.premiumView()
        val result = billingClient.launchBillingFlow(activity, params)
        if (result.responseCode != BillingClient.BillingResponseCode.OK) {
            recordBillingError("launch", result)
        }
    }

    fun close() {
        closed = true
        reconnectJob?.cancel()
        reconnectJob = null
        billingClient.endConnection()
    }

    private fun connect() {
        if (closed || billingClient.isReady) return
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(result: BillingResult) {
                if (closed) return
                if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                    reconnectAttempt = 0
                    reconnectJob?.cancel()
                    reconnectJob = null
                    queryProduct()
                    restorePurchases()
                } else {
                    recordBillingError("setup", result)
                    scheduleReconnect()
                }
            }

            override fun onBillingServiceDisconnected() {
                scheduleReconnect()
            }
        })
    }

    private fun scheduleReconnect() {
        if (closed || reconnectJob?.isActive == true) return
        val delayMillis = BillingReconnectPolicy.delayMillis(reconnectAttempt)
        reconnectAttempt = BillingReconnectPolicy.nextAttempt(reconnectAttempt)
        reconnectJob = scope.launch {
            delay(delayMillis)
            reconnectJob = null
            connect()
        }
    }

    private fun handlePurchases(purchases: List<Purchase>, source: PurchaseGrantSource) {
        purchases.forEach { purchase ->
            if (
                REMOVE_ADS_PRODUCT_ID in purchase.products &&
                purchase.purchaseState == Purchase.PurchaseState.PURCHASED
            ) {
                if (!purchase.isAcknowledged) {
                    val params = AcknowledgePurchaseParams.newBuilder()
                        .setPurchaseToken(purchase.purchaseToken)
                        .build()
                    billingClient.acknowledgePurchase(params) { result ->
                        if (result.responseCode != BillingClient.BillingResponseCode.OK) {
                            recordBillingError("acknowledge", result)
                        }
                    }
                }
                grantPremium(source, purchase.purchaseToken)
            }
        }
    }

    private fun queryProduct() {
        val product = QueryProductDetailsParams.Product.newBuilder()
            .setProductId(REMOVE_ADS_PRODUCT_ID)
            .setProductType(BillingClient.ProductType.INAPP)
            .build()
        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(listOf(product))
            .build()

        billingClient.queryProductDetailsAsync(params) { result, response ->
            if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                removeAdsProduct = response.productDetailsList.firstOrNull()
                val selectedOffer = removeAdsProduct
                    ?.oneTimePurchaseOfferDetailsList
                    ?.firstOrNull()
                removeAdsOfferToken = selectedOffer?.offerToken
                val localizedPrice = selectedOffer?.formattedPrice
                    ?: removeAdsProduct?.oneTimePurchaseOfferDetails?.formattedPrice
                onPriceChanged(localizedPrice)
            } else {
                recordBillingError("product_query", result)
            }
        }
    }

    private fun restorePurchases() {
        val params = QueryPurchasesParams.newBuilder()
            .setProductType(BillingClient.ProductType.INAPP)
            .build()

        billingClient.queryPurchasesAsync(params) { result, purchases ->
            if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                handlePurchases(purchases, PurchaseGrantSource.RESTORE)
            } else {
                recordBillingError("restore", result)
            }
        }
    }

    private fun grantPremium(source: PurchaseGrantSource, purchaseToken: String) {
        onPremiumChanged(true)
        val shouldLogSuccess = PurchaseGrantPolicy.shouldLogPurchaseSuccess(source) &&
            loggedLivePurchaseTokens.add(purchaseToken)
        if (shouldLogSuccess) {
            AppEvents.purchaseSuccess(REMOVE_ADS_PRODUCT_ID)
        }
        CoroutineScope(Dispatchers.IO).launch {
            ProfileStore.setAdsRemoved(context, true)
        }
    }

    private fun recordBillingError(stage: String, result: BillingResult) {
        AppEvents.recordError(
            IllegalStateException("Billing $stage failed [${result.responseCode}]: ${result.debugMessage}"),
            mapOf("component" to "billing", "stage" to stage, "response_code" to result.responseCode)
        )
    }
}
