package com.kito.core.utils

actual fun Double.formatDecimal(digits: Int): String = "%.${digits}f".format(this)
