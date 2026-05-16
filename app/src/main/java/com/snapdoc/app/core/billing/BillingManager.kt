package com.snapdoc.app.core.billing

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
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import com.snapdoc.app.core.storage.UserPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import timber.log.Timber

/**
 * One-time "Remove Ads" IAP per spec §5.2. Wraps Play Billing v7.
 *
 * Lifecycle:
 *   1. [initialize] is called once at app start (connects, queries existing purchases).
 *   2. The UI observes [productDetails] for pricing and [removeAdsOwned] for ownership.
 *   3. The paywall calls [launchPurchase] with the current Activity.
 *   4. [restorePurchases] re-queries purchases (used by the Restore Purchase button).
 *
 * The hard source of truth for "is the user paid?" is Play's purchases
 * query result. We mirror that into [UserPreferences] for fast UI reads
 * but always re-verify on app start.
 */
@Singleton
class BillingManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val prefs: UserPreferences,
) : PurchasesUpdatedListener {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val _productDetails = MutableStateFlow<ProductDetails?>(null)
    val productDetails: StateFlow<ProductDetails?> = _productDetails.asStateFlow()

    private val _removeAdsOwned = MutableStateFlow(false)
    val removeAdsOwned: StateFlow<Boolean> = _removeAdsOwned.asStateFlow()

    private val client: BillingClient by lazy {
        BillingClient.newBuilder(context)
            .setListener(this)
            .enablePendingPurchases(
                PendingPurchasesParams.newBuilder().enableOneTimeProducts().build(),
            )
            .build()
    }

    fun initialize() {
        client.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(result: BillingResult) {
                if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                    scope.launch {
                        queryProductDetails()
                        restorePurchases()
                    }
                } else {
                    Timber.w("Billing setup failed: %s", result.debugMessage)
                }
            }
            override fun onBillingServiceDisconnected() {
                Timber.w("Billing service disconnected")
            }
        })
    }

    private suspend fun queryProductDetails() {
        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(
                listOf(
                    QueryProductDetailsParams.Product.newBuilder()
                        .setProductId(REMOVE_ADS_PRODUCT_ID)
                        .setProductType(BillingClient.ProductType.INAPP)
                        .build(),
                ),
            )
            .build()
        val result = suspendCancellableCoroutine { cont ->
            client.queryProductDetailsAsync(params) { _, list ->
                cont.resume(list)
            }
        }
        _productDetails.value = result.firstOrNull()
    }

    suspend fun restorePurchases() {
        val params = QueryPurchasesParams.newBuilder()
            .setProductType(BillingClient.ProductType.INAPP)
            .build()
        val purchases = suspendCancellableCoroutine { cont ->
            client.queryPurchasesAsync(params) { _, list ->
                cont.resume(list)
            }
        }
        val owned = purchases.any { it.products.contains(REMOVE_ADS_PRODUCT_ID) && it.purchaseState == Purchase.PurchaseState.PURCHASED }
        _removeAdsOwned.value = owned
        prefs.setRemoveAdsPurchased(owned)
        // Acknowledge any unacknowledged purchase.
        purchases.filter { !it.isAcknowledged && it.purchaseState == Purchase.PurchaseState.PURCHASED }
            .forEach { acknowledge(it) }
    }

    fun launchPurchase(activity: Activity) {
        val product = _productDetails.value ?: return
        val params = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(
                listOf(
                    BillingFlowParams.ProductDetailsParams.newBuilder()
                        .setProductDetails(product)
                        .build(),
                ),
            )
            .build()
        client.launchBillingFlow(activity, params)
    }

    private fun acknowledge(purchase: Purchase) {
        val params = AcknowledgePurchaseParams.newBuilder()
            .setPurchaseToken(purchase.purchaseToken)
            .build()
        client.acknowledgePurchase(params) { result ->
            if (result.responseCode != BillingClient.BillingResponseCode.OK) {
                Timber.w("Acknowledge failed: %s", result.debugMessage)
            }
        }
    }

    override fun onPurchasesUpdated(result: BillingResult, purchases: MutableList<Purchase>?) {
        if (result.responseCode != BillingClient.BillingResponseCode.OK || purchases == null) return
        purchases.filter { it.products.contains(REMOVE_ADS_PRODUCT_ID) }
            .forEach { purchase ->
                if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
                    scope.launch {
                        _removeAdsOwned.value = true
                        prefs.setRemoveAdsPurchased(true)
                        if (!purchase.isAcknowledged) acknowledge(purchase)
                    }
                }
            }
    }

    companion object {
        const val REMOVE_ADS_PRODUCT_ID = "snapdoc_remove_ads"
    }
}
