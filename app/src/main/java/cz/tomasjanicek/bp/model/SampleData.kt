package cz.tomasjanicek.bp.model

import java.time.LocalDateTime
import java.time.ZoneOffset
import kotlin.random.Random

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
        addressLabel = "Fakultní nemocnice Brno",
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
        addressLabel = "Poliklinika Vltava, Praha 4",
        image = "prakticky_lekar"
        // 'email', 'subtitle', 'latitude', 'longitude' jsou null
    ),

    // 3. Doktorka bez telefonního čísla, ale s emailem
    Doctor(
        id = 3,
        name = "MUDr. Jana Veselá",
        specialization = "Dermatologie",
        email = "info@kozni-vesela.cz",
        addressLabel = "Soukromá praxe, Ostrava-Poruba",
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
        addressLabel = "Nemocnice Jihlava",
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

// --- Kategorie 1: Tělesná váha ---
val sampleCategoryWeight = MeasurementCategory(
    id = 1,
    name = "Tělesná váha",
    description = "Sledování hmotnosti v průběhu času."
)
val sampleFieldWeight = MeasurementCategoryField(
    id = 1,
    categoryId = 1,
    name = "weight",
    label = "Váha",
    unit = "kg",
    minValue = 75.0,
    maxValue = 85.0
)

// --- Kategorie 2: Krevní tlak ---
val sampleCategoryBloodPressure = MeasurementCategory(
    id = 2,
    name = "Krevní tlak",
    description = "Pravidelné měření systolického a diastolického tlaku a tepové frekvence."
)
val sampleFieldsBloodPressure = listOf(
    MeasurementCategoryField(
        id = 2,
        categoryId = 2,
        name = "systolic",
        label = "Systolický tlak",
        unit = "mmHg",
        minValue = 110.0,
        maxValue = 130.0
    ),
    MeasurementCategoryField(
        id = 3,
        categoryId = 2,
        name = "diastolic",
        label = "Diastolický tlak",
        unit = "mmHg",
        minValue = 70.0,
        maxValue = 85.0
    ),
    MeasurementCategoryField(
        id = 4,
        categoryId = 2,
        name = "pulse",
        label = "Tepová frekvence",
        unit = "bpm",
        minValue = 60.0,
        maxValue = 90.0
    )
)

// --- Generování ukázkových měření a jejich hodnot ---

// Funkce pro generování dat
fun generateSampleMeasurementsAndValues(): Pair<List<Measurement>, List<MeasurementValue>> {
    val measurements = mutableListOf<Measurement>()
    val values = mutableListOf<MeasurementValue>()
    var measurementIdCounter = 1L

    // Data pro VÁHU (ID kategorie 1)
    // Generujeme data za poslední rok, cca každých 5 dní
    for (i in 0..73) { // 365 / 5 = 73
        val date = LocalDateTime.now().minusDays(i * 5L)
        val measurement = Measurement(
            id = measurementIdCounter,
            categoryId = 1,
            measuredAt = date.toEpochSecond(ZoneOffset.UTC) * 1000
        )
        measurements.add(measurement)

        // Náhodná hodnota váhy kolem 82 kg s výkyvy
        val weightValue = 82.0 + Random.nextDouble(-4.0, 4.0)
        values.add(
            MeasurementValue(
                measurementId = measurementIdCounter,
                categoryFieldId = 1, // ID pole pro "Váha"
                value = "%.1f".format(weightValue).replace(",", ".").toDouble()
            )
        )
        measurementIdCounter++
    }

    // Data pro KREVNÍ TLAK (ID kategorie 2)
    // Generujeme data za poslední rok, cca každých 7 dní
    for (i in 0..52) { // 365 / 7 = 52
        val date = LocalDateTime.now().minusDays(i * 7L).withHour(Random.nextInt(7, 21))
        val measurement = Measurement(
            id = measurementIdCounter,
            categoryId = 2,
            measuredAt = date.toEpochSecond(ZoneOffset.UTC) * 1000
        )
        measurements.add(measurement)

        // Náhodné hodnoty tlaku a pulsu
        val systolicValue = 125.0 + Random.nextDouble(-15.0, 15.0)
        val diastolicValue = 80.0 + Random.nextDouble(-10.0, 10.0)
        val pulseValue = 75.0 + Random.nextDouble(-15.0, 15.0)

        // Systolický tlak (ID pole 2)
        values.add(
            MeasurementValue(
                measurementId = measurementIdCounter,
                categoryFieldId = 2,
                value = systolicValue.toLong().toDouble()
            )
        )
        // Diastolický tlak (ID pole 3)
        values.add(
            MeasurementValue(
                measurementId = measurementIdCounter,
                categoryFieldId = 3,
                value = diastolicValue.toLong().toDouble()
            )
        )
        // Tepová frekvence (ID pole 4)
        values.add(
            MeasurementValue(
                measurementId = measurementIdCounter,
                categoryFieldId = 4,
                value = pulseValue.toLong().toDouble()
            )
        )
        measurementIdCounter++
    }

    return Pair(measurements, values)
}