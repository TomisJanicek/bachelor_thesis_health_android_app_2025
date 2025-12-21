package cz.tomasjanicek.bp.model.data

object MeasurementData {

    // Pomocná třída pro definici pole (bez ID a categoryId)
    data class FieldDef(
        val name: String, // klíč pro kód (např. "systolic")
        val label: String, // název pro uživatele (např. "Systolický")
        val unit: String?,
        val min: Double? = null,
        val max: Double? = null
    )

    // Pomocná třída pro definici kategorie
    data class CategoryDef(
        val name: String,
        val description: String?,
        val fields: List<FieldDef>
    )

    // SEZNAM DEFAULTNÍCH KATEGORIÍ
    val defaultCategories = listOf(
        CategoryDef(
            name = "Krevní tlak",
            description = "Měření systolického a diastolického tlaku a tepu.",
            fields = listOf(
                FieldDef("systolic", "Systolický tlak", "mmHg", 0.0, 300.0),
                FieldDef("diastolic", "Diastolický tlak", "mmHg", 0.0, 200.0),
                FieldDef("pulse", "Tep", "bpm", 0.0, 250.0)
            )
        ),
        CategoryDef(
            name = "Hmotnost",
            description = "Tělesná hmotnost a BMI.",
            fields = listOf(
                FieldDef("weight", "Váha", "kg", 0.0, 300.0),
                FieldDef("bmi", "BMI", null, 0.0, 100.0),
                FieldDef("fat", "Tělesný tuk", "%", 0.0, 100.0)
            )
        ),
        CategoryDef(
            name = "Krevní cukr",
            description = "Glykémie.",
            fields = listOf(
                FieldDef("glucose", "Glukóza", "mmol/L", 0.0, 50.0),
                FieldDef("meal_context", "Doba měření", null) // např. 1 = před jídlem, 2 = po jídle (pozn: toto by se dalo řešit i jinak, ale pro číselné hodnoty stačí takto)
            )
        ),
        CategoryDef(
            name = "Teplota",
            description = "Tělesná teplota.",
            fields = listOf(
                FieldDef("temperature", "Teplota", "°C", 30.0, 45.0)
            )
        ),
        CategoryDef(
            name = "Krevní testy",
            description = "Základní biochemie a krevní obraz (dle norem ČSKB/WHO).",
            fields = listOf(
                // 1. Cholesterol - klíčový pro kardiovaskulární riziko
                FieldDef(
                    name = "cholesterol_total",
                    label = "Celkový cholesterol",
                    unit = "mmol/L",
                    min = 2.9,
                    max = 5.0 // Doporučená horní hranice pro dospělé
                ),
                FieldDef(
                    name = "cholesterol_ldl",
                    label = "LDL ('Zlý') cholesterol",
                    unit = "mmol/L",
                    min = 1.2,
                    max = 3.0 // U rizikových pacientů by mělo být méně, toto je obecná norma
                ),
                FieldDef(
                    name = "cholesterol_hdl",
                    label = "HDL ('Hodný') cholesterol",
                    unit = "mmol/L",
                    min = 1.0, // Muži > 1.0, Ženy > 1.2
                    max = 3.0 // Zde spíše platí "čím více, tím lépe"
                ),

                // 2. Zánět
                FieldDef(
                    name = "crp",
                    label = "CRP (Zánět)",
                    unit = "mg/L",
                    min = 0.0,
                    max = 5.0 // Norma do 5 mg/L, při infekci stoupá
                ),

                // 3. Krevní obraz (Červené/Bílé krvinky, Destičky)
                FieldDef(
                    name = "hgb",
                    label = "Hemoglobin",
                    unit = "g/L",
                    min = 120.0, // Ženy od 120, Muži od 135
                    max = 175.0
                ),
                FieldDef(
                    name = "wbc",
                    label = "Leukocyty (WBC)",
                    unit = "10^9/L",
                    min = 4.0,
                    max = 10.0
                ),
                FieldDef(
                    name = "plt",
                    label = "Trombocyty (Destičky)",
                    unit = "10^9/L",
                    min = 150.0,
                    max = 400.0
                ),

                // 4. Játra (volitelné, ale časté)
                FieldDef(
                    name = "alt",
                    label = "ALT (Jaterní test)",
                    unit = "µkat/L",
                    min = 0.1,
                    max = 0.8 // Orientační horní mez (muži mají vyšší)
                )
            )
        )
    )
}