package cz.tomasjanicek.bp.model

import java.time.LocalDateTime
import java.time.ZoneOffset

/**
 * Tento soubor obsahuje ukázková data pro použití v náhledech a pro testování.
 */
val sampleDoctors = listOf(
    // 1. Doktor s většinou vyplněných údajů
    Doctor(
        id = 1,
        name = "MUDr. Eva Dvořáková",
        specialization = "Kardiologie",
        phone = "+420 123 456 789",
        email = "eva.dvorakova@kardio.cz",
        location = "Fakultní nemocnice Brno",
        subtitle = "Vedoucí lékařka oddělení. Více než 20 let praxe.",
        latitude = 49.1920,
        longitude = 16.6096,
        image = null
    ),

    // 2. Doktor jen s povinnými a základními kontaktními údaji
    Doctor(
        id = 2,
        name = "MUDr. Petr Novotný",
        specialization = "Praktický lékař",
        phone = "+420 777 888 998",
        location = "Poliklinika Vltava, Praha 4",
        image = "prakticky_lekar"
        // 'email', 'subtitle', 'latitude', 'longitude' jsou null
    ),

    // 3. Doktorka bez telefonního čísla, ale s emailem
    Doctor(
        id = 3,
        name = "MUDr. Jana Veselá",
        specialization = "Dermatologie",
        email = "info@kozni-vesela.cz",
        location = "Soukromá praxe, Ostrava-Poruba",
        subtitle = "Specializace na dětskou dermatologii a akné."
        ),

    // 4. Doktor jen s minimem povinných údajů
    Doctor(
        id = 4,
        name = "MUDr. Tomáš Pokorný",
        specialization = "Ortopedie"
        // Všechny ostatní nepovinné parametry budou automaticky null
    ),

    // 5. Doktorka se všemi údaji včetně souřadnic pro mapu
    Doctor(
        id = 5,
        name = "MUDr. Alena Svobodová",
        specialization = "Gynekologie",
        phone = "+420 605 234 567",
        email = "mudr.svobodova@gynekologie-jihlava.cz",
        location = "Nemocnice Jihlava",
        subtitle = "Péče o těhotné a preventivní prohlídky.",
        latitude = 49.3995,
        longitude = 15.5925
    )
)

/**
 * Tento soubor obsahuje ukázková data pro použití v náhledech a pro testování.
 */
// ... (zde je tvůj stávající list 'sampleDoctors') ...
val sampleExaminations = listOf(
    // 1. Naplánovaná prohlídka u kardiologa
    Examination(
        id = 1,
        doctorId = 1, // MUDr. Eva Dvořáková
        type = ExaminationType.PROHLIDKA,
        purpose = "Pravidelná roční kontrola srdce",
        note = "Vzít s sebou výsledky z minulého měření tlaku.",
        dateTime = LocalDateTime.of(2025, 11, 28, 10, 30).toEpochSecond(ZoneOffset.UTC) * 1000,
        status = ExaminationStatus.PLANNED
        // 'result' je null, protože je to naplánovaná prohlídka
    ),

    // 2. Dokončený odběr krve u praktika
    Examination(
        id = 2,
        doctorId = 2, // MUDr. Petr Novotný
        type = ExaminationType.ODBER_KRVE,
        purpose = "Preventivní odběr - krevní obraz",
        dateTime = LocalDateTime.of(2025, 10, 15, 8, 0).toEpochSecond(ZoneOffset.UTC) * 1000,
        status = ExaminationStatus.COMPLETED,
        result = "Všechny hodnoty v normě, cholesterol mírně zvýšený."
    ),

    // 3. Naplánovaný zákrok u dermatoložky
    Examination(
        id = 3,
        doctorId = 3, // MUDr. Jana Veselá
        type = ExaminationType.ZAKROK,
        purpose = "Odstranění znaménka na zádech",
        dateTime = LocalDateTime.of(2026, 1, 20, 14, 0).toEpochSecond(ZoneOffset.UTC) * 1000,
        status = ExaminationStatus.PLANNED,
        note = "Po zákroku budu potřebovat odvoz domů."
    ),

    // 4. Historické vyšetření u ortopeda
    Examination(
        id = 4,
        doctorId = 4, // MUDr. Tomáš Pokorný
        type = ExaminationType.VYSETRENI,
        purpose = "Bolest v levém koleni po sportu",
        dateTime = LocalDateTime.of(2025, 9, 5, 11, 15).toEpochSecond(ZoneOffset.UTC) * 1000,
        status = ExaminationStatus.COMPLETED,
        result = "Natažené vazy, doporučen klid a ortéza na 2 týdny."
    ),

    // 5. Zrušená prohlídka
    Examination(
        id = 5,
        doctorId = 2, // MUDr. Petr Novotný
        type = ExaminationType.PROHLIDKA,
        purpose = "Vstupní prohlídka do zaměstnání",
        dateTime = LocalDateTime.of(2025, 11, 1, 13, 0).toEpochSecond(ZoneOffset.UTC) * 1000,
        status = ExaminationStatus.CANCELLED,
        note = "Zrušeno z důvodu nemoci."
    )
)