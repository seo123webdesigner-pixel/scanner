package com.snapdoc.app.feature.onboarding

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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.snapdoc.app.core.storage.UserPreferences
import com.snapdoc.app.core.ui.components.GhostButton
import com.snapdoc.app.core.ui.components.PrimaryButton
import com.snapdoc.app.core.ui.theme.SnapdocText
import com.snapdoc.app.core.ui.theme.SnapdocTheme
import com.snapdoc.app.navigation.SnapdocRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.launch

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val prefs: UserPreferences,
) : ViewModel() {
    fun complete(onDone: () -> Unit) {
        viewModelScope.launch {
            prefs.markOnboardingComplete()
            onDone()
        }
    }
}

private data class OnboardingPage(
    val icon: ImageVector,
    val title: String,
    val body: String,
)

private val pages = listOf(
    OnboardingPage(
        Icons.Outlined.Lock,
        "Your scans stay on your phone.",
        "We don't have a server. Your documents never leave your device unless you ask AI to summarize them.",
    ),
    OnboardingPage(
        Icons.Outlined.AutoAwesome,
        "Understand any document in seconds.",
        "AI summaries pull out the key points from bills, contracts, and notes. Only the typed-out text is sent — never the image.",
    ),
    OnboardingPage(
        Icons.Outlined.Search,
        "Find anything by typing what's inside.",
        "Every scan is auto-organized into categories and made searchable, even by the words inside the document.",
    ),
)

@Composable
fun OnboardingScreen(
    onComplete: (String) -> Unit,
    viewModel: OnboardingViewModel = hiltViewModel(),
) {
    val colors = SnapdocTheme.colors
    val pagerState = rememberPagerState { pages.size }
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.bg)
            .padding(horizontal = 24.dp, vertical = 48.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
        ) {
            GhostButton(text = "Skip", onClick = {
                viewModel.complete { onComplete(SnapdocRoute.Home.path) }
            })
        }
        Spacer(Modifier.height(32.dp))
        HorizontalPager(state = pagerState, modifier = Modifier.weight(1f)) { index ->
            OnboardingPageContent(pages[index])
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 24.dp),
            horizontalArrangement = Arrangement.Center,
        ) {
            repeat(pages.size) { i ->
                val active = i == pagerState.currentPage
                Box(
                    modifier = Modifier
                        .padding(horizontal = 4.dp)
                        .size(if (active) 10.dp else 8.dp)
                        .clip(CircleShape)
                        .background(if (active) colors.primary else colors.border),
                )
            }
        }
        val isLast = pagerState.currentPage == pages.size - 1
        PrimaryButton(
            text = if (isLast) "Get started" else "Next",
            onClick = {
                if (isLast) {
                    viewModel.complete { onComplete(SnapdocRoute.Home.path) }
                } else {
                    scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
                }
            },
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun OnboardingPageContent(page: OnboardingPage) {
    val colors = SnapdocTheme.colors
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .size(96.dp)
                .clip(SnapdocTheme.shapes.full)
                .background(colors.surfaceVariant),
            contentAlignment = Alignment.Center,
        ) {
            Icon(page.icon, contentDescription = null, modifier = Modifier.size(40.dp), tint = colors.primary)
        }
        Spacer(Modifier.height(48.dp))
        Text(
            page.title,
            style = SnapdocText.headlineXl,
            color = colors.textPrimary,
            textAlign = TextAlign.Center,
            modifier = Modifier.widthIn(max = 320.dp),
        )
        Spacer(Modifier.height(16.dp))
        Text(
            page.body,
            style = SnapdocText.bodyLg,
            color = colors.textSecondary,
            textAlign = TextAlign.Center,
            modifier = Modifier.widthIn(max = 320.dp),
        )
    }
}
