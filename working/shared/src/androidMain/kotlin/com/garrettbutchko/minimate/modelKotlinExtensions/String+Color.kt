package com.garrettbutchko.minimate.modelKotlinExtensions

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb

/**
 * The named colors persisted on course/user models as plain strings (e.g. a course's
 * scoreCardColorDT). Mirrors SwiftUI's standard system color set/names, which is why the
 * hex values line up with iOS's Color.red/.orange/etc. — single source of truth so
 * [toColor], [toColorName], and any UI swatch list (see ColorPicker.kt) can't drift
 * apart the way toColorName had (it used to be reimplemented privately, hardcoding the
 * same hex values a second time, in admin-android's ContentView.kt).
 */
val swiftColorPalette: List<Pair<String, Color>> = listOf(
    "red" to Color(0xFFFF383C),
    "orange" to Color(0xFFFF8D28),
    "yellow" to Color(0xFFFFCC00),
    "green" to Color(0xFF34C759),
    "mint" to Color(0xFF00C8B3),
    "teal" to Color(0xFF00C3D0),
    "cyan" to Color(0xFF00C0E8),
    "blue" to Color(0xFF0088FF),
    "indigo" to Color(0xFF6155F5),
    "purple" to Color(0xFFCB30E0),
    "pink" to Color(0xFFFF2D55),
    "brown" to Color(0xFFAC7F5E)
)

private val graySynonymColor = Color(0xFF8E8E93)

fun String?.toColor(): Color? {
    if (this == null) return null
    val key = this.lowercase()

    swiftColorPalette.firstOrNull { it.first == key }?.let { return it.second }
    if (key == "gray" || key == "grey") return graySynonymColor

    val hex = this.trimStart('#')
    return when (hex.length) {
        6 -> hex.toLongOrNull(16)?.let { Color(it.or(0xFF000000L).toInt()) }
        8 -> hex.toLongOrNull(16)?.let { Color(it.toInt()) }
        else -> null
    }
}

/** Inverse of [toColor] for the named palette; falls back to a hex string otherwise. */
fun Color.toColorName(): String =
    swiftColorPalette.firstOrNull { it.second == this }?.first
        ?: String.format("#%08X", toArgb())
