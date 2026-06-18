package com.nejracoric.digitalnialbum.util

import android.content.Context
import android.content.Intent
import com.nejracoric.digitalnialbum.data.model.Sticker

object ShareUtil {
    fun shareSticker(context: Context, sticker: Sticker) {
        val text = buildString {
            append("SP 2026 – ")
            append(sticker.name)
            append("\nDres: ")
            append(sticker.number)
            append(" | ")
            append(sticker.team)
            append("\nPozicija: ")
            append(sticker.position)
            append(" | ")
            append(sticker.rarity)
            if (sticker.owned) {
                append("\nStatus: u albumu")
                if (sticker.ownedCount > 1) append(" (x${sticker.ownedCount})")
            } else {
                append("\nStatus: nedostaje")
            }
        }
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
        }
        context.startActivity(Intent.createChooser(intent, null))
    }
}
