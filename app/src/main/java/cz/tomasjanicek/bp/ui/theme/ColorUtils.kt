package cz.tomasjanicek.bp.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.graphics.ColorUtils

/**
 * Rozhodne, zda je lepší použít světlý nebo tmavý text na daném pozadí.
 *
 * @param backgroundColor Barva pozadí.
 * @param lightColor Barva, která se vrátí, pokud je pozadí tmavé (výchozí je bílá).
 * @param darkColor Barva, která se vrátí, pokud je pozadí světlé (výchozí je černá).
 * @return `lightColor` nebo `darkColor` podle toho, co je lépe čitelné.
 */
fun getContrastingTextColor(
    backgroundColor: Color,
    lightColor: Color = Color.White,
    darkColor: Color = Color.Black
): Color {
    // 1. Převedeme Compose barvu na ARGB integer, se kterým umí pracovat knihovna ColorUtils.
    val backgroundArgb = backgroundColor.toArgb()

    // 2. Spočítáme "světlost" (luminance) barvy pozadí. Výsledek je hodnota mezi 0 (černá) a 1 (bílá).
    val luminance = ColorUtils.calculateLuminance(backgroundArgb)

    // 3. Rozhodneme: Pokud je světlost menší než 0.5 (barva je spíše tmavá), vrátíme světlý text.
    //    Jinak (barva je spíše světlá) vrátíme tmavý text.
    return if (luminance < 0.5) {
        lightColor
    } else {
        darkColor
    }
}