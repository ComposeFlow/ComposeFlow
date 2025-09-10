package io.composeflow.model.parameter

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import io.composeflow.Res
import io.composeflow.kotlinpoet.GenerationContext
import io.composeflow.kotlinpoet.MemberHolder
import io.composeflow.kotlinpoet.wrapper.ClassNameWrapper
import io.composeflow.kotlinpoet.wrapper.CodeBlockWrapper
import io.composeflow.kotlinpoet.wrapper.MemberNameWrapper
import io.composeflow.materialicons.ImageVectorHolder
import io.composeflow.model.action.ActionType
import io.composeflow.model.enumwrapper.TextFieldColorsWrapper
import io.composeflow.model.modifier.generateModifierCode
import io.composeflow.model.palette.PaletteRenderParams
import io.composeflow.model.palette.TraitCategory
import io.composeflow.model.parameter.wrapper.ShapeWrapper
import io.composeflow.model.project.Project
import io.composeflow.model.project.appscreen.screen.composenode.ComposeNode
import io.composeflow.model.project.findCanvasEditableHavingNodeOrNull
import io.composeflow.model.project.findLocalStateOrNull
import io.composeflow.model.property.AssignableProperty
import io.composeflow.model.property.PropertyContainer
import io.composeflow.model.property.StringProperty
import io.composeflow.model.property.ValueFromCompanionState
import io.composeflow.model.property.asBooleanValue
import io.composeflow.model.state.ScreenState
import io.composeflow.model.state.StateHolder
import io.composeflow.model.state.WriteableState
import io.composeflow.model.type.ComposeFlowType
import io.composeflow.model.validator.ComposeStateValidator
import io.composeflow.model.validator.TextFieldValidator
import io.composeflow.serializer.FallbackEnumSerializer
import io.composeflow.tooltip_textfield_trait
import io.composeflow.ui.CanvasNodeCallbacks
import io.composeflow.ui.modifierForCanvas
import io.composeflow.ui.zoomablecontainer.ZoomableContainerStateHolder
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.jetbrains.compose.resources.StringResource

@Serializable
@SerialName("TextFieldTrait")
data class TextFieldTrait(
    val value: AssignableProperty = StringProperty.StringIntrinsicValue(),
    val enabled: AssignableProperty? = null,
    val readOnly: AssignableProperty? = null,
    val label: AssignableProperty? = null,
    val placeholder: AssignableProperty? = null,
    val leadingIcon: ImageVectorHolder? = null,
    val trailingIcon: ImageVectorHolder? = null,
    val singleLine: Boolean? = true,
    val maxLines: Int? = null,
    val shapeWrapper: ShapeWrapper? = null,
    val textFieldType: TextFieldType = TextFieldType.Default,
    val transparentIndicator: Boolean? = null,
    val enableValidator: Boolean? = false,
    val textFieldValidator: TextFieldValidator? = TextFieldValidator.StringValidator(),
    val passwordField: Boolean? = null,
) : ComposeTrait {
    override fun getPropertyContainers(): List<PropertyContainer> =
        listOf(
            PropertyContainer("Value", value, ComposeFlowType.StringType()),
            PropertyContainer("Enabled", enabled, ComposeFlowType.BooleanType()),
            PropertyContainer("Read only", readOnly, ComposeFlowType.BooleanType()),
            PropertyContainer("Label", label, ComposeFlowType.StringType()),
            PropertyContainer("Placeholder", placeholder, ComposeFlowType.StringType()),
        )

    override fun companionState(composeNode: ComposeNode): ScreenState<*> =
        ScreenState.StringScreenState(
            id = composeNode.companionStateId,
            name = composeNode.label.value,
            companionNodeId = composeNode.id,
        )

    override fun onAttachStateToNode(
        project: Project,
        stateHolder: StateHolder,
        node: ComposeNode,
    ) {
        node.label.value = stateHolder.createUniqueLabel(project, node, node.label.value)
        node.trait.value =
            this.copy(value = ValueFromCompanionState(node))
    }

    override fun updateCompanionStateProperties(composeNode: ComposeNode) {
        if (value is ValueFromCompanionState) {
            composeNode.trait.value = this.copy(value = ValueFromCompanionState(composeNode))
        }
    }

    private fun generateParamsCode(
        project: Project,
        node: ComposeNode,
        context: GenerationContext,
        dryRun: Boolean,
    ): CodeBlockWrapper {
        val codeBlockBuilder = CodeBlockWrapper.builder()
        codeBlockBuilder.add("value = ")
        codeBlockBuilder.add(
            value.transformedCodeBlock(
                project,
                context,
                ComposeFlowType.StringType(),
                dryRun = dryRun,
            ),
        )
        codeBlockBuilder.addStatement(",")

        val canvasEditable = project.findCanvasEditableHavingNodeOrNull(node)
        val writeState = value.findReadableState(project, canvasEditable, node)

        if (writeState != null &&
            canvasEditable != null &&
            writeState is WriteableState &&
            node.companionStateId == writeState.id
        ) {
            codeBlockBuilder.addStatement("onValueChange = {")
            codeBlockBuilder.add(
                writeState.generateWriteBlock(
                    project,
                    canvasEditable,
                    context,
                    dryRun = dryRun,
                ),
            )

            if (node.actionsMap[ActionType.OnChange]?.isNotEmpty() == true) {
                if (enableValidator == true) {
                    codeBlockBuilder.add(
                        CodeBlockWrapper.of(
                            "if (${writeState.getValidateResultName(context)}.%M()) {",
                            MemberHolder.ComposeFlow.isSuccess,
                        ),
                    )
                }
                node.actionsMap[ActionType.OnChange]?.forEach {
                    codeBlockBuilder.add(it.generateCodeBlock(project, context, dryRun = dryRun))
                }
                if (enableValidator == true) {
                    codeBlockBuilder.add("}") // Close if expression for checking validator result
                }
            }

            codeBlockBuilder.addStatement("},")
        } else {
            codeBlockBuilder.addStatement("onValueChange = {},")
        }
        enabled?.let {
            codeBlockBuilder.add("enabled = ")
            codeBlockBuilder.add(it.transformedCodeBlock(project, context, dryRun = dryRun))
            codeBlockBuilder.addStatement(",")
        }
        readOnly?.let {
            codeBlockBuilder.add("readOnly = ")
            codeBlockBuilder.add(it.transformedCodeBlock(project, context, dryRun = dryRun))
            codeBlockBuilder.addStatement(",")
        }
        val text = MemberNameWrapper.get("androidx.compose.material3", "Text")
        val modifier = MemberNameWrapper.get("androidx.compose.ui", "Modifier")
        val alpha = MemberNameWrapper.get("androidx.compose.ui.draw", "alpha")
        label?.let {
            codeBlockBuilder.addStatement(
                """label = {
                %M(""",
                text,
            )
            codeBlockBuilder.add(
                it.transformedCodeBlock(
                    project,
                    context,
                    ComposeFlowType.StringType(),
                    dryRun = dryRun,
                ),
            )
            codeBlockBuilder.addStatement(
                """,
                  modifier = %M.%M(0.7f))
                },""",
                modifier,
                alpha,
            )
        }
        placeholder?.let {
            codeBlockBuilder.addStatement(
                """placeholder = {
                %M(""",
                text,
            )
            codeBlockBuilder.add(
                it.transformedCodeBlock(
                    project,
                    context,
                    ComposeFlowType.StringType(),
                    dryRun = dryRun,
                ),
            )
            codeBlockBuilder.addStatement(
                """,
                  modifier = %M.%M(0.7f))
                },""",
                modifier,
                alpha,
            )
        }
        leadingIcon?.let {
            val iconMember = MemberNameWrapper.get("androidx.compose.material3", "Icon")
            val iconsMember = MemberNameWrapper.get("androidx.compose.material.icons", "Icons")
            val imageVectorMember =
                MemberNameWrapper.get(
                    "androidx.compose.material.icons.${it.packageDescriptor}",
                    it.name,
                )
            codeBlockBuilder.addStatement(
                """leadingIcon = { %M(imageVector = %M.${it.memberDescriptor}.%M, contentDescription = null) },""",
                iconMember,
                iconsMember,
                imageVectorMember,
            )
        }
        trailingIcon?.let {
            val iconMember = MemberNameWrapper.get("androidx.compose.material3", "Icon")
            val iconsMember = MemberNameWrapper.get("androidx.compose.material.icons", "Icons")
            val imageVectorMember =
                MemberNameWrapper.get(
                    "androidx.compose.material.icons.${it.packageDescriptor}",
                    it.name,
                )
            codeBlockBuilder.addStatement(
                """trailingIcon = { %M(imageVector = %M.${it.memberDescriptor}.%M, contentDescription = null) },""",
                iconMember,
                iconsMember,
                imageVectorMember,
            )
        }
        singleLine?.let {
            codeBlockBuilder.addStatement("singleLine = $it,")
        }
        maxLines?.let {
            codeBlockBuilder.addStatement("maxLines = $it,")
        }
        shapeWrapper?.generateCode(codeBlockBuilder)
        if (shouldUseTransparentIndicator()) {
            codeBlockBuilder.add(
                CodeBlockWrapper.of(
                    """colors = %M.colors(
                    focusedIndicatorColor = %M.Transparent,
                    unfocusedIndicatorColor = %M.Transparent,
                    disabledIndicatorColor = %M.Transparent,
                    errorIndicatorColor = %M.Transparent,
                    ),""",
                    TextFieldColorsWrapper.Default.toMemberName(),
                    MemberHolder.AndroidX.Ui.Color,
                    MemberHolder.AndroidX.Ui.Color,
                    MemberHolder.AndroidX.Ui.Color,
                    MemberHolder.AndroidX.Ui.Color,
                ),
            )
        }
        if (node.actionsMap[ActionType.OnSubmit]?.isNotEmpty() == true) {
            codeBlockBuilder.addStatement(
                """keyboardOptions = %T.Default.copy(
                imeAction = %T.Done
            ),""",
                ClassNameWrapper.get("androidx.compose.foundation.text", "KeyboardOptions"),
                ClassNameWrapper.get("androidx.compose.ui.text.input", "ImeAction"),
            )

            codeBlockBuilder.add(
                "keyboardActions = %T(",
                ClassNameWrapper.get("androidx.compose.foundation.text", "KeyboardActions"),
            )
            codeBlockBuilder.add("onDone = {")

            if (enableValidator == true && writeState != null && writeState is WriteableState) {
                codeBlockBuilder.add(
                    CodeBlockWrapper.of(
                        "if (${writeState.getValidateResultName(context)}.%M()) {",
                        MemberHolder.ComposeFlow.isSuccess,
                    ),
                )
            }
            node.actionsMap[ActionType.OnSubmit]?.forEach {
                codeBlockBuilder.add(it.generateCodeBlock(project, context, dryRun = dryRun))
            }
            if (enableValidator == true && writeState != null) {
                codeBlockBuilder.add("}") // Close if statement for checking the validator result
            }

            codeBlockBuilder.add("}")
            codeBlockBuilder.addStatement("),")
        }
        if (enableValidator == true && writeState != null && writeState is WriteableState) {
            codeBlockBuilder.add(
                "isError = ${writeState.getValidateResultName(context)} is %M.Failure,",
                MemberHolder.ComposeFlow.ValidateResult,
            )
            codeBlockBuilder.add(
                CodeBlockWrapper.of(
                    """
                supportingText = ${writeState.getValidateResultName(context)}.%M()?.let {
                    {
                        %M(it)
                    }
                },
            """,
                    MemberHolder.ComposeFlow.asErrorMessage,
                    MemberHolder.Material3.Text,
                ),
            )
        }
        passwordField?.let {
            codeBlockBuilder.addStatement(
                """visualTransformation = %T(),
                keyboardOptions = %T(
                        keyboardType = %T.Password,
                        imeAction = %T.Done,
                    ),""",
                ClassNameWrapper.get(
                    "androidx.compose.ui.text.input",
                    "PasswordVisualTransformation",
                ),
                ClassNameWrapper.get("androidx.compose.foundation.text", "KeyboardOptions"),
                ClassNameWrapper.get("androidx.compose.ui.text.input", "KeyboardType"),
                ClassNameWrapper.get("androidx.compose.ui.text.input", "ImeAction"),
            )
        }
        return codeBlockBuilder.build()
    }

    override fun defaultComposeNode(project: Project): ComposeNode =
        ComposeNode(
            modifierList = defaultModifierList(),
            trait = mutableStateOf(TextFieldTrait(value = StringProperty.StringIntrinsicValue(""))),
        )

    override fun icon(): ImageVector = Icons.Outlined.EditNote

    override fun iconText(): String = "TextField"

    override fun paletteCategories(): List<TraitCategory> = listOf(TraitCategory.Basic)

    override fun tooltipResource(): StringResource = Res.string.tooltip_textfield_trait

    override fun actionTypes(): List<ActionType> =
        listOf(
            ActionType.OnSubmit,
            ActionType.OnChange,
            ActionType.OnFocused,
            ActionType.OnUnfocused,
        )

    @Composable
    override fun RenderedNode(
        project: Project,
        node: ComposeNode,
        canvasNodeCallbacks: CanvasNodeCallbacks,
        paletteRenderParams: PaletteRenderParams,
        zoomableContainerStateHolder: ZoomableContainerStateHolder,
        modifier: Modifier,
    ) {
        val colors =
            if (shouldUseTransparentIndicator()) {
                TextFieldDefaults.colors(
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent,
                )
            } else {
                when (textFieldType) {
                    TextFieldType.Default -> {
                        TextFieldDefaults.colors()
                    }

                    TextFieldType.Outlined -> {
                        OutlinedTextFieldDefaults.colors()
                    }
                }
            }
        when (textFieldType) {
            TextFieldType.Default -> {
                TextField(
                    value = "",
                    onValueChange = {},
                    enabled = enabled.asBooleanValue(),
                    // Always true to not show the caret inside so that it can be deleted with delete key
                    readOnly = true,
                    label =
                        label?.let {
                            {
                                Text(
                                    text = it.transformedValueExpression(project),
                                    modifier = Modifier.alpha(0.7f),
                                )
                            }
                        },
                    placeholder =
                        placeholder?.let {
                            {
                                Text(
                                    it.transformedValueExpression(project),
                                    modifier = Modifier.alpha(0.7f),
                                )
                            }
                        },
                    leadingIcon =
                        leadingIcon?.let {
                            {
                                Icon(
                                    imageVector = it.imageVector,
                                    contentDescription = null,
                                )
                            }
                        },
                    trailingIcon =
                        trailingIcon?.let {
                            {
                                Icon(
                                    imageVector = it.imageVector,
                                    contentDescription = null,
                                )
                            }
                        },
                    singleLine = singleLine ?: false,
                    maxLines = maxLines ?: Int.MAX_VALUE,
                    shape = shapeWrapper?.toShape() ?: TextFieldDefaults.shape,
                    colors = colors,
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

            TextFieldType.Outlined -> {
                OutlinedTextField(
                    value = "",
                    onValueChange = {},
                    enabled = enabled.asBooleanValue(),
                    // Always true to not show the caret inside so that it can be deleted with delete key
                    readOnly = true,
                    label =
                        label?.let {
                            {
                                Text(
                                    text = it.transformedValueExpression(project),
                                    modifier = Modifier.alpha(0.7f),
                                )
                            }
                        },
                    placeholder =
                        placeholder?.let {
                            {
                                Text(
                                    it.transformedValueExpression(project),
                                    modifier = Modifier.alpha(0.7f),
                                )
                            }
                        },
                    leadingIcon =
                        leadingIcon?.let {
                            {
                                Icon(
                                    imageVector = it.imageVector,
                                    contentDescription = null,
                                )
                            }
                        },
                    trailingIcon =
                        trailingIcon?.let {
                            {
                                Icon(
                                    imageVector = it.imageVector,
                                    contentDescription = null,
                                )
                            }
                        },
                    singleLine = singleLine ?: false,
                    maxLines = maxLines ?: Int.MAX_VALUE,
                    shape = shapeWrapper?.toShape() ?: TextFieldDefaults.shape,
                    colors = colors,
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
        }
    }

    override fun generateCode(
        project: Project,
        node: ComposeNode,
        context: GenerationContext,
        dryRun: Boolean,
    ): CodeBlockWrapper {
        val codeBlockBuilder = CodeBlockWrapper.builder()
        codeBlockBuilder.addStatement("%M(", textFieldType.toMemberName())
        codeBlockBuilder.add(
            generateParamsCode(
                project = project,
                node = node,
                context = context,
                dryRun = dryRun,
            ),
        )
        val hasFocusedActions = node.actionsMap[ActionType.OnFocused]?.isNotEmpty() == true
        val hasUnfocusedActions = node.actionsMap[ActionType.OnUnfocused]?.isNotEmpty() == true
        val focusModifierCode =
            if (hasFocusedActions || hasUnfocusedActions) {
                val additionalCodeBuilder = CodeBlockWrapper.builder()
                additionalCodeBuilder.addStatement(
                    ".%M {",
                    MemberNameWrapper.get("androidx.compose.ui.focus", "onFocusChanged"),
                )
                if (hasFocusedActions) {
                    additionalCodeBuilder.addStatement("if (it.isFocused) {")
                    node.actionsMap[ActionType.OnFocused]?.forEach {
                        additionalCodeBuilder.add(
                            it.generateCodeBlock(
                                project,
                                context,
                                dryRun = dryRun,
                            ),
                        )
                    }
                    additionalCodeBuilder.addStatement("}")
                }
                if (hasUnfocusedActions) {
                    additionalCodeBuilder.addStatement("if (!it.isFocused) {")
                    node.actionsMap[ActionType.OnUnfocused]?.forEach {
                        additionalCodeBuilder.add(
                            it.generateCodeBlock(
                                project,
                                context,
                                dryRun = dryRun,
                            ),
                        )
                    }
                    additionalCodeBuilder.addStatement("}")
                }
                additionalCodeBuilder.addStatement("}")
                additionalCodeBuilder.build()
            } else {
                null
            }
        codeBlockBuilder.add(
            node.generateModifierCode(
                project,
                context,
                additionalCode = focusModifierCode,
                dryRun = dryRun,
            ),
        )
        codeBlockBuilder.addStatement(")")

        if (node.actionsMap[ActionType.OnChange]?.isNotEmpty() == true) {
            node.companionStateId.let { companionStateId ->
                val companionState = project.findLocalStateOrNull(companionStateId)

                if (companionState != null && companionState is WriteableState) {
                    val launchedEffectBuilder = CodeBlockWrapper.builder()
                    launchedEffectBuilder.add(
                        """
                    %M(Unit) {
                        ${context.currentEditable.viewModelName}.${
                            companionState.getFlowName(
                                context,
                            )
                        }.%M {
                """,
                        MemberHolder.AndroidX.Runtime.LaunchedEffect,
                        MemberHolder.Coroutines.Flow.collectLatest,
                    )
                    node.actionsMap[ActionType.OnChange]?.forEach {
                        launchedEffectBuilder.add(
                            it.generateCodeBlock(
                                project,
                                context,
                                dryRun = dryRun,
                            ),
                        )
                    }
                    launchedEffectBuilder.addStatement("}") // End of collectLatest
                    launchedEffectBuilder.addStatement("}") // End of LaunchedEffect

                    context.addLaunchedEffectBlock(
                        launchedEffectBuilder.build(),
                        dryRun = dryRun,
                    )
                }
            }
        }

        return codeBlockBuilder.build()
    }

    private fun shouldUseTransparentIndicator(): Boolean = transparentIndicator == true && textFieldType == TextFieldType.Default

    override fun getStateValidator(): ComposeStateValidator? =
        if (enableValidator == true) {
            textFieldValidator
        } else {
            null
        }
}

object TextFieldTypeSerializer : FallbackEnumSerializer<TextFieldType>(
    TextFieldType::class,
    TextFieldType.entries.toTypedArray(),
)

@Serializable(TextFieldTypeSerializer::class)
enum class TextFieldType {
    Default {
        override fun toMemberName(): MemberNameWrapper = MemberNameWrapper.get("androidx.compose.material3", "TextField")
    },
    Outlined {
        override fun toMemberName(): MemberNameWrapper = MemberNameWrapper.get("androidx.compose.material3", "OutlinedTextField")
    },
    ;

    abstract fun toMemberName(): MemberNameWrapper
}
