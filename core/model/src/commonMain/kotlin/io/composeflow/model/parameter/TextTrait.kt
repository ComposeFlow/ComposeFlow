package io.composeflow.model.parameter

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.TextFields
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import io.composeflow.kotlinpoet.wrapper.CodeBlockWrapper
import io.composeflow.kotlinpoet.wrapper.CodeBlockBuilderWrapper
import io.composeflow.kotlinpoet.wrapper.MemberNameWrapper
import io.composeflow.Res
import io.composeflow.kotlinpoet.GenerationContext
import io.composeflow.model.enumwrapper.FontStyleWrapper
import io.composeflow.model.enumwrapper.TextAlignWrapper
import io.composeflow.model.enumwrapper.TextDecorationWrapper
import io.composeflow.model.enumwrapper.TextOverflowWrapper
import io.composeflow.model.enumwrapper.TextStyleWrapper
import io.composeflow.model.modifier.generateModifierCode
import io.composeflow.model.palette.PaletteRenderParams
import io.composeflow.model.palette.TraitCategory
import io.composeflow.model.parameter.wrapper.ColorWrapper
import io.composeflow.model.parameter.wrapper.Material3ColorWrapper
import io.composeflow.model.project.COMPOSEFLOW_PACKAGE
import io.composeflow.model.project.Project
import io.composeflow.model.project.appscreen.screen.composenode.ComposeNode
import io.composeflow.model.property.AssignableProperty
import io.composeflow.model.property.ColorProperty
import io.composeflow.model.property.EnumProperty
import io.composeflow.model.property.PropertyContainer
import io.composeflow.model.property.StringProperty
import io.composeflow.model.type.ComposeFlowType
import io.composeflow.toRichHtmlString
import io.composeflow.tooltip_text_trait
import io.composeflow.ui.CanvasNodeCallbacks
import io.composeflow.ui.modifierForCanvas
import io.composeflow.ui.zoomablecontainer.ZoomableContainerStateHolder
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.jetbrains.compose.resources.StringResource

@Serializable
@SerialName("TextTrait")
data class TextTrait(
    val text: AssignableProperty = StringProperty.StringIntrinsicValue(),
    val placeholderText: PlaceholderText = PlaceholderText.NoUsage,
    val colorWrapper: AssignableProperty? = null,
    val fontStyle: AssignableProperty? = null,
    val textDecoration: AssignableProperty? = null,
    val textAlign: AssignableProperty? = null,
    val overflow: AssignableProperty? = null,
    val softWrap: Boolean? = null,
    val maxLines: Int? = null,
    val textStyleWrapper: AssignableProperty? = null,
    val parseHtml: Boolean? = null,
) : ComposeTrait {
    override fun getPropertyContainers(): List<PropertyContainer> =
        listOf(
            PropertyContainer("Text", text, ComposeFlowType.StringType()),
            PropertyContainer("Color", colorWrapper, ComposeFlowType.Color()),
            PropertyContainer(
                "FontStyle",
                fontStyle,
                ComposeFlowType.Enum(enumClass = FontStyleWrapper::class),
            ),
            PropertyContainer(
                "Text decoration",
                textDecoration,
                ComposeFlowType.Enum(enumClass = TextDecorationWrapper::class),
            ),
            PropertyContainer(
                "Text align",
                textAlign,
                ComposeFlowType.Enum(enumClass = TextAlignWrapper::class),
            ),
            PropertyContainer(
                "Overflow",
                overflow,
                ComposeFlowType.Enum(enumClass = TextOverflowWrapper::class),
            ),
            PropertyContainer(
                "TextStyle",
                textStyleWrapper,
                ComposeFlowType.Enum(enumClass = TextStyleWrapper::class),
            ),
        )

    private fun fontStyleValue(): FontStyle? =
        when (fontStyle) {
            is EnumProperty -> {
                FontStyleWrapper.entries[fontStyle.value.enumValue().ordinal].fontStyle
            }

            else -> null
        }

    private fun textDecorationValue(): TextDecoration? =
        when (textDecoration) {
            is EnumProperty -> {
                TextDecorationWrapper.entries[textDecoration.value.enumValue().ordinal].textDecoration
            }

            else -> null
        }

    private fun textAlignValue(): TextAlign? =
        when (textAlign) {
            is EnumProperty -> {
                TextAlignWrapper.entries[textAlign.value.enumValue().ordinal].textAlign
            }

            else -> null
        }

    private fun overflowValue(): TextOverflow? =
        when (overflow) {
            is EnumProperty -> {
                TextOverflowWrapper.entries[overflow.value.enumValue().ordinal].textOverflow
            }

            else -> null
        }

    @Composable
    fun textStyleValue(): TextStyle? =
        when (textStyleWrapper) {
            is EnumProperty -> {
                TextStyleWrapper.entries[textStyleWrapper.value.enumValue().ordinal].getStyle()
            }

            else -> null
        }

    private fun generateParamsCode(
        project: Project,
        context: GenerationContext,
        dryRun: Boolean,
    ): CodeBlockWrapper {
        val codeBlockBuilder = CodeBlockWrapper.builder()
        codeBlockBuilder.add("text = ")
        if (parseHtml == true) {
            codeBlockBuilder.add(
                "%L.%M()",
                text.transformedCodeBlock(
                    project,
                    context,
                    ComposeFlowType.StringType(),
                    dryRun = dryRun,
                ),
                MemberNameWrapper.get("${COMPOSEFLOW_PACKAGE}.util", "toRichHtmlString", isExtension = true),
            )
        } else {
            codeBlockBuilder.add(
                text.transformedCodeBlock(
                    project,
                    context,
                    ComposeFlowType.StringType(),
                    dryRun = dryRun,
                ),
            )
        }
        codeBlockBuilder.addStatement(",")

        colorWrapper?.let {
            codeBlockBuilder.add("color = ")
            codeBlockBuilder.add(
                it.transformedCodeBlock(
                    project,
                    context,
                    ComposeFlowType.Color(),
                    dryRun = dryRun,
                ),
            )
            codeBlockBuilder.addStatement(",")
        }

        fontStyle?.let {
            codeBlockBuilder.add("fontStyle = ")
            codeBlockBuilder.add(it.transformedCodeBlock(project, context, dryRun = dryRun))
            codeBlockBuilder.addStatement(",")
        }
        textDecoration?.let {
            codeBlockBuilder.add("textDecoration = ")
            codeBlockBuilder.add(it.transformedCodeBlock(project, context, dryRun = dryRun))
            codeBlockBuilder.addStatement(",")
        }
        textAlign?.let {
            codeBlockBuilder.add("textAlign = ")
            codeBlockBuilder.add(it.transformedCodeBlock(project, context, dryRun = dryRun))
            codeBlockBuilder.addStatement(",")
        }
        overflow?.let {
            codeBlockBuilder.add("overflow = ")
            codeBlockBuilder.add(it.transformedCodeBlock(project, context, dryRun = dryRun))
            codeBlockBuilder.addStatement(",")
        }
        maxLines?.let {
            codeBlockBuilder.addStatement("maxLines = $it,")
        }
        softWrap?.let {
            codeBlockBuilder.addStatement("softWrap = $it,")
        }
        textStyleWrapper?.let {
            codeBlockBuilder.add("style = ")
            codeBlockBuilder.add(it.transformedCodeBlock(project, context, dryRun = dryRun))
            codeBlockBuilder.addStatement(",")
        }
        return codeBlockBuilder.build()
    }

    override fun defaultComposeNode(project: Project): ComposeNode =
        ComposeNode(
            trait =
                mutableStateOf(
                    TextTrait(
                        text = StringProperty.StringIntrinsicValue("Example text"),
                        colorWrapper =
                            ColorProperty.ColorIntrinsicValue(
                                ColorWrapper(
                                    Material3ColorWrapper.OnSurface,
                                ),
                            ),
                    ),
                ),
            modifierList = defaultModifierList(),
        )

    override fun icon(): ImageVector = Icons.Outlined.TextFields

    override fun isResizeable(): Boolean = false

    override fun iconText(): String = "Text"

    @Composable
    override fun RenderedNode(
        project: Project,
        node: ComposeNode,
        canvasNodeCallbacks: CanvasNodeCallbacks,
        paletteRenderParams: PaletteRenderParams,
        zoomableContainerStateHolder: ZoomableContainerStateHolder,
        modifier: Modifier,
    ) {
        val textWithPlaceholder =
            when (val usage = placeholderText) {
                PlaceholderText.NoUsage -> text.transformedValueExpression(project)
                is PlaceholderText.Used -> usage.value.transformedValueExpression(project)
            }
        Text(
            text =
                if (parseHtml == true) {
                    textWithPlaceholder.toRichHtmlString()
                } else {
                    AnnotatedString(textWithPlaceholder)
                },
            color =
                (colorWrapper as? ColorProperty.ColorIntrinsicValue)?.value?.getColor()
                    ?: Color.Unspecified,
            fontStyle = fontStyleValue(),
            textDecoration = textDecorationValue(),
            textAlign = textAlignValue(),
            overflow = overflowValue() ?: TextOverflow.Clip,
            softWrap = softWrap ?: true,
            maxLines = maxLines ?: Int.MAX_VALUE,
            style =
                textStyleValue()
                    ?: LocalTextStyle.current,
            modifier =
                modifier.then(
                    node
                        .modifierChainForCanvas()
                        .modifierForCanvas(
                            project = project,
                            node = node,
                            canvasNodeCallbacks = canvasNodeCallbacks,
                            paletteRenderParams = paletteRenderParams,
                            zoomableContainerStateHolder = zoomableContainerStateHolder,
                        ),
                ),
        )
    }

    override fun paletteCategories(): List<TraitCategory> = listOf(TraitCategory.Common, TraitCategory.Basic)

    override fun tooltipResource(): StringResource = Res.string.tooltip_text_trait

    override fun generateCode(
        project: Project,
        node: ComposeNode,
        context: GenerationContext,
        dryRun: Boolean,
    ): CodeBlockWrapper {
        val codeBlockBuilder = CodeBlockWrapper.builder()
        val textMember = MemberNameWrapper.get("androidx.compose.material3", "Text")
        codeBlockBuilder.addStatement("%M(", textMember)
        codeBlockBuilder.add(
            generateParamsCode(
                project = project,
                context = context,
                dryRun = dryRun,
            ),
        )
        codeBlockBuilder.add(
            node.generateModifierCode(project, context, dryRun = dryRun),
        )
        codeBlockBuilder.addStatement(")")
        return codeBlockBuilder.build()
    }
}

private const val DEFAULT_PLACEHOLDER_TEXT = "This is a placeholder only visible in the editor"

/**
 * Represents the text only visible in the editor as a placeholder.
 */
@Serializable
sealed interface PlaceholderText {
    @Serializable
    @SerialName("NoUsage")
    data object NoUsage : PlaceholderText

    @Serializable
    @SerialName("Used")
    data class Used(
        val value: StringProperty = StringProperty.StringIntrinsicValue(DEFAULT_PLACEHOLDER_TEXT),
    ) : PlaceholderText
}
