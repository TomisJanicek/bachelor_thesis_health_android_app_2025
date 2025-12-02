package cz.tomasjanicek.bp.utils

import androidx.annotation.DrawableRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

/**
 * Najde a vrátí ID drawable zdroje podle jeho názvu.
 * Toto je velmi efektivní, protože Android si ID mapuje při kompilaci.
 * Je to "Composable" funkce, protože pro získání kontextu používá LocalContext.current.
 *
 * @param imageName Název obrázku uložený v databázi (např. "dr_novak").
 * @param defaultResId ID výchozího obrázku, pokud se zadaný nenajde (např. silueta).
 * @return ID zdroje pro použití v `painterResource()`.
 */
@Composable
fun getDrawableResourceId(imageName: String?, @DrawableRes defaultResId: Int): Int {
    // Získáme kontext, který je nutný pro přístup k aplikačním zdrojům.
    val context = LocalContext.current

    // Pokud je název obrázku prázdný, okamžitě vrátíme výchozí ID.
    if (imageName.isNullOrBlank()) {
        return defaultResId
    }

    // Pomocí kontextu se pokusíme najít ID zdroje.
    // Hledáme v balíčku naší aplikace ("cz.tomasjanicek.bp") zdroj typu "drawable"
    // s názvem, který jsme dostali z databáze (imageName).
    val resourceId = context.resources.getIdentifier(imageName, "drawable", context.packageName)

    // getIdentifier vrátí 0, pokud zdroj nenajde. V takovém případě vrátíme
    // naše bezpečné výchozí ID, aby aplikace nespadla.
    return if (resourceId == 0) defaultResId else resourceId
}