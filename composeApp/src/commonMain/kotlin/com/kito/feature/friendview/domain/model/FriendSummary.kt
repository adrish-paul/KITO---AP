package com.kito.feature.friendview.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class FriendSummary(
    val roll: String,
    val name: String = "",
    val section: String = "",
    val batch: String = "",
    val elective1: String = "",
    val elective2: String = "",
    val isLoading: Boolean = false,
    val notFound: Boolean = false
) {
    val displayName: String
        get() = name.ifBlank { roll }

    val monogram: String
        get() {
            val cleanName = name.trim()
            if (cleanName.isNotBlank()) {
                val parts = cleanName.split(" ").filter { it.isNotBlank() }
                return if (parts.size >= 2) {
                    "${parts[0].first()}${parts[1].first()}".uppercase()
                } else {
                    cleanName.take(2).uppercase()
                }
            }
            if (roll.startsWith("SEC:") && section.isNotBlank()) {
                return section.take(2).uppercase()
            }
            return if (roll.length >= 2) roll.takeLast(2) else roll.ifBlank { "FR" }
        }

    val subtitleText: String
        get() {
            if (notFound) return "Details unavailable"
            if (section.isBlank()) return if (isLoading) "Loading details..." else "No section found"
            val electives = listOf(elective1, elective2).filter { it.isNotBlank() }
            return if (electives.isNotEmpty()) {
                "$section • ${electives.joinToString(", ")}"
            } else {
                section
            }
        }
}
