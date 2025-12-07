package cz.tomasjanicek.bp.model

enum class InjectionCategory(val displayName: String) {
    MANDATORY("Povinné"),
    RECOMMENDED("Doporučené"),
    TRAVEL("Cestovatelské"),
    RISK_GROUPS("Pro rizikové skupiny"),
    SPECIAL("Speciální a po expozici"),
    HISTORICAL("Historické / méně používané")
}