package cz.tomasjanicek.bp.ui.screens.examination.doctorEdit

/**
 * Interface definující uživatelské akce na obrazovce pro úpravu lékaře.
 */
interface DoctorEditAction {

    fun saveDoctor()
    fun onNameChanged(name: String)
    fun onPhoneChanged(phone: String)
    fun onEmailChanged(email: String)
    fun onLocationChanged(location: String)
    fun onSubtitleChanged(subtitle: String) // Přidáno dle vašeho seznamu
    /**
     * Spustí navigaci na obrazovku pro výběr polohy na mapě.
     */
}