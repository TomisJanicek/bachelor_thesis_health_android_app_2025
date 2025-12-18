package cz.tomasjanicek.bp.model.data

import cz.tomasjanicek.bp.model.Doctor

object DoctorData {
    val defaultDoctors = listOf(
        Doctor(
            specialization = "Praktický lékař",
            image = "prakticky_lekar" // název obrázku v drawable (bez .png/.xml)
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
            specialization = "Plicní (Pneumo...)",
            image = "doctor_pneumo"
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
            image = "doctor_chirurgie"
        ),
        Doctor(
            specialization = "Psychiatrie",
            image = "doctor_psych"
        ),
        Doctor(
            specialization = "Revmatologie",
            image = "doctor_revma"
        ),
        Doctor(
            specialization = "Dětský lékař",
            image = "doctor_child"
        ),
        Doctor(
            specialization = "Nutriční lékař",
            image = "doctor_nutrition"
        ),
        Doctor(
            specialization = "Onkologie",
            image = "doctor_onkology"
        ),
        Doctor(
            specialization = "Logopedie",
            image = "doctor_logopedy"
        ),
        Doctor(
            specialization = "Jiné",
            image = "doctor_other"
        )
    )
}