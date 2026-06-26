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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import com.garrettbutchko.minimate.content.helper.LogoLoadingView
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.android.billingclient.api.Purchase
import com.garrettbutchko.minimate.di.KoinHelperParent
import com.garrettbutchko.minimate.helper.IAPManager
import com.garrettbutchko.minimate.viewModels.AuthViewModel
import kotlinx.coroutines.launch

@Composable
fun ProView(
    iapManager: IAPManager,
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
            Logger.i("✅ Pro purchase persisted for ${upgraded.googleId}. token=${purchase?.purchaseToken}")
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
                errorMessage = "No previous Pro purchase found."
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .padding(vertical = 12.dp)
                .size(width = 38.dp, height = 6.dp)
                .clip(CircleShape)
                .background(Color.Gray.copy(alpha = 0.4f))
        )

        Spacer(modifier = Modifier.weight(1f))

        Text(
            text = "Buy Pro Now!",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Your support helps to build awesome new features. Thank you! 🙏",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.weight(1f))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    RoundedCornerShape(25.dp)
                )
                .padding(vertical = 8.dp, horizontal = 4.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            benefits.forEach { benefit ->
                BenefitLine(text = benefit)
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Icon(
            painter = painterResource(com.garrettbutchko.minimate.shared.R.drawable.logo),
            contentDescription = null,
            tint = colorResource(com.garrettbutchko.minimate.shared.R.color.mainOpp),
            modifier = Modifier.size(40.dp)
        )

        Spacer(modifier = Modifier.weight(1f))

        AnimatedVisibility(visible = thankYou, enter = fadeIn(), exit = fadeOut()) {
            Text(
                text = "🎉 Thank you for your purchase!",
                color = Color(0xFF2E7D32),
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            )
        }

        if (errorMessage != null) {
            Text(
                text = errorMessage!!,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            )
        }

        Text(
            text = "$1.99 one-time purchase",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Button(
            onClick = {
                val product = products.firstOrNull()
                when {
                    activity == null -> errorMessage = "Unable to open purchase flow."
                    product == null -> errorMessage = "No products available yet."
                    else -> {
                        errorMessage = null
                        iapManager.purchase(activity, product)
                    }
                }
            },
            enabled = !isPurchasing,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFBC02D)),
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .shadow(6.dp, RoundedCornerShape(27.dp), spotColor = Color(0xFFFBC02D).copy(alpha = 0.6f)),
            shape = RoundedCornerShape(27.dp)
        ) {
            if (isPurchasing) {
                LogoLoadingView(size = 20.dp)
            } else {
                Text(
                    "Upgrade Now",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }

        TextButton(
            onClick = {
                scope.launch {
                    errorMessage = null
                    iapManager.checkIsPurchasedPro(authModel)
                }
            },
            enabled = !isPurchasing
        ) {
            Text(
                "Restore Purchases",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun BenefitLine(text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = null,
            tint = Color(0xFFFBC02D),
            modifier = Modifier.size(20.dp)
        )
        Text(
            text = text,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold
        )
    }
}
