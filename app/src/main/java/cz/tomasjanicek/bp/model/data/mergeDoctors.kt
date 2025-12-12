package cz.tomasjanicek.bp.model.data

import cz.tomasjanicek.bp.model.Doctor

/**
 * Inteligentně sloučí doktory ze zálohy s novými defaultními doktory z aplikace.
 * Cíl: Zachovat uživatelská data, ale aktualizovat obrázky a přidat nové typy.
 */
fun mergeDoctors(
    backupDoctors: List<Doctor>,
    defaultDoctors: List<Doctor>
): List<Doctor> {
    val resultDoctors = mutableListOf<Doctor>()

    // 1. Projdeme všechny defaultní doktory (to, co je v nové verzi appky)
    for (defaultDoc in defaultDoctors) {
        // Zkusíme najít odpovídajícího doktora v záloze podle SPECIALIZACE
        val backupDoc = backupDoctors.find { it.specialization == defaultDoc.specialization }

        if (backupDoc != null) {
            // DOKTOR EXISTUJE V ZÁLOZE -> ZACHOVÁME UŽIVATELSKÁ DATA
            // Ale vynutíme nový obrázek z kódu (defaultDoc.image)
            resultDoctors.add(backupDoc.copy(
                image = defaultDoc.image
            ))
        } else {
            // DOKTOR V ZÁLOZE NENÍ -> JE TO NOVÝ DOKTOR V TÉTO VERZI
            // Přidáme ho jako nového
            resultDoctors.add(defaultDoc)
        }
    }

    // Poznámka: Pokud bys chtěl zachovat i doktory, které uživatel přidal ručně
    // (a nejsou v defaultDoctors), musel bys je sem přidat.
    // V tvém aktuálním modelu ale uživatel doktory ručně nepřidává (jen edituje existující),
    // takže tento kód stačí.

    return resultDoctors
}