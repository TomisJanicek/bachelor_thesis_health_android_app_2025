package cz.tomasjanicek.bp.utils

import java.text.Normalizer
import java.util.Locale

private val NON_ALPHANUMERIC = "[^\\w\\d]".toRegex()
private val WHITESPACE = "\\s+".toRegex()

fun String.toSnakeCase(): String {
    // 1. Převede "Č,š,ř" na "C,s,r"
    val normalized = Normalizer.normalize(this, Normalizer.Form.NFD)
        .replace("\\p{InCombiningDiacriticalMarks}+".toRegex(), "")
    // 2. Nahradí mezery a jiné znaky podtržítkem
    return normalized
        .trim()
        .replace(WHITESPACE, "_")
        .replace(NON_ALPHANUMERIC, "_")
        .lowercase(Locale.getDefault())
}