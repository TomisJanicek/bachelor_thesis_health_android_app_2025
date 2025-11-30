package cz.tomasjanicek.bp.ui.screens.examination.addEdit

import cz.tomasjanicek.bp.model.Doctor
import cz.tomasjanicek.bp.model.ExaminationStatus
import cz.tomasjanicek.bp.model.ExaminationType

/**
 * Interface definující všechny uživatelské akce, které lze provést
 * na obrazovce pro přidání nebo úpravu prohlídky (`AddEditExaminationScreen`).
 */
interface AddEditExaminationAction {

    // --- Akce pro uložení a smazání ---

    /**
     * Uloží aktuální stav prohlídky (ať už novou nebo existující) do databáze.
     */
    fun saveExamination()

    /**
     * Smaže aktuálně upravovanou prohlídku z databáze.
     * Tato akce by měla být dostupná pouze v režimu úprav.
     */
    fun deleteExamination()


    // --- Akce pro změnu hodnot formuláře ---

    /**
     * Aktualizuje účel prohlídky (např. "Preventivní prohlídka", "Bolest zubu").
     * @param purpose Nový textový řetězec účelu.
     */
    fun onPurposeChanged(purpose: String)

    /**
     * Aktualizuje typ prohlídky (např. PROHLIDKA, ZAKROK).
     * @param type Nově vybraný typ z enumu [ExaminationType].
     */
    fun onTypeChanged(type: ExaminationType)

    /**
     * Aktualizuje datum a čas konání prohlídky.
     * @param dateTime Nový čas a datum vyjádřený jako [Long] (timestamp).
     */
    fun onDateTimeChanged(dateTime: Long)

    /**
     * Aktualizuje vybraného lékaře pro danou prohlídku.
     * @param doctorId Nově vybraný objekt [Long].
     */
    fun onDoctorChanged(doctorId: Long)

    /**
     * Aktualizuje volitelnou poznámku k prohlídce.
     * @param note Nový text poznámky, může být i prázdný.
     */
    fun onNoteChanged(note: String)

    /**
     * Aktualizuje volitelný výsledek prohlídky (např. po návštěvě lékaře).
     * @param result Nový text výsledku, může být i prázdný.
     */
    fun onResultChanged(result: String)

    /**
     * Aktualizuje stav prohlídky (např. PLANNED, COMPLETED, CANCELLED).
     * @param status Nový stav z enumu [ExaminationStatus].
     */
    fun onStatusChanged(status: ExaminationStatus)

}