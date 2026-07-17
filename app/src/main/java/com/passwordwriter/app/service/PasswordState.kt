package com.passwordwriter.app.service

object PasswordState {
    var pendingPassword: String? = null
    var isCountdownActive: Boolean = false
    var countdownRemaining: Int = 0
    var onCountdownTick: ((Int) -> Unit)? = null
    var onCountdownEnd: (() -> Unit)? = null
    var onPasswordReady: ((String) -> Unit)? = null

    fun schedulePassword(password: String) {
        pendingPassword = password
        isCountdownActive = true
        countdownRemaining = 5
        notifyPasswordReady(password)
    }

    fun clear() {
        pendingPassword = null
        isCountdownActive = false
        countdownRemaining = 0
        onCountdownTick = null
        onCountdownEnd = null
        onPasswordReady = null
    }

    private fun notifyPasswordReady(password: String) {
        onPasswordReady?.invoke(password)
    }
}
