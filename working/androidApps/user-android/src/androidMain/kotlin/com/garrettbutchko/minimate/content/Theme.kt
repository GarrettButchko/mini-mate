
package com.garrettbutchko.minimate.content

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

// Semantic colors — same in both modes
private val SwiftBlue  = Color(0xFF0088FF)
private val SwiftRed   = Color(0xFFFF383C)

val MiniMateLightColorScheme = lightColorScheme(
    primary                = SwiftBlue,
    onPrimary              = Color.White,
    primaryContainer       = Color(0xFFD0E8FF),
    onPrimaryContainer     = Color(0xFF001D35),

    secondary              = Color(0xFFF4F4F4), // subTwo
    onSecondary            = Color(0xFF2A2A2E), // subTwoOpp
    secondaryContainer     = Color(0xFFEEEEEE), // courseSub
    onSecondaryContainer   = Color(0xFF1C1C1F),

    background             = Color(0xFFF4F5F5), // bg
    onBackground           = Color(0xFF000000), // mainOpp

    surface                = Color(0xFFFFFFFF), // main / sub
    onSurface              = Color(0xFF000000), // mainOpp
    surfaceVariant         = Color(0xFFFFFFFF), // sub
    onSurfaceVariant       = Color(0xFF1C1C1F), // subOpp

    outline                = Color(0xFFE0DFE6), // textField

    error                  = SwiftRed,
    onError                = Color.White,
    errorContainer         = Color(0xFFFFDAD6),
    onErrorContainer       = Color(0xFF410002)
)

val MiniMateDarkColorScheme = darkColorScheme(
    primary                = SwiftBlue,
    onPrimary              = Color.White,
    primaryContainer       = Color(0xFF001D35),
    onPrimaryContainer     = Color(0xFFD0E8FF),

    secondary              = Color(0xFF2A2A2E), // subTwo (dark)
    onSecondary            = Color(0xFFF4F4F4), // subTwoOpp (dark)
    secondaryContainer     = Color(0xFF272727), // courseSub (dark)
    onSecondaryContainer   = Color(0xFFEEEEEF),

    background             = Color(0xFF000000), // bg (dark)
    onBackground           = Color(0xFFF2F2F7), // mainOpp (dark)

    surface                = Color(0xFF000000), // main (dark)
    onSurface              = Color(0xFFF2F2F7), // mainOpp (dark)
    surfaceVariant         = Color(0xFF1C1C1E), // sub (dark)
    onSurfaceVariant       = Color(0xFFEEEEEF), // subOpp (dark)

    outline                = Color(0xFF3A3A3C), // textField (dark)

    error                  = SwiftRed,
    onError                = Color.White,
    errorContainer         = Color(0xFF93000A),
    onErrorContainer       = Color(0xFFFFDAD6)
)