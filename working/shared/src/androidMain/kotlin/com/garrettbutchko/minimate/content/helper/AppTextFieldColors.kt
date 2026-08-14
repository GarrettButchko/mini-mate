package com.garrettbutchko.minimate.content.helper

import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.TextFieldColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import com.garrettbutchko.minimate.shared.R

/**
 * Shared color scheme for every OutlinedTextField in the app so they all render
 * identically: a transparent background, `mainOpp` text/label, a neutral
 * `subThreeOpp` resting border/label, and an accent (default `swiftBlue`) for
 * the focused border and cursor.
 *
 * @param accent the focused border/cursor color; pass `swiftRed` for destructive
 *               flows (e.g. account deletion).
 */
@Composable
fun appTextFieldColors(accent: Color = colorResource(id = R.color.swiftBlue)): TextFieldColors =
    OutlinedTextFieldDefaults.colors(
        focusedTextColor = colorResource(id = R.color.mainOpp),
        unfocusedTextColor = colorResource(id = R.color.mainOpp),
        focusedLabelColor = accent,
        unfocusedLabelColor = colorResource(id = R.color.subThreeOpp),
        cursorColor = accent,
        focusedBorderColor = accent,
        unfocusedBorderColor = colorResource(id = R.color.subThreeOpp)
    )
