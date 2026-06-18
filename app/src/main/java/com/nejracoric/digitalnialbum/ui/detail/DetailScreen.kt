package com.nejracoric.digitalnialbum.ui.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
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
import com.nejracoric.digitalnialbum.ui.components.AppTopBar
import com.nejracoric.digitalnialbum.ui.components.FifaBackground
import com.nejracoric.digitalnialbum.ui.components.StickerImageLarge
import com.nejracoric.digitalnialbum.ui.theme.FifaGold
import com.nejracoric.digitalnialbum.ui.theme.FifaGray
import com.nejracoric.digitalnialbum.ui.theme.FifaGreen
import com.nejracoric.digitalnialbum.ui.theme.FifaNavy
import com.nejracoric.digitalnialbum.ui.theme.FifaNavyCard
import com.nejracoric.digitalnialbum.util.ShareUtil

@Composable
fun DetailScreen(
    viewModel: DetailViewModel,
    onBack: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    val sticker = state.sticker

    FifaBackground {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                AppTopBar(
                    title = sticker?.name ?: "Detalj",
                    showBack = true,
                    onBack = onBack,
                    actionIcon = if (sticker?.isFavorite == true) Icons.Default.Favorite
                    else Icons.Default.FavoriteBorder,
                    onAction = { viewModel.toggleFavorite() }
                )
            }
        ) { padding ->
            if (state.loading) {
                Column(
                    Modifier.fillMaxSize().padding(padding),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator(Modifier.padding(32.dp), color = FifaGreen)
                }
            } else if (sticker == null) {
                Text(
                    state.message ?: "Greška",
                    modifier = Modifier.padding(padding).padding(16.dp)
                )
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp)
                ) {
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = FifaNavyCard),
                        elevation = CardDefaults.cardElevation(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        StickerImageLarge(
                            url = sticker.imageUrl,
                            name = sticker.name,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(320.dp),
                            isGolden = sticker.isGolden
                        )
                    }
                    Text(
                        sticker.name,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 20.dp)
                    )
                    if (sticker.rarity.contains("Legend", true) || sticker.isGolden) {
                        Text(
                            "LEGENDARY",
                            style = MaterialTheme.typography.labelLarge,
                            color = FifaGold,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp)
                            .background(FifaNavyCard, RoundedCornerShape(12.dp))
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        StatCell("Tim", sticker.team)
                        StatCell("Dres", "#${sticker.number}")
                        StatCell("Poz.", sticker.position.take(6))
                    }
                    Text(
                        when {
                            !sticker.owned -> "Nedostaje u albumu"
                            sticker.ownedCount > 1 -> "Duplikat x${sticker.ownedCount}"
                            else -> "U tvojoj kolekciji"
                        },
                        color = if (sticker.owned) FifaGreen else FifaGray,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    Button(
                        onClick = { viewModel.toggleWishlist() },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = FifaNavyCard,
                            contentColor = Color.White
                        )
                    ) {
                        Icon(
                            if (sticker.isWished) Icons.Default.Star else Icons.Default.StarBorder,
                            contentDescription = null,
                            tint = FifaGold
                        )
                        Text(
                            if (sticker.isWished) "Na listi želja" else "Dodaj na listu želja",
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                    Button(
                        onClick = { ShareUtil.shareSticker(context, sticker) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 10.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = FifaGreen,
                            contentColor = FifaNavy
                        )
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null)
                        Text("Podijeli", modifier = Modifier.padding(start = 8.dp), fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun StatCell(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = FifaGray)
        Text(value, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
    }
}
