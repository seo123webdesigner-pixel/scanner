package com.snapdoc.app.core.ads

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.snapdoc.app.BuildConfig
import com.snapdoc.app.core.storage.UserPreferences
import com.snapdoc.app.core.ui.theme.SnapdocText
import com.snapdoc.app.core.ui.theme.SnapdocTheme
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.SharingStarted
import androidx.lifecycle.viewModelScope

@HiltViewModel
class BannerAdViewModel @Inject constructor(prefs: UserPreferences) : ViewModel() {
    val removeAdsPurchased: StateFlow<Boolean> = prefs.removeAdsPurchased
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)
}

/**
 * Banner ad slot per spec §8.7.1. Renders nothing if Remove Ads is owned.
 */
@Composable
fun BannerAd(
    modifier: Modifier = Modifier,
    viewModel: BannerAdViewModel = hiltViewModel(),
) {
    val owned by viewModel.removeAdsPurchased.collectAsState()
    if (owned) return
    val colors = SnapdocTheme.colors
    val context = LocalContext.current
    val adView = remember {
        AdView(context).apply {
            setAdSize(AdSize.BANNER)
            adUnitId = BuildConfig.ADMOB_BANNER_ID
            loadAd(AdRequest.Builder().build())
        }
    }
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(50.dp)
            .background(colors.surface)
            .padding(top = 0.dp),
    ) {
        AndroidView(factory = { adView }, modifier = Modifier.fillMaxWidth())
        Text(
            text = "Ad",
            style = SnapdocText.labelSm,
            color = colors.textTertiary,
            modifier = Modifier.padding(start = 4.dp, top = 2.dp),
        )
    }
}
