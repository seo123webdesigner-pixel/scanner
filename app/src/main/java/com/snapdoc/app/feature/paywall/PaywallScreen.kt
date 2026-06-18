package com.snapdoc.app.feature.paywall

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.snapdoc.app.core.billing.BillingManager
import com.snapdoc.app.core.ui.components.GhostButton
import com.snapdoc.app.core.ui.components.IconOnlyButton
import com.snapdoc.app.core.ui.components.PrimaryButton
import com.snapdoc.app.core.ui.theme.SnapdocText
import com.snapdoc.app.core.ui.theme.SnapdocTheme
import com.snapdoc.app.core.ui.theme.tabular
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class PaywallUiState(
    val priceLabel: String? = null,
    val owned: Boolean = false,
)

@HiltViewModel
class PaywallViewModel @Inject constructor(
    private val billing: BillingManager,
) : ViewModel() {

    init {
        // Re-attempt the product/price load when the paywall opens.
        billing.refresh()
    }

    val state: StateFlow<PaywallUiState> = combine(
        billing.productDetails,
        billing.removeAdsOwned,
    ) { details, owned ->
        PaywallUiState(
            priceLabel = details?.oneTimePurchaseOfferDetails?.formattedPrice,
            owned = owned,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), PaywallUiState())

    fun buy(activity: Activity) = billing.launchPurchase(activity)
    fun restore() {
        viewModelScope.launch { billing.restorePurchases() }
    }
    fun refresh() = billing.refresh()
}

@Composable
fun PaywallScreen(
    onBack: () -> Unit,
    viewModel: PaywallViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()
    val colors = SnapdocTheme.colors
    val context = LocalContext.current

    Scaffold(
        containerColor = colors.bg,
        topBar = {
            Row(
                modifier = Modifier.fillMaxWidth().padding(8.dp),
                horizontalArrangement = Arrangement.Start,
            ) {
                IconOnlyButton(icon = Icons.Outlined.ArrowBack, contentDescription = "Back", onClick = onBack)
            }
        },
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(colors.bg)
                .padding(padding)
                .padding(horizontal = 16.dp, vertical = 24.dp),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(SnapdocTheme.shapes.xl)
                    .background(colors.surface)
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Icon(Icons.Outlined.Lock, contentDescription = null, modifier = Modifier.size(48.dp), tint = colors.primary)
                Spacer(Modifier.height(24.dp))
                Text("Remove ads", style = SnapdocText.display, color = colors.textPrimary)
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "${state.priceLabel ?: "₹99"} once. Forever.",
                    style = SnapdocText.headlineLg.tabular(),
                    color = colors.textSecondary,
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(32.dp))
                listOf(
                    "Remove all banner, native, and full-screen ads",
                    "Unlimited AI summaries — no rewarded ad gate",
                    "Restore on any Android device with the same Google account",
                ).forEach { line ->
                    Row(modifier = Modifier.padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.Check, contentDescription = null, tint = colors.success, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.size(12.dp))
                        Text(line, style = SnapdocText.bodyLg, color = colors.textPrimary)
                    }
                }
                Spacer(Modifier.height(32.dp))
                if (state.owned) {
                    Text(
                        "You own Remove Ads. Thank you.",
                        style = SnapdocText.titleLg,
                        color = colors.success,
                    )
                } else {
                    val priceReady = state.priceLabel != null
                    PrimaryButton(
                        text = if (priceReady) "Buy — ${state.priceLabel}" else "Loading price…",
                        enabled = priceReady,
                        onClick = { (context as? Activity)?.let(viewModel::buy) },
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Spacer(Modifier.height(12.dp))
                    GhostButton(
                        text = "Restore purchase",
                        onClick = viewModel::restore,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    if (!priceReady) {
                        Spacer(Modifier.height(16.dp))
                        Text(
                            "Couldn't load the price. This works once the app is live on Google Play " +
                                "(installed from the Store) and the product is active. Tap retry in a moment.",
                            style = SnapdocText.caption,
                            color = colors.textTertiary,
                            textAlign = TextAlign.Center,
                        )
                        Spacer(Modifier.height(8.dp))
                        GhostButton(
                            text = "Retry",
                            onClick = viewModel::refresh,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }
            }
        }
    }
}
