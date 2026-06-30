package com.nejracoric.digitalnialbum.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.nejracoric.digitalnialbum.ui.theme.DarkBlueBackground
import com.nejracoric.digitalnialbum.ui.theme.GlassFill
import com.nejracoric.digitalnialbum.ui.theme.GoldAccent
import com.nejracoric.digitalnialbum.ui.theme.NeonCyan
import com.nejracoric.digitalnialbum.ui.theme.TextGray
import com.nejracoric.digitalnialbum.ui.theme.TextWhite

@Composable
fun GlassChip(
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
            containerColor = GlassFill,
            labelColor = TextGray,
            selectedContainerColor = NeonCyan,
            selectedLabelColor = DarkBlueBackground
        ),
        border = FilterChipDefaults.filterChipBorder(
            enabled = true,
            selected = selected,
            borderColor = Color(0x4400E5FF),
            selectedBorderColor = GoldAccent.copy(0.8f)
        )
    )
}
