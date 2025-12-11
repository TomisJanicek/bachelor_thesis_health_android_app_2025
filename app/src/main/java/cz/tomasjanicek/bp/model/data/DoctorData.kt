package cz.tomasjanicek.bp.model.data

import cz.tomasjanicek.bp.model.Doctor

object DoctorData {
    val defaultDoctors = listOf(
        Doctor(
            specialization = "Praktický lékař",
            image = "doctor_general" // název obrázku v drawable (bez .png/.xml)
        ),
        Doctor(
            specialization = "Zubař",
            image = "doctor_dentist"
        ),
        Doctor(
            specialization = "Gynekolog",
            image = "doctor_gyn"
        ),
        Doctor(
            specialization = "Oční (Oftalmologie)",
            image = "doctor_eye"
        ),
        Doctor(
            specialization = "Kožní (Dermatologie)",
            image = "doctor_skin"
        ),
        Doctor(
            specialization = "Ortopedie",
            image = "doctor_ortho"
        ),
        Doctor(
            specialization = "Neurologie",
            image = "doctor_neuro"
        ),
        Doctor(
            specialization = "Alergologie",
            image = "doctor_allergy"
        ),
        Doctor(
            specialization = "Kardiologie",
            image = "doctor_cardio"
        ),
        Doctor(
            specialization = "Chirurgie",
            image = "doctor_surgery"
        ),
        Doctor(
            specialization = "Psychiatrie",
            image = "doctor_psych"
        ),
        Doctor(
            specialization = "Jiné",
            image = "doctor_other"
        )
    )
}