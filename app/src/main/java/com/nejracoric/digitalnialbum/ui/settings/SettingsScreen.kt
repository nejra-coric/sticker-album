package com.nejracoric.digitalnialbum.ui.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nejracoric.digitalnialbum.DigitalAlbumApp
import com.nejracoric.digitalnialbum.data.model.ListLayout
import com.nejracoric.digitalnialbum.ui.components.AppTopBar
import com.nejracoric.digitalnialbum.ui.components.GlassCard
import com.nejracoric.digitalnialbum.ui.theme.GoldAccent
import com.nejracoric.digitalnialbum.ui.theme.NeonCyan
import com.nejracoric.digitalnialbum.ui.theme.TextGray
import com.nejracoric.digitalnialbum.ui.theme.TextWhite
import com.nejracoric.digitalnialbum.util.ShareUtil

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    showBack: Boolean = true,
    title: String = "Postavke",
    onMemory: (() -> Unit)? = null,
    onTrade: (() -> Unit)? = null,
    viewModel: SettingsViewModel = viewModel(
        factory = SettingsViewModelFactory(
            (androidx.compose.ui.platform.LocalContext.current.applicationContext as DigitalAlbumApp).preferences
        )
    )
) {
    val context = LocalContext.current
    val app = context.applicationContext as DigitalAlbumApp
    val stickers by app.repository.stickers.collectAsState(initial = emptyList())
    val points by app.preferences.points.collectAsState(initial = 0f)
    val missingCount = remember(stickers) { stickers.count { !it.owned } }
    val duplicateCount = remember(stickers) { stickers.count { it.ownedCount > 1 } }

    val layout by viewModel.layout.collectAsState()
    val team by viewModel.team.collectAsState()
    val teams = listOf(
        "Sve", "BiH", "Hrvatska", "Argentina", "Brazil",
        "Njemačka", "Francuska", "Engleska", "Portugal", "Turska"
    )
    var expanded by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = androidx.compose.ui.graphics.Color.Transparent,
        topBar = { AppTopBar(title = title, showBack = showBack, onBack = onBack) }
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            Text(
                "Poeni: ${"%.1f".format(points).trimEnd('0').trimEnd('.')}",
                color = GoldAccent,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            if (onMemory != null || onTrade != null) {
                Text(
                    "Igrice",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextWhite,
                    fontWeight = FontWeight.Bold
                )
                if (onMemory != null) {
                    GlassCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 10.dp),
                        corner = 16.dp,
                        golden = true,
                        onClick = onMemory
                    ) {
                        ProfileRow(
                            title = "Memory",
                            subtitle = "Leveli · zaradi poene za paketiće",
                            trailing = null
                        )
                    }
                }
                if (onTrade != null) {
                    GlassCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 10.dp),
                        corner = 16.dp,
                        onClick = onTrade
                    ) {
                        ProfileRow(
                            title = "Trade",
                            subtitle = "Zamijeni duplikat za nedostajuću (+poeni)",
                            trailing = null
                        )
                    }
                }
            }

            Text(
                "Podijeli album",
                style = MaterialTheme.typography.titleMedium,
                color = TextWhite,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 28.dp)
            )
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp),
                corner = 16.dp,
                onClick = { ShareUtil.shareMissingList(context, stickers) }
            ) {
                ProfileRow(
                    title = "Lista nedostajućih",
                    subtitle = if (missingCount > 0) "$missingCount sličica" else "Album je kompletan",
                    enabled = missingCount > 0,
                    leading = Icons.Default.Share,
                    trailing = "WhatsApp / Viber"
                )
            }
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp),
                corner = 16.dp,
                onClick = { ShareUtil.shareDuplicatesList(context, stickers) }
            ) {
                ProfileRow(
                    title = "Moji duplikati",
                    subtitle = if (duplicateCount > 0) "$duplicateCount za zamjenu" else "Nema duplikata",
                    enabled = duplicateCount > 0,
                    leading = Icons.Default.Share,
                    trailing = "WhatsApp / Viber"
                )
            }

            Text(
                "Prikaz albuma",
                style = MaterialTheme.typography.titleMedium,
                color = TextWhite,
                modifier = Modifier.padding(top = 28.dp)
            )
            ListLayout.entries.forEach { mode ->
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = layout == mode,
                        onClick = { viewModel.setLayout(mode) }
                    )
                    Text(
                        if (mode == ListLayout.LISTA) "Lista" else "Mreža",
                        color = TextWhite,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }
            }
            Text(
                "Omiljeni tim",
                style = MaterialTheme.typography.titleMedium,
                color = TextWhite,
                modifier = Modifier.padding(top = 24.dp)
            )
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            ) {
                OutlinedTextField(
                    value = team,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Tim") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                )
                ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    teams.forEach { t ->
                        DropdownMenuItem(
                            text = { Text(t) },
                            onClick = {
                                viewModel.setTeam(t)
                                expanded = false
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ProfileRow(
    title: String,
    subtitle: String,
    enabled: Boolean = true,
    leading: ImageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
    trailing: String? = null
) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            leading,
            contentDescription = null,
            tint = if (enabled) GoldAccent else TextGray,
            modifier = Modifier.size(22.dp)
        )
        Column(Modifier.padding(start = 14.dp).weight(1f)) {
            Text(
                title,
                color = if (enabled) TextWhite else TextGray,
                fontWeight = FontWeight.SemiBold,
                style = MaterialTheme.typography.titleSmall
            )
            Text(subtitle, color = TextGray, style = MaterialTheme.typography.bodySmall)
        }
        if (trailing != null) {
            Text(
                trailing,
                color = if (enabled) NeonCyan else TextGray,
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.padding(start = 8.dp)
            )
        }
    }
}
