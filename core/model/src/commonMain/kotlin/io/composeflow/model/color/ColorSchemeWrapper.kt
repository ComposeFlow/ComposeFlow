package io.composeflow.model.color

import androidx.compose.material3.ColorScheme
import androidx.compose.ui.graphics.Color
import io.composeflow.kotlinpoet.wrapper.PropertySpecWrapper
import io.composeflow.kotlinpoet.ClassHolder
import io.composeflow.serializer.LocationAwareColorSerializer
import io.composeflow.serializer.asString
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Wrapper class for [ColorScheme] so that the class can be serialized/deserialized.
 */
@Serializable
@SerialName("ColorSchemeWrapper")
data class ColorSchemeWrapper(
    @Serializable(LocationAwareColorSerializer::class)
    val primary: Color,
    @Serializable(LocationAwareColorSerializer::class)
    val onPrimary: Color,
    @Serializable(LocationAwareColorSerializer::class)
    val primaryContainer: Color,
    @Serializable(LocationAwareColorSerializer::class)
    val onPrimaryContainer: Color,
    @Serializable(LocationAwareColorSerializer::class)
    val inversePrimary: Color,
    @Serializable(LocationAwareColorSerializer::class)
    val secondary: Color,
    @Serializable(LocationAwareColorSerializer::class)
    val onSecondary: Color,
    @Serializable(LocationAwareColorSerializer::class)
    val secondaryContainer: Color,
    @Serializable(LocationAwareColorSerializer::class)
    val onSecondaryContainer: Color,
    @Serializable(LocationAwareColorSerializer::class)
    val tertiary: Color,
    @Serializable(LocationAwareColorSerializer::class)
    val onTertiary: Color,
    @Serializable(LocationAwareColorSerializer::class)
    val tertiaryContainer: Color,
    @Serializable(LocationAwareColorSerializer::class)
    val onTertiaryContainer: Color,
    @Serializable(LocationAwareColorSerializer::class)
    val background: Color,
    @Serializable(LocationAwareColorSerializer::class)
    val onBackground: Color,
    @Serializable(LocationAwareColorSerializer::class)
    val surface: Color,
    @Serializable(LocationAwareColorSerializer::class)
    val onSurface: Color,
    @Serializable(LocationAwareColorSerializer::class)
    val surfaceVariant: Color,
    @Serializable(LocationAwareColorSerializer::class)
    val onSurfaceVariant: Color,
    @Serializable(LocationAwareColorSerializer::class)
    val surfaceTint: Color,
    @Serializable(LocationAwareColorSerializer::class)
    val inverseSurface: Color,
    @Serializable(LocationAwareColorSerializer::class)
    val inverseOnSurface: Color,
    @Serializable(LocationAwareColorSerializer::class)
    val error: Color,
    @Serializable(LocationAwareColorSerializer::class)
    val onError: Color,
    @Serializable(LocationAwareColorSerializer::class)
    val errorContainer: Color,
    @Serializable(LocationAwareColorSerializer::class)
    val onErrorContainer: Color,
    @Serializable(LocationAwareColorSerializer::class)
    val outline: Color,
    @Serializable(LocationAwareColorSerializer::class)
    val outlineVariant: Color,
    @Serializable(LocationAwareColorSerializer::class)
    val scrim: Color,
    @Serializable(LocationAwareColorSerializer::class)
    val surfaceBright: Color,
    @Serializable(LocationAwareColorSerializer::class)
    val surfaceDim: Color,
    @Serializable(LocationAwareColorSerializer::class)
    val surfaceContainer: Color,
    @Serializable(LocationAwareColorSerializer::class)
    val surfaceContainerHigh: Color,
    @Serializable(LocationAwareColorSerializer::class)
    val surfaceContainerHighest: Color,
    @Serializable(LocationAwareColorSerializer::class)
    val surfaceContainerLow: Color,
    @Serializable(LocationAwareColorSerializer::class)
    val surfaceContainerLowest: Color,
) {
    fun toColorScheme(): ColorScheme =
        ColorScheme(
            primary = this.primary,
            onPrimary = this.onPrimary,
            primaryContainer = this.primaryContainer,
            onPrimaryContainer = this.onPrimaryContainer,
            inversePrimary = this.inversePrimary,
            secondary = this.secondary,
            onSecondary = this.onSecondary,
            secondaryContainer = this.secondaryContainer,
            onSecondaryContainer = this.onSecondaryContainer,
            tertiary = this.tertiary,
            onTertiary = this.onTertiary,
            tertiaryContainer = this.tertiaryContainer,
            onTertiaryContainer = this.onTertiaryContainer,
            background = this.background,
            onBackground = this.onBackground,
            surface = this.surface,
            onSurface = this.onSurface,
            surfaceVariant = this.surfaceVariant,
            onSurfaceVariant = this.onSurfaceVariant,
            surfaceTint = this.surfaceTint,
            inverseSurface = this.inverseSurface,
            inverseOnSurface = this.inverseOnSurface,
            error = this.error,
            onError = this.onError,
            errorContainer = this.errorContainer,
            onErrorContainer = this.onErrorContainer,
            outline = this.outline,
            outlineVariant = this.outlineVariant,
            scrim = this.scrim,
            surfaceBright = this.surfaceBright,
            surfaceDim = this.surfaceDim,
            surfaceContainer = this.surfaceContainer,
            surfaceContainerHigh = this.surfaceContainerHigh,
            surfaceContainerHighest = this.surfaceContainerHighest,
            surfaceContainerLow = this.surfaceContainerLow,
            surfaceContainerLowest = this.surfaceContainerLowest,
        )

    fun generateColorProperties(suffix: String = ""): List<PropertySpecWrapper> =
        listOf(
            PropertySpecWrapper
                .builder("primary$suffix", ClassHolder.AndroidX.Ui.Color)
                .initializer("%T(${primary.asString()})", ClassHolder.AndroidX.Ui.Color)
                .build(),
            PropertySpecWrapper
                .builder("onPrimary$suffix", ClassHolder.AndroidX.Ui.Color)
                .initializer("%T(${onPrimary.asString()})", ClassHolder.AndroidX.Ui.Color)
                .build(),
            PropertySpecWrapper
                .builder("primaryContainer$suffix", ClassHolder.AndroidX.Ui.Color)
                .initializer("%T(${primaryContainer.asString()})", ClassHolder.AndroidX.Ui.Color)
                .build(),
            PropertySpecWrapper
                .builder("onPrimaryContainer$suffix", ClassHolder.AndroidX.Ui.Color)
                .initializer("%T(${onPrimaryContainer.asString()})", ClassHolder.AndroidX.Ui.Color)
                .build(),
            PropertySpecWrapper
                .builder("inversePrimary$suffix", ClassHolder.AndroidX.Ui.Color)
                .initializer("%T(${inversePrimary.asString()})", ClassHolder.AndroidX.Ui.Color)
                .build(),
            PropertySpecWrapper
                .builder("secondary$suffix", ClassHolder.AndroidX.Ui.Color)
                .initializer("%T(${secondary.asString()})", ClassHolder.AndroidX.Ui.Color)
                .build(),
            PropertySpecWrapper
                .builder("onSecondary$suffix", ClassHolder.AndroidX.Ui.Color)
                .initializer("%T(${onSecondary.asString()})", ClassHolder.AndroidX.Ui.Color)
                .build(),
            PropertySpecWrapper
                .builder("secondaryContainer$suffix", ClassHolder.AndroidX.Ui.Color)
                .initializer("%T(${secondaryContainer.asString()})", ClassHolder.AndroidX.Ui.Color)
                .build(),
            PropertySpecWrapper
                .builder("onSecondaryContainer$suffix", ClassHolder.AndroidX.Ui.Color)
                .initializer(
                    "%T(${onSecondaryContainer.asString()})",
                    ClassHolder.AndroidX.Ui.Color,
                ).build(),
            PropertySpecWrapper
                .builder("tertiary$suffix", ClassHolder.AndroidX.Ui.Color)
                .initializer("%T(${tertiary.asString()})", ClassHolder.AndroidX.Ui.Color)
                .build(),
            PropertySpecWrapper
                .builder("onTertiary$suffix", ClassHolder.AndroidX.Ui.Color)
                .initializer("%T(${onTertiary.asString()})", ClassHolder.AndroidX.Ui.Color)
                .build(),
            PropertySpecWrapper
                .builder("tertiaryContainer$suffix", ClassHolder.AndroidX.Ui.Color)
                .initializer("%T(${tertiaryContainer.asString()})", ClassHolder.AndroidX.Ui.Color)
                .build(),
            PropertySpecWrapper
                .builder("onTertiaryContainer$suffix", ClassHolder.AndroidX.Ui.Color)
                .initializer("%T(${onTertiaryContainer.asString()})", ClassHolder.AndroidX.Ui.Color)
                .build(),
            PropertySpecWrapper
                .builder("background$suffix", ClassHolder.AndroidX.Ui.Color)
                .initializer("%T(${background.asString()})", ClassHolder.AndroidX.Ui.Color)
                .build(),
            PropertySpecWrapper
                .builder("onBackground$suffix", ClassHolder.AndroidX.Ui.Color)
                .initializer("%T(${onBackground.asString()})", ClassHolder.AndroidX.Ui.Color)
                .build(),
            PropertySpecWrapper
                .builder("surface$suffix", ClassHolder.AndroidX.Ui.Color)
                .initializer("%T(${surface.asString()})", ClassHolder.AndroidX.Ui.Color)
                .build(),
            PropertySpecWrapper
                .builder("onSurface$suffix", ClassHolder.AndroidX.Ui.Color)
                .initializer("%T(${onSurface.asString()})", ClassHolder.AndroidX.Ui.Color)
                .build(),
            PropertySpecWrapper
                .builder("surfaceVariant$suffix", ClassHolder.AndroidX.Ui.Color)
                .initializer("%T(${surfaceVariant.asString()})", ClassHolder.AndroidX.Ui.Color)
                .build(),
            PropertySpecWrapper
                .builder("onSurfaceVariant$suffix", ClassHolder.AndroidX.Ui.Color)
                .initializer("%T(${onSurfaceVariant.asString()})", ClassHolder.AndroidX.Ui.Color)
                .build(),
            PropertySpecWrapper
                .builder("surfaceTint$suffix", ClassHolder.AndroidX.Ui.Color)
                .initializer("%T(${surfaceTint.asString()})", ClassHolder.AndroidX.Ui.Color)
                .build(),
            PropertySpecWrapper
                .builder("inverseSurface$suffix", ClassHolder.AndroidX.Ui.Color)
                .initializer("%T(${inverseSurface.asString()})", ClassHolder.AndroidX.Ui.Color)
                .build(),
            PropertySpecWrapper
                .builder("inverseOnSurface$suffix", ClassHolder.AndroidX.Ui.Color)
                .initializer("%T(${inverseOnSurface.asString()})", ClassHolder.AndroidX.Ui.Color)
                .build(),
            PropertySpecWrapper
                .builder("error$suffix", ClassHolder.AndroidX.Ui.Color)
                .initializer("%T(${error.asString()})", ClassHolder.AndroidX.Ui.Color)
                .build(),
            PropertySpecWrapper
                .builder("onError$suffix", ClassHolder.AndroidX.Ui.Color)
                .initializer("%T(${onError.asString()})", ClassHolder.AndroidX.Ui.Color)
                .build(),
            PropertySpecWrapper
                .builder("errorContainer$suffix", ClassHolder.AndroidX.Ui.Color)
                .initializer("%T(${errorContainer.asString()})", ClassHolder.AndroidX.Ui.Color)
                .build(),
            PropertySpecWrapper
                .builder("onErrorContainer$suffix", ClassHolder.AndroidX.Ui.Color)
                .initializer("%T(${onErrorContainer.asString()})", ClassHolder.AndroidX.Ui.Color)
                .build(),
            PropertySpecWrapper
                .builder("outline$suffix", ClassHolder.AndroidX.Ui.Color)
                .initializer("%T(${outline.asString()})", ClassHolder.AndroidX.Ui.Color)
                .build(),
            PropertySpecWrapper
                .builder("outlineVariant$suffix", ClassHolder.AndroidX.Ui.Color)
                .initializer("%T(${outlineVariant.asString()})", ClassHolder.AndroidX.Ui.Color)
                .build(),
            PropertySpecWrapper
                .builder("scrim$suffix", ClassHolder.AndroidX.Ui.Color)
                .initializer("%T(${scrim.asString()})", ClassHolder.AndroidX.Ui.Color)
                .build(),
            PropertySpecWrapper
                .builder("surfaceBright$suffix", ClassHolder.AndroidX.Ui.Color)
                .initializer("%T(${surfaceBright.asString()})", ClassHolder.AndroidX.Ui.Color)
                .build(),
            PropertySpecWrapper
                .builder("surfaceDim$suffix", ClassHolder.AndroidX.Ui.Color)
                .initializer("%T(${surfaceDim.asString()})", ClassHolder.AndroidX.Ui.Color)
                .build(),
            PropertySpecWrapper
                .builder("surfaceContainer$suffix", ClassHolder.AndroidX.Ui.Color)
                .initializer("%T(${surfaceContainer.asString()})", ClassHolder.AndroidX.Ui.Color)
                .build(),
            PropertySpecWrapper
                .builder("surfaceContainerHigh$suffix", ClassHolder.AndroidX.Ui.Color)
                .initializer(
                    "%T(${surfaceContainerHigh.asString()})",
                    ClassHolder.AndroidX.Ui.Color,
                ).build(),
            PropertySpecWrapper
                .builder("surfaceContainerHighest$suffix", ClassHolder.AndroidX.Ui.Color)
                .initializer(
                    "%T(${surfaceContainerHighest.asString()})",
                    ClassHolder.AndroidX.Ui.Color,
                ).build(),
            PropertySpecWrapper
                .builder("surfaceContainerLow$suffix", ClassHolder.AndroidX.Ui.Color)
                .initializer("%T(${surfaceContainerLow.asString()})", ClassHolder.AndroidX.Ui.Color)
                .build(),
            PropertySpecWrapper
                .builder("surfaceContainerLowest$suffix", ClassHolder.AndroidX.Ui.Color)
                .initializer(
                    "%T(${surfaceContainerLowest.asString()})",
                    ClassHolder.AndroidX.Ui.Color,
                ).build(),
        )

    companion object {
        fun fromColorScheme(colorScheme: ColorScheme): ColorSchemeWrapper =
            ColorSchemeWrapper(
                primary = colorScheme.primary,
                onPrimary = colorScheme.onPrimary,
                primaryContainer = colorScheme.primaryContainer,
                onPrimaryContainer = colorScheme.onPrimaryContainer,
                inversePrimary = colorScheme.inversePrimary,
                secondary = colorScheme.secondary,
                onSecondary = colorScheme.onSecondary,
                secondaryContainer = colorScheme.secondaryContainer,
                onSecondaryContainer = colorScheme.onSecondaryContainer,
                tertiary = colorScheme.tertiary,
                onTertiary = colorScheme.onTertiary,
                tertiaryContainer = colorScheme.tertiaryContainer,
                onTertiaryContainer = colorScheme.onTertiaryContainer,
                background = colorScheme.background,
                onBackground = colorScheme.onBackground,
                surface = colorScheme.surface,
                onSurface = colorScheme.onSurface,
                surfaceVariant = colorScheme.surfaceVariant,
                onSurfaceVariant = colorScheme.onSurfaceVariant,
                surfaceTint = colorScheme.surfaceTint,
                inverseSurface = colorScheme.inverseSurface,
                inverseOnSurface = colorScheme.inverseOnSurface,
                error = colorScheme.error,
                onError = colorScheme.onError,
                errorContainer = colorScheme.errorContainer,
                onErrorContainer = colorScheme.onErrorContainer,
                outline = colorScheme.outline,
                outlineVariant = colorScheme.outlineVariant,
                scrim = colorScheme.scrim,
                surfaceBright = colorScheme.surfaceBright,
                surfaceDim = colorScheme.surfaceDim,
                surfaceContainer = colorScheme.surfaceContainer,
                surfaceContainerHigh = colorScheme.surfaceContainerHigh,
                surfaceContainerHighest = colorScheme.surfaceContainerHighest,
                surfaceContainerLow = colorScheme.surfaceContainerLow,
                surfaceContainerLowest = colorScheme.surfaceContainerLowest,
            )
    }
}
