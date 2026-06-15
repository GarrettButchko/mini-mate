package com.garrettbutchko.minimate.helper

import android.app.Activity
import android.content.Context
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import com.android.billingclient.api.acknowledgePurchase
import com.android.billingclient.api.queryProductDetails
import com.android.billingclient.api.queryPurchasesAsync
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import co.touchlab.kermit.Logger
import com.garrettbutchko.minimate.viewModels.AuthViewModel

/**
 * Equivalent of the iOS IAPManager using Google Play Billing Library.
 */
class IAPManager(
    private val context: Context,
    private val coroutineScope: CoroutineScope
) : PurchasesUpdatedListener {

    private val log = Logger.withTag("IAPManager")

    private val _products = MutableStateFlow<List<ProductDetails>>(emptyList())
    val products: StateFlow<List<ProductDetails>> = _products.asStateFlow()

    private val _isPurchasing = MutableStateFlow(false)
    val isPurchasing: StateFlow<Boolean> = _isPurchasing.asStateFlow()

    private var isInitialized = false

    // Callbacks to communicate with the AuthViewModel/KMP logic from the UI layer
    var onPurchaseSuccess: (Purchase) -> Unit = {}
    var onProStatusChecked: (Boolean) -> Unit = {}

    // Initialize the billing client
    private val billingClient = BillingClient.newBuilder(context)
        .setListener(this)
        .enablePendingPurchases()
        .build()

    fun initialize() {
        if (isInitialized) return

        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    isInitialized = true
                    coroutineScope.launch {
                        retrieveProducts()
                    }
                }
            }

            override fun onBillingServiceDisconnected() {
                // Try to restart the connection on the next request to Google Play by calling the startConnection() method.
                isInitialized = false
            }
        })
    }

    private suspend fun retrieveProducts() {
        val productList = listOf(
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId("com.minimate.pro")
                .setProductType(BillingClient.ProductType.INAPP)
                .build()
        )

        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(productList)
            .build()

        val result = billingClient.queryProductDetails(params)
        if (result.billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
            _products.value = result.productDetailsList ?: emptyList()
            log.i("Products retrieved: ${_products.value.map { it.name }}")
        } else {
            log.e("Failed to fetch products: ${result.billingResult.debugMessage}")
        }
    }

    /**
     * Pass the Activity from Jetpack Compose using `LocalContext.current as Activity`
     */
    fun purchase(activity: Activity, productDetails: ProductDetails) {
        log.i("🛒 Purchase started for product: ${productDetails.productId}")
        _isPurchasing.value = true

        val productDetailsParamsList = listOf(
            BillingFlowParams.ProductDetailsParams.newBuilder()
                .setProductDetails(productDetails)
                .build()
        )

        val billingFlowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(productDetailsParamsList)
            .build()

        // Launches the native Google Play bottom sheet overlay
        billingClient.launchBillingFlow(activity, billingFlowParams)
    }

    /**
     * Equivalent to `for await update in Transaction.updates`
     */
    override fun onPurchasesUpdated(billingResult: BillingResult, purchases: List<Purchase>?) {
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
            for (purchase in purchases) {
                coroutineScope.launch {
                    handlePurchase(purchase)
                }
            }
        } else if (billingResult.responseCode == BillingClient.BillingResponseCode.USER_CANCELED) {
            log.i("🛑 Purchase: User cancelled the payment sheet.")
            _isPurchasing.value = false
        } else {
            log.e("🚨 Purchase: System error: ${billingResult.debugMessage}")
            _isPurchasing.value = false
        }
    }

    private suspend fun handlePurchase(purchase: Purchase) {
        if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
            log.i("✅ Google Play: Purchase call successful. Verifying...")

            // Android requires acknowledging within 3 days or it refunds automatically!
            if (!purchase.isAcknowledged) {
                val acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder()
                    .setPurchaseToken(purchase.purchaseToken)
                    .build()
                
                val ackResult = billingClient.acknowledgePurchase(acknowledgePurchaseParams)
                if (ackResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    log.i("🏁 Transaction finished with Google.")
                    onPurchaseSuccess(purchase)
                    _isPurchasing.value = false
                } else {
                    log.e("❌ Verification: Failed to acknowledge purchase.")
                    _isPurchasing.value = false
                }
            } else {
                // Already acknowledged
                onPurchaseSuccess(purchase)
                _isPurchasing.value = false
            }
        }
    }

    /**
     * Equivalent to `Transaction.currentEntitlements`
     */
    suspend fun checkIsPurchasedPro(authModel: AuthViewModel) {
        val params = QueryPurchasesParams.newBuilder()
            .setProductType(BillingClient.ProductType.INAPP)
            .build()

        val result = billingClient.queryPurchasesAsync(params)
        var hasPro = false

        if (result.billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
            for (purchase in result.purchasesList) {
                if (purchase.products.contains("com.minimate.pro") && purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
                    hasPro = true
                    break
                }
            }
        }

        val user = authModel.userModel.value
        if (user != null && !user.isPro) {
            authModel.setUserModel(user.copy(isPro = hasPro))
        }

        onProStatusChecked(hasPro)
    }
}
