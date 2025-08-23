package io.composeflow

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.capitalize
import androidx.compose.ui.text.intl.Locale
import com.mohamedrejeb.richeditor.model.rememberRichTextState
import io.github.nomisrev.JsonPath
import io.github.nomisrev.path
import kotlinx.serialization.json.JsonElement

fun <T> MutableList<T>.swap(
    index1: Int,
    index2: Int,
) {
    val tmp = this[index1]
    this[index1] = this[index2]
    this[index2] = tmp
}

fun String.withoutNewLines() =
    this
        .split("\n")
        .joinToString("")

fun String.trimForCompare() = this.trim().withoutNewLines().filterNot { it.isWhitespace() }

fun String.replaceSpaces() = this.replace("\\s+".toRegex(), "")

/**
 * Returns the indices of the characters in this string that are part of the subsequence match of the given substring.
 */
fun String.getSubsequenceMatchIndices(sub: String): Set<Int> {
    val indices = mutableSetOf<Int>()
    var subIndex = 0

    for ((i, c) in withIndex()) {
        if (subIndex < sub.length && c == sub[subIndex]) {
            indices.add(i)
            subIndex++
        }
        if (subIndex == sub.length) break
    }

    return if (subIndex == sub.length) indices else emptySet()
}

fun JsonElement.selectString(
    jsonPath: String,
    replaceQuotation: Boolean = false,
): String {
    val extracted = JsonPath.path(jsonPath).getOrNull(this).toString()
    return if (replaceQuotation) {
        extracted.replace("\"", "")
    } else {
        extracted
    }
}

fun <T> Array<T>.random(): T = get(indices.random())

fun <T> List<T>.eachEquals(
    other: List<T>,
    predicate: (T, T) -> Boolean,
): Boolean {
    if (size != other.size) return false
    var result = true
    forEachIndexed { i, t ->
        if (!predicate(t, other[i])) {
            result = false
            return@forEachIndexed
        }
    }
    return result
}

fun String.removeLineBreak() = this.replace("\r\n|\r|\n".toRegex(), "")

fun <K, V> MutableMap<K, V>.copyAsMutableStateMap(): MutableMap<K, V> =
    mutableStateMapOf<K, V>().apply {
        putAll(this@copyAsMutableStateMap)
    }

fun String.asClassName() =
    this
        .replace("-", "_")
        .replace(" ", "")
        .capitalize(Locale.current)

fun String.asVariableName() =
    this
        .replace("-", "_")
        .replace(" ", "")
        .replaceFirstChar { it.lowercase() }

@Composable
fun String.toRichHtmlString(): AnnotatedString {
    val state = rememberRichTextState()

    LaunchedEffect(this) {
        state.setHtml(this@toRichHtmlString)
    }

    return state.annotatedString
}
