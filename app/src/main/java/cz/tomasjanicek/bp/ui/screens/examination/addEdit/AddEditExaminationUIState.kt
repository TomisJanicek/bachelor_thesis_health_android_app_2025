package cz.tomasjanicek.bp.ui.screens.examination.addEdit

// Sealed class je ideální pro reprezentaci stavů, které se vzájemně vylučují.
// Obrazovka může být POUZE v jednom z těchto stavů najednou.
sealed class AddEditExaminationUIState {

    // Stav 1: Načítání
    // Použije se, když se obrazovka poprvé otevírá a načítá data
    // (např. existující prohlídku nebo seznam lékařů).
    data object Loading : AddEditExaminationUIState()

    // Stav 2: Prohlídka byla úspěšně uložena
    // Po úspěšném uložení ViewModel přepne stav na tento,
    // což v UI vyvolá navigaci zpět na předchozí obrazovku.
    data object ExaminationSaved : AddEditExaminationUIState()

    // Stav 3: Prohlídka byla úspěšně smazána
    // Podobné jako 'ExaminationSaved', použije se pro navigaci zpět po smazání.
    data object ExaminationDeleted : AddEditExaminationUIState()

    // Stav 4: Hlavní stav, kdy uživatel interaguje s formulářem
    // Tento stav drží všechna data z formuláře (purpose, doctor, note atd.)
    // a případné chyby. Kdykoliv uživatel něco změní, ViewModel vytvoří
    // novou instanci tohoto stavu s aktualizovanými daty.
    data class ExaminationChanged(val data: AddEditExaminationData) : AddEditExaminationUIState()
}