/*
 * File: Color.kt
 * Author: Todd Bryant
 * Company: artius.iD, Inc.
 */

package com.artiusid.sdk.ui.theme

import androidx.compose.ui.graphics.Color

// Material3 default theme colors
val Purple80 = Color(0xFFD0BCFF)
val PurpleGrey80 = Color(0xFFCCC2DC)
val Pink80 = Color(0xFFEFB8C8)
val Purple40 = Color(0xFF6650a4)
val PurpleGrey40 = Color(0xFF625b71)
val Pink40 = Color(0xFF7D5260)

// artius.iD iOS Standalone App color scheme - Updated to match default theme
val WhiteA700 = Color(0xFFFFFFFF)
val WhiteA7007f = Color(0x7FFFFFFF)
val WhiteA70019 = Color(0x19FFFFFF)
val WhiteA70028 = Color(0x28FFFFFF)

val Gray500 = Color(0xFF9E9E9E) // iOS Gray500 for secondary elements
val Gray900 = Color(0xFF18202A) // iOS Gray900 - darkest gradient color
val Gray917 = Color(0xFF171717)
val Gray3007f = Color(0x7F9E9E9E)

val Bluegray100 = Color(0xFFCFD8DC)
val Bluegray900 = Color(0xFF22354D) // Updated to match artius.iD primary (iOS Bluegray900)
val Bluegray901 = Color(0xFF1A2B3D) // Updated to match artius.iD primary dark
val Bluegray902 = Color(0xFF162029)

val Yellow900 = Color(0xFFF58220) // Correct - matches artius.iD secondary (iOS Yellow900)
val LightGreen900 = Color(0xFF2E7D32)
val LightBlueA20063 = Color(0x634FC6FF)
val Indigo80063 = Color(0x633F51B5)

// Primary colors - Updated to match artius.iD theme
val Primary = Color(0xFF22354D) // artius.iD primary (iOS Bluegray900)
val PrimaryDark = Color(0xFF1A2B3D) // artius.iD primary dark
val PrimaryLight = Color(0xFF3E517A) // artius.iD primary light

// Secondary colors - Updated to match artius.iD theme
val Secondary = Color(0xFFF58220) // artius.iD secondary (iOS Yellow900)
val SecondaryDark = Color(0xFFE57100) // artius.iD secondary dark
val SecondaryLight = Color(0xFFFFB74D) // artius.iD secondary light

// Background colors - Updated to match iOS screenshot appearance
val Background = Color(0xFF22354D) // iOS Bluegray900 - primary background (lighter of gradient)
val Surface = Color(0xFF22354D) // iOS Bluegray900 - surface color

// Text colors - Updated to match iOS standalone app DARK theme  
val TextPrimary = Color(0xFFFFFFFF) // White text on dark background
val TextSecondary = Color(0xB3FFFFFF) // Semi-transparent white for secondary text
val TextDisabled = Color(0x80FFFFFF) // More transparent white for disabled text

// Status colors
val Success = Color(0xFF4CAF50)
val Error = Color(0xFFE53935)
val Warning = Color(0xFFFFA726)
val Info = Color(0xFF29B6F6)

// Overlay colors
val Overlay = Color(0x80000000)
val OverlayLight = Color(0x40000000) 