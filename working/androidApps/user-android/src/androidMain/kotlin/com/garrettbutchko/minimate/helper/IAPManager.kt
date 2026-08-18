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
import com.garrettbutchko.minimate.utilities.AppLogger
import com.garrettbutchko.minimate.viewModels.AuthViewModel

/**
 * Equivalent of the iOS IAPManager using Google Play Billing Library.
 */
class IAPManager(
    private val context: Context,
    private val coroutineScope: CoroutineScope
) : PurchasesUpdatedListener {

    private val _products = MutableStateFlow<List<ProductDetails>>(emptyList())
    val products: StateFlow<List<ProductDetails>> = _products.asStateFlow()

    private val _isPurchasing = MutableStateFlow(false)
    val isPurchasing: StateFlow<Boolean> = _isPurchasing.asStateFlow()

    private var isInitialized = false
    private var pendingProCheck: (suspend () -> Unit)? = null

    // Callbacks to communicate with the AuthViewModel/KMP logic from the UI layer
    var onPurchaseSuccess: (Purchase) -> Unit = {}
    var onProStatusChecked: (Boolean) -> Unit = {}

    // Initialize the billing client
    private val billingClient = BillingClient.newBuilder(context)
        .setListener(this)
        .enablePendingPurchases()
        .build()

    fun initialize(onReady: (suspend () -> Unit)? = null) {
        if (onReady != null) pendingProCheck = onReady
        if (isInitialized) {
            onReady?.let { coroutineScope.launch { it() } }
            return
        }

        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    isInitialized = true
                    coroutineScope.launch {
                        retrieveProducts()
                        pendingProCheck?.invoke()
                        pendingProCheck = null
                    }
                }
            }

            override fun onBillingServiceDisconnected() {
                isInitialized = false
            }
        })
    }

    private suspend fun retrieveProducts() {
        val productList = listOf(
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId("com.minimate.user.pro")
                .setProductType(BillingClient.ProductType.INAPP)
                .build()
        )

        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(productList)
            .build()

        val result = billingClient.queryProductDetails(params)
        if (result.billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
            _products.value = result.productDetailsList ?: emptyList()
            AppLogger.info("IAPManager", "Products retrieved: ${_products.value.map { it.name }}")
        } else {
            AppLogger.error("IAPManager", "Failed to fetch products: ${result.billingResult.debugMessage}")
        }
    }

    /**
     * Pass the Activity from Jetpack Compose using `LocalContext.current as Activity`
     */
    fun purchase(activity: Activity, productDetails: ProductDetails) {
        AppLogger.info("IAPManager", "Purchase started for product: ${productDetails.productId}")
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
        when (billingResult.responseCode) {
            BillingClient.BillingResponseCode.OK if purchases != null -> {
                for (purchase in purchases) {
                    coroutineScope.launch {
                        handlePurchase(purchase)
                    }
                }
            }
            BillingClient.BillingResponseCode.USER_CANCELED -> {
                AppLogger.warning("IAPManager", "Purchase: User cancelled the payment sheet.")
                _isPurchasing.value = false
            }
            else -> {
                AppLogger.error("IAPManager", "Purchase: System error: ${billingResult.debugMessage}")
                _isPurchasing.value = false
            }
        }
    }

    private suspend fun handlePurchase(purchase: Purchase) {
        if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
            AppLogger.success("IAPManager", "Google Play: Purchase call successful. Verifying...")

            // Android requires acknowledging within 3 days or it refunds automatically!
            if (!purchase.isAcknowledged) {
                val acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder()
                    .setPurchaseToken(purchase.purchaseToken)
                    .build()

                val ackResult = billingClient.acknowledgePurchase(acknowledgePurchaseParams)
                if (ackResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    AppLogger.success("IAPManager", "Transaction finished with Google.")
                    onPurchaseSuccess(purchase)
                    _isPurchasing.value = false
                } else {
                    AppLogger.error("IAPManager", "Verification: Failed to acknowledge purchase.")
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
                if (purchase.products.contains("com.minimate.user.pro") && purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
                    hasPro = true
                    break
                }
            }
        } else {
            AppLogger.warning("IAPManager", "checkIsPurchasedPro: billing not ready (${result.billingResult.responseCode}), skipping")
            return
        }

        if (!hasPro) {
            AppLogger.info("IAPManager", "checkIsPurchasedPro: no active Pro entitlement")
            onProStatusChecked(false)
            return
        }

        AppLogger.info("IAPManager", "checkIsPurchasedPro: Pro entitlement found")
        val user = authModel.userModel.value
        if (user != null) {
            val wasAlreadyPro = user.isPro
            authModel.setUserModel(user.copy(isPro = true))
            if (!wasAlreadyPro) {
                runCatching { authModel.userRepository.saveUnified(user.googleId, user.copy(isPro = true)) }
                    .onSuccess { AppLogger.success("IAPManager", "Pro status synced to Firebase") }
                    .onFailure { AppLogger.warning("IAPManager", "Failed to sync Pro status to Firebase: ${it.message}") }
            }
        }

        onProStatusChecked(true)
    }
}
