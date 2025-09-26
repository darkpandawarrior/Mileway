package com.miletracker.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Insights
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.miletracker.core.ui.components.DotsIndicator
import com.miletracker.core.ui.theme.DesignTokens
import com.miletracker.stub.MarketingCarouselItem
import org.koin.compose.viewmodel.koinViewModel

/**
 * Bottom content padding so the last section clears the floating ~100dp bubble bottom bar
 * drawn by the shell. The Home screen is a top-level tab, so this padding is mandatory.
 */
private val BottomBarClearance = 140.dp

/**
 * The Home tab.
 *
 * A top-level destination: it owns its top inset through [HomeProfileHeader]'s gradient
 * (which applies `statusBarsPadding` internally) and has NO [androidx.compose.material3.TopAppBar] —
 * the gradient header *is* the top of the screen. All content scrolls in a single column and
 * floats above the bubble bottom bar via [BottomBarClearance].
 *
 * The screen is stateless: it reads an immutable [HomeUiState] and routes every user action
 * back through the [viewModel]'s one-shot intents to the hoisted navigation callbacks.
 *
 * @param onStartTracking opens the live mileage-tracking flow (primary CTA).
 * @param onAddExpense begins expense logging; the integrator maps this to "log miles".
 * @param onOpenAccount opens account / details surfaces (At A Glance rows, account actions).
 */
@Composable
fun HomeScreen(
    onStartTracking: () -> Unit,
    onAddExpense: () -> Unit,
    onOpenAccount: () -> Unit,
    viewModel: HomeViewModel = koinViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    HomeScreenContent(
        state = state,
        onStartTracking = { viewModel.onStartTracking(onStartTracking) },
        onAddExpense = { viewModel.onAddExpense(onAddExpense) },
        onOpenAccount = { viewModel.onOpenAccount(onOpenAccount) },
    )
}

/**
 * Stateless body of the Home tab. Split out from [HomeScreen] so it can be exercised with a
 * hand-built [HomeUiState] and plain lambdas, independent of Koin and the ViewModel.
 */
@Composable
private fun HomeScreenContent(
    state: HomeUiState,
    onStartTracking: () -> Unit,
    onAddExpense: () -> Unit,
    onOpenAccount: () -> Unit,
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState),
    ) {
        // 1. Gradient header (draws behind the status bar; owns the top inset).
        HomeProfileHeader(
            name = state.greetingName,
            notificationCount = state.notificationCount,
            onSearch = onOpenAccount,
            onNotifications = onOpenAccount,
        )

        Column(
            modifier = Modifier.padding(horizontal = DesignTokens.Spacing.screenHorizontal),
            verticalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.sectionSpacing),
        ) {
            Spacer(Modifier.height(DesignTokens.Spacing.l))

            // 2. Action Required card.
            ActionRequiredCard(
                banner = state.actionRequired,
                onTakeAction = onAddExpense,
            )

            // 3. Quick Actions.
            Column(verticalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.m)) {
                HomeSectionHeader(title = "Quick Actions", leadingIcon = Icons.Filled.Bolt)
                QuickActionsRow(
                    actions = quickActions(
                        onAddExpense = onAddExpense,
                        onIllustrative = onOpenAccount,
                    ),
                )
            }
        }

        Spacer(Modifier.height(DesignTokens.Spacing.sectionSpacing))

        // 4. Feature / mileage carousel (full-bleed horizontal pager).
        FeatureCarousel(
            onStartTracking = onStartTracking,
            onIllustrative = onOpenAccount,
        )

        Column(
            modifier = Modifier.padding(horizontal = DesignTokens.Spacing.screenHorizontal),
            verticalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.sectionSpacing),
        ) {
            Spacer(Modifier.height(DesignTokens.Spacing.sectionSpacing))

            // 5. At A Glance.
            Column(verticalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.xs)) {
                HomeSectionHeader(title = "At A Glance", leadingIcon = Icons.Filled.Insights)
                Spacer(Modifier.height(DesignTokens.Spacing.xs))
                atAGlanceRows(state.atAGlance).forEach { row ->
                    AtAGlanceRowView(row = row, onClick = onOpenAccount)
                }
            }
        }

        Spacer(Modifier.height(DesignTokens.Spacing.sectionSpacing))

        // 6. Marketing / benefits strip (full-bleed horizontal list).
        MarketingStrip(items = state.marketingItems)

        Spacer(Modifier.height(BottomBarClearance))
    }
}

/**
 * The horizontally paging feature carousel (Mileage / Customer Navigation / Track Reportees /
 * Center Check-In) with a [DotsIndicator] beneath it.
 */
@Composable
private fun FeatureCarousel(
    onStartTracking: () -> Unit,
    onIllustrative: () -> Unit,
) {
    val cards = remember(onStartTracking, onIllustrative) {
        featureCarouselCards(onStartTracking = onStartTracking, onIllustrative = onIllustrative)
    }
    val pagerState = rememberPagerState(pageCount = { cards.size })

    Column(verticalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.m)) {
        HorizontalPager(
            state = pagerState,
            contentPadding = PaddingValues(horizontal = DesignTokens.Spacing.screenHorizontal),
            pageSpacing = DesignTokens.Spacing.m,
        ) { page ->
            FeatureCarouselCardView(card = cards[page])
        }
        DotsIndicator(
            pageCount = cards.size,
            selectedIndex = pagerState.currentPage,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

/** The static marketing/benefits card strip. */
@Composable
private fun MarketingStrip(items: List<MarketingCarouselItem>) {
    Column(verticalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.m)) {
        Box(modifier = Modifier.padding(horizontal = DesignTokens.Spacing.screenHorizontal)) {
            HomeSectionHeader(title = "Benefits", leadingIcon = Icons.Filled.CardGiftcard)
        }
        LazyRow(
            contentPadding = PaddingValues(horizontal = DesignTokens.Spacing.screenHorizontal),
            horizontalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.carouselSpacing),
        ) {
            items(items) { item ->
                MarketingCardView(item = item)
            }
        }
    }
}
