package io.composeflow.model.parameter.wrapper

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import io.composeflow.kotlinpoet.MemberHolder
import io.composeflow.kotlinpoet.wrapper.CodeBlockWrapper
import io.composeflow.kotlinpoet.wrapper.MemberNameWrapper
import io.composeflow.serializer.FallbackEnumSerializer
import io.composeflow.serializer.LocationAwareColorSerializer
import io.composeflow.serializer.asString
import io.composeflow.ui.common.AppTheme
import kotlinx.serialization.Serializable

val defaultColorWrapper = ColorWrapper(themeColor = null, color = Color.Unspecified)

@Serializable
data class ColorWrapper(
    val themeColor: Material3ColorWrapper? = null,
    @Serializable(with = LocationAwareColorSerializer::class)
    val color: Color? = null,
) {
    @Composable
    fun getColor(): Color? = themeColor?.getAppColor() ?: color

    fun generateCode(): CodeBlockWrapper {
        val builder = CodeBlockWrapper.builder()
        if (themeColor != null) {
            builder.add(
                "%M.colorScheme.${themeColor.colorName}",
                MemberHolder.Material3.MaterialTheme,
            )
        } else {
            val colorMember = MemberNameWrapper.get("androidx.compose.ui.graphics", "Color")
            color?.let {
                builder.add(
                    """%M(${color.asString()})""",
                    colorMember,
                )
            } ?: run {
                builder.add("%M.Unspecified", colorMember)
            }
        }
        return builder.build()
    }

    fun asString(): String = themeColor?.name ?: color!!.asString()
}

object Material3ColorWrapperSerializer :
    FallbackEnumSerializer<Material3ColorWrapper>(Material3ColorWrapper::class)

/**
 * Wrapper class for [Color] to distinguish the same color by semantics.
 * For example, if MaterialTheme.colorScheme.background and MaterialTheme.colorScheme.surface have
 * the same color, it's not possible to distinguish those.
 *
 * To address that issue, this class stores the semantic color name as [Material3ColorWrapper]
 *
 * Also this class has a benefit of constructing the color from non Composable function context.
 */
@Serializable(Material3ColorWrapperSerializer::class)
enum class Material3ColorWrapper(
    val colorName: String,
) {
    Primary("primary"),
    OnPrimary("onPrimary"),
    PrimaryContainer("primaryContainer"),
    OnPrimaryContainer("onPrimaryContainer"),
    Secondary("secondary"),
    OnSecondary("onSecondary"),
    SecondaryContainer("secondaryContainer"),
    OnSecondaryContainer("onSecondaryContainer"),
    Tertiary("tertiary"),
    OnTertiary("onTertiary"),
    TertiaryContainer("tertiaryContainer"),
    OnTertiaryContainer("onTertiaryContainer"),
    Error("error"),
    OnError("onError"),
    ErrorContainer("errorContainer"),
    OnErrorContainer("onErrorContainer"),
    SurfaceDim("surfaceDim"),
    Surface("surface"),
    SurfaceBright("surfaceBright"),
    OnSurface("onSurface"),
    SurfaceContainerLowest("surfaceContainerLowest"),
    SurfaceContainerLow("surfaceContainerLow"),
    SurfaceContainer("surfaceContainer"),
    SurfaceContainerHigh("surfaceContainerHigh"),
    SurfaceContainerHighest("surfaceContainerHighest"),
    SurfaceTint("surfaceTint"),
    InverseSurface("inverseSurface"),
    InverseOnSurface("inverseOnSurface"),
    InversePrimary("inversePrimary"),
    Background("background"),
    OnBackground("onBackground"),
    SurfaceVariant("surfaceVariant"),
    OnSurfaceVariant("onSurfaceVariant"),
    Outline("outline"),
    OutlineVariant("outlineVariant"),
    Scrim("scrim"),
    ;

    @Composable
    fun getAppColor(): Color {
        var color: Color = Color.Unspecified
        AppTheme {
            color =
                when (this) {
                    Primary -> MaterialTheme.colorScheme.primary
                    OnPrimary -> MaterialTheme.colorScheme.onPrimary
                    PrimaryContainer -> MaterialTheme.colorScheme.primaryContainer
                    OnPrimaryContainer -> MaterialTheme.colorScheme.onPrimaryContainer
                    InversePrimary -> MaterialTheme.colorScheme.inversePrimary
                    Secondary -> MaterialTheme.colorScheme.secondary
                    OnSecondary -> MaterialTheme.colorScheme.onSecondary
                    SecondaryContainer -> MaterialTheme.colorScheme.secondaryContainer
                    OnSecondaryContainer -> MaterialTheme.colorScheme.onSecondaryContainer
                    Tertiary -> MaterialTheme.colorScheme.tertiary
                    OnTertiary -> MaterialTheme.colorScheme.onTertiary
                    TertiaryContainer -> MaterialTheme.colorScheme.tertiaryContainer
                    OnTertiaryContainer -> MaterialTheme.colorScheme.onTertiaryContainer
                    Background -> MaterialTheme.colorScheme.background
                    OnBackground -> MaterialTheme.colorScheme.onBackground
                    Surface -> MaterialTheme.colorScheme.surface
                    OnSurface -> MaterialTheme.colorScheme.onSurface
                    SurfaceVariant -> MaterialTheme.colorScheme.surfaceVariant
                    OnSurfaceVariant -> MaterialTheme.colorScheme.onSurfaceVariant
                    SurfaceTint -> MaterialTheme.colorScheme.surfaceTint
                    InverseSurface -> MaterialTheme.colorScheme.inverseSurface
                    InverseOnSurface -> MaterialTheme.colorScheme.inverseOnSurface
                    Error -> MaterialTheme.colorScheme.error
                    OnError -> MaterialTheme.colorScheme.onError
                    ErrorContainer -> MaterialTheme.colorScheme.errorContainer
                    OnErrorContainer -> MaterialTheme.colorScheme.onErrorContainer
                    Outline -> MaterialTheme.colorScheme.outline
                    OutlineVariant -> MaterialTheme.colorScheme.outlineVariant
                    Scrim -> MaterialTheme.colorScheme.scrim
                    SurfaceDim -> MaterialTheme.colorScheme.surfaceDim
                    SurfaceBright -> MaterialTheme.colorScheme.surfaceBright
                    SurfaceContainerLowest -> MaterialTheme.colorScheme.surfaceContainerLowest
                    SurfaceContainerLow -> MaterialTheme.colorScheme.surfaceContainerLow
                    SurfaceContainer -> MaterialTheme.colorScheme.surfaceContainer
                    SurfaceContainerHigh -> MaterialTheme.colorScheme.surfaceContainerHigh
                    SurfaceContainerHighest -> MaterialTheme.colorScheme.surfaceContainerHighest
                }
        }
        return color
    }

    @Composable
    fun getTextColor(): Color =
        when (this) {
            Primary -> OnPrimary.getAppColor()
            OnPrimary -> Primary.getAppColor()
            PrimaryContainer -> OnPrimaryContainer.getAppColor()
            OnPrimaryContainer -> PrimaryContainer.getAppColor()
            InversePrimary -> OnPrimaryContainer.getAppColor()
            Secondary -> OnSecondary.getAppColor()
            OnSecondary -> Secondary.getAppColor()
            SecondaryContainer -> OnSecondaryContainer.getAppColor()
            OnSecondaryContainer -> SecondaryContainer.getAppColor()
            Tertiary -> OnTertiary.getAppColor()
            OnTertiary -> Tertiary.getAppColor()
            TertiaryContainer -> OnTertiaryContainer.getAppColor()
            OnTertiaryContainer -> TertiaryContainer.getAppColor()
            Background -> OnBackground.getAppColor()
            OnBackground -> Background.getAppColor()
            Surface -> OnSurface.getAppColor()
            OnSurface -> Surface.getAppColor()
            SurfaceVariant -> OnSurfaceVariant.getAppColor()
            OnSurfaceVariant -> SurfaceVariant.getAppColor()
            SurfaceTint -> PrimaryContainer.getAppColor()
            InverseSurface -> Surface.getAppColor()
            InverseOnSurface -> OnSurface.getAppColor()
            Error -> OnError.getAppColor()
            OnError -> Error.getAppColor()
            ErrorContainer -> OnErrorContainer.getAppColor()
            OnErrorContainer -> ErrorContainer.getAppColor()
            Outline -> Background.getAppColor()
            OutlineVariant -> Secondary.getAppColor()
            SurfaceDim -> OnSurface.getAppColor()
            SurfaceBright -> OnSurface.getAppColor()
            SurfaceContainerLowest -> OnSurface.getAppColor()
            SurfaceContainerLow -> OnSurface.getAppColor()
            SurfaceContainer -> OnSurface.getAppColor()
            SurfaceContainerHigh -> OnSurface.getAppColor()
            SurfaceContainerHighest -> OnSurface.getAppColor()
            Scrim -> Color.White
        }
}
