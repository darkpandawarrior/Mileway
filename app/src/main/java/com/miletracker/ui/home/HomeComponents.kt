package com.miletracker.ui.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NoteAdd
import androidx.compose.material.icons.filled.PriorityHigh
import androidx.compose.material.icons.filled.RequestQuote
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.NotificationImportant
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.RadioButtonChecked
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.miletracker.core.ui.components.AutoSizeGreeting
import com.miletracker.core.ui.components.CountBadge
import com.miletracker.core.ui.theme.DesignTokens
import com.miletracker.stub.ActionRequiredBanner
import com.miletracker.stub.AtAGlanceCounts
import com.miletracker.stub.MarketingCarouselItem
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// =============================================================================
// Header
// =============================================================================

/** Height of the gradient header content below the status bar inset. */
private val HeaderContentHeight = 96.dp

/** Diameter of the leading avatar circle in the header. */
private val AvatarSize = 44.dp

/**
 * Primary-gradient home header that draws behind the status bar.
 *
 * Owns the status-bar inset itself (`statusBarsPadding` inside the gradient box) so the
 * colour bleeds to the top edge while the content stays clear of the system clock. A faint
 * translucent dot-grid Canvas overlay evokes a world map without needing an image asset.
 *
 * Layout: avatar circle, an amber auto-sizing "Hello, <name>" greeting with a
 * "Let's manage your spends!" subtitle, then a trailing search icon and a notification bell
 * carrying a red [CountBadge].
 *
 * @param name greeting name (already reduced to a first name by the ViewModel).
 * @param notificationCount unread count rendered in the bell badge.
 */
@Composable
fun HomeProfileHeader(
    name: String,
    notificationCount: Int,
    onSearch: () -> Unit,
    onNotifications: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(DesignTokens.topBarGradientBrush()),
    ) {
        // Translucent dot-grid overlay — a subtle "world map" texture, asset-free.
        WorldMapDotGrid(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding(),
            dotColor = Color.White.copy(alpha = 0.13f),
        )
        // Diagonal gloss sheen — top-left to centre, low-alpha white stripe.
        Canvas(modifier = Modifier.fillMaxSize().statusBarsPadding()) {
            val sheenBrush = Brush.linearGradient(
                colors = listOf(Color.White.copy(alpha = 0.10f), Color.Transparent),
                start = Offset(0f, 0f),
                end = Offset(size.width * 0.55f, size.height),
            )
            drawRect(brush = sheenBrush)
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .height(HeaderContentHeight)
                .padding(horizontal = DesignTokens.Spacing.l),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.m),
        ) {
            // Avatar circle with gradient ring for extra polish.
            Box(
                modifier = Modifier
                    .size(AvatarSize)
                    .border(
                        width = 2.dp,
                        brush = Brush.linearGradient(listOf(Color.White.copy(alpha = 0.7f), Color.White.copy(alpha = 0.2f))),
                        shape = CircleShape,
                    )
                    .padding(2.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.22f)),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = name.take(1).uppercase(),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                AutoSizeGreeting(
                    greeting = "Hello,",
                    name = name,
                    color = DesignTokens.StatusColors.warning,
                )
                Text(
                    text = "Let's manage your spends!",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.85f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            HeaderIconButton(
                icon = Icons.Filled.Search,
                contentDescription = "Search",
                onClick = onSearch,
            )

            // Notification bell with red count badge anchored at the top-end.
            Box {
                HeaderIconButton(
                    icon = Icons.Filled.Notifications,
                    contentDescription = "Notifications",
                    onClick = onNotifications,
                )
                if (notificationCount > 0) {
                    CountBadge(
                        count = notificationCount,
                        modifier = Modifier.align(Alignment.TopEnd),
                        backgroundColor = DesignTokens.StatusColors.error,
                        textColor = Color.White,
                    )
                }
            }
        }
    }
}

/** Round translucent icon button used in the gradient header. */
@Composable
private fun HeaderIconButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        shape = CircleShape,
        color = Color.White.copy(alpha = 0.18f),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = Color.White,
            modifier = Modifier
                .padding(9.dp)
                .size(DesignTokens.IconSize.navigation),
        )
    }
}

/** Asset-free dot grid used as a faint "world map" texture behind the header. */
@Composable
private fun WorldMapDotGrid(
    modifier: Modifier = Modifier,
    dotColor: Color = Color.White.copy(alpha = 0.1f),
) {
    Canvas(modifier = modifier) {
        val step = 18.dp.toPx()
        val radius = 1.4.dp.toPx()
        var y = step / 2f
        while (y < size.height) {
            var x = step / 2f
            while (x < size.width) {
                drawCircle(color = dotColor, radius = radius, center = Offset(x, y))
                x += step
            }
            y += step
        }
    }
}

// =============================================================================
// Action Required card
// =============================================================================

/**
 * White card with a red top accent surfacing items that need the user's attention.
 *
 * Shows "₹X in N transactions awaiting voucher submission" composed from the
 * [ActionRequiredBanner], a tonal "Take Action" button, and a single-page [DotsIndicator]
 * footer hinting that more alerts could exist.
 */
@Composable
fun ActionRequiredCard(
    banner: ActionRequiredBanner,
    onTakeAction: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = DesignTokens.Shape.roundedMd,
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = DesignTokens.Elevation.card,
        shadowElevation = DesignTokens.Elevation.card,
    ) {
        Column {
            // Red top accent strip.
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .background(DesignTokens.StatusColors.error),
            )
            Column(modifier = Modifier.padding(DesignTokens.Spacing.l)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.s),
                ) {
                    Icon(
                        imageVector = Icons.Filled.PriorityHigh,
                        contentDescription = null,
                        tint = DesignTokens.StatusColors.error,
                        modifier = Modifier.size(DesignTokens.IconSize.badge),
                    )
                    Text(
                        text = "Action Required",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }

                Spacer(Modifier.height(DesignTokens.Spacing.s))

                Text(
                    text = "${banner.amountText} in ${banner.count} transactions " +
                        "awaiting voucher submission",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )

                Spacer(Modifier.height(DesignTokens.Spacing.m))

                Button(
                    onClick = onTakeAction,
                    modifier = Modifier.fillMaxWidth(),
                    shape = DesignTokens.Shape.roundedSm,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                        contentColor = MaterialTheme.colorScheme.primary,
                    ),
                ) {
                    Text(
                        text = "Take Action",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
        }
    }
}

// =============================================================================
// Quick Actions
// =============================================================================

/** A single quick-action: label + the icon shown inside its tonal tile. */
data class QuickAction(
    val label: String,
    val icon: ImageVector,
    val onClick: () -> Unit,
)

/** Four evenly distributed tonal icon tiles. */
@Composable
fun QuickActionsRow(
    actions: List<QuickAction>,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        actions.forEach { action ->
            QuickActionTile(
                action = action,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun QuickActionTile(
    action: QuickAction,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.s),
    ) {
        Surface(
            onClick = action.onClick,
            shape = DesignTokens.Shape.actionTile,
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
            modifier = Modifier.size(DesignTokens.ActionTileSize.defaultContainer),
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = action.icon,
                    contentDescription = action.label,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(DesignTokens.IconSize.actionTile),
                )
            }
        }
        Text(
            text = action.label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            modifier = Modifier.width(DesignTokens.ActionTileSize.defaultWidth),
        )
    }
}

/**
 * Builds the four canonical quick actions. Only "Add Expense" is wired to a real entry
 * point ([onAddExpense], which the integrator maps to log-miles); the rest are illustrative
 * no-ops in this offline demo.
 */
fun quickActions(
    onAddExpense: () -> Unit,
    onIllustrative: () -> Unit,
): List<QuickAction> = listOf(
    QuickAction("Add Expense", Icons.Filled.NoteAdd, onAddExpense),
    QuickAction("Create Voucher", Icons.Filled.CreditCard, onIllustrative),
    QuickAction("Ask Advance", Icons.Filled.RequestQuote, onIllustrative),
    QuickAction("Add Invoice", Icons.Filled.AttachFile, onIllustrative),
)

// =============================================================================
// Mileage carousel
// =============================================================================

/**
 * One page of the home feature carousel. [primary] cards render the dark "Start ›" pill
 * and a stronger tint; non-primary cards are illustrative.
 */
data class FeatureCarouselCard(
    val title: String,
    val subtitle: String,
    val accent: Color,
    val icon: ImageVector,
    val primary: Boolean,
    val onAction: () -> Unit,
)

/** Builds the four feature carousel cards. Card 1 (Mileage) is the only primary one. */
fun featureCarouselCards(
    onStartTracking: () -> Unit,
    onIllustrative: () -> Unit,
): List<FeatureCarouselCard> = listOf(
    FeatureCarouselCard(
        title = "Mileage",
        subtitle = "Get reimbursed for fuel by your miles travelled",
        accent = DesignTokens.StatusColors.success,
        icon = Icons.Filled.DirectionsCar,
        primary = true,
        onAction = onStartTracking,
    ),
    FeatureCarouselCard(
        title = "Customer Navigation",
        subtitle = "Navigate to your next customer location",
        accent = DesignTokens.StatusColors.info,
        icon = Icons.Filled.Navigation,
        primary = false,
        onAction = onIllustrative,
    ),
    FeatureCarouselCard(
        title = "Track Reportees",
        subtitle = "View team analytics and field activity",
        accent = DesignTokens.StatusColors.success,
        icon = Icons.Filled.Group,
        primary = false,
        onAction = onIllustrative,
    ),
    FeatureCarouselCard(
        title = "Center Check-In",
        subtitle = "Check in at a center near you",
        accent = DesignTokens.StatusColors.warning,
        icon = Icons.Filled.Storefront,
        primary = false,
        onAction = onIllustrative,
    ),
)

/** Fixed height for the feature carousel pages. */
val FeatureCarouselCardHeight = 150.dp

/**
 * A single feature carousel page: tinted illustrative panel, title + subtitle, and either a
 * dark "Start ›" pill (primary) or a forward chevron affordance (illustrative).
 */
@Composable
fun FeatureCarouselCardView(
    card: FeatureCarouselCard,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(FeatureCarouselCardHeight),
        shape = DesignTokens.Shape.carouselCard,
        color = card.accent.copy(alpha = 0.14f),
    ) {
        Box {
            // Large translucent icon "art" anchored to the right edge.
            Icon(
                imageVector = card.icon,
                contentDescription = null,
                tint = card.accent.copy(alpha = 0.22f),
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = DesignTokens.Spacing.s)
                    .size(112.dp),
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(DesignTokens.Spacing.l),
                verticalArrangement = Arrangement.SpaceBetween,
            ) {
                Column {
                    // Accent badge icon.
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(card.accent),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = card.icon,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(DesignTokens.IconSize.header),
                        )
                    }
                    Spacer(Modifier.height(DesignTokens.Spacing.s))
                    Text(
                        text = card.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = card.subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.fillMaxWidth(0.7f),
                    )
                }

                if (card.primary) {
                    StartPill(onClick = card.onAction)
                } else {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = card.title,
                        tint = card.accent,
                        modifier = Modifier
                            .align(Alignment.End)
                            .size(DesignTokens.IconSize.header),
                    )
                }
            }
        }
    }
}

/** Dark rounded "Start ›" call-to-action pill used on the primary mileage card. */
@Composable
private fun StartPill(onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(50),
        color = MaterialTheme.colorScheme.onSurface,
    ) {
        Row(
            modifier = Modifier.padding(
                horizontal = DesignTokens.Spacing.l,
                vertical = DesignTokens.Spacing.s,
            ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.xs),
        ) {
            Text(
                text = "Start",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.surface,
            )
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.surface,
                modifier = Modifier.size(DesignTokens.IconSize.inline),
            )
        }
    }
}

// =============================================================================
// At A Glance
// =============================================================================

/** One "At A Glance" row: a big count, a title, a subtitle and a trailing chevron. */
data class AtAGlanceRow(
    val count: Int,
    val title: String,
    val subtitle: String,
)

/** Maps the [AtAGlanceCounts] into the three rows the section renders, in order. */
fun atAGlanceRows(counts: AtAGlanceCounts): List<AtAGlanceRow> = listOf(
    AtAGlanceRow(
        count = counts.unreportedTransactions,
        title = "Unreported Transactions",
        subtitle = "expenses & vouchers this month",
    ),
    AtAGlanceRow(
        count = counts.upcomingTrips,
        title = "Upcoming Trips",
        subtitle = "scheduled in the coming weeks",
    ),
    AtAGlanceRow(
        count = counts.pendingInvoices,
        title = "Pending Invoices",
        subtitle = "awaiting processing",
    ),
)

/** A single tappable "At A Glance" row. */
@Composable
fun AtAGlanceRowView(
    row: AtAGlanceRow,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .clip(DesignTokens.Shape.roundedSm)
            .clickable(onClick = onClick)
            .fillMaxWidth()
            .padding(vertical = DesignTokens.Spacing.m),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.l),
    ) {
        Text(
            text = row.count.toString(),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.width(56.dp),
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = row.title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = row.subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(DesignTokens.IconSize.navigation),
        )
    }
}

// =============================================================================
// Marketing strip
// =============================================================================

/** Width of one marketing/benefits card in the horizontal strip. */
val MarketingCardWidth = 260.dp

/** A single static marketing/benefits card. */
@Composable
fun MarketingCardView(
    item: MarketingCarouselItem,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier
            .width(MarketingCardWidth)
            .height(132.dp),
        shape = DesignTokens.Shape.carouselCard,
        color = MaterialTheme.colorScheme.primaryContainer,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(DesignTokens.Spacing.l),
            verticalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.s),
        ) {
            Surface(
                shape = DesignTokens.Shape.chip,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.18f),
            ) {
                Text(
                    text = item.badge,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(
                        horizontal = DesignTokens.Spacing.s,
                        vertical = 2.dp,
                    ),
                )
            }
            Text(
                text = item.title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = item.subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

/** Shared section heading used between the home sections. */
@Composable
fun HomeSectionHeader(
    title: String,
    leadingIcon: ImageVector,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.s),
    ) {
        Icon(
            imageVector = leadingIcon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(DesignTokens.IconSize.header),
        )
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

// =============================================================================
// Phase O — Animated Banner Strip
// =============================================================================

private data class BannerSpec(
    val icon: ImageVector,
    val text: String,
    val color: Color,
    val dismissible: Boolean = false,
)

@Composable
fun AnimatedBannerStrip(
    isTrackingActive: Boolean = false,
    modifier: Modifier = Modifier,
) {
    val banners = remember(isTrackingActive) {
        buildList {
            if (isTrackingActive) add(BannerSpec(Icons.Filled.RadioButtonChecked, "Tracking active · 4.2 km · 00:18:32", Color(0xFF00695C)))
            add(BannerSpec(Icons.Filled.NotificationImportant, "3 items need your attention", Color(0xFFF57C00)))
            add(BannerSpec(Icons.Filled.Info, "Submit your Oct expenses before 31st Nov.", Color(0xFF1565C0), dismissible = true))
        }
    }
    var currentIndex by remember { mutableIntStateOf(0) }
    var dismissed by remember { mutableStateOf(false) }

    LaunchedEffect(banners.size) {
        while (true) {
            delay(4000L)
            currentIndex = (currentIndex + 1) % banners.size
        }
    }

    if (!dismissed && banners.isNotEmpty()) {
        val banner = banners[currentIndex % banners.size]
        AnimatedVisibility(
            visible = true,
            enter = fadeIn() + slideInVertically { -it },
            exit = fadeOut() + slideOutVertically { -it },
            modifier = modifier.fillMaxWidth(),
        ) {
            Surface(
                color = banner.color.copy(alpha = 0.12f),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = DesignTokens.Spacing.l, vertical = DesignTokens.Spacing.s),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.s),
                ) {
                    Icon(
                        imageVector = banner.icon,
                        contentDescription = null,
                        tint = banner.color,
                        modifier = Modifier.size(20.dp),
                    )
                    Text(
                        text = banner.text,
                        style = MaterialTheme.typography.bodySmall,
                        color = banner.color,
                        modifier = Modifier.weight(1f),
                    )
                    if (banner.dismissible) {
                        IconButton(onClick = { dismissed = true }, modifier = Modifier.size(28.dp)) {
                            Icon(Icons.Filled.Close, contentDescription = "Dismiss", tint = banner.color, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }
    }
}

// =============================================================================
// Phase O — At A Glance 2×2 grid
// =============================================================================

private data class GlanceCell(val count: Int, val label: String, val color: Color)

@Composable
fun AtAGlanceGrid(
    counts: AtAGlanceCounts,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val cells = listOf(
        GlanceCell(counts.pendingExpenses, "Pending Expenses", Color(0xFFF57C00)),
        GlanceCell(counts.upcomingTrips, "Upcoming Trips", Color(0xFF1976D2)),
        GlanceCell(counts.pendingApprovals, "Pending Approvals", Color(0xFFE53935)),
        GlanceCell(counts.unreadNotifications, "Unread Notifications", Color(0xFF7B1FA2)),
    )
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.s)) {
        Row(horizontalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.s), modifier = Modifier.fillMaxWidth()) {
            GlanceCell(cells[0], onClick, Modifier.weight(1f))
            GlanceCell(cells[1], onClick, Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.s), modifier = Modifier.fillMaxWidth()) {
            GlanceCell(cells[2], onClick, Modifier.weight(1f))
            GlanceCell(cells[3], onClick, Modifier.weight(1f))
        }
    }
}

@Composable
private fun GlanceCell(cell: GlanceCell, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = DesignTokens.Shape.roundedMd,
        color = cell.color.copy(alpha = 0.08f),
        border = BorderStroke(1.dp, cell.color.copy(alpha = 0.2f)),
    ) {
        Column(
            modifier = Modifier.padding(DesignTokens.Spacing.m),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(cell.count.toString(), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = cell.color)
            Text(cell.label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2)
        }
    }
}

// =============================================================================
// Phase O — My Cards carousel
// =============================================================================

private data class MockCard(val label: String, val balance: String, val last4: String, val gradient: Brush)

private val MOCK_CARDS = listOf(
    MockCard("PETTY CASH", "₹2,400", "QR1", Brush.linearGradient(listOf(Color(0xFF00695C), Color(0xFF00ACC1)))),
    MockCard("CORPORATE", "₹48,000", "4821", Brush.linearGradient(listOf(Color(0xFF1A237E), Color(0xFF283593)))),
)

@Composable
fun MyCardsSection(
    onSnackbar: suspend () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scope = rememberCoroutineScope()
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.m)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            HomeSectionHeader("My Cards", Icons.Filled.CreditCard)
            TextButton(onClick = { scope.launch { onSnackbar() } }) {
                Text("Request Card", style = MaterialTheme.typography.labelMedium)
            }
        }
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.m),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 2.dp),
        ) {
            items(MOCK_CARDS) { card ->
                MockCardView(card = card, onAction = { scope.launch { onSnackbar() } })
            }
        }
    }
}

@Composable
private fun MockCardView(card: MockCard, onAction: () -> Unit) {
    Box(
        modifier = Modifier
            .width(260.dp)
            .height(140.dp)
            .clip(DesignTokens.Shape.roundedLg)
            .background(card.gradient)
            .padding(DesignTokens.Spacing.l),
    ) {
        Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.SpaceBetween) {
            Column {
                Text(card.label, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = Color.White)
                Text("**** ${card.last4}", style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.8f))
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Column {
                    Text("Balance", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.7f))
                    Text(card.balance, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.White)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Surface(onClick = onAction, shape = RoundedCornerShape(20.dp), color = Color.White.copy(alpha = 0.2f)) {
                        Text("Pay", style = MaterialTheme.typography.labelSmall, color = Color.White, modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp))
                    }
                    Surface(onClick = onAction, shape = RoundedCornerShape(20.dp), color = Color.Transparent, border = BorderStroke(1.dp, Color.White.copy(alpha = 0.6f))) {
                        Text("Request", style = MaterialTheme.typography.labelSmall, color = Color.White, modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp))
                    }
                }
            }
        }
    }
}

// =============================================================================
// Phase O — Recent Activity feed
// =============================================================================

private enum class ActivityStatus { APPROVED, PENDING, SUBMITTED, REJECTED }
private data class ActivityItem(val id: String, val title: String, val amount: String, val status: ActivityStatus, val date: String)

private val RECENT_ACTIVITIES = listOf(
    ActivityItem("EXP-001", "Business dinner", "₹3,200", ActivityStatus.APPROVED, "Today"),
    ActivityItem("EXP-002", "Office supplies", "₹680", ActivityStatus.PENDING, "Today"),
    ActivityItem("MI-001", "Client visit · 48 km", "₹576", ActivityStatus.SUBMITTED, "Yesterday"),
    ActivityItem("ADV-001", "Field advance", "₹5,000", ActivityStatus.APPROVED, "Yesterday"),
    ActivityItem("INV-001", "Vendor invoice", "₹12,000", ActivityStatus.PENDING, "2 days ago"),
)

@Composable
fun RecentActivitySection(
    onSnackbar: suspend () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scope = rememberCoroutineScope()
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.xs)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            HomeSectionHeader("Recent Activity", Icons.Filled.CheckCircle)
            TextButton(onClick = { scope.launch { onSnackbar() } }) {
                Text("View All", style = MaterialTheme.typography.labelMedium)
            }
        }
        RECENT_ACTIVITIES.forEachIndexed { index, item ->
            ActivityRow(item = item, onClick = { scope.launch { onSnackbar() } })
            if (index < RECENT_ACTIVITIES.lastIndex) {
                HorizontalDivider(modifier = Modifier.padding(start = 48.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            }
        }
    }
}

@Composable
private fun ActivityRow(item: ActivityItem, onClick: () -> Unit) {
    val statusColor = when (item.status) {
        ActivityStatus.APPROVED -> Color(0xFF2E7D32)
        ActivityStatus.PENDING -> Color(0xFFF57C00)
        ActivityStatus.SUBMITTED -> Color(0xFF1565C0)
        ActivityStatus.REJECTED -> Color(0xFFC62828)
    }
    val statusLabel = when (item.status) {
        ActivityStatus.APPROVED -> "Approved"
        ActivityStatus.PENDING -> "Pending"
        ActivityStatus.SUBMITTED -> "Submitted"
        ActivityStatus.REJECTED -> "Rejected"
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = DesignTokens.Spacing.s),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.m),
    ) {
        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(statusColor))
        Column(modifier = Modifier.weight(1f)) {
            Text(item.title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
            Text("${item.id} · ${item.date}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(item.amount, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
            Surface(shape = DesignTokens.Shape.chip, color = statusColor.copy(alpha = 0.12f)) {
                Text(statusLabel, style = MaterialTheme.typography.labelSmall, color = statusColor, modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp))
            }
        }
    }
}

@Composable
private fun TextButton(onClick: () -> Unit, content: @Composable () -> Unit) {
    androidx.compose.material3.TextButton(onClick = onClick) { content() }
}
