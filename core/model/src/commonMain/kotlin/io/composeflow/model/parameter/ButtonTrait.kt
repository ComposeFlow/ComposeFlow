package io.composeflow.model.parameter

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.SmartButton
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import io.composeflow.Res
import io.composeflow.kotlinpoet.GenerationContext
import io.composeflow.kotlinpoet.MemberHolder
import io.composeflow.kotlinpoet.wrapper.CodeBlockWrapper
import io.composeflow.kotlinpoet.wrapper.MemberNameWrapper
import io.composeflow.materialicons.ImageVectorHolder
import io.composeflow.materialicons.asCodeBlock
import io.composeflow.model.action.ActionType
import io.composeflow.model.modifier.generateModifierCode
import io.composeflow.model.palette.PaletteRenderParams
import io.composeflow.model.palette.TraitCategory
import io.composeflow.model.parameter.wrapper.ShapeWrapper
import io.composeflow.model.project.Project
import io.composeflow.model.project.appscreen.screen.composenode.ComposeNode
import io.composeflow.model.property.AssignableProperty
import io.composeflow.model.property.PropertyContainer
import io.composeflow.model.property.StringProperty
import io.composeflow.model.property.asBooleanValue
import io.composeflow.model.type.ComposeFlowType
import io.composeflow.serializer.FallbackEnumSerializer
import io.composeflow.tooltip_button_trait
import io.composeflow.ui.CanvasNodeCallbacks
import io.composeflow.ui.modifierForCanvas
import io.composeflow.ui.zoomablecontainer.ZoomableContainerStateHolder
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.jetbrains.compose.resources.StringResource

@Serializable
@SerialName("ButtonTrait")
data class ButtonTrait(
    val textProperty: AssignableProperty = StringProperty.StringIntrinsicValue("Example button"),
    val imageVectorHolder: ImageVectorHolder? = null,
    val enabled: AssignableProperty? = null,
    val shapeWrapper: ShapeWrapper? = null,
    val buttonType: ButtonType = ButtonType.Default,
) : ComposeTrait {
    override fun getPropertyContainers(): List<PropertyContainer> =
        listOf(
            PropertyContainer("Text", textProperty, ComposeFlowType.StringType()),
            PropertyContainer("Enabled", enabled, ComposeFlowType.BooleanType()),
        )

    private fun generateParamsCode(
        project: Project,
        node: ComposeNode,
        context: GenerationContext,
        dryRun: Boolean,
    ): CodeBlockWrapper {
        val codeBlockBuilder = CodeBlockWrapper.builder()
        codeBlockBuilder.addStatement("onClick = {")
        node.actionsMap[ActionType.OnClick]?.forEach {
            codeBlockBuilder.add(it.generateCodeBlock(project, context, dryRun = dryRun))
        }
        codeBlockBuilder.addStatement("},")

        val validateResultBuilder = CodeBlockWrapper.builder()
        val validatorNodes =
            node.getDependentValidatorNodesForActionType(project, ActionType.OnClick)
        validatorNodes.forEachIndexed { index, validatorNode ->
            val validateResultName =
                validatorNode.getCompanionStateOrNull(project)?.getValidateResultName(context)
            validateResultBuilder.add(
                CodeBlockWrapper.of(
                    "$validateResultName.%M()",
                    MemberHolder.ComposeFlow.isSuccess,
                ),
            )
            if (index != validatorNodes.lastIndex) {
                validateResultBuilder.add(" && ")
            }
        }

        codeBlockBuilder.add("enabled = ")
        if (enabled != null) {
            codeBlockBuilder.add(
                enabled.transformedCodeBlock(project, context, dryRun = dryRun),
            )
        }
        if (validatorNodes.isNotEmpty()) {
            if (enabled != null) {
                codeBlockBuilder.add(" && ")
            }
            codeBlockBuilder.add(validateResultBuilder.build())
        }
        if (enabled == null && validateResultBuilder.isEmpty()) {
            codeBlockBuilder.add("true")
        }
        codeBlockBuilder.addStatement(",")

        shapeWrapper?.generateCode(codeBlockBuilder)
        return codeBlockBuilder.build()
    }

    override fun defaultComposeNode(project: Project): ComposeNode =
        ComposeNode(
            modifierList = defaultModifierList(),
            trait =
                mutableStateOf(
                    ButtonTrait(
                        textProperty = StringProperty.StringIntrinsicValue("Example button"),
                    ),
                ),
        )

    override fun icon(): ImageVector = Icons.Outlined.SmartButton

    override fun iconText(): String = "Button"

    override fun paletteCategories(): List<TraitCategory> = listOf(TraitCategory.Basic)

    override fun tooltipResource(): StringResource = Res.string.tooltip_button_trait

    override fun isDroppable(): Boolean = false

    override fun onClickIncludedInParams(): Boolean = true

    override fun actionTypes(): List<ActionType> = listOf(ActionType.OnClick)

    @Composable
    override fun RenderedNode(
        project: Project,
        node: ComposeNode,
        canvasNodeCallbacks: CanvasNodeCallbacks,
        paletteRenderParams: PaletteRenderParams,
        zoomableContainerStateHolder: ZoomableContainerStateHolder,
        modifier: Modifier,
    ) {
        when (buttonType) {
            ButtonType.Default -> {
                Button(
                    onClick = {},
                    enabled = enabled.asBooleanValue(),
                    shape = shapeWrapper?.toShape() ?: ButtonDefaults.shape,
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
                ) {
                    imageVectorHolder?.let {
                        Icon(
                            imageVector = it.imageVector,
                            contentDescription = "",
                        )
                    }
                    Text(text = textProperty.transformedValueExpression(project))
                }
            }

            ButtonType.Elevated -> {
                ElevatedButton(
                    onClick = {},
                    enabled = enabled.asBooleanValue(),
                    shape = shapeWrapper?.toShape() ?: ButtonDefaults.shape,
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
                ) {
                    imageVectorHolder?.let {
                        Icon(
                            imageVector = it.imageVector,
                            contentDescription = "",
                        )
                    }
                    Text(text = textProperty.transformedValueExpression(project))
                }
            }

            ButtonType.Outlined -> {
                OutlinedButton(
                    onClick = {},
                    enabled = enabled.asBooleanValue(),
                    shape = shapeWrapper?.toShape() ?: ButtonDefaults.shape,
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
                ) {
                    imageVectorHolder?.let {
                        Icon(
                            imageVector = it.imageVector,
                            contentDescription = "",
                        )
                    }
                    Text(text = textProperty.transformedValueExpression(project))
                }
            }

            ButtonType.FilledTonal -> {
                FilledTonalButton(
                    onClick = {},
                    enabled = enabled.asBooleanValue(),
                    shape = shapeWrapper?.toShape() ?: ButtonDefaults.shape,
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
                ) {
                    imageVectorHolder?.let {
                        Icon(
                            imageVector = it.imageVector,
                            contentDescription = "",
                        )
                    }
                    Text(text = textProperty.transformedValueExpression(project))
                }
            }

            ButtonType.Text -> {
                TextButton(
                    onClick = {},
                    enabled = enabled.asBooleanValue(),
                    shape = shapeWrapper?.toShape() ?: ButtonDefaults.shape,
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
                ) {
                    imageVectorHolder?.let {
                        Icon(
                            imageVector = it.imageVector,
                            contentDescription = "",
                        )
                    }
                    Text(text = textProperty.transformedValueExpression(project))
                }
            }
        }
    }

    override fun generateCode(
        project: Project,
        node: ComposeNode,
        context: GenerationContext,
        dryRun: Boolean,
    ): CodeBlockWrapper {
        val codeBlockBuilder = CodeBlockWrapper.builder()
        val buttonMember = buttonType.toMemberName()
        codeBlockBuilder.addStatement("%M(", buttonMember)
        codeBlockBuilder.add(
            generateParamsCode(
                project = project,
                context = context,
                node = node,
                dryRun = dryRun,
            ),
        )
        codeBlockBuilder.add(
            node.generateModifierCode(project, context, dryRun = dryRun),
        )
        codeBlockBuilder.addStatement(") {")
        imageVectorHolder?.let {
            codeBlockBuilder.add("%M(", MemberHolder.Material3.Icon)
            codeBlockBuilder.add("imageVector = ")
            codeBlockBuilder.add(it.asCodeBlock())
            codeBlockBuilder.addStatement(",")
            codeBlockBuilder.add("contentDescription = null")
            codeBlockBuilder.addStatement(")")
        }
        codeBlockBuilder.add("%M(", MemberHolder.Material3.Text)
        codeBlockBuilder.add("text = ")
        codeBlockBuilder.add(
            textProperty.transformedCodeBlock(
                project,
                context,
                ComposeFlowType.StringType(),
                dryRun = dryRun,
            ),
        )
        codeBlockBuilder.addStatement(",")

        codeBlockBuilder.addStatement(")")
        codeBlockBuilder.addStatement("}")
        return codeBlockBuilder.build()
    }
}

@Serializable(ButtonType.Serializer::class)
enum class ButtonType {
    Default {
        override fun toMemberName(): MemberNameWrapper = MemberNameWrapper.get("androidx.compose.material3", "Button")
    },
    Elevated {
        override fun toMemberName(): MemberNameWrapper = MemberNameWrapper.get("androidx.compose.material3", "ElevatedButton")
    },
    Outlined {
        override fun toMemberName(): MemberNameWrapper = MemberNameWrapper.get("androidx.compose.material3", "OutlinedButton")
    },
    FilledTonal {
        override fun toMemberName(): MemberNameWrapper = MemberNameWrapper.get("androidx.compose.material3", "FilledTonalButton")
    },
    Text {
        override fun toMemberName(): MemberNameWrapper = MemberNameWrapper.get("androidx.compose.material3", "TextButton")
    },
    ;

    abstract fun toMemberName(): MemberNameWrapper

    object Serializer : FallbackEnumSerializer<ButtonType>(
        ButtonType::class,
        entries.toTypedArray(),
    )
}
