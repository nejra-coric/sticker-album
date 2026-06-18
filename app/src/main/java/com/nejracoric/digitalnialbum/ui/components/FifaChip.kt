package com.nejracoric.digitalnialbum.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.nejracoric.digitalnialbum.ui.theme.FifaGreen
import com.nejracoric.digitalnialbum.ui.theme.FifaNavy
import com.nejracoric.digitalnialbum.ui.theme.FifaNavyCard

@Composable
fun FifaChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = {
            Text(
                label.uppercase(),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
            )
        },
        modifier = modifier.padding(vertical = 2.dp),
        colors = FilterChipDefaults.filterChipColors(
            containerColor = FifaNavyCard,
            labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
            selectedContainerColor = FifaGreen,
            selectedLabelColor = FifaNavy
        )
    )
}
