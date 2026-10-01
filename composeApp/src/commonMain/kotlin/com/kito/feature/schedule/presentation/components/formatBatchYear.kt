package com.kito.feature.schedule.presentation.components

fun formatBatchYear(batch: String): String {
    return when (batch.lowercase()) {
        "batch_1" -> "Year 1"
        "batch_2" -> "Year 2"
        "batch_3" -> "Year 3"
        "batch_4" -> "Year 4"
        else -> {
            val num = Regex("\\d+").find(batch)?.value
            if (num != null) {
                "Year $num"
            } else {
                batch
            }
        }
    }
}
