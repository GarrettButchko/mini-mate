package com.garrettbutchko.minimate.modelKotlinExtensions

import androidx.compose.ui.graphics.Color

fun String?.toColor(): Color? {
    if (this == null) return null
    when (this.lowercase()) {
        "red" -> return Color(0xFFFF383C)
        "orange" -> return Color(0xFFFF8D28)
        "yellow" -> return Color(0xFFFFCC00)
        "green" -> return Color(0xFF34C759)
        "mint" -> return Color(0xFF00C8B3)
        "teal" -> return Color(0xFF00C3D0)
        "cyan" -> return Color(0xFF00C0E8)
        "blue" -> return Color(0xFF0088FF)
        "indigo" -> return Color(0xFF6155F5)
        "purple" -> return Color(0xFFCB30E0)
        "pink" -> return Color(0xFFFF2D55)
        "brown" -> return Color(0xFFAC7F5E)
    }
    val hex = this.trimStart('#')
    return when (hex.length) {
        6 -> hex.toLongOrNull(16)?.let { Color(it.or(0xFF000000L).toInt()) }
        8 -> hex.toLongOrNull(16)?.let { Color(it.toInt()) }
        else -> null
    }
}
