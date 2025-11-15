package com.miletracker.feature.approvals.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AirplanemodeActive
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.MoneyOff
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.miletracker.feature.approvals.model.ApprovalItem
import com.miletracker.feature.approvals.model.ApprovalStatus
import com.miletracker.feature.approvals.model.ApprovalType
import com.miletracker.feature.approvals.repository.ApprovalsRepository
import com.miletracker.feature.approvals.viewmodel.ApprovalsViewModel
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ApprovalsScreen(
    onOpenDetail: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ApprovalsViewModel = koinViewModel()
) {
    val state by viewModel.listState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var selectedTab by remember { mutableIntStateOf(0) }
    var selectionMode by rememberSaveable { mutableStateOf(false) }
    val selectedIds = remember { mutableStateOf(setOf<String>()) }

    LaunchedEffect(state.snackbarMessage) {
        state.snackbarMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearSnackbar()
        }
    }

    val pendingItems = state.items.filter { it.status == ApprovalStatus.PENDING }
    val pendingCount = pendingItems.size

    Scaffold(
        modifier = modifier,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            ApprovalsGradientHeader(
                pendingCount = pendingCount,
                selectionMode = selectionMode,
                onCancelSelection = { selectionMode = false; selectedIds.value = emptySet() }
            )
        },
        bottomBar = {
            if (selectionMode && selectedTab == 0) {
                Surface(
                    tonalElevation = 4.dp,
                    shadowElevation = 4.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = { scope.launch { snackbarHostState.showSnackbar("Bulk action is illustrative.") }; selectionMode = false; selectedIds.value = emptySet() },
                            modifier = Modifier.weight(1f)
                        ) { Text("Reject All (${selectedIds.value.size})") }
                        Button(
                            onClick = { scope.launch { snackbarHostState.showSnackbar("Bulk action is illustrative.") }; selectionMode = false; selectedIds.value = emptySet() },
                            modifier = Modifier.weight(1f)
                        ) { Text("Approve All (${selectedIds.value.size})") }
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            PrimaryTabRow(selectedTabIndex = selectedTab) {
                listOf("TO APPROVE", "TEAM", "MY REQUESTS").forEachIndexed { idx, title ->
                    Tab(
                        selected = selectedTab == idx,
                        onClick = { selectedTab = idx; selectionMode = false; selectedIds.value = emptySet() },
                        text = { Text(title, style = MaterialTheme.typography.labelMedium) }
                    )
                }
            }
            when (selectedTab) {
                0 -> ApprovalListTab(
                    items = pendingItems,
                    onOpenDetail = onOpenDetail,
                    selectionMode = selectionMode,
                    selectedIds = selectedIds.value,
                    onLongPress = { id -> selectionMode = true; selectedIds.value = selectedIds.value + id },
                    onToggleSelect = { id -> selectedIds.value = if (id in selectedIds.value) selectedIds.value - id else selectedIds.value + id },
                )
                1 -> ApprovalListTab(
                    items = ApprovalsRepository.teamItems,
                    onOpenDetail = {},
                    selectionMode = false,
                    selectedIds = emptySet(),
                    onLongPress = {},
                    onToggleSelect = {},
                )
                2 -> ApprovalListTab(
                    items = ApprovalsRepository.myRequests,
                    onOpenDetail = {},
                    selectionMode = false,
                    selectedIds = emptySet(),
                    onLongPress = {},
                    onToggleSelect = {},
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ApprovalListTab(
    items: List<ApprovalItem>,
    onOpenDetail: (String) -> Unit,
    selectionMode: Boolean,
    selectedIds: Set<String>,
    onLongPress: (String) -> Unit,
    onToggleSelect: (String) -> Unit,
) {
    if (items.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No items", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        return
    }
    LazyColumn(
        modifier = Modifier.fillMaxSize().navigationBarsPadding(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(items, key = { it.id }) { item ->
            ApprovalCard(
                item = item,
                selectionMode = selectionMode,
                isSelected = item.id in selectedIds,
                onClick = { if (selectionMode) onToggleSelect(item.id) else onOpenDetail(item.id) },
                onLongClick = { onLongPress(item.id) },
            )
        }
    }
}

@Composable
private fun ApprovalsGradientHeader(
    pendingCount: Int,
    selectionMode: Boolean,
    onCancelSelection: () -> Unit,
) {
    val gradient = Brush.horizontalGradient(
        listOf(Color(0xFF6C63FF), Color(0xFF9C6BFF))
    )
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(gradient)
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = if (selectionMode) "Select Items" else "Approvals",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (selectionMode) {
                        TextButton(onClick = onCancelSelection) {
                            Icon(Icons.Default.Close, contentDescription = "Cancel", tint = Color.White)
                            Spacer(Modifier.width(4.dp))
                            Text("Cancel", color = Color.White)
                        }
                    } else {
                        if (pendingCount > 0) {
                            Surface(
                                shape = CircleShape,
                                color = Color(0xFFFF6B6B),
                            ) {
                                Text(
                                    text = "$pendingCount",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                )
                            }
                            Spacer(Modifier.width(8.dp))
                        }
                        IconButton(onClick = {}) {
                            Icon(Icons.Default.FilterList, contentDescription = "Filter", tint = Color.White)
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ApprovalCard(
    item: ApprovalItem,
    selectionMode: Boolean,
    isSelected: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(onClick = onClick, onLongClick = onLongClick),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (selectionMode) {
                Checkbox(
                    checked = isSelected,
                    onCheckedChange = { onClick() },
                    modifier = Modifier.size(24.dp),
                )
                Spacer(Modifier.width(8.dp))
            }
            TypeIconContainer(type = item.type)
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = item.requesterName,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.weight(1f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    StatusChip(status = item.status)
                }
                Spacer(Modifier.height(2.dp))
                Text(
                    text = item.summary,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = "₹%,.2f".format(item.amountRupees),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Text(
                        text = timeAgo(item.timestampMs),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                if (item.policyViolation) {
                    Spacer(Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Warning, contentDescription = null,
                            tint = Color(0xFFFF6B35), modifier = Modifier.size(12.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Policy violation", style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFFFF6B35))
                    }
                }
            }
        }
    }
}

@Composable
private fun TypeIconContainer(type: ApprovalType) {
    val (icon, color) = when (type) {
        ApprovalType.MILEAGE -> Icons.Default.DirectionsCar to Color(0xFF4CAF50)
        ApprovalType.EXPENSE -> Icons.Default.Receipt to Color(0xFF2196F3)
        ApprovalType.TRAVEL -> Icons.Default.AirplanemodeActive to Color(0xFF9C27B0)
        ApprovalType.ADVANCE -> Icons.Default.MoneyOff to Color(0xFFFF9800)
    }
    Box(
        modifier = Modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(color.copy(alpha = 0.15f)),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, contentDescription = type.name, tint = color, modifier = Modifier.size(22.dp))
    }
}

@Composable
private fun StatusChip(status: ApprovalStatus) {
    val (label, color) = when (status) {
        ApprovalStatus.PENDING -> "Pending" to Color(0xFFFF9800)
        ApprovalStatus.APPROVED -> "Approved" to Color(0xFF4CAF50)
        ApprovalStatus.REJECTED -> "Rejected" to Color(0xFFF44336)
    }
    Surface(
        shape = RoundedCornerShape(50),
        color = color.copy(alpha = 0.15f),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = color,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
        )
    }
}

private fun dateBucket(ms: Long): String {
    val sdf = SimpleDateFormat("EEE, dd MMM yyyy", Locale.getDefault())
    return sdf.format(Date(ms))
}

private fun timeAgo(ms: Long): String {
    val diff = System.currentTimeMillis() - ms
    val hours = diff / 3_600_000
    return when {
        hours < 1 -> "Just now"
        hours < 24 -> "${hours}h ago"
        else -> "${hours / 24}d ago"
    }
}
