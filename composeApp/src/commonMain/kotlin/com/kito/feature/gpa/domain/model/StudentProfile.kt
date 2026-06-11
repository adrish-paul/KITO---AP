package com.kito.feature.gpa.domain.model

/** Minimal student info the GPA screen needs — section to derive branch, roll for display. */
data class StudentProfile(
    val roll: String,
    val section: String,
    val batch: String,
)
