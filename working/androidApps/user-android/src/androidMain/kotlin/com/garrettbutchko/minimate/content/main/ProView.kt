package com.garrettbutchko.minimate.content.main

import co.touchlab.kermit.Logger

import android.app.Activity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.android.billingclient.api.Purchase
import com.garrettbutchko.minimate.di.KoinHelper
import com.garrettbutchko.minimate.di.KoinHelperParent
import com.garrettbutchko.minimate.helper.IAPManager
import com.garrettbutchko.minimate.interfaces.LocationFinding
import com.garrettbutchko.minimate.repositories.gameRepos.LocalGameRepository
import com.garrettbutchko.minimate.utilities.LocationHandler
import com.garrettbutchko.minimate.viewModels.AuthViewModel
import com.garrettbutchko.minimate.viewModels.GameViewModel
import com.garrettbutchko.minimate.viewModels.HostViewModel
import kotlinx.coroutines.launch

@Composable
fun ProView(
    iapManager: IAPManager,
    showSheet: Boolean,
    onShowSheetChange: (Boolean) -> Unit,
    activity: Activity?
) {
    val benefits = remember {
        listOf(
            "Ad-free experience",
            "Store unlimited played games",
            "Early access to new content",
            "More coming soon..."
        )
    }

    val authModel: AuthViewModel = KoinHelperParent.getAuthViewModel()


    val products by iapManager.products.collectAsState()
    val isPurchasing by iapManager.isPurchasing.collectAsState()

    val scope = rememberCoroutineScope()
    var thankYou by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    fun persistUserProUpgrade(purchase: Purchase?) {
        val currentUser = authModel.userModel.value ?: return
        if (currentUser.isPro) {
            thankYou = true
            onShowSheetChange(false)
            return
        }

        scope.launch {
            val upgraded = currentUser.copy(isPro = true)
            authModel.setUserModel(upgraded)
            authModel.userRepository.saveUnified(upgraded.googleId, upgraded)
            Logger.i("✅ Pro purchase persisted for user ${upgraded.googleId}. Purchase token=${purchase?.purchaseToken}")
            thankYou = true
            onShowSheetChange(false)
        }
    }

    LaunchedEffect(iapManager, authModel) {
        iapManager.onPurchaseSuccess = { purchase ->
            errorMessage = null
            persistUserProUpgrade(purchase)
        }

        iapManager.onProStatusChecked = { hasPro ->
            if (hasPro) {
                errorMessage = null
                persistUserProUpgrade(null)
            } else {
                errorMessage = "No previous Pro purchase found for this account."
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .size(width = 38.dp, height = 6.dp)
                .clip(CircleShape)
                .background(Color.Gray.copy(alpha = 0.6f))
        )

        Text(
            text = "Buy Pro Now!",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        Text(
            text = "Your support helps build awesome new features. Thank you!",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                benefits.forEach { benefit ->
                    BenefitLine(icon = Icons.Default.CheckCircle, text = benefit)
                }
            }
        }

        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            androidx.compose.material3.Icon(
                imageVector = Icons.Default.Star,
                contentDescription = "Pro",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(36.dp)
            )
        }

        AnimatedVisibility(visible = thankYou, enter = fadeIn(), exit = fadeOut()) {
            Text(
                text = "Thank you for your purchase!",
                color = Color(0xFF2E7D32),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }

        if (errorMessage != null) {
            Text(
                text = errorMessage!!,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }

        Button(
            onClick = {
                val product = products.firstOrNull()
                when {
                    activity == null -> errorMessage = "Unable to open purchase flow from this screen."
                    product == null -> errorMessage = "No products available for purchase yet."
                    else -> {
                        errorMessage = null
                        iapManager.purchase(activity, product)
                    }
                }
            },
            enabled = !isPurchasing,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFBC02D)),
            modifier = Modifier.fillMaxWidth()
        ) {
            if (isPurchasing) {
                CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp, color = Color.White)
            } else {
                Text("Upgrade Now", color = Color.White)
            }
        }

        Button(
            onClick = {
                scope.launch {
                    errorMessage = null
                    iapManager.checkIsPurchasedPro()
                }
            },
            enabled = !isPurchasing,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
        ) {
            Text("Restore Purchases")
        }

        Spacer(modifier = Modifier.height(if (showSheet) 8.dp else 0.dp))
    }
}

@Composable
private fun BenefitLine(icon: ImageVector, text: String) {
    androidx.compose.foundation.layout.Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        androidx.compose.material3.Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color(0xFFFBC02D)
        )
        Text(text = text, style = MaterialTheme.typography.titleSmall)
    }
}


