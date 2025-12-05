package cz.tomasjanicek.bp.services

import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import java.util.Locale

/**
* Převede zeměpisné souřadnice na čitelnou adresu.
* Vrací první nalezenou adresu jako String, nebo null v případě selhání.
*
* @param context Aplikační kontext.
* @param latitude Zeměpisná šířka.
* @param longitude Zeměpisná délka.
* @return Formátovaná adresa nebo null.
*/
suspend fun getAddressFromCoordinates(
    context: Context,
    latitude: Double,
    longitude: Double
): String? {
    // Geocoder potřebuje Context a Locale (jazyk, ve kterém chceme výsledky)
    val geocoder = Geocoder(context, Locale.getDefault())

    // Operaci spouštíme na I/O vlákně, aby neblokovala UI
    return withContext(Dispatchers.IO) {
        try {
            // Použijeme blokující metodu pro všechny verze Androidu.
            // Je to bezpečné, protože jsme v `withContext(Dispatchers.IO)`.
            // Argument `maxResults` je nastaven na 1, protože nás zajímá jen nejlepší shoda.
            @Suppress("DEPRECATION")
            val addresses: List<Address>? = geocoder.getFromLocation(latitude, longitude, 1)

            // Zkontrolujeme, zda jsme dostali nějaký výsledek
            if (addresses.isNullOrEmpty()) {
                Log.w(
                    "GeoCoding",
                    "Geocoder nevrátil žádnou adresu pro souřadnice: lat=$latitude, lng=$longitude"
                )
                return@withContext null
            }

            val address = addresses[0]
            // Sestavíme adresu z dostupných částí.
            // Můžete si vybrat, jak detailní adresa má být.
            val addressParts = listOfNotNull(
                address.thoroughfare, // Ulice
                address.subThoroughfare, // Číslo popisné/orientační
                address.locality, // Město
                address.postalCode // PSČ
            )
            // .filter { it.isNotBlank() } zajistí, že nespojíme prázdné řetězce
            val finalAddress = addressParts.filter { it.isNotBlank() }.joinToString(", ")

            // Pokud je adresa po spojení prázdná, vrátíme null
            if (finalAddress.isBlank()) null else finalAddress

        } catch (e: IOException) {
            // Chyba při komunikaci se serverem geocoderu (nejčastěji chybějící síť nebo problém emulátoru)
            Log.e("GeoCoding", "Služba geocodingu není dostupná. Chyba: ${e.message}")
            null
        } catch (e: IllegalArgumentException) {
            // Neplatné souřadnice
            Log.e("GeoCoding", "Neplatné souřadnice pro Geocoder: lat=$latitude, lng=$longitude")
            null
        } catch (e: Exception) {
            // Jakákoliv jiná neočekávaná chyba
            Log.e("GeoCoding", "Neočekávaná chyba v getAddressFromCoordinates: ${e.message}")
            null
        }
    }
}