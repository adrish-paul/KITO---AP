package com.kito.feature.calendar.presentation

import androidx.compose.ui.graphics.Color

object CalendarColors {
    val orange      = Color(0xFFFF6B35)
    val orangeLight = Color(0xFFFF8C42)
    val orangeDark  = Color(0xFFE85D04)
    val purple      = Color(0xFF7C3AED)
    val purpleLight = Color(0xFFA78BFA)
    val red         = Color(0xFFDC2626)
    val teal        = Color(0xFF0D9488)
    val blue        = Color(0xFF2563EB)
    val pink        = Color(0xFFDB2777)

    val bgDeep      = Color(0xFF08060F)
    val bgCard      = Color(0xFF0F0D18)
    val textPrimary = Color(0xFFF0ECF8)
    val divider     = Color(0x0DFFFFFF)

    val months = listOf(
        "January","February","March","April","May","June",
        "July","August","September","October","November","December"
    )
    val monthsShort = listOf(
        "Jan","Feb","Mar","Apr","May","Jun",
        "Jul","Aug","Sep","Oct","Nov","Dec"
    )
    val daysShort = listOf("S","M","T","W","T","F","S")
    val daysFull  = listOf("Sunday","Monday","Tuesday","Wednesday","Thursday","Friday","Saturday")

    fun categoryColor(cat: String?): Color = when (cat) {
        "class" -> orange
        "lab"   -> blue
        "exam"  -> red
        "event" -> purple
        "study" -> pink
        else    -> orangeLight
    }

    fun categoryIcon(cat: String?): String = when (cat) {
        "class" -> "📚"
        "lab"   -> "🔬"
        "exam"  -> "📝"
        "event" -> "🎉"
        "study" -> "✍️"
        else    -> "📌"
    }

    fun fromHex(hex: String?): Color? {
        if (hex.isNullOrBlank()) return null
        return try {
            val clean = hex.trimStart('#')
            val argb = if (clean.length == 6) "FF$clean" else clean
            Color(argb.toLong(16).toInt())
        } catch (e: Exception) { null }
    }
}