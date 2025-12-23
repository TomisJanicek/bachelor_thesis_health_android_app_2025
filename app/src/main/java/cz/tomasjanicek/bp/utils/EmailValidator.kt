package cz.tomasjanicek.bp.utils

import android.util.Patterns

object EmailValidator {
    fun isValid(email: String): Boolean {
        if (email.isBlank()) return true
        // Tady voláme tu Androidí třídu, ale teď je to zabalené
        return Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
}