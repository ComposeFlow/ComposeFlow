package io.composeflow.model.project.string

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import io.composeflow.override.mutableStateListEqualsOverrideOf
import io.composeflow.serializer.MutableStateListSerializer
import io.composeflow.serializer.MutableStateSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Escapes special XML characters in text content.
 * Handles: &, <, >, ", '
 */
private fun String.escapeXml(): String =
    this
        .replace("&", "&amp;") // Must be first to avoid double-escaping
        .replace("<", "&lt;")
        .replace(">", "&gt;")
        .replace("\"", "&quot;")
        .replace("'", "&apos;")

@Serializable
@SerialName("StringResourceHolder")
data class StringResourceHolder(
    @Serializable(with = MutableStateListSerializer::class)
    val stringResources: MutableList<StringResource> = mutableStateListEqualsOverrideOf(),
    @Serializable(MutableStateSerializer::class)
    val defaultLocale: MutableState<ResourceLocale> = mutableStateOf(ResourceLocale.ENGLISH_US),
    @Serializable(with = MutableStateListSerializer::class)
    val supportedLocales: MutableList<ResourceLocale> = mutableStateListEqualsOverrideOf(defaultLocale.value),
) {
    /**
     * Generates strings.xml content for each supported locale.
     * Returns a map of locale to XML content.
     * For the default locale, the key will be empty string.
     */
    private fun generateStringsXmlContent(): Map<String, String> {
        if (stringResources.isEmpty()) return emptyMap()

        val result = mutableMapOf<String, String>()

        // Generate for each supported locale
        supportedLocales.forEach { locale ->
            if (stringResources.none { it.localizedValues.contains(locale) }) {
                return@forEach
            }

            val xmlContent =
                buildString {
                    appendLine("<?xml version=\"1.0\" encoding=\"utf-8\"?>")
                    appendLine("<resources>")
                    stringResources.forEach { resource ->
                        resource.localizedValues[locale]?.let { value ->
                            val escapedValue = value.escapeXml()
                            appendLine("    <string name=\"${resource.key}\">$escapedValue</string>")
                        }
                    }
                    appendLine("</resources>")
                }

            // Default locale goes to values/strings.xml (empty key)
            // Other locales go to values-{locale}/strings.xml
            val localeKey = if (locale == defaultLocale.value) "" else locale.toString()
            result[localeKey] = xmlContent
        }

        return result
    }

    /**
     * Generates a map of file paths to XML content for string resources.
     * Returns a map where:
     * - Key: destination path relative to project root
     * - Value: XML content to write
     */
    fun generateStringResourceFiles(): Map<String, String> {
        val xmlContents = generateStringsXmlContent()
        if (xmlContents.isEmpty()) return emptyMap()

        return xmlContents
            .map { (locale, content) ->
                val folder = if (locale.isEmpty()) "values" else "values-$locale"
                val path = "composeApp/src/commonMain/composeResources/$folder/strings.xml"
                path to content
            }.toMap()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is StringResourceHolder) return false

        if (stringResources != other.stringResources) return false
        if (defaultLocale.value != other.defaultLocale.value) return false
        if (supportedLocales != other.supportedLocales) return false

        return true
    }

    override fun hashCode(): Int {
        var result = stringResources.hashCode()
        result = 31 * result + defaultLocale.value.hashCode()
        result = 31 * result + supportedLocales.hashCode()
        return result
    }
}

fun StringResourceHolder.copyContents(other: StringResourceHolder) {
    stringResources.clear()
    stringResources.addAll(other.stringResources)
    defaultLocale.value = other.defaultLocale.value
    supportedLocales.clear()
    supportedLocales.addAll(other.supportedLocales)
}

/**
 * Returns true if the StringResourceHolder has multiple locales,
 * indicating that string resources are translatable.
 */
val StringResourceHolder.isTranslatable: Boolean
    get() = supportedLocales.any { it != defaultLocale.value }
