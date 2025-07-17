package io.composeflow.util

import io.composeflow.ComposeScreenConstant
import io.composeflow.ViewModelConstant

fun generateUniqueName(initial: String, existing: Set<String>): String {
    val existingPlusReserved = existing + ComposeScreenConstant.entries + ViewModelConstant.entries
    if (!existingPlusReserved.contains(initial)) {
        return initial
    }

    var id = 1
    while (true) {
        val name = "${initial}${id++}"
        if (!existingPlusReserved.contains(name)) {
            return name
        }
    }
}

fun String.toPackageName(): String = this
    .replace("-", "_")
    .replace(" ", "")
    .lowercase()

fun String.toKotlinFileName(): String = this
    .replace("-", "_")
    .replace(" ", "")
