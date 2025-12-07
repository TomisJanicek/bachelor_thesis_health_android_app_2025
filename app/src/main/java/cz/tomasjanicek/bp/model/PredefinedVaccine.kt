package cz.tomasjanicek.bp.model

/**
 * Datová struktura pro jednu předdefinovanou vakcínu.
 * OPRAVA: Přidali jsme 'category', aby každá vakcína věděla, kam patří.
 */
data class PredefinedVaccine(
    val vaccineName: String,
    val disease: String,
    val category: InjectionCategory
)

/**
 * Objekt sloužící jako databáze všech předdefinovaných vakcín.
 */
object PredefinedVaccines {

    // Nyní máme jednu velkou mapu, kde klíč je název sekce
    // a hodnota je seznam vakcín. To nám usnadní práci v UI.
    val allVaccines: Map<String, List<PredefinedVaccine>> = mapOf(
        "Povinná" to listOf(
            PredefinedVaccine("Hexavakcína", "Záškrt, tetanus, černý kašel, Hep-B, dětská obrna, Hib", InjectionCategory.MANDATORY),
            PredefinedVaccine("MMR (Priorix)", "Spalničky, zarděnky, příušnice", InjectionCategory.MANDATORY)
        ),
        "Doporučená" to listOf(
            PredefinedVaccine("Chřipka", "Chřipka (influenza)", InjectionCategory.RECOMMENDED),
            PredefinedVaccine("Pneumokoková vakcína (Prevenar, Apexxnar)", "Pneumokokové infekce", InjectionCategory.RECOMMENDED),
            PredefinedVaccine("Meningokok ACWY (Nimenrix, MenQuadfi)", "Meningokok A, C, W, Y", InjectionCategory.RECOMMENDED),
            PredefinedVaccine("Meningokok B (Bexsero, Trumenba)", "Meningokok B", InjectionCategory.RECOMMENDED),
            PredefinedVaccine("HPV vakcína (Gardasil, Cervarix)", "Lidský papilomavirus (HPV)", InjectionCategory.RECOMMENDED),
            PredefinedVaccine("Plané neštovice (Varivax, Varilrix)", "Plané neštovice (varicella)", InjectionCategory.RECOMMENDED),
            PredefinedVaccine("Hepatitida A (Havrix, Vaqta)", "Hepatitida A", InjectionCategory.RECOMMENDED),
            PredefinedVaccine("Klíšťová encefalitida (FSME-Immun, Encepur)", "Klíšťová encefalitida", InjectionCategory.RECOMMENDED),
            PredefinedVaccine("Tetanus (přeočkování)", "Tetanus", InjectionCategory.RECOMMENDED),
            PredefinedVaccine("Černý kašel (přeočkování, Adacel)", "Černý kašel, záškrt, tetanus", InjectionCategory.RECOMMENDED)
        ),
        "Cestovatelská" to listOf(
            PredefinedVaccine("Žlutá zimnice (Stamaril)", "Žlutá zimnice", InjectionCategory.TRAVEL),
            PredefinedVaccine("Břišní tyfus (Typhim Vi)", "Břišní tyfus", InjectionCategory.TRAVEL),
            PredefinedVaccine("Cholera (Dukoral)", "Cholera", InjectionCategory.TRAVEL),
            PredefinedVaccine("Vzteklina (Verorab, Rabipur)", "Vzteklina", InjectionCategory.TRAVEL),
            PredefinedVaccine("Japonská encefalitida (Ixiaro)", "Japonská encefalitida", InjectionCategory.TRAVEL)
        ),
        "Pro rizikové skupiny" to listOf(
            PredefinedVaccine("RSV (Abrysvo, Arexvy)", "Respirační syncytiální virus (RSV)", InjectionCategory.RISK_GROUPS),
            PredefinedVaccine("Pásový opar (Shingrix)", "Pásový opar (Herpes zoster)", InjectionCategory.RISK_GROUPS),
            PredefinedVaccine("COVID-19 (Comirnaty, Spikevax)", "COVID-19", InjectionCategory.RISK_GROUPS),
            PredefinedVaccine("TBC (BCG Vaccine)", "Tuberkulóza (TBC)", InjectionCategory.RISK_GROUPS)
        )
    )
}