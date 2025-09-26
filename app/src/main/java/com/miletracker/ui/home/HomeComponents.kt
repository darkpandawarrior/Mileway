package com.miletracker.ui.home

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
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
            dotColor = Color.White.copy(alpha = 0.10f),
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .height(HeaderContentHeight)
                .padding(horizontal = DesignTokens.Spacing.l),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.m),
        ) {
            // Avatar circle (initial of the greeting name).
            Box(
                modifier = Modifier
                    .size(AvatarSize)
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
