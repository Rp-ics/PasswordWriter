package com.passwordwriter.app.service

import java.security.SecureRandom

object PasswordGenerator {

    private val random = SecureRandom()

    data class Options(
        val length: Int = 16,
        val useUpper: Boolean = true,
        val useLower: Boolean = true,
        val useDigits: Boolean = true,
        val useSymbols: Boolean = false
    )

    private val UPPER = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
    private val LOWER = "abcdefghijklmnopqrstuvwxyz"
    private val DIGITS = "0123456789"
    private val SYMBOLS = "!@#$%^&*()_+-=[]{}|;:,.<>?"

    fun generate(options: Options): String {
        val chars = StringBuilder()
        if (options.useUpper) chars.append(UPPER)
        if (options.useLower) chars.append(LOWER)
        if (options.useDigits) chars.append(DIGITS)
        if (options.useSymbols) chars.append(SYMBOLS)

        if (chars.isEmpty()) chars.append(LOWER)

        val pool = chars.toString()
        val len = options.length.coerceIn(4, 128)
        return (1..len).map { pool[random.nextInt(pool.length)] }.joinToString("")
    }
}
