package com.nejracoric.digitalnialbum.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import com.nejracoric.digitalnialbum.ui.theme.GoldAccent
import com.nejracoric.digitalnialbum.ui.theme.NeonCyan
import com.nejracoric.digitalnialbum.ui.theme.TextWhite

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTopBar(
    title: String,
    showBack: Boolean,
    onBack: () -> Unit,
    actionIcon: ImageVector? = null,
    onAction: (() -> Unit)? = null
) {
    TopAppBar(
        title = { Text(title, fontWeight = FontWeight.Bold) },
        navigationIcon = {
            if (showBack) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                }
            }
        },
        actions = {
            if (actionIcon != null && onAction != null) {
                IconButton(onClick = onAction) {
                    Icon(actionIcon, contentDescription = null, tint = GoldAccent)
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color(0x3300E5FF),
            titleContentColor = TextWhite,
            navigationIconContentColor = NeonCyan
        )
    )
}
