package com.kito.core.auth

import com.kito.core.auth.AccessPolicy.isEmailAllowed


/**
 * Pure helpers for the KIIT access rule and identity extraction.
 *
 * IMPORTANT: [isEmailAllowed] is a CLIENT-SIDE convenience check for UX only. The authoritative,
 * tamper-proof enforcement is the server-side Supabase auth hook (domain + allowed_emails table).
 * Never rely on this alone to keep non-KIIT users out.
 */
object AccessPolicy {

    const val ALLOWED_DOMAIN: String = "kiit.ac.in"

    /**
     * Allowed if the email is on the university domain OR explicitly allowlisted.
     * [allowlist] entries should be lowercase.
     */
    fun isEmailAllowed(email: String, allowlist: Set<String> = emptySet()): Boolean {
        val normalized = email.trim().lowercase()
        return normalized.endsWith("@$ALLOWED_DOMAIN") || normalized in allowlist
    }

    /**
     * Roll number = numeric local part of the email (e.g. 23053382@kiit.ac.in -> 23053382).
     * Returns null if the local part isn't a plain numeric roll (e.g. allowlisted reviewer accounts).
     */
    fun rollFromEmail(email: String): String? {
        val local = email.substringBefore("@").trim()
        return local.takeIf { it.isNotEmpty() && it.all(Char::isDigit) }
    }
}
