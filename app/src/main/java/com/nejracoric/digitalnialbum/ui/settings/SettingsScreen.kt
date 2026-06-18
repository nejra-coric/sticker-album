package com.nejracoric.digitalnialbum.ui.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nejracoric.digitalnialbum.DigitalAlbumApp
import com.nejracoric.digitalnialbum.data.model.ListLayout
import com.nejracoric.digitalnialbum.ui.components.AppTopBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    viewModel: SettingsViewModel = viewModel(
        factory = SettingsViewModelFactory(
            (androidx.compose.ui.platform.LocalContext.current.applicationContext as DigitalAlbumApp).preferences
        )
    )
) {
    val layout by viewModel.layout.collectAsState()
    val team by viewModel.team.collectAsState()
    val teams = listOf(
        "Sve", "BiH", "Hrvatska", "Argentina", "Brazil",
        "Njemačka", "Francuska", "Engleska", "Portugal", "Turska"
    )
    var expanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = { AppTopBar(title = "Postavke", showBack = true, onBack = onBack) }
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            Text("Prikaz albuma", style = MaterialTheme.typography.titleMedium)
            ListLayout.entries.forEach { mode ->
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = layout == mode,
                        onClick = { viewModel.setLayout(mode) }
                    )
                    Text(
                        if (mode == ListLayout.LISTA) "Lista" else "Mreža",
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }
            }
            Text(
                "Omiljeni tim",
                style = MaterialTheme.typography.titleMedium,
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
