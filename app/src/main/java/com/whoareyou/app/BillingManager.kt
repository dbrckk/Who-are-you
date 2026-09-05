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

    private val billingClient: BillingClient = BillingClient.newBuilder(context)
        .enablePendingPurchases(
            PendingPurchasesParams.newBuilder()
                .enableOneTimeProducts()
                .build()
        )
        .setListener { result, purchases ->
            if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                handlePurchases(purchases.orEmpty())
            }
        }
        .build()

    fun start() {
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(result: BillingResult) {
                if (result.responseCode != BillingClient.BillingResponseCode.OK) return
                queryProduct()
                restorePurchases()
            }

            override fun onBillingServiceDisconnected() = Unit
        })
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
        billingClient.launchBillingFlow(activity, params)
    }

    fun close() {
        billingClient.endConnection()
    }

    private fun handlePurchases(purchases: List<Purchase>) {
        purchases.forEach { purchase ->
            if (
                REMOVE_ADS_PRODUCT_ID in purchase.products &&
                purchase.purchaseState == Purchase.PurchaseState.PURCHASED
            ) {
                if (!purchase.isAcknowledged) {
                    val params = AcknowledgePurchaseParams.newBuilder()
                        .setPurchaseToken(purchase.purchaseToken)
                        .build()
                    billingClient.acknowledgePurchase(params) { }
                }
                grantPremium()
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
            }
        }
    }

    private fun restorePurchases() {
        val params = QueryPurchasesParams.newBuilder()
            .setProductType(BillingClient.ProductType.INAPP)
            .build()

        billingClient.queryPurchasesAsync(params) { result, purchases ->
            if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                handlePurchases(purchases)
            }
        }
    }

    private fun grantPremium() {
        onPremiumChanged(true)
        AppEvents.purchaseSuccess(REMOVE_ADS_PRODUCT_ID)
        CoroutineScope(Dispatchers.IO).launch {
            ProfileStore.setAdsRemoved(context, true)
        }
    }
}
