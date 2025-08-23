package io.composeflow.model.modifier

import androidx.annotation.FloatRange
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.debugInspectorInfo
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import io.composeflow.kotlinpoet.wrapper.CodeBlockBuilderWrapper
import io.composeflow.kotlinpoet.wrapper.CodeBlockWrapper
import io.composeflow.kotlinpoet.wrapper.MemberNameWrapper
import io.composeflow.kotlinpoet.GenerationContext
import io.composeflow.kotlinpoet.MemberHolder
import io.composeflow.model.action.ActionType
import io.composeflow.model.parameter.BoxTrait
import io.composeflow.model.parameter.ColumnTrait
import io.composeflow.model.parameter.ComposeTrait
import io.composeflow.model.parameter.RowTrait
import io.composeflow.model.parameter.wrapper.AlignmentHorizontalWrapper
import io.composeflow.model.parameter.wrapper.AlignmentVerticalWrapper
import io.composeflow.model.parameter.wrapper.AlignmentWrapper
import io.composeflow.model.parameter.wrapper.ColorWrapper
import io.composeflow.model.parameter.wrapper.Material3ColorWrapper
import io.composeflow.model.parameter.wrapper.ShapeWrapper
import io.composeflow.model.parameter.wrapper.defaultBrushWrapper
import io.composeflow.model.project.Project
import io.composeflow.model.project.appscreen.screen.composenode.ComposeNode
import io.composeflow.model.project.issue.Issue
import io.composeflow.model.property.AssignableProperty
import io.composeflow.model.property.BrushProperty
import io.composeflow.model.property.ColorProperty
import io.composeflow.serializer.LocationAwareDpNonNegativeSerializer
import io.composeflow.serializer.LocationAwareDpSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
@SerialName("ModifierWrapper")
sealed class ModifierWrapper(
    // Doesn't have to be restored because this is a tentative property
    @Transient
    val visible: MutableState<Boolean> = mutableStateOf(true),
) {
    @Serializable
    @SerialName("Alpha")
    data class Alpha(
        @FloatRange(from = 0.0, to = 1.0)
        val alpha: Float = 1f,
    ) : ModifierWrapper() {
        override fun toModifier(): Modifier = Modifier.alpha(alpha)

        override fun displayName(): String = "Alpha"

        override fun generateCode(
            project: Project,
            context: GenerationContext,
            codeBlockBuilder: CodeBlockBuilderWrapper,
            dryRun: Boolean,
        ): CodeBlockBuilderWrapper {
            val member = MemberNameWrapper.get("androidx.compose.ui.draw", "alpha")
            val alpha = if (alpha == 1.0f) "" else "alpha = ${alpha}f"
            codeBlockBuilder.addStatement(".%M($alpha)", member)
            return codeBlockBuilder
        }

        override fun category() = ModifierCategory.Drawing

        override fun tooltipText(): String = "Draw content with modified alpha that may be less than 1."
    }

    @Serializable
    @SerialName("AspectRatio")
    data class AspectRatio(
        val ratio: Float,
        val matchHeightConstraintsFirst: Boolean? = null,
    ) : ModifierWrapper() {
        override fun toModifier(): Modifier =
            Modifier.aspectRatio(
                ratio,
                matchHeightConstraintsFirst ?: false,
            )

        override fun displayName(): String = "Aspect ratio"

        override fun generateCode(
            project: Project,
            context: GenerationContext,
            codeBlockBuilder: CodeBlockBuilderWrapper,
            dryRun: Boolean,
        ): CodeBlockBuilderWrapper {
            val aspectRatioMember = MemberNameWrapper.get("androidx.compose.foundation.layout", "aspectRatio")
            val ratioParam = "ratio = ${ratio}f,"
            val matchHeightConstraintsFirstParam =
                if (matchHeightConstraintsFirst != null) {
                    "matchHeightConstraintsFirst = $matchHeightConstraintsFirst,"
                } else {
                    ""
                }
            codeBlockBuilder
                .addStatement(
                    ".%M($ratioParam $matchHeightConstraintsFirstParam)",
                    aspectRatioMember,
                )
            return codeBlockBuilder
        }

        override fun category() = ModifierCategory.Size

        override fun tooltipText(): String = "Attempts to size the content to match a specified aspect ratio"
    }

    @Serializable
    @SerialName("Background")
    data class Background(
        val colorWrapper: AssignableProperty =
            ColorProperty.ColorIntrinsicValue(
                ColorWrapper(Material3ColorWrapper.SecondaryContainer),
            ),
        val brushWrapper: AssignableProperty? = null,
        val shapeWrapper: ShapeWrapper = ShapeWrapper.Rectangle,
    ) : ModifierWrapper() {
        fun defaultColorProperty() =
            ColorProperty.ColorIntrinsicValue(
                ColorWrapper(Material3ColorWrapper.SecondaryContainer),
            )

        fun defaultBrushProperty() =
            BrushProperty.BrushIntrinsicValue(
                defaultBrushWrapper,
            )

        override fun toModifier(): Modifier =
            Modifier.composed {
                // Check if we have a brush first
                val resolvedBrush =
                    (brushWrapper as? BrushProperty.BrushIntrinsicValue)?.value?.getBrush()

                if (resolvedBrush != null) {
                    Modifier.background(
                        brush = resolvedBrush,
                        shape = shapeWrapper.toShape(),
                    )
                } else {
                    // Fall back to color
                    val resolvedColor =
                        if ((colorWrapper as? ColorProperty.ColorIntrinsicValue)?.value?.getColor() != null) {
                            (colorWrapper as? ColorProperty.ColorIntrinsicValue)?.value?.getColor()
                        } else if (defaultColorProperty().value.getColor() != null) {
                            defaultColorProperty().value.getColor()
                        } else {
                            Color.Unspecified
                        }

                    Modifier.background(
                        color = resolvedColor!!,
                        shape = shapeWrapper.toShape(),
                    )
                }
            }

        override fun displayName(): String = "Background"

        override fun generateCode(
            project: Project,
            context: GenerationContext,
            codeBlockBuilder: CodeBlockBuilderWrapper,
            dryRun: Boolean,
        ): CodeBlockBuilderWrapper {
            val backgroundMember = MemberNameWrapper.get("androidx.compose.foundation", "background")
            codeBlockBuilder.addStatement(
                """.%M(""",
                backgroundMember,
            )

            // Check if we have a brush first
            if (brushWrapper != null) {
                codeBlockBuilder.add("brush = ")
                codeBlockBuilder.add(
                    brushWrapper.transformedCodeBlock(
                        project,
                        context,
                        dryRun = dryRun,
                    ),
                )
            } else {
                // Fall back to color
                codeBlockBuilder.add("color = ")
                codeBlockBuilder.add(
                    colorWrapper.transformedCodeBlock(
                        project,
                        context,
                        dryRun = dryRun,
                    ),
                )
            }
            codeBlockBuilder.addStatement(",")

            shapeWrapper.generateCode(codeBlockBuilder)
            codeBlockBuilder.addStatement(")")
            return codeBlockBuilder
        }

        override fun category() = ModifierCategory.Drawing

        override fun tooltipText(): String = "Draws shape with a solid color behind the content."
    }

    @Serializable
    @SerialName("Border")
    data class Border(
        @Serializable(with = LocationAwareDpSerializer::class)
        val width: Dp,
        val colorWrapper: AssignableProperty =
            ColorProperty.ColorIntrinsicValue(
                ColorWrapper(Material3ColorWrapper.Outline),
            ),
        val shapeWrapper: ShapeWrapper = ShapeWrapper.Rectangle,
    ) : ModifierWrapper() {
        fun defaultColorProperty() =
            ColorProperty.ColorIntrinsicValue(
                ColorWrapper(Material3ColorWrapper.Outline),
            )

        override fun toModifier(): Modifier =
            Modifier.composed {
                val resolvedColor =
                    if ((colorWrapper as? ColorProperty.ColorIntrinsicValue)?.value?.getColor() != null) {
                        (colorWrapper as? ColorProperty.ColorIntrinsicValue)?.value?.getColor()
                    } else if (defaultColorProperty().value.getColor() != null) {
                        defaultColorProperty().value.getColor()
                    } else {
                        Color.Unspecified
                    }
                Modifier.border(
                    width = width,
                    color = resolvedColor!!,
                    shape = shapeWrapper.toShape(),
                )
            }

        override fun displayName(): String = "Border"

        override fun generateCode(
            project: Project,
            context: GenerationContext,
            codeBlockBuilder: CodeBlockBuilderWrapper,
            dryRun: Boolean,
        ): CodeBlockBuilderWrapper {
            val borderMember = MemberNameWrapper.get("androidx.compose.foundation", "border")
            val dpMember = MemberNameWrapper.get("androidx.compose.ui.unit", "dp")
            codeBlockBuilder.addStatement(
                """.%M(width = ${width.value.toInt()}.%M,""",
                borderMember,
                dpMember,
            )
            codeBlockBuilder.add("color = ")
            codeBlockBuilder.add(
                colorWrapper.transformedCodeBlock(
                    project,
                    context,
                    dryRun = dryRun,
                ),
            )
            codeBlockBuilder.addStatement(",")
            shapeWrapper.generateCode(codeBlockBuilder)
            codeBlockBuilder.addStatement(")")
            return codeBlockBuilder
        }

        override fun category() = ModifierCategory.Border

        override fun tooltipText(): String =
            """Modify element to add border with appearance specified with a width,
a color and a shape and clip it"""
    }

    @Serializable
    @SerialName("Clip")
    data class Clip(
        val shapeWrapper: ShapeWrapper = ShapeWrapper.Rectangle,
    ) : ModifierWrapper() {
        // TODO: When rendered in canvas, this also clips the border and Composable label. Is it possible to avoid it?
        override fun toModifier(): Modifier = Modifier.clip(shape = shapeWrapper.toShape())

        override fun displayName(): String = "Clip"

        override fun generateCode(
            project: Project,
            context: GenerationContext,
            codeBlockBuilder: CodeBlockBuilderWrapper,
            dryRun: Boolean,
        ): CodeBlockBuilderWrapper {
            val member = MemberNameWrapper.get("androidx.compose.ui.draw", "clip")

            codeBlockBuilder.addStatement(".%M(", member)
            shapeWrapper.generateCode(codeBlockBuilder)
            codeBlockBuilder.addStatement(")")
            return codeBlockBuilder
        }

        override fun category() = ModifierCategory.Drawing

        override fun tooltipText(): String = "Clip the content to shape"
    }

    @Serializable
    @SerialName("Height")
    data class Height(
        @Serializable(with = LocationAwareDpSerializer::class)
        val height: Dp,
    ) : ModifierWrapper() {
        override fun toModifier(): Modifier = Modifier.height(height = height)

        override fun displayName(): String = "Height"

        override fun generateCode(
            project: Project,
            context: GenerationContext,
            codeBlockBuilder: CodeBlockBuilderWrapper,
            dryRun: Boolean,
        ): CodeBlockBuilderWrapper {
            val heightMember = MemberNameWrapper.get("androidx.compose.foundation.layout", "height")
            val dpMember = MemberNameWrapper.get("androidx.compose.ui.unit", "dp")
            codeBlockBuilder
                .addStatement(".%M(${height.value.toInt()}.%M)", heightMember, dpMember)
            return codeBlockBuilder
        }

        override fun heightDecider(parent: ComposeTrait?) = "${height.value.toInt()}dp"

        override fun category() = ModifierCategory.Size

        override fun tooltipText(): String = "Declare the preferred height of the content to be exactly height dp"
    }

    @Serializable
    @SerialName("FillMaxHeight")
    data class FillMaxHeight(
        @FloatRange(from = 0.0, to = 1.0)
        val fraction: Float = 1f,
    ) : ModifierWrapper() {
        override fun toModifier(): Modifier = Modifier.fillMaxHeight(fraction)

        override fun displayName(): String = "Fill max height"

        override fun generateCode(
            project: Project,
            context: GenerationContext,
            codeBlockBuilder: CodeBlockBuilderWrapper,
            dryRun: Boolean,
        ): CodeBlockBuilderWrapper {
            val member = MemberNameWrapper.get("androidx.compose.foundation.layout", "fillMaxHeight")
            val fraction = if (fraction == 1.0f) "" else "fraction = ${fraction}f"
            codeBlockBuilder.addStatement(".%M($fraction)", member)
            return codeBlockBuilder
        }

        override fun heightDecider(parent: ComposeTrait?) = fraction.fillAsString()

        override fun category() = ModifierCategory.Size

        override fun tooltipText(): String = "Have the content fill (possibly only partially)"
    }

    @Serializable
    @SerialName("FillMaxWidth")
    data class FillMaxWidth(
        @FloatRange(from = 0.0, to = 1.0)
        val fraction: Float = 1f,
    ) : ModifierWrapper() {
        override fun toModifier(): Modifier = Modifier.fillMaxWidth(fraction)

        override fun displayName(): String = "Fill max width"

        override fun generateCode(
            project: Project,
            context: GenerationContext,
            codeBlockBuilder: CodeBlockBuilderWrapper,
            dryRun: Boolean,
        ): CodeBlockBuilderWrapper {
            val member = MemberNameWrapper.get("androidx.compose.foundation.layout", "fillMaxWidth")
            val fraction = if (fraction == 1.0f) "" else "fraction = ${fraction}f"
            codeBlockBuilder.addStatement(".%M($fraction)", member)
            return codeBlockBuilder
        }

        override fun widthDecider(parent: ComposeTrait?) = fraction.fillAsString()

        override fun category() = ModifierCategory.Size

        override fun tooltipText(): String = "Have the content fill (possibly only partially)"
    }

    @Serializable
    @SerialName("FillMaxSize")
    data class FillMaxSize(
        @FloatRange(from = 0.0, to = 1.0)
        val fraction: Float = 1f,
    ) : ModifierWrapper() {
        override fun toModifier(): Modifier = Modifier.fillMaxSize(fraction)

        override fun displayName(): String = "Fill max size"

        override fun generateCode(
            project: Project,
            context: GenerationContext,
            codeBlockBuilder: CodeBlockBuilderWrapper,
            dryRun: Boolean,
        ): CodeBlockBuilderWrapper {
            val member = MemberNameWrapper.get("androidx.compose.foundation.layout", "fillMaxSize")
            val fraction = if (fraction == 1.0f) "" else "fraction = ${fraction}f"
            codeBlockBuilder.addStatement(".%M($fraction)", member)
            return codeBlockBuilder
        }

        override fun heightDecider(parent: ComposeTrait?) = fraction.fillAsString()

        override fun widthDecider(parent: ComposeTrait?) = fraction.fillAsString()

        override fun category() = ModifierCategory.Size

        override fun tooltipText(): String = "Have the content fill (possibly only partially)"
    }

    @Serializable
    @SerialName("Offset")
    data class Offset(
        @Serializable(with = LocationAwareDpSerializer::class)
        val x: Dp = 0.dp,
        @Serializable(with = LocationAwareDpSerializer::class)
        val y: Dp = 0.dp,
    ) : ModifierWrapper() {
        override fun toModifier(): Modifier = Modifier.offset(x = x, y = y)

        override fun displayName(): String = "Offset"

        override fun generateCode(
            project: Project,
            context: GenerationContext,
            codeBlockBuilder: CodeBlockBuilderWrapper,
            dryRun: Boolean,
        ): CodeBlockBuilderWrapper {
            val offsetMember = MemberNameWrapper.get("androidx.compose.foundation.layout", "offset")
            val dpMember = MemberNameWrapper.get("androidx.compose.ui.unit", "dp")
            codeBlockBuilder
                .addStatement(
                    ".%M(x = ${x.value.toInt()}.%M, y = ${y.value.toInt()}.%M)",
                    offsetMember,
                    dpMember,
                    dpMember,
                )
            return codeBlockBuilder
        }

        override fun category() = ModifierCategory.Position

        override fun tooltipText(): String = "Offset the content by (x dp, y dp)"
    }

    @Serializable
    @SerialName("Padding")
    data class Padding(
        @Serializable(with = LocationAwareDpNonNegativeSerializer::class)
        val start: Dp = 0.dp,
        @Serializable(with = LocationAwareDpNonNegativeSerializer::class)
        val top: Dp = 0.dp,
        @Serializable(with = LocationAwareDpNonNegativeSerializer::class)
        val end: Dp = 0.dp,
        @Serializable(with = LocationAwareDpNonNegativeSerializer::class)
        val bottom: Dp = 0.dp,
    ) : ModifierWrapper() {
        constructor(all: Dp) : this(start = all, top = all, end = all, bottom = all)

        constructor(horizontal: Dp, vertical: Dp) :
            this(start = horizontal, end = horizontal, top = vertical, bottom = vertical)

        override fun toModifier(): Modifier = Modifier.padding(start = start, top = top, end = end, bottom = bottom)

        override fun displayName(): String = "Padding"

        override fun generateCode(
            project: Project,
            context: GenerationContext,
            codeBlockBuilder: CodeBlockBuilderWrapper,
            dryRun: Boolean,
        ): CodeBlockBuilderWrapper {
            val paddingMember = MemberNameWrapper.get("androidx.compose.foundation.layout", "padding")
            val dpMember = MemberNameWrapper.get("androidx.compose.ui.unit", "dp")
            codeBlockBuilder.add(".%M(", paddingMember)
            when (spec()) {
                PaddingSpec.All -> {
                    codeBlockBuilder.add(
                        "all = ${start.value.toInt()}.%M",
                        dpMember,
                    )
                }

                PaddingSpec.HorizontalVertical -> {
                    // Horizontal and vertical paddings
                    if (start.value != 0f) {
                        codeBlockBuilder.addStatement(
                            "horizontal = ${start.value.toInt()}.%M,",
                            dpMember,
                        )
                    }
                    if (top.value != 0f) {
                        codeBlockBuilder.addStatement(
                            "vertical = ${top.value.toInt()}.%M",
                            dpMember,
                        )
                    }
                }

                PaddingSpec.Individual -> {
                    if (start.value != 0f) {
                        codeBlockBuilder.addStatement(
                            "start = ${start.value.toInt()}.%M,",
                            dpMember,
                        )
                    }
                    if (top.value != 0f) {
                        codeBlockBuilder.addStatement("top = ${top.value.toInt()}.%M,", dpMember)
                    }
                    if (end.value != 0f) {
                        codeBlockBuilder.addStatement("end = ${end.value.toInt()}.%M,", dpMember)
                    }
                    if (bottom.value != 0f) {
                        codeBlockBuilder.addStatement(
                            "bottom = ${bottom.value.toInt()}.%M",
                            dpMember,
                        )
                    }
                }
            }
            codeBlockBuilder.addStatement(")")
            return codeBlockBuilder
        }

        override fun category() = ModifierCategory.Padding

        override fun tooltipText(): String = "Apply additional space along each edge of the content"

        fun spec(): PaddingSpec =
            if (start == end && top == bottom) {
                if (start == top) {
                    PaddingSpec.All
                } else {
                    PaddingSpec.HorizontalVertical
                }
            } else {
                PaddingSpec.Individual
            }

        infix operator fun plus(another: Padding): Padding =
            Padding(
                start = start.plus(another.start),
                top = top.plus(another.top),
                end = end.plus(another.end),
                bottom = bottom.plus(another.bottom),
            )

        enum class PaddingSpec {
            All,
            HorizontalVertical,
            Individual,
        }
    }

    @Serializable
    @SerialName("Rotate")
    data class Rotate(
        val degrees: Float = 0f,
    ) : ModifierWrapper() {
        override fun toModifier(): Modifier = Modifier.rotate(degrees)

        override fun displayName(): String = "Rotate"

        override fun generateCode(
            project: Project,
            context: GenerationContext,
            codeBlockBuilder: CodeBlockBuilderWrapper,
            dryRun: Boolean,
        ): CodeBlockBuilderWrapper {
            val member = MemberNameWrapper.get("androidx.compose.ui.draw", "rotate")
            val degreesParam = "${degrees}f"
            codeBlockBuilder.addStatement(".%M($degreesParam)", member)
            return codeBlockBuilder
        }

        override fun category() = ModifierCategory.Transformations

        override fun tooltipText(): String = "Sets the degrees the view is rotated around the center of the composable"
    }

    @Serializable
    @SerialName("Scale")
    data class Scale(
        val scaleX: Float? = null,
        val scaleY: Float? = null,
    ) : ModifierWrapper() {
        constructor(scale: Float) : this(scale, scale)

        override fun toModifier(): Modifier =
            Modifier.scale(
                scaleX = scaleX ?: 1f,
                scaleY = scaleY ?: 1f,
            )

        override fun displayName(): String = "Scale"

        override fun generateCode(
            project: Project,
            context: GenerationContext,
            codeBlockBuilder: CodeBlockBuilderWrapper,
            dryRun: Boolean,
        ): CodeBlockBuilderWrapper {
            val scaleMember = MemberNameWrapper.get("androidx.compose.ui.draw", "scale")
            codeBlockBuilder.addStatement(".%M(", scaleMember)
            when (spec()) {
                ScaleSpec.All -> {
                    codeBlockBuilder.addStatement("${scaleX ?: 1.0}f")
                }

                ScaleSpec.XandY -> {
                    val scaleXParam = "scaleX = ${scaleX ?: 1.0}f,"
                    val scaleYParam = "scaleY = ${scaleY ?: 1.0}f"
                    codeBlockBuilder.addStatement("$scaleXParam$scaleYParam")
                }
            }
            codeBlockBuilder.addStatement(")")
            return codeBlockBuilder
        }

        override fun category() = ModifierCategory.Transformations

        override fun tooltipText(): String =
            """Scale the contents of the composable by the following
scale factors along the horizontal and vertical axis respectively"""

        fun spec(): ScaleSpec =
            if (scaleX == scaleY) {
                ScaleSpec.All
            } else {
                ScaleSpec.XandY
            }

        enum class ScaleSpec {
            All,
            XandY,
        }
    }

    @Serializable
    @SerialName("Shadow")
    data class Shadow(
        @Serializable(LocationAwareDpSerializer::class)
        val elevation: Dp = 0.dp,
        val shapeWrapper: ShapeWrapper = ShapeWrapper.Rectangle,
    ) : ModifierWrapper() {
        override fun toModifier(): Modifier = Modifier.shadow(elevation = elevation, shape = shapeWrapper.toShape())

        override fun displayName(): String = "Shadow"

        override fun generateCode(
            project: Project,
            context: GenerationContext,
            codeBlockBuilder: CodeBlockBuilderWrapper,
            dryRun: Boolean,
        ): CodeBlockBuilderWrapper {
            val shadowMember = MemberNameWrapper.get("androidx.compose.ui.draw", "shadow")
            codeBlockBuilder.addStatement(
                ".%M(elevation = ${elevation.value.toInt()}.%M,",
                shadowMember,
                MemberHolder.AndroidX.Ui.dp,
            )
            shapeWrapper.generateCode(codeBlockBuilder)
            codeBlockBuilder.addStatement(")")
            return codeBlockBuilder
        }

        override fun category() = ModifierCategory.Drawing

        override fun tooltipText(): String = """Creates a graphicsLayer that draws a shadow"""
    }

    @Serializable
    @SerialName("Size")
    data class Size(
        @Serializable(with = LocationAwareDpSerializer::class)
        val width: Dp = Dp.Unspecified,
        @Serializable(with = LocationAwareDpSerializer::class)
        val height: Dp = Dp.Unspecified,
    ) : ModifierWrapper() {
        constructor(size: Dp) : this(width = size, height = size)

        override fun toModifier(): Modifier = Modifier.size(width = width, height = height)

        override fun displayName(): String = "Size"

        override fun generateCode(
            project: Project,
            context: GenerationContext,
            codeBlockBuilder: CodeBlockBuilderWrapper,
            dryRun: Boolean,
        ): CodeBlockBuilderWrapper {
            val sizeMember = MemberNameWrapper.get("androidx.compose.foundation.layout", "size")
            val dpMember = MemberNameWrapper.get("androidx.compose.ui.unit", "dp")
            val dpObjectMember = MemberNameWrapper.get("androidx.compose.ui.unit", "Dp")
            codeBlockBuilder.addStatement(".%M(", sizeMember)
            if (width != Dp.Unspecified) {
                codeBlockBuilder.addStatement("width = ${width.value.toInt()}.%M,", dpMember)
            } else {
                codeBlockBuilder.addStatement("width = %M.Unspecified,", dpObjectMember)
            }
            if (height != Dp.Unspecified) {
                codeBlockBuilder.addStatement("height = ${height.value.toInt()}.%M,", dpMember)
            } else {
                codeBlockBuilder.addStatement("height = %M.Unspecified,", dpObjectMember)
            }
            codeBlockBuilder.addStatement(")")
            return codeBlockBuilder
        }

        override fun category() = ModifierCategory.Size

        override fun tooltipText(): String = """Declare the preferred size of the content to be exactly width dp by height dp"""

        override fun heightDecider(parent: ComposeTrait?) = if (height != Dp.Unspecified) "${height.value.toInt()}dp" else null

        override fun widthDecider(parent: ComposeTrait?) = if (width != Dp.Unspecified) "${width.value.toInt()}dp" else null
    }

    @Serializable
    @SerialName("HorizontalScroll")
    data object HorizontalScroll : ModifierWrapper() {
        override fun toModifier(): Modifier =
            Modifier.composed {
                Modifier.horizontalScroll(rememberScrollState())
            }

        override fun displayName(): String = "HorizontalScroll"

        override fun generateCode(
            project: Project,
            context: GenerationContext,
            codeBlockBuilder: CodeBlockBuilderWrapper,
            dryRun: Boolean,
        ): CodeBlockBuilderWrapper {
            val verticalScroll = MemberNameWrapper.get("androidx.compose.foundation", "horizontalScroll")
            val rememberScrollState =
                MemberNameWrapper.get("androidx.compose.foundation", "rememberScrollState")
            codeBlockBuilder.addStatement(".%M(%M())", verticalScroll, rememberScrollState)
            return codeBlockBuilder
        }

        override fun category() = ModifierCategory.Scroll

        override fun tooltipText(): String =
            """Modify element to allow to scroll horizontally when width of the content is bigger than max constraints allow"""
    }

    @Serializable
    @SerialName("VerticalScroll")
    data object VerticalScroll : ModifierWrapper() {
        override fun toModifier(): Modifier =
            Modifier.composed {
                Modifier.verticalScroll(rememberScrollState())
            }

        override fun displayName(): String = "VerticalScroll"

        override fun generateCode(
            project: Project,
            context: GenerationContext,
            codeBlockBuilder: CodeBlockBuilderWrapper,
            dryRun: Boolean,
        ): CodeBlockBuilderWrapper {
            val verticalScroll = MemberNameWrapper.get("androidx.compose.foundation", "verticalScroll")
            val rememberScrollState =
                MemberNameWrapper.get("androidx.compose.foundation", "rememberScrollState")
            codeBlockBuilder.addStatement(".%M(%M())", verticalScroll, rememberScrollState)
            return codeBlockBuilder
        }

        override fun category() = ModifierCategory.Scroll

        override fun tooltipText(): String =
            """Modify element to allow to scroll vertically when height of the content is bigger than max constraints allow"""
    }

    @Serializable
    @SerialName("Width")
    data class Width(
        @Serializable(with = LocationAwareDpSerializer::class)
        val width: Dp,
    ) : ModifierWrapper() {
        override fun toModifier(): Modifier = Modifier.width(width = width)

        override fun displayName(): String = "Width"

        override fun generateCode(
            project: Project,
            context: GenerationContext,
            codeBlockBuilder: CodeBlockBuilderWrapper,
            dryRun: Boolean,
        ): CodeBlockBuilderWrapper {
            val widthMember = MemberNameWrapper.get("androidx.compose.foundation.layout", "width")
            val dpMember = MemberNameWrapper.get("androidx.compose.ui.unit", "dp")
            codeBlockBuilder.addStatement(
                ".%M(${width.value.toInt()}.%M)",
                widthMember,
                dpMember,
            )
            return codeBlockBuilder
        }

        override fun category() = ModifierCategory.Size

        override fun tooltipText(): String = "Declare the preferred width of the content to be exactly width dp"

        override fun widthDecider(parent: ComposeTrait?) = if (width != Dp.Unspecified) "${width.value.toInt()}dp" else null
    }

    @Serializable
    @SerialName("WrapContentHeight")
    data class WrapContentHeight(
        val align: AlignmentVerticalWrapper? = null,
        val unbounded: Boolean? = null,
    ) : ModifierWrapper() {
        override fun toModifier(): Modifier =
            Modifier.wrapContentHeight(
                align?.alignment ?: Alignment.CenterVertically,
                unbounded ?: false,
            )

        override fun displayName(): String = "Wrap content height"

        override fun generateCode(
            project: Project,
            context: GenerationContext,
            codeBlockBuilder: CodeBlockBuilderWrapper,
            dryRun: Boolean,
        ): CodeBlockBuilderWrapper {
            codeBlockBuilder.addStatement(
                ".%M(",
                MemberNameWrapper.get("androidx.compose.foundation.layout", "wrapContentHeight"),
            )
            align?.let {
                codeBlockBuilder.addStatement(
                    "align = %M.${it.name},",
                    MemberNameWrapper.get("androidx.compose.ui", "Alignment"),
                )
            }
            unbounded?.let {
                codeBlockBuilder.addStatement("unbounded = $it")
            }
            codeBlockBuilder.addStatement(")")
            return codeBlockBuilder
        }

        override fun category() = ModifierCategory.Size

        override fun tooltipText(): String =
            """Allow the content to measure at its desired height
without regard for the incoming measurement"""
    }

    @Serializable
    @SerialName("WrapContentSize")
    data class WrapContentSize(
        val align: AlignmentWrapper? = null,
        val unbounded: Boolean? = null,
    ) : ModifierWrapper() {
        override fun toModifier(): Modifier =
            Modifier.wrapContentSize(
                align?.alignment ?: Alignment.Center,
                unbounded ?: false,
            )

        override fun displayName(): String = "Wrap content size"

        override fun generateCode(
            project: Project,
            context: GenerationContext,
            codeBlockBuilder: CodeBlockBuilderWrapper,
            dryRun: Boolean,
        ): CodeBlockBuilderWrapper {
            codeBlockBuilder.addStatement(
                ".%M(",
                MemberNameWrapper.get("androidx.compose.foundation.layout", "wrapContentSize"),
            )
            align?.let {
                codeBlockBuilder.addStatement(
                    "align = %M.${it.name},",
                    MemberNameWrapper.get("androidx.compose.ui", "Alignment"),
                )
            }
            unbounded?.let {
                codeBlockBuilder.addStatement("unbounded = $it")
            }
            codeBlockBuilder.addStatement(")")
            return codeBlockBuilder
        }

        override fun category() = ModifierCategory.Size

        override fun tooltipText(): String =
            """Allow the content to measure at its desired size without
regard for the incoming measurement"""
    }

    @Serializable
    @SerialName("WrapContentWidth")
    data class WrapContentWidth(
        val align: AlignmentHorizontalWrapper? = null,
        val unbounded: Boolean? = null,
    ) : ModifierWrapper() {
        override fun toModifier(): Modifier =
            Modifier.wrapContentWidth(
                align?.alignment ?: Alignment.CenterHorizontally,
                unbounded ?: false,
            )

        override fun displayName(): String = "Wrap content width"

        override fun generateCode(
            project: Project,
            context: GenerationContext,
            codeBlockBuilder: CodeBlockBuilderWrapper,
            dryRun: Boolean,
        ): CodeBlockBuilderWrapper {
            codeBlockBuilder.addStatement(
                ".%M(",
                MemberNameWrapper.get("androidx.compose.foundation.layout", "wrapContentWidth"),
            )
            align?.let {
                codeBlockBuilder.addStatement(
                    "align = %M.${it.name},",
                    MemberNameWrapper.get("androidx.compose.ui", "Alignment"),
                )
            }
            unbounded?.let {
                codeBlockBuilder.addStatement("unbounded = $it")
            }
            codeBlockBuilder.addStatement(")")
            return codeBlockBuilder
        }

        override fun category() = ModifierCategory.Size

        override fun tooltipText(): String =
            """Allow the content to measure at its desired size without
regard for the incoming measurement"""
    }

    @Serializable
    @SerialName("ZIndex")
    data class ZIndex(
        val zIndex: Float? = null,
    ) : ModifierWrapper() {
        override fun toModifier(): Modifier = Modifier.zIndex(zIndex = zIndex ?: 0f)

        override fun displayName(): String = "zIndex"

        override fun generateCode(
            project: Project,
            context: GenerationContext,
            codeBlockBuilder: CodeBlockBuilderWrapper,
            dryRun: Boolean,
        ): CodeBlockBuilderWrapper {
            val zIndexMember = MemberNameWrapper.get("androidx.compose.ui", "zIndex")
            codeBlockBuilder.addStatement(".%M(zIndex = ${zIndex ?: 0}f)", zIndexMember)
            return codeBlockBuilder
        }

        override fun category() = ModifierCategory.Drawing

        override fun tooltipText(): String =
            """Creates a modifier that controls the drawing order
for the children of the same layout parent"""
    }

    // Modifiers that need specific parent
    @Serializable
    @SerialName("Align")
    data class Align(
        val align: AlignmentWrapper = AlignmentWrapper.TopStart,
    ) : ModifierWrapper() {
        override fun toModifier(): Modifier = createAlignModifier(align.alignment)

        override fun displayName(): String = "Align"

        override fun generateCode(
            project: Project,
            context: GenerationContext,
            codeBlockBuilder: CodeBlockBuilderWrapper,
            dryRun: Boolean,
        ): CodeBlockBuilderWrapper {
            codeBlockBuilder.addStatement(
                ".align(%M.${align.name})",
                MemberNameWrapper.get("androidx.compose.ui", "Alignment"),
            )
            return codeBlockBuilder
        }

        override fun hasValidParent(parent: ComposeTrait): Boolean = parent is BoxTrait

        override fun category() = ModifierCategory.Alignment

        override fun tooltipText(): String =
            """Creates a modifier that controls the drawing order for the children of the same layout parent"""
    }

    @Serializable
    @SerialName("AlignHorizontal")
    data class AlignHorizontal(
        val align: AlignmentHorizontalWrapper = AlignmentHorizontalWrapper.Start,
    ) : ModifierWrapper() {
        override fun toModifier(): Modifier {
            return ModifierHelper.createHorizontalAlignModifier(align.alignment)
        }

        override fun displayName(): String = "Align horizontal"

        override fun generateCode(
            project: Project,
            context: GenerationContext,
            codeBlockBuilder: CodeBlockBuilderWrapper,
            dryRun: Boolean,
        ): CodeBlockBuilderWrapper {
            codeBlockBuilder.addStatement(
                ".align(%M.${align.name})",
                MemberNameWrapper.get("androidx.compose.ui", "Alignment"),
            )
            return codeBlockBuilder
        }

        override fun hasValidParent(parent: ComposeTrait): Boolean = parent is ColumnTrait

        override fun category() = ModifierCategory.Alignment

        override fun tooltipText(): String = "Align the element horizontally within the Column"
    }

    @Serializable
    @SerialName("AlignVertical")
    data class AlignVertical(
        val align: AlignmentVerticalWrapper = AlignmentVerticalWrapper.Top,
    ) : ModifierWrapper() {
        override fun toModifier(): Modifier {
            return ModifierHelper.createVerticalAlignModifier(align.alignment)
        }

        override fun displayName(): String = "Align vertical"

        override fun generateCode(
            project: Project,
            context: GenerationContext,
            codeBlockBuilder: CodeBlockBuilderWrapper,
            dryRun: Boolean,
        ): CodeBlockBuilderWrapper {
            codeBlockBuilder.addStatement(
                ".align(%M.${align.name})",
                MemberNameWrapper.get("androidx.compose.ui", "Alignment"),
            )
            return codeBlockBuilder
        }

        override fun hasValidParent(parent: ComposeTrait): Boolean = parent is RowTrait

        override fun category() = ModifierCategory.Alignment

        override fun tooltipText(): String = "Align the element vertically within the Row"
    }

    @Serializable
    @SerialName("Weight")
    data class Weight(
        val weight: Float = 1f,
        val fill: Boolean = true,
    ) : ModifierWrapper() {
        override fun toModifier(): Modifier {
            return ModifierHelper.createWeightModifier(weight, fill)
        }

        override fun displayName(): String = "Weight"

        override fun generateCode(
            project: Project,
            context: GenerationContext,
            codeBlockBuilder: CodeBlockBuilderWrapper,
            dryRun: Boolean,
        ): CodeBlockBuilderWrapper {
            // weight doesn't have to be with the package name because it's inferred from the
            // parent ColumnScope or RowScope
            codeBlockBuilder.addStatement(".weight(weight = ${weight}f, fill = $fill)")
            return codeBlockBuilder
        }

        override fun heightDecider(parent: ComposeTrait?) =
            if (parent is ColumnTrait) {
                "weight $weight"
            } else {
                null
            }

        override fun widthDecider(parent: ComposeTrait?) =
            if (parent is RowTrait) {
                "weight $weight"
            } else {
                null
            }

        override fun category() = ModifierCategory.Size

        override fun hasValidParent(parent: ComposeTrait): Boolean = parent is RowTrait || parent is ColumnTrait

        override fun tooltipText(): String =
            """Size the element's width proportional to its weight
relative to other weighted sibling elements in the Row"""
    }

    fun generateIssues(parent: ComposeTrait?): List<Issue> =
        if (parent == null) {
            emptyList()
        } else {
            if (!hasValidParent(parent)) {
                listOf(
                    Issue.InvalidModifierRelation(),
                )
            } else {
                emptyList()
            }
        }

    abstract fun toModifier(): Modifier

    abstract fun displayName(): String

    abstract fun generateCode(
        project: Project,
        context: GenerationContext,
        codeBlockBuilder: CodeBlockBuilderWrapper,
        dryRun: Boolean,
    ): CodeBlockBuilderWrapper

    abstract fun category(): ModifierCategory

    abstract fun tooltipText(): String

    /**
     * Check if the ComposeNode containing the ModifierWrapper instance  has a valid parent.
     * This is true for most of the ModifierWrappers except for ModifierWrappers that correspond
     * to Modifier that needs to be called in a specific Scope (e.g. BoxScope, RowScope, ColumnScope)
     */
    open fun hasValidParent(parent: ComposeTrait): Boolean = true

    /**
     * If the modifier contributes to decide the height of a Composable, returns the factor as a
     * String representation.
     * E.g. [Height] or [FillMaxHeight] modifier returns its String representations.
     */
    open fun heightDecider(parent: ComposeTrait?): String? = null

    /**
     * If the modifier contributes to decide the width of a Composable, returns the factor as a
     * String representation.
     * E.g. [Width] or [FillMaxWidth] modifier returns its String representations.
     */
    open fun widthDecider(parent: ComposeTrait?): String? = null

    companion object {
        fun values(): List<ModifierWrapper> =
            listOf(
                Align(),
                AlignHorizontal(),
                AlignVertical(),
                Alpha(),
                AspectRatio(ratio = 1f),
                Background(),
                Border(width = 1.dp),
                Clip(),
                Height(80.dp),
                FillMaxHeight(),
                FillMaxWidth(),
                FillMaxSize(),
                HorizontalScroll,
                VerticalScroll,
                Offset(),
                Rotate(),
                Padding(all = 8.dp),
                Scale(),
                Shadow(),
                Size(),
                Weight(),
                Width(80.dp),
                WrapContentHeight(),
                WrapContentWidth(),
                WrapContentSize(),
                ZIndex(),
            )
    }
}

enum class ModifierCategory {
    Size,
    Padding,
    Alignment,
    Border,
    Drawing,
    Position,
    Scroll,
    Transformations,
}

fun List<ModifierWrapper>.toModifierChain(): Modifier {
    val initial: Modifier = Modifier
    return fold(initial = initial) { acc, element ->
        if (element.visible.value) {
            acc.then(element.toModifier())
        } else {
            acc
        }
    }
}

fun List<ModifierWrapper>.sumPadding(): ModifierWrapper.Padding =
    fold(initial = ModifierWrapper.Padding()) { acc, element ->
        if (element is ModifierWrapper.Padding && element.visible.value) {
            acc + element
        } else {
            acc
        }
    }

fun List<ModifierWrapper>.generateCode(
    project: Project,
    context: GenerationContext,
    codeBlockBuilder: CodeBlockBuilderWrapper = CodeBlockWrapper.builder(),
    dryRun: Boolean,
): CodeBlockBuilderWrapper =
    if (isEmpty()) {
        codeBlockBuilder
    } else {
        val modifier = MemberNameWrapper.get("androidx.compose.ui", "Modifier")
        codeBlockBuilder.addStatement("modifier = %M", modifier)
        this@generateCode.forEach {
            it.generateCode(project = project, context, codeBlockBuilder, dryRun = dryRun)
        }
        codeBlockBuilder.addStatement(",")
        codeBlockBuilder
    }

fun ComposeNode.generateModifierCode(
    project: Project,
    context: GenerationContext,
    dryRun: Boolean,
    additionalCode: CodeBlockWrapper? = null,
): CodeBlockWrapper {
    val codeBlockBuilder = CodeBlockWrapper.builder()
    var modifierImported = false
    val modifierMember = MemberNameWrapper.get("androidx.compose.ui", "Modifier")
    if (modifierList.isNotEmpty()) {
        modifierImported = true
        codeBlockBuilder.add("modifier = %M", modifierMember)
        modifierList.forEach {
            // If the modifier has an issue, (e.g. AI-generated code may have a modifier that
            // violates the parent relationship) skip generating the code
            if (it.generateIssues(parentNode?.trait?.value).isEmpty()) {
                it.generateCode(project = project, context, codeBlockBuilder, dryRun = dryRun)
            }
        }
    }

    if (actionsMap[ActionType.OnDoubleClick]?.isNotEmpty() == true ||
        actionsMap[ActionType.OnLongClick]?.isNotEmpty() == true
    ) {
        if (!modifierImported) {
            codeBlockBuilder.add("modifier = %M", modifierMember)
            modifierImported = true
        }
        codeBlockBuilder.addStatement(
            ".%M(",
            MemberNameWrapper.get("androidx.compose.foundation", "combinedClickable"),
        )
        // onClick is a mandatory parameter for combinedClickable regardless of if the actions is set
        codeBlockBuilder.add("onClick = {")
        actionsMap[ActionType.OnClick]?.forEach {
            codeBlockBuilder.add(it.generateCodeBlock(project, context, dryRun = dryRun))
        }
        codeBlockBuilder.addStatement("},")
        if (actionsMap[ActionType.OnDoubleClick]?.isNotEmpty() == true) {
            codeBlockBuilder.add("onDoubleClick = {")
            actionsMap[ActionType.OnDoubleClick]?.forEach {
                codeBlockBuilder.add(it.generateCodeBlock(project, context, dryRun))
            }
            codeBlockBuilder.addStatement("},")
        }
        if (actionsMap[ActionType.OnLongClick]?.isNotEmpty() == true) {
            codeBlockBuilder.add("onLongClick = {")
            actionsMap[ActionType.OnLongClick]?.forEach {
                codeBlockBuilder.add(it.generateCodeBlock(project, context, dryRun))
            }
            codeBlockBuilder.addStatement("},")
        }
        codeBlockBuilder.add(")")
    } else if (!trait.value.onClickIncludedInParams() && actionsMap[ActionType.OnClick]?.isNotEmpty() == true) {
        if (!modifierImported) {
            codeBlockBuilder.add("modifier = %M", modifierMember)
            modifierImported = true
        }
        codeBlockBuilder.add(
            ".%M {",
            MemberNameWrapper.get("androidx.compose.foundation", "clickable"),
        )
        actionsMap[ActionType.OnClick]?.forEach {
            codeBlockBuilder.add(it.generateCodeBlock(project, context, dryRun))
        }
        codeBlockBuilder.add("}")
    }

    if (modifierImported) {
        additionalCode?.let {
            codeBlockBuilder.add(it)
        }
        codeBlockBuilder.addStatement(",")
    } else if (additionalCode != null) {
        codeBlockBuilder.add("modifier = %M", modifierMember)
        codeBlockBuilder.add(additionalCode)
        codeBlockBuilder.addStatement(",")
    }
    return codeBlockBuilder.build()
}

private fun Float.fillAsString(): String = if (this == 1f) "Fill max" else "Fill $this"
