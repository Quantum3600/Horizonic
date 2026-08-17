package com.trishit.horizonic.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.trishit.horizonic.R

val Parkinsans = FontFamily(
    Font(R.font.parkinsans)
)

// Set of Material typography styles to start with
val Typography = Typography(
    bodyLarge = TextStyle(
        fontFamily = Parkinsans,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    ),
    titleLarge = TextStyle(
        fontFamily = Parkinsans,
        fontWeight = FontWeight.Normal,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),
    labelSmall = TextStyle(
        fontFamily = Parkinsans,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    ),
    displayLarge = TextStyle(fontFamily = Parkinsans),
    displayMedium = TextStyle(fontFamily = Parkinsans),
    displaySmall = TextStyle(fontFamily = Parkinsans),
    headlineLarge = TextStyle(fontFamily = Parkinsans),
    headlineMedium = TextStyle(fontFamily = Parkinsans),
    headlineSmall = TextStyle(fontFamily = Parkinsans),
    titleMedium = TextStyle(fontFamily = Parkinsans),
    titleSmall = TextStyle(fontFamily = Parkinsans),
    bodyMedium = TextStyle(fontFamily = Parkinsans),
    bodySmall = TextStyle(fontFamily = Parkinsans),
    labelLarge = TextStyle(fontFamily = Parkinsans),
    labelMedium = TextStyle(fontFamily = Parkinsans)
)