package cz.tomasjanicek.bp.geo

import android.content.Context
import android.location.Geocoder
import android.os.Build
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
            // Pro Android T (API 33) a vyšší existuje nová metoda s callbackem,
            // ale pro jednoduchost a zpětnou kompatibilitu použijeme starší blokující metodu
            // uvnitř withContext, což je bezpečné.
            val addresses = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                // Novější API (asynchronní, ale my ho zde voláme synchronně v IO kontextu)
                var addressList: List<android.location.Address>? = null
                geocoder.getFromLocation(latitude, longitude, 1) {
                    addressList = it
                }
                addressList
            } else {
                // Starší, zastaralé API (blokující)
                @Suppress("DEPRECATION")
                geocoder.getFromLocation(latitude, longitude, 1)
            }

            // Zkontrolujeme, zda jsme dostali nějaký výsledek
            if (!addresses.isNullOrEmpty()) {
                val address = addresses[0]
                // Sestavíme adresu z dostupných částí.
                // Můžete si vybrat, jak detailní adresa má být.
                val addressParts = listOfNotNull(
                    address.thoroughfare, // Ulice
                    address.subThoroughfare, // Číslo popisné/orientační
                    address.locality, // Město
                    address.postalCode, // PSČ
                    address.countryName // Stát
                )
                addressParts.joinToString(", ")
            } else {
                // Pokud se nepodařilo najít žádnou adresu
                null
            }
        } catch (e: IOException) {
            // Chyba při komunikaci se serverem geocoderu
            e.printStackTrace()
            null
        } catch (e: IllegalArgumentException) {
            // Neplatné souřadnice
            e.printStackTrace()
            null
        }
    }
}
