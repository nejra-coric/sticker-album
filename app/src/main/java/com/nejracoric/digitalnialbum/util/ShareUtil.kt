package com.nejracoric.digitalnialbum.util

import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.nejracoric.digitalnialbum.data.model.Sticker

object ShareUtil {
    fun shareSticker(context: Context, sticker: Sticker) {
        val intro = when {
            !sticker.owned -> "Hej, nedostaje mi ova sličica!"
            sticker.ownedCount > 1 -> "Ej, vidi ove duple imam!"
            else -> "Hej, vidi šta sam dobio/la!"
        }
        shareText(context, formatStickerMessage(intro, sticker), "Podijeli sličicu")
    }

    fun shareMissingList(context: Context, stickers: List<Sticker>) {
        val missing = stickers.filter { !it.owned }
        if (missing.isEmpty()) {
            Toast.makeText(context, "Čestitamo — nema nedostajućih sličica!", Toast.LENGTH_SHORT).show()
            return
        }
        val text = buildString {
            appendLine("Hej! Evo liste sličica koje mi nedostaju u albumu:")
            appendLine()
            missing
                .sortedWith(compareBy({ it.team }, { it.name }))
                .forEachIndexed { index, sticker ->
                    appendLine("${index + 1}. ${stickerLine(sticker)}")
                }
            appendLine()
            append("Ukupno nedostaje: ${missing.size}")
        }
        shareText(context, text, "Podijeli listu nedostajućih")
    }

    fun shareDuplicatesList(context: Context, stickers: List<Sticker>) {
        val duplicates = stickers.filter { it.ownedCount > 1 }
        if (duplicates.isEmpty()) {
            Toast.makeText(context, "Nema duplikata za dijeljenje.", Toast.LENGTH_SHORT).show()
            return
        }
        val text = buildString {
            appendLine("Ej, vidi ove duple imam! Mogu zamijeniti:")
            appendLine()
            duplicates
                .sortedWith(compareBy({ it.team }, { it.name }))
                .forEachIndexed { index, sticker ->
                    appendLine("${index + 1}. ${stickerLine(sticker)} (x${sticker.ownedCount})")
                }
            appendLine()
            append("Ukupno duplikata: ${duplicates.size}")
        }
        shareText(context, text, "Podijeli duplikate")
    }

    private fun formatStickerMessage(intro: String, sticker: Sticker): String = buildString {
        appendLine(intro)
        appendLine()
        appendLine(stickerLine(sticker))
        append("Pozicija: ${sticker.position}")
        if (sticker.ownedCount > 1) {
            append(" | Kopije: x${sticker.ownedCount}")
        }
    }

    private fun stickerLine(sticker: Sticker): String =
        "${sticker.name} (#${sticker.number.padStart(3, '0')}) — ${sticker.team}, ${sticker.rarity}"

    private fun shareText(context: Context, text: String, chooserTitle: String) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
        }
        context.startActivity(Intent.createChooser(intent, chooserTitle))
    }
}
