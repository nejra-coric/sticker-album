package com.nejracoric.digitalnialbum.ui.wishlist

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
import androidx.compose.material3.MaterialTheme
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
import com.nejracoric.digitalnialbum.data.model.Sticker
import com.nejracoric.digitalnialbum.ui.components.AppTopBar
import com.nejracoric.digitalnialbum.ui.components.GlassBackground
import com.nejracoric.digitalnialbum.ui.components.GlassCard
import com.nejracoric.digitalnialbum.ui.components.StickerImage
import com.nejracoric.digitalnialbum.ui.theme.GoldAccent
import com.nejracoric.digitalnialbum.ui.theme.NeonCyan
import com.nejracoric.digitalnialbum.ui.theme.TextGray
import com.nejracoric.digitalnialbum.ui.theme.TextWhite

@Composable
fun WishlistScreen(
    onBack: () -> Unit,
    onOpenDetail: (Int) -> Unit
) {
    val app = LocalContext.current.applicationContext as DigitalAlbumApp
    val list by app.repository.wishlist.collectAsState(initial = emptyList())

    GlassBackground {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                AppTopBar(title = "Sačuvane", showBack = true, onBack = onBack)
            }
        ) { padding ->
            WishlistContent(
                list = list,
                onOpenDetail = onOpenDetail,
                modifier = Modifier.padding(padding)
            )
        }
    }
}

@Composable
fun WishlistContent(
    list: List<Sticker>,
    onOpenDetail: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    if (list.isEmpty()) {
        Box(modifier.fillMaxSize().then(modifier), contentAlignment = Alignment.Center) {
            Text("Nema sačuvanih sličica", color = TextGray)
        }
    } else {
        LazyColumn(
            modifier = modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(list, key = { it.id }) { sticker ->
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    corner = 14.dp,
                    golden = sticker.isGolden,
                    onClick = { onOpenDetail(sticker.id) }
                ) {
                    Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        StickerImage(
                            url = sticker.imageUrl,
                            name = sticker.name,
                            stickerId = sticker.id,
                            size = 56.dp,
                            owned = sticker.owned,
                            isGolden = sticker.isGolden
                        )
                        Column(Modifier.padding(start = 12.dp)) {
                            Text(sticker.name, fontWeight = FontWeight.SemiBold, color = TextWhite)
                            Text(
                                "${sticker.team} · #${sticker.number}",
                                color = TextGray,
                                style = MaterialTheme.typography.bodySmall
                            )
                            Text(
                                if (sticker.owned) "Već imaš" else "Tražiš",
                                color = if (sticker.owned) NeonCyan else GoldAccent,
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
