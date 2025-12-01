package com.miletracker.ui.toast

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ke.don.koffee.domain.style
import ke.don.koffee.model.ToastAction
import ke.don.koffee.model.ToastData

@Composable
fun AppToast(data: ToastData) {
    val (icon, tint) = data.type.style

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = tint,
                    modifier = Modifier.size(32.dp).padding(end = 12.dp),
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = data.title,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = data.description,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            if (data.primaryAction != null || data.secondaryAction != null) {
                Spacer(modifier = Modifier.height(12.dp))
                AppToastActionRow(
                    modifier = Modifier.align(Alignment.End),
                    primaryAction = data.primaryAction,
                    secondaryAction = data.secondaryAction,
                    tint = tint,
                )
            }
        }
    }
}

@Composable
private fun AppToastActionRow(
    modifier: Modifier = Modifier,
    primaryAction: ToastAction?,
    secondaryAction: ToastAction?,
    tint: Color,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        secondaryAction?.let { action ->
            TextButton(
                onClick = action.onClick,
                colors = ButtonDefaults.textButtonColors(contentColor = tint)
            ) {
                Text(text = action.label.uppercase(), style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
            }
        }
        primaryAction?.let { action ->
            Button(
                onClick = action.onClick,
                colors = ButtonDefaults.buttonColors(containerColor = tint)
            ) {
                Text(text = action.label.uppercase(), style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
            }
        }
    }
}
