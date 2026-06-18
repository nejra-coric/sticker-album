package com.nejracoric.digitalnialbum.ui.favorites

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.nejracoric.digitalnialbum.DigitalAlbumApp
import com.nejracoric.digitalnialbum.ui.components.AppTopBar
import com.nejracoric.digitalnialbum.ui.components.FifaBackground
import com.nejracoric.digitalnialbum.ui.components.StickerImage
import com.nejracoric.digitalnialbum.ui.theme.FifaGray
import com.nejracoric.digitalnialbum.ui.theme.FifaNavyCard

@Composable
fun FavoritesScreen(
    onBack: () -> Unit,
    onOpenDetail: (Int) -> Unit
) {
    val app = LocalContext.current.applicationContext as DigitalAlbumApp
    val list by app.repository.favorites.collectAsState(initial = emptyList())

    FifaBackground {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = { AppTopBar(title = "Favoriti", showBack = true, onBack = onBack) }
        ) { padding ->
            if (list.isEmpty()) {
                Box(
                    Modifier.fillMaxSize().padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Nema favorita", color = FifaGray)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.padding(padding),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(list, key = { it.id }) { sticker ->
                        Card(
                            Modifier
                                .fillMaxWidth()
                                .clickable { onOpenDetail(sticker.id) },
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = FifaNavyCard)
                        ) {
                            Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                StickerImage(
                                    url = sticker.imageUrl,
                                    name = sticker.name,
                                    size = 56.dp,
                                    owned = sticker.owned,
                                    isGolden = sticker.isGolden
                                )
                                Column(Modifier.padding(start = 12.dp)) {
                                    Text(sticker.name, fontWeight = FontWeight.SemiBold)
                                    Text("${sticker.team} · #${sticker.number}", color = FifaGray)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
