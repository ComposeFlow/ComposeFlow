package io.composeflow.model.parameter.wrapper

import androidx.compose.runtime.Composable
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TileMode
import io.composeflow.kotlinpoet.wrapper.CodeBlockWrapper
import io.composeflow.kotlinpoet.wrapper.MemberNameWrapper
import io.composeflow.serializer.FallbackEnumSerializer
import kotlinx.serialization.Serializable

val defaultBrushWrapper =
    BrushWrapper(
        brushType = BrushType.LinearGradient,
        colors =
            listOf(
                ColorWrapper(color = Color.Blue),
                ColorWrapper(color = Color.Red),
            ),
        tileMode = null,
    )

@Serializable
data class BrushWrapper(
    val brushType: BrushType,
    val colors: List<ColorWrapper>,
    val colorStops: List<Float>? = null,
    val tileMode: TileModeWrapper? = null,
    val startX: Float = 0f,
    val startY: Float = 0f,
    val endX: Float = Float.POSITIVE_INFINITY,
    val endY: Float = Float.POSITIVE_INFINITY,
    val centerX: Float = 0.5f,
    val centerY: Float = 0.5f,
    val radius: Float = Float.POSITIVE_INFINITY,
) {
    @Composable
    fun getBrush(): Brush? {
        if (colors.isEmpty()) return null

        val resolvedColors = colors.mapNotNull { it.getColor() }
        if (resolvedColors.isEmpty()) return null

        val composeTileMode = tileMode?.toTileMode() ?: TileMode.Clamp

        return when (brushType) {
            BrushType.LinearGradient -> {
                if (colorStops != null && colorStops.size == resolvedColors.size) {
                    Brush.linearGradient(
                        colorStops = colorStops.zip(resolvedColors).toTypedArray(),
                        start = Offset(startX, startY),
                        end = Offset(endX, endY),
                        tileMode = composeTileMode,
                    )
                } else {
                    Brush.linearGradient(
                        colors = resolvedColors,
                        start = Offset(startX, startY),
                        end = Offset(endX, endY),
                        tileMode = composeTileMode,
                    )
                }
            }

            BrushType.HorizontalGradient -> {
                if (colorStops != null && colorStops.size == resolvedColors.size) {
                    Brush.horizontalGradient(
                        colorStops = colorStops.zip(resolvedColors).toTypedArray(),
                        tileMode = composeTileMode,
                    )
                } else {
                    Brush.horizontalGradient(
                        colors = resolvedColors,
                        tileMode = composeTileMode,
                    )
                }
            }

            BrushType.VerticalGradient -> {
                if (colorStops != null && colorStops.size == resolvedColors.size) {
                    Brush.verticalGradient(
                        colorStops = colorStops.zip(resolvedColors).toTypedArray(),
                        tileMode = composeTileMode,
                    )
                } else {
                    Brush.verticalGradient(
                        colors = resolvedColors,
                        tileMode = composeTileMode,
                    )
                }
            }

            BrushType.RadialGradient -> {
                if (colorStops != null && colorStops.size == resolvedColors.size) {
                    Brush.radialGradient(
                        colorStops = colorStops.zip(resolvedColors).toTypedArray(),
                        center = Offset(centerX, centerY),
                        radius = radius,
                        tileMode = composeTileMode,
                    )
                } else {
                    Brush.radialGradient(
                        colors = resolvedColors,
                        center = Offset(centerX, centerY),
                        radius = radius,
                        tileMode = composeTileMode,
                    )
                }
            }

            BrushType.SweepGradient -> {
                if (colorStops != null && colorStops.size == resolvedColors.size) {
                    Brush.sweepGradient(
                        colorStops = colorStops.zip(resolvedColors).toTypedArray(),
                        center = Offset(centerX, centerY),
                    )
                } else {
                    Brush.sweepGradient(
                        colors = resolvedColors,
                        center = Offset(centerX, centerY),
                    )
                }
            }
        }
    }

    fun generateCode(): CodeBlockWrapper {
        val builder = CodeBlockWrapper.builder()
        val brushMember = MemberNameWrapper.get("androidx.compose.ui.graphics", "Brush")

        if (colors.isEmpty()) {
            builder.add("null")
            return builder.build()
        }

        when (brushType) {
            BrushType.LinearGradient -> {
                builder.add("%M.linearGradient(", brushMember)

                if (colorStops != null && colorStops.size == colors.size) {
                    builder.add("colorStops = arrayOf(")
                    colors.zip(colorStops).forEachIndexed { index, (color, stop) ->
                        if (index > 0) builder.add(", ")
                        builder.add("$stop to ")
                        builder.add(color.generateCode())
                    }
                    builder.add(")")
                } else {
                    builder.add("colors = listOf(")
                    colors.forEachIndexed { index, color ->
                        if (index > 0) builder.add(", ")
                        builder.add(color.generateCode())
                    }
                    builder.add(")")
                }

                if (startX != 0f || startY != 0f) {
                    builder.add(
                        ", start = %M(${startX}f, ${startY}f)",
                        MemberNameWrapper.get("androidx.compose.ui.geometry", "Offset"),
                    )
                }
                if (endX != Float.POSITIVE_INFINITY || endY != Float.POSITIVE_INFINITY) {
                    builder.add(
                        ", end = %M(${endX}f, ${endY}f)",
                        MemberNameWrapper.get("androidx.compose.ui.geometry", "Offset"),
                    )
                }
                if (tileMode != null) {
                    builder.add(
                        ", tileMode = %M.${tileMode.toTileMode()}",
                        MemberNameWrapper.get("androidx.compose.ui.graphics", "TileMode"),
                    )
                }
            }

            BrushType.HorizontalGradient -> {
                builder.add("%M.horizontalGradient(", brushMember)

                if (colorStops != null && colorStops.size == colors.size) {
                    builder.add("colorStops = arrayOf(")
                    colors.zip(colorStops).forEachIndexed { index, (color, stop) ->
                        if (index > 0) builder.add(", ")
                        builder.add("$stop to ")
                        builder.add(color.generateCode())
                    }
                    builder.add(")")
                } else {
                    builder.add("colors = listOf(")
                    colors.forEachIndexed { index, color ->
                        if (index > 0) builder.add(", ")
                        builder.add(color.generateCode())
                    }
                    builder.add(")")
                }

                if (tileMode != null) {
                    builder.add(
                        ", tileMode = %M.${tileMode.toTileMode()}",
                        MemberNameWrapper.get("androidx.compose.ui.graphics", "TileMode"),
                    )
                }
            }

            BrushType.VerticalGradient -> {
                builder.add("%M.verticalGradient(", brushMember)

                if (colorStops != null && colorStops.size == colors.size) {
                    builder.add("colorStops = arrayOf(")
                    colors.zip(colorStops).forEachIndexed { index, (color, stop) ->
                        if (index > 0) builder.add(", ")
                        builder.add("$stop to ")
                        builder.add(color.generateCode())
                    }
                    builder.add(")")
                } else {
                    builder.add("colors = listOf(")
                    colors.forEachIndexed { index, color ->
                        if (index > 0) builder.add(", ")
                        builder.add(color.generateCode())
                    }
                    builder.add(")")
                }

                if (tileMode != null) {
                    builder.add(
                        ", tileMode = %M.${tileMode.toTileMode()}",
                        MemberNameWrapper.get("androidx.compose.ui.graphics", "TileMode"),
                    )
                }
            }

            BrushType.RadialGradient -> {
                builder.add("%M.radialGradient(", brushMember)

                if (colorStops != null && colorStops.size == colors.size) {
                    builder.add("colorStops = arrayOf(")
                    colors.zip(colorStops).forEachIndexed { index, (color, stop) ->
                        if (index > 0) builder.add(", ")
                        builder.add("$stop to ")
                        builder.add(color.generateCode())
                    }
                    builder.add(")")
                } else {
                    builder.add("colors = listOf(")
                    colors.forEachIndexed { index, color ->
                        if (index > 0) builder.add(", ")
                        builder.add(color.generateCode())
                    }
                    builder.add(")")
                }

                if (centerX != 0.5f || centerY != 0.5f) {
                    builder.add(
                        ", center = %M(${centerX}f, ${centerY}f)",
                        MemberNameWrapper.get("androidx.compose.ui.geometry", "Offset"),
                    )
                }
                if (radius != Float.POSITIVE_INFINITY) {
                    builder.add(", radius = ${radius}f")
                }
                if (tileMode != null) {
                    builder.add(
                        ", tileMode = %M.${tileMode.toTileMode()}",
                        MemberNameWrapper.get("androidx.compose.ui.graphics", "TileMode"),
                    )
                }
            }

            BrushType.SweepGradient -> {
                builder.add("%M.sweepGradient(", brushMember)

                if (colorStops != null && colorStops.size == colors.size) {
                    builder.add("colorStops = arrayOf(")
                    colors.zip(colorStops).forEachIndexed { index, (color, stop) ->
                        if (index > 0) builder.add(", ")
                        builder.add("$stop to ")
                        builder.add(color.generateCode())
                    }
                    builder.add(")")
                } else {
                    builder.add("colors = listOf(")
                    colors.forEachIndexed { index, color ->
                        if (index > 0) builder.add(", ")
                        builder.add(color.generateCode())
                    }
                    builder.add(")")
                }

                if (centerX != 0.5f || centerY != 0.5f) {
                    builder.add(
                        ", center = %M(${centerX}f, ${centerY}f)",
                        MemberNameWrapper.get("androidx.compose.ui.geometry", "Offset"),
                    )
                }
            }
        }

        builder.add(")")
        return builder.build()
    }

    fun asString(): String {
        val colorNames = colors.map { it.asString() }
        return "${brushType.displayName} (${colorNames.joinToString(", ")})"
    }
}

object BrushTypeSerializer : FallbackEnumSerializer<BrushType>(BrushType::class)

@Serializable(BrushTypeSerializer::class)
enum class BrushType(
    val displayName: String,
) {
    LinearGradient("Linear Gradient"),
    HorizontalGradient("Horizontal Gradient"),
    VerticalGradient("Vertical Gradient"),
    RadialGradient("Radial Gradient"),
    SweepGradient("Sweep Gradient"),
}
