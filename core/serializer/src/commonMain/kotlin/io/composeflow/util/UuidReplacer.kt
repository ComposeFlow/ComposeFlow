package io.composeflow.util

import kotlin.uuid.Uuid

fun replaceInvalidUuid(yamlContent: String): String {
    val ids = extractIds(yamlContent)
    val formattedIds =
        ids.associateWith {
            formatToUuid(it)
        }
    var result = yamlContent
    formattedIds.forEach { (oldId, newId) ->
        result = result.replace("\"$oldId\"", "\"$newId\"")
    }
    return result
}

internal fun extractIds(yamlContent: String): List<String> {
    val regex = """\s*(?:id|.+?Id):\s*"([^"]+)""".toRegex(RegexOption.MULTILINE)
    return regex.findAll(yamlContent).map { it.groupValues[1] }.toList()
}

internal fun formatToUuid(inputString: String): String {
    try {
        // Attempt to parse the input as a UUID directly. If it's already a valid UUID, just return it.
        return Uuid.parse(inputString).toString()
    } catch (e: IllegalArgumentException) {
        // If parsing fails, proceed with formatting.
        val parts = inputString.split("-")

        if (parts.size != 5) {
            return Uuid.random().toString()
        }

        val formattedParts =
            parts.mapIndexed { index, part ->
                val expectedLength =
                    when (index) {
                        0 -> 8
                        1, 2, 3 -> 4
                        4 -> 12
                        else -> 0 // Should not happen
                    }

                val cleanedPart = part.filter { it in '0'..'9' || it in 'a'..'f' || it in 'A'..'F' }

                if (cleanedPart.isEmpty()) {
                    // If the part is empty after cleaning, generate a random segment
                    generateRandomUuidSegment(expectedLength)
                } else if (cleanedPart.length < expectedLength) {
                    cleanedPart.padEnd(expectedLength, '0')
                } else if (cleanedPart.length > expectedLength) {
                    cleanedPart.substring(0, expectedLength)
                } else {
                    cleanedPart
                }
            }
        return formattedParts.joinToString("-")
    }
}

private fun generateRandomUuidSegment(length: Int): String {
    val uuid = Uuid.random().toString().replace("-", "")
    return if (uuid.length >= length) {
        uuid.substring(0, length)
    } else {
        uuid.padEnd(length, '0')
    }
}
