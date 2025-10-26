package com.miletracker.feature.travel.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AirplanemodeActive
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.DirectionsRailway
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.miletracker.core.ui.theme.DesignTokens
import com.miletracker.feature.travel.model.BookingRecord
import com.miletracker.feature.travel.model.TransportMode
import com.miletracker.feature.travel.repository.TravelRepository
import com.miletracker.stub.AnalyticsMockData
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val TealGradient = Brush.horizontalGradient(
    colors = listOf(Color(0xFF00695C), Color(0xFF00BCD4))
)

@Composable
fun TravelHomeScreen(
    repository: TravelRepository = koinInject()
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val activeBooking = repository.activeBooking()
    val upcoming = repository.upcomingBookings()
    val totalSpend = AnalyticsMockData.categoryTotals["Travel"] ?: 0.0

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            item {
                TravelHeader()
            }
            item {
                SummaryStrip(
                    activeTripCount = if (activeBooking != null) 1 else 0,
                    upcomingCount = upcoming.size,
                    totalSpend = totalSpend
                )
            }
            if (activeBooking != null) {
                item {
                    Spacer(Modifier.height(DesignTokens.Spacing.m))
                    ActiveTripCard(
                        booking = activeBooking,
                        onViewBoardingPass = {
                            scope.launch {
                                snackbarHostState.showSnackbar("Boarding pass not available in demo")
                            }
                        },
                        modifier = Modifier.padding(horizontal = DesignTokens.Spacing.l)
                    )
                }
            }
            item {
                Spacer(Modifier.height(DesignTokens.Spacing.m))
                Text(
                    text = "Upcoming Bookings",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(horizontal = DesignTokens.Spacing.l)
                )
                Spacer(Modifier.height(DesignTokens.Spacing.s))
            }
            items(upcoming.size) { index ->
                UpcomingBookingCard(
                    booking = upcoming[index],
                    modifier = Modifier.padding(horizontal = DesignTokens.Spacing.l)
                )
                if (index < upcoming.size - 1) {
                    Spacer(Modifier.height(DesignTokens.Spacing.s))
                }
            }
            item {
                Spacer(Modifier.height(DesignTokens.Spacing.m))
                QuickActionsRow(
                    onBookFlight = {
                        scope.launch { snackbarHostState.showSnackbar("Booking is illustrative.") }
                    },
                    onBookTrain = {
                        scope.launch { snackbarHostState.showSnackbar("Booking is illustrative.") }
                    },
                    modifier = Modifier.padding(horizontal = DesignTokens.Spacing.l)
                )
            }
            item {
                Spacer(Modifier.height(DesignTokens.Spacing.m))
                TravelPolicyCard(
                    modifier = Modifier.padding(horizontal = DesignTokens.Spacing.l)
                )
                Spacer(Modifier.height(DesignTokens.Spacing.xl))
            }
        }
    }
}

@Composable
private fun TravelHeader() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(TealGradient)
            .windowInsetsPadding(WindowInsets.statusBars)
            .padding(horizontal = DesignTokens.Spacing.l, vertical = DesignTokens.Spacing.xl)
    ) {
        Column {
            Text(
                text = "Travel",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = "Bookings & trips",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.85f)
            )
        }
    }
}

@Composable
private fun SummaryStrip(
    activeTripCount: Int,
    upcomingCount: Int,
    totalSpend: Double
) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = DesignTokens.Spacing.m),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            MetricItem(label = "Active Trips", value = activeTripCount.toString())
            VerticalDivider()
            MetricItem(label = "Upcoming", value = upcomingCount.toString())
            VerticalDivider()
            MetricItem(label = "Total Spend", value = "₹${"%,.0f".format(totalSpend)}")
        }
    }
}

@Composable
private fun MetricItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun VerticalDivider() {
    HorizontalDivider(
        modifier = Modifier.size(width = 1.dp, height = 36.dp),
        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
    )
}

@Composable
private fun ActiveTripCard(
    booking: BookingRecord,
    onViewBoardingPass: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        shape = DesignTokens.Shape.roundedMd
    ) {
        Column(modifier = Modifier.padding(DesignTokens.Spacing.l)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Active Trip",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                )
                SuggestionChip(
                    onClick = {},
                    label = {
                        Text(
                            text = "ON TIME",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    colors = SuggestionChipDefaults.suggestionChipColors(
                        containerColor = Color(0xFF4CAF50),
                        labelColor = Color.White
                    ),
                    border = null
                )
            }
            Spacer(Modifier.height(DesignTokens.Spacing.s))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = transportIcon(booking.mode),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.size(DesignTokens.Spacing.s))
                Text(
                    text = "${booking.origin} → ${booking.destination}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            Spacer(Modifier.height(DesignTokens.Spacing.xs))
            Text(
                text = "${booking.carrier} ${booking.flightOrTrainNumber}" +
                        (if (booking.boardingTime != null) " · Boarding ${booking.boardingTime}" else "") +
                        (if (booking.gate != null) " · Gate ${booking.gate}" else ""),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
            )
            Spacer(Modifier.height(DesignTokens.Spacing.l))
            OutlinedButton(
                onClick = onViewBoardingPass,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("View Boarding Pass")
            }
        }
    }
}

@Composable
private fun UpcomingBookingCard(
    booking: BookingRecord,
    modifier: Modifier = Modifier
) {
    val dateFormatter = remember { SimpleDateFormat("d MMM", Locale.getDefault()) }
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = DesignTokens.Shape.roundedMd
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(DesignTokens.Spacing.l),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.m)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        MaterialTheme.colorScheme.secondaryContainer,
                        RoundedCornerShape(8.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = transportIcon(booking.mode),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.size(22.dp)
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${booking.origin} → ${booking.destination}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "${booking.carrier} · ${dateFormatter.format(Date(booking.departureMs))}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Surface(
                color = MaterialTheme.colorScheme.tertiaryContainer,
                shape = RoundedCornerShape(6.dp)
            ) {
                Text(
                    text = "₹${"%.0f".format(booking.amountRupees)}",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onTertiaryContainer,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}

@Composable
private fun QuickActionsRow(
    onBookFlight: () -> Unit,
    onBookTrain: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.m)
    ) {
        OutlinedButton(
            onClick = onBookFlight,
            modifier = Modifier.weight(1f)
        ) {
            Icon(Icons.Filled.AirplanemodeActive, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.size(6.dp))
            Text("Book Flight")
        }
        OutlinedButton(
            onClick = onBookTrain,
            modifier = Modifier.weight(1f)
        ) {
            Icon(Icons.Filled.DirectionsRailway, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.size(6.dp))
            Text("Book Train")
        }
    }
}

@Composable
private fun TravelPolicyCard(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = DesignTokens.Shape.roundedMd
    ) {
        Row(
            modifier = Modifier.padding(DesignTokens.Spacing.l),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.s)
        ) {
            Icon(
                imageVector = Icons.Filled.Info,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = "Bookings > ₹10,000 require pre-approval. Submit via the Approvals tab.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private fun transportIcon(mode: TransportMode): ImageVector = when (mode) {
    TransportMode.FLIGHT -> Icons.Filled.AirplanemodeActive
    TransportMode.TRAIN -> Icons.Filled.DirectionsRailway
    TransportMode.BUS -> Icons.Filled.DirectionsBus
    TransportMode.CAB -> Icons.Filled.DirectionsBus
}
