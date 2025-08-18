@file:OptIn(kotlin.time.ExperimentalTime::class)

package io.composeflow.model.property

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.outlined.ColorLens
import androidx.compose.material.icons.outlined.DataObject
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.ForkLeft
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material.icons.outlined.Numbers
import androidx.compose.material.icons.outlined.QuestionMark
import androidx.compose.material.icons.outlined.TextFields
import androidx.compose.material.icons.outlined.ToggleOff
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.MemberName
import com.squareup.kotlinpoet.ParameterSpec
import io.composeflow.ComposeScreenConstant
import io.composeflow.Res
import io.composeflow.ViewModelConstant
import io.composeflow.custom.ComposeFlowIcons
import io.composeflow.custom.composeflowicons.Dbms
import io.composeflow.custom.composeflowicons.Firebase
import io.composeflow.editor.validator.FloatValidator
import io.composeflow.editor.validator.IntValidator
import io.composeflow.editor.validator.ValidateResult
import io.composeflow.invalid_reference
import io.composeflow.invalid_type
import io.composeflow.kotlinpoet.ClassHolder
import io.composeflow.kotlinpoet.GeneratedPlace
import io.composeflow.kotlinpoet.GenerationContext
import io.composeflow.kotlinpoet.KOTLINPOET_COLUMN_LIMIT
import io.composeflow.kotlinpoet.MemberHolder
import io.composeflow.model.apieditor.ApiId
import io.composeflow.model.apieditor.isList
import io.composeflow.model.datatype.DataFieldType
import io.composeflow.model.datatype.DataTypeId
import io.composeflow.model.enumwrapper.EnumWrapper
import io.composeflow.model.parameter.lazylist.LazyListChildParams
import io.composeflow.model.parameter.wrapper.BrushWrapper
import io.composeflow.model.parameter.wrapper.ColorWrapper
import io.composeflow.model.parameter.wrapper.InstantWrapper
import io.composeflow.model.parameter.wrapper.defaultBrushWrapper
import io.composeflow.model.parameter.wrapper.defaultColorWrapper
import io.composeflow.model.project.COMPOSEFLOW_PACKAGE
import io.composeflow.model.project.ParameterId
import io.composeflow.model.project.Project
import io.composeflow.model.project.appscreen.screen.composenode.ComposeNode
import io.composeflow.model.project.custom_enum.CustomEnumId
import io.composeflow.model.project.findApiDefinitionOrNull
import io.composeflow.model.project.findComposeNodeOrNull
import io.composeflow.model.project.findComposeNodeOrThrow
import io.composeflow.model.project.findCustomEnumOrNull
import io.composeflow.model.project.findDataTypeOrNull
import io.composeflow.model.project.findDataTypeOrThrow
import io.composeflow.model.project.findFirestoreCollectionOrNull
import io.composeflow.model.project.findLocalStateOrNull
import io.composeflow.model.project.findParameterOrNull
import io.composeflow.model.project.findParameterOrThrow
import io.composeflow.model.project.firebase.CollectionId
import io.composeflow.model.project.string.StringResourceId
import io.composeflow.model.project.string.stringResourceDefaultValue
import io.composeflow.model.project.string.updateStringResourceDefaultLocaleValue
import io.composeflow.model.state.AppState
import io.composeflow.model.state.ReadableState
import io.composeflow.model.state.ScreenState
import io.composeflow.model.state.StateId
import io.composeflow.model.state.WriteableState
import io.composeflow.model.type.ComposeFlowType
import io.composeflow.model.type.convertCodeFromType
import io.composeflow.model.type.emptyDocumentIdType
import io.composeflow.override.mutableStateListEqualsOverrideOf
import io.composeflow.replaceSpaces
import io.composeflow.selectString
import io.composeflow.serializer.FallbackMutableStateListSerializer
import io.composeflow.serializer.JsonAsStringSerializer
import io.composeflow.ui.icon.ComposeFlowIcon
import io.composeflow.ui.propertyeditor.AssignableBooleanPropertyEditor
import io.composeflow.ui.propertyeditor.AssignableBrushPropertyEditor
import io.composeflow.ui.propertyeditor.AssignableColorPropertyEditor
import io.composeflow.ui.propertyeditor.AssignableDataTypePropertyEditor
import io.composeflow.ui.propertyeditor.AssignableEditableTextPropertyEditor
import io.composeflow.ui.propertyeditor.AssignableInstantPropertyEditor
import io.composeflow.ui.propertyeditor.DropdownProperty
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import org.jetbrains.compose.resources.stringResource
import kotlin.uuid.Uuid

@Serializable
@SerialName("AssignableProperty")
sealed interface AssignableProperty {
    val propertyTransformers: MutableList<PropertyTransformer>

    /**
     * Get the eum value from the property. Only effective if the property is [EnumProperty].
     * Returns null otherwise.
     */
    fun getEnumValue(): Enum<*>? = null

    /**
     * Get all AssignableProperties contained within this property, including those in transformers.
     */
    fun getAssignableProperties(includeSelf: Boolean = true): List<AssignableProperty> =
        propertyTransformers.flatMap { it.getAssignableProperties() } +
            if (includeSelf) {
                listOf(this)
            } else {
                emptyList()
            }

    fun valueType(project: Project): ComposeFlowType

    fun transformedValueType(project: Project): ComposeFlowType =
        if (propertyTransformers.isEmpty()) {
            valueType(project)
        } else {
            if (propertyTransformers.size == 1) {
                if (propertyTransformers.areValidRelationships() &&
                    propertyTransformers[0].fromType()::class == valueType(
                        project,
                    )::class
                ) {
                    propertyTransformers.last().toType()
                } else {
                    ComposeFlowType.UnknownType()
                }
            } else if (propertyTransformers.areValidRelationships()) {
                propertyTransformers.last().toType()
            } else {
                ComposeFlowType.UnknownType()
            }
        }

    /**
     * String expression in the dropped Composable int the preview in the editor UI
     */
    fun valueExpression(project: Project): String

    fun transformedValueExpression(project: Project): String {
        var result = valueExpression(project)
        propertyTransformers.forEach {
            result = it.transformedValueExpression(project, result)
        }
        return result
    }

    fun displayText(project: Project): String

    fun getPropertyAvailableAt(): PropertyAvailableAt = PropertyAvailableAt.Both

    /**
     * Generate a ParameterSpec passed to a method to a ViewModel.
     * For example, when the property is only available at Compose screen, when the property needs
     * to be read from ViewModel, that parameter needs to be passed to the method in the ViewModel.
     */
    fun generateParameterSpec(project: Project): ParameterSpec? = null

    /**
     * Generate CodeBlock that represents this property.
     *
     * @param project contains project related information
     * @param context represents the calling context to generate the code block such as whether
     *                this is called from ComposeScreen or ViewModel
     * @param writeType The type to which this property is assigned. If not specified, the type of
     *                  this property itself is used
     */
    fun generateCodeBlock(
        project: Project,
        context: GenerationContext,
        writeType: ComposeFlowType = valueType(project),
        dryRun: Boolean,
    ): CodeBlock

    /**
     * Generates the CodeBlock if the action that triggers the action needs to be wrapped with
     * some code.
     * E.g. Firestore collecion property needs to wrapped with destructuring the result based on
     * the loading state for example, we want to use the result in the Text composable.
     *
     * In that case, the code that reads the result of the Firestore needs to be wrapped with the
     * when expression.
     * ```
     * when (val result = firestoreCollectionResult) {
     *     QueryResult.NotReady -> {
     *         CircularProgressIndicator()
     *     }
     *     is QueryResult.Ready -> {
     *         result.forEach {
     *             Text(it.name)
     *         }
     *     }
     * }
     * ```.
     * }
     */
    fun generateWrapWithComposableBlock(
        project: Project,
        insideContent: CodeBlock,
    ): CodeBlock? = null

    /**
     * Counter part of the code block that needs to be placed in the ViewModel
     */
    fun generateWrapWithViewModelBlock(
        project: Project,
        insideContent: CodeBlock,
    ): CodeBlock? = null

    /**
     * Generate CodeBlock that represents this property including the transformations by using
     * [propertyTransformers].
     *
     * @param project contains project related information
     * @param context represents the calling context to generate the code block such as whether
     *                this is called from ComposeScreen or ViewModel
     * @param writeType The type to which this property is assigned. If not specified, the type of
     *                  this property itself is used
     */
    fun transformedCodeBlock(
        project: Project,
        context: GenerationContext,
        writeType: ComposeFlowType = transformedValueType(project),
        dryRun: Boolean,
    ): CodeBlock

    fun addReadProperty(
        project: Project,
        context: GenerationContext,
        dryRun: Boolean,
    ) {
    }

    fun getDependentComposeNodes(project: Project): List<ComposeNode> = emptyList()

    fun isDependent(sourceId: String): Boolean = propertyTransformers.any { it.isDependent(sourceId) }

    /**
     * Method to compare the identity with the other property.
     * This is usually comparing the identity except for a case where the same property is
     * represented as [ValueFromCompanionState], which is considered as same with [ValueFromState]
     * with the same state ID.
     */
    fun isIdentical(other: AssignableProperty): Boolean = this == other

    @Composable
    fun Editor(
        project: Project,
        node: ComposeNode,
        initialProperty: AssignableProperty?,
        label: String,
        onValidPropertyChanged: (AssignableProperty, lazyListSource: LazyListChildParams?) -> Unit,
        onInitializeProperty: (() -> Unit)?,
        functionScopeProperties: List<FunctionScopeParameterProperty>,
        modifier: Modifier,
        destinationStateId: StateId?,
        validateInput: ((String) -> ValidateResult)?,
        editable: Boolean,
    ) {
    }
}

fun AssignableProperty?.mergeProperty(
    project: Project,
    newProperty: AssignableProperty,
): AssignableProperty =
    if (this is IntrinsicProperty<*> &&
        newProperty is IntrinsicProperty<*> &&
        this.valueType(project) == newProperty.valueType(project)
    ) {
        newProperty.propertyTransformers.addAll(this.propertyTransformers)
        newProperty
    } else if (this is StringProperty.ValueFromStringResource &&
        newProperty is StringProperty.StringIntrinsicValue
    ) {
        project.stringResourceHolder.updateStringResourceDefaultLocaleValue(
            resourceId = stringResourceId,
            newDefaultLocaleValue = newProperty.value,
        )
        this
    } else {
        newProperty
    }

@Serializable
@SerialName("AssignablePropertyBase")
abstract class AssignablePropertyBase : AssignableProperty {
    @Serializable(FallbackMutableStateListSerializer::class)
    final override val propertyTransformers: MutableList<PropertyTransformer> =
        mutableStateListEqualsOverrideOf()

    /**
     * Flag to remember if the transformations has applied to avoid that the same transformations
     * are applied multiple times.
     */
    @Transient
    private var isTransformed: Boolean = false

    final override fun transformedCodeBlock(
        project: Project,
        context: GenerationContext,
        writeType: ComposeFlowType,
        dryRun: Boolean,
    ): CodeBlock {
        var result = generateCodeBlock(project, context, dryRun = dryRun)
        if (isTransformed) {
            return writeType.convertCodeFromType(transformedValueType(project), result)
        }

        propertyTransformers.forEach {
            result = it.transformedCodeBlock(project, result, context, dryRun = dryRun)
        }

        if (!dryRun) {
            isTransformed = true
        }
        return writeType.convertCodeFromType(transformedValueType(project), result)
    }
}

@Serializable
sealed interface IntrinsicProperty<T> {
    val value: T

    fun asCodeBlock(): CodeBlock
}

@Serializable
sealed interface StringProperty : AssignableProperty {
    override fun valueType(project: Project) = ComposeFlowType.StringType()

    @Serializable
    @SerialName("StringIntrinsicValue")
    data class StringIntrinsicValue(
        override val value: String = "",
    ) : AssignablePropertyBase(),
        StringProperty,
        IntrinsicProperty<String> {
        override fun valueExpression(project: Project): String = value

        override fun generateCodeBlock(
            project: Project,
            context: GenerationContext,
            writeType: ComposeFlowType,
            dryRun: Boolean,
        ) = asCodeBlock()

        override fun displayText(project: Project): String = value

        override fun asCodeBlock(): CodeBlock =
            if (value.contains("\n") || value.contains("\r") || value.length >= KOTLINPOET_COLUMN_LIMIT) {
                // It looks like Kotlinpoet's column limit is hard-coded as 100.
                // That means a string more than 100 characters can be translated as multiline
                // string unintentionally.
                CodeBlock.of("\"\"\"%L\"\"\"", value)
            } else {
                CodeBlock.of("\"%L\"", value)
            }
    }

    @Serializable
    @SerialName("ValueByJsonPath")
    data class ValueByJsonPath(
        val jsonPath: String,
        @Serializable(with = JsonAsStringSerializer::class)
        val jsonElement: JsonElement,
    ) : AssignablePropertyBase(),
        StringProperty {
        override fun generateCodeBlock(
            project: Project,
            context: GenerationContext,
            writeType: ComposeFlowType,
            dryRun: Boolean,
        ): CodeBlock =
            CodeBlock.of(
                """%M(jsonElement, "$jsonPath", replaceQuotation = true)""",
                MemberName("${COMPOSEFLOW_PACKAGE}.util", "selectString"),
            )

        override fun valueExpression(project: Project) = "[JsonPath: $jsonPath]"

        override fun displayText(project: Project): String = jsonElement.selectString(jsonPath, replaceQuotation = true)
    }

    @Serializable
    @SerialName("ValueFromStringResource")
    data class ValueFromStringResource(
        val stringResourceId: StringResourceId,
    ) : AssignablePropertyBase(),
        StringProperty {
        override fun valueExpression(project: Project): String =
            project.stringResourceHolder.stringResourceDefaultValue(stringResourceId).orEmpty()

        override fun generateCodeBlock(
            project: Project,
            context: GenerationContext,
            writeType: ComposeFlowType,
            dryRun: Boolean,
        ): CodeBlock {
            val stringResource = project.stringResourceHolder.stringResources.find { it.id == stringResourceId }
            return if (stringResource != null && stringResource.key.isNotBlank()) {
                CodeBlock.of(
                    "%M(%M.string.%M)",
                    MemberHolder.JetBrains.stringResource,
                    MemberHolder.ComposeFlow.Res,
                    MemberName(
                        COMPOSEFLOW_PACKAGE,
                        stringResource.key,
                    ),
                )
            } else {
                CodeBlock.of("\"\"")
            }
        }

        override fun displayText(project: Project): String =
            project.stringResourceHolder.stringResourceDefaultValue(stringResourceId).orEmpty()

        override fun isDependent(sourceId: String): Boolean =
            stringResourceId == sourceId || super<AssignablePropertyBase>.isDependent(sourceId)
    }

    @Composable
    override fun Editor(
        project: Project,
        node: ComposeNode,
        initialProperty: AssignableProperty?,
        label: String,
        onValidPropertyChanged: (AssignableProperty, lazyListSource: LazyListChildParams?) -> Unit,
        onInitializeProperty: (() -> Unit)?,
        functionScopeProperties: List<FunctionScopeParameterProperty>,
        modifier: Modifier,
        destinationStateId: StateId?,
        validateInput: ((String) -> ValidateResult)?,
        editable: Boolean,
    ) {
        AssignableEditableTextPropertyEditor(
            project = project,
            node = node,
            acceptableType = valueType(project),
            initialProperty = initialProperty,
            label = label,
            onValidPropertyChanged = { property, lazyListSource ->
                onValidPropertyChanged(property, lazyListSource)
            },
            modifier = modifier,
            onInitializeProperty = onInitializeProperty,
            destinationStateId = destinationStateId,
            leadingIcon = {
                ComposeFlowIcon(
                    imageVector = Icons.Outlined.TextFields,
                    contentDescription = null,
                )
            },
            validateInput = validateInput ?: { ValidateResult.Success },
            editable = editable,
            functionScopeProperties = functionScopeProperties,
        )
    }
}

@Serializable
sealed interface DocumentIdProperty : AssignableProperty {
    @Serializable
    @SerialName("FirestoreDocumentIdProperty")
    data class FirestoreDocumentIdProperty(
        val firestoreCollectionId: CollectionId,
    ) : AssignablePropertyBase(),
        DocumentIdProperty {
        override fun valueType(project: Project): ComposeFlowType =
            project.findFirestoreCollectionOrNull(firestoreCollectionId)?.let {
                ComposeFlowType.DocumentIdType(it.id)
            } ?: ComposeFlowType.UnknownType()

        override fun valueExpression(project: Project): String = ""

        override fun displayText(project: Project): String = ""

        override fun generateCodeBlock(
            project: Project,
            context: GenerationContext,
            writeType: ComposeFlowType,
            dryRun: Boolean,
        ): CodeBlock = CodeBlock.of("")
    }

    /**
     * Empty document ID property that is used for a placeholder, which needs a data source
     * (instead of Intrinsic value).
     */
    @Serializable
    @SerialName("EmptyDocumentId")
    data object EmptyDocumentId :
        AssignablePropertyBase(),
        DocumentIdProperty {
        override fun valueType(project: Project) = emptyDocumentIdType

        override fun valueExpression(project: Project): String = ""

        override fun displayText(project: Project): String = ""

        override fun generateCodeBlock(
            project: Project,
            context: GenerationContext,
            writeType: ComposeFlowType,
            dryRun: Boolean,
        ): CodeBlock = CodeBlock.of("")
    }

    @Composable
    override fun Editor(
        project: Project,
        node: ComposeNode,
        initialProperty: AssignableProperty?,
        label: String,
        onValidPropertyChanged: (AssignableProperty, lazyListSource: LazyListChildParams?) -> Unit,
        onInitializeProperty: (() -> Unit)?,
        functionScopeProperties: List<FunctionScopeParameterProperty>,
        modifier: Modifier,
        destinationStateId: StateId?,
        validateInput: ((String) -> ValidateResult)?,
        editable: Boolean,
    ) {
        AssignableEditableTextPropertyEditor(
            project = project,
            node = node,
            acceptableType = valueType(project),
            initialProperty = initialProperty,
            label = label,
            onValidPropertyChanged = { property, lazyListSource ->
                onValidPropertyChanged(property, lazyListSource)
            },
            modifier = modifier,
            onInitializeProperty = onInitializeProperty,
            destinationStateId = destinationStateId,
            leadingIcon = {
                ComposeFlowIcon(
                    imageVector = ComposeFlowIcons.Firebase,
                    contentDescription = null,
                )
            },
            validateInput = validateInput ?: { ValidateResult.Success },
            editable = true,
            functionScopeProperties = functionScopeProperties,
        )
    }
}

@Serializable
sealed interface IntProperty : AssignableProperty {
    override fun valueType(project: Project) = ComposeFlowType.IntType()

    @Serializable
    @SerialName("IntIntrinsicValue")
    data class IntIntrinsicValue(
        override val value: Int = 0,
    ) : AssignablePropertyBase(),
        IntProperty,
        IntrinsicProperty<Int> {
        override fun valueExpression(project: Project) = value.toString()

        override fun displayText(project: Project): String = value.toString()

        override fun generateCodeBlock(
            project: Project,
            context: GenerationContext,
            writeType: ComposeFlowType,
            dryRun: Boolean,
        ) = asCodeBlock()

        override fun asCodeBlock(): CodeBlock = CodeBlock.of("$value")
    }

    @Serializable
    @SerialName("ValueFromLazyListIndex")
    data class ValueFromLazyListIndex(
        val lazyListNodeId: String,
    ) : AssignablePropertyBase(),
        IntProperty {
        override fun generateCodeBlock(
            project: Project,
            context: GenerationContext,
            writeType: ComposeFlowType,
            dryRun: Boolean,
        ): CodeBlock {
            val lazyList = project.findComposeNodeOrThrow(lazyListNodeId)
            val codeBlock =
                CodeBlock.of(
                    "${
                        lazyList.trait.value.iconText().replaceSpaces()
                            .replaceFirstChar { it.lowercase() }
                    }Index",
                )
            return writeType.convertCodeFromType(
                ComposeFlowType.IntType(),
                codeBlock,
            )
        }

        override fun valueExpression(project: Project): String = textFromState(project, lazyListNodeId)

        override fun displayText(project: Project): String = textFromState(project, lazyListNodeId)

        private fun textFromState(
            project: Project,
            composeNodeId: String,
        ): String =
            project.findComposeNodeOrNull(composeNodeId)?.let {
                "[${it.displayName(project) + "Index"}]"
            } ?: "[Invalid]"
    }

    @Composable
    override fun Editor(
        project: Project,
        node: ComposeNode,
        initialProperty: AssignableProperty?,
        label: String,
        onValidPropertyChanged: (AssignableProperty, lazyListSource: LazyListChildParams?) -> Unit,
        onInitializeProperty: (() -> Unit)?,
        functionScopeProperties: List<FunctionScopeParameterProperty>,
        modifier: Modifier,
        destinationStateId: StateId?,
        validateInput: ((String) -> ValidateResult)?,
        editable: Boolean,
    ) {
        AssignableEditableTextPropertyEditor(
            project = project,
            node = node,
            acceptableType = valueType(project),
            initialProperty = initialProperty,
            label = label,
            onValidPropertyChanged = { property, lazyListSource ->
                onValidPropertyChanged(property, lazyListSource)
            },
            modifier = modifier,
            destinationStateId = destinationStateId,
            onInitializeProperty = onInitializeProperty,
            leadingIcon = {
                ComposeFlowIcon(
                    Icons.Outlined.Numbers,
                    contentDescription = null,
                )
            },
            validateInput = validateInput ?: { IntValidator().validate(it) },
            editable = editable,
            functionScopeProperties = functionScopeProperties,
        )
    }
}

@Serializable
sealed interface FloatProperty : AssignableProperty {
    override fun valueType(project: Project) = ComposeFlowType.FloatType()

    @Serializable
    @SerialName("FloatIntrinsicValue")
    data class FloatIntrinsicValue(
        override val value: Float = 0f,
    ) : AssignablePropertyBase(),
        FloatProperty,
        IntrinsicProperty<Float> {
        override fun valueExpression(project: Project) = value.toString()

        override fun displayText(project: Project): String = value.toString()

        override fun generateCodeBlock(
            project: Project,
            context: GenerationContext,
            writeType: ComposeFlowType,
            dryRun: Boolean,
        ) = asCodeBlock()

        override fun asCodeBlock(): CodeBlock = CodeBlock.of("${value}f")
    }

    @Composable
    override fun Editor(
        project: Project,
        node: ComposeNode,
        initialProperty: AssignableProperty?,
        label: String,
        onValidPropertyChanged: (AssignableProperty, lazyListSource: LazyListChildParams?) -> Unit,
        onInitializeProperty: (() -> Unit)?,
        functionScopeProperties: List<FunctionScopeParameterProperty>,
        modifier: Modifier,
        destinationStateId: StateId?,
        validateInput: ((String) -> ValidateResult)?,
        editable: Boolean,
    ) {
        AssignableEditableTextPropertyEditor(
            project = project,
            node = node,
            acceptableType = valueType(project),
            initialProperty = initialProperty,
            label = label,
            onValidPropertyChanged = { property, lazyListSource ->
                onValidPropertyChanged(property, lazyListSource)
            },
            modifier = modifier,
            destinationStateId = destinationStateId,
            onInitializeProperty = onInitializeProperty,
            leadingIcon = {
                ComposeFlowIcon(
                    Icons.Outlined.Numbers,
                    contentDescription = null,
                )
            },
            validateInput = validateInput ?: { FloatValidator().validate(it) },
            editable = editable,
            functionScopeProperties = functionScopeProperties,
        )
    }
}

@Serializable
sealed interface BooleanProperty : AssignableProperty {
    override fun valueType(project: Project) = ComposeFlowType.BooleanType()

    @Serializable
    @SerialName("BooleanIntrinsicValue")
    data class BooleanIntrinsicValue(
        override val value: Boolean = false,
    ) : AssignablePropertyBase(),
        BooleanProperty,
        IntrinsicProperty<Boolean> {
        override fun valueExpression(project: Project) = value.toString()

        override fun displayText(project: Project): String = value.toString()

        override fun generateCodeBlock(
            project: Project,
            context: GenerationContext,
            writeType: ComposeFlowType,
            dryRun: Boolean,
        ) = asCodeBlock()

        override fun asCodeBlock(): CodeBlock = CodeBlock.of("$value")
    }

    @Composable
    override fun Editor(
        project: Project,
        node: ComposeNode,
        initialProperty: AssignableProperty?,
        label: String,
        onValidPropertyChanged: (AssignableProperty, lazyListSource: LazyListChildParams?) -> Unit,
        onInitializeProperty: (() -> Unit)?,
        functionScopeProperties: List<FunctionScopeParameterProperty>,
        modifier: Modifier,
        destinationStateId: StateId?,
        validateInput: ((String) -> ValidateResult)?,
        editable: Boolean,
    ) {
        AssignableBooleanPropertyEditor(
            project = project,
            node = node,
            acceptableType = valueType(project),
            initialProperty = initialProperty,
            label = label,
            onValidPropertyChanged = { property, lazyListSource ->
                onValidPropertyChanged(property, lazyListSource)
            },
            modifier = modifier,
            destinationStateId = destinationStateId,
            onInitializeProperty = onInitializeProperty,
            editable = editable,
            functionScopeProperties = functionScopeProperties,
        )
    }

    /**
     * Empty property that is used for a placeholder, which needs a data source
     * (instead of Intrinsic value). For example, as an initial placeholder for
     * [ConditionalProperty] for the BooleanProperty.
     */
    @Serializable
    @SerialName("BooleanPropertyEmpty")
    data object Empty :
        AssignablePropertyBase(),
        BooleanProperty {
        override fun valueExpression(project: Project): String = ""

        override fun displayText(project: Project): String = ""

        override fun generateCodeBlock(
            project: Project,
            context: GenerationContext,
            writeType: ComposeFlowType,
            dryRun: Boolean,
        ): CodeBlock = throw IllegalStateException("Empty property isn't able to generate code")
    }
}

@Serializable
sealed interface ColorProperty : AssignableProperty {
    override fun valueType(project: Project) = ComposeFlowType.Color()

    @Serializable
    @SerialName("ColorIntrinsicValue")
    data class ColorIntrinsicValue(
        override val value: ColorWrapper = defaultColorWrapper,
    ) : AssignablePropertyBase(),
        ColorProperty,
        IntrinsicProperty<ColorWrapper> {
        override fun valueExpression(project: Project) = value.asString()

        override fun displayText(project: Project): String = value.asString()

        override fun generateCodeBlock(
            project: Project,
            context: GenerationContext,
            writeType: ComposeFlowType,
            dryRun: Boolean,
        ) = asCodeBlock()

        override fun asCodeBlock(): CodeBlock = value.generateCode()
    }

    @Composable
    override fun Editor(
        project: Project,
        node: ComposeNode,
        initialProperty: AssignableProperty?,
        label: String,
        onValidPropertyChanged: (AssignableProperty, lazyListSource: LazyListChildParams?) -> Unit,
        onInitializeProperty: (() -> Unit)?,
        functionScopeProperties: List<FunctionScopeParameterProperty>,
        modifier: Modifier,
        destinationStateId: StateId?,
        validateInput: ((String) -> ValidateResult)?,
        editable: Boolean,
    ) {
        AssignableColorPropertyEditor(
            project = project,
            node = node,
            label = label,
            initialProperty = initialProperty,
            onValidPropertyChanged = onValidPropertyChanged,
            onInitializeProperty = onInitializeProperty,
            modifier = modifier,
            destinationStateId = destinationStateId,
            editable = editable,
            functionScopeProperties = functionScopeProperties,
        )
    }
}

@Serializable
sealed interface BrushProperty : AssignableProperty {
    override fun valueType(project: Project) = ComposeFlowType.Brush()

    @Serializable
    @SerialName("BrushIntrinsicValue")
    data class BrushIntrinsicValue(
        override val value: BrushWrapper = defaultBrushWrapper,
    ) : AssignablePropertyBase(),
        BrushProperty,
        IntrinsicProperty<BrushWrapper> {
        override fun valueExpression(project: Project) = value.asString()

        override fun displayText(project: Project): String = value.asString()

        override fun generateCodeBlock(
            project: Project,
            context: GenerationContext,
            writeType: ComposeFlowType,
            dryRun: Boolean,
        ) = asCodeBlock()

        override fun asCodeBlock(): CodeBlock = value.generateCode()
    }

    @Composable
    override fun Editor(
        project: Project,
        node: ComposeNode,
        initialProperty: AssignableProperty?,
        label: String,
        onValidPropertyChanged: (AssignableProperty, lazyListSource: LazyListChildParams?) -> Unit,
        onInitializeProperty: (() -> Unit)?,
        functionScopeProperties: List<FunctionScopeParameterProperty>,
        modifier: Modifier,
        destinationStateId: StateId?,
        validateInput: ((String) -> ValidateResult)?,
        editable: Boolean,
    ) {
        AssignableBrushPropertyEditor(
            project = project,
            node = node,
            label = label,
            initialProperty = initialProperty,
            onValidPropertyChanged = onValidPropertyChanged,
            onInitializeProperty = onInitializeProperty,
            modifier = modifier,
            destinationStateId = destinationStateId,
            editable = editable,
            functionScopeProperties = functionScopeProperties,
        )
    }
}

@Serializable
sealed interface InstantProperty : AssignableProperty {
    override fun valueType(project: Project) = ComposeFlowType.InstantType()

    @Serializable
    @SerialName("InstantIntrinsicValue")
    data class InstantIntrinsicValue(
        override val value: InstantWrapper = InstantWrapper(),
    ) : AssignablePropertyBase(),
        InstantProperty,
        IntrinsicProperty<InstantWrapper> {
        override fun valueExpression(project: Project) = value.asString()

        override fun displayText(project: Project): String = value.asString()

        override fun generateCodeBlock(
            project: Project,
            context: GenerationContext,
            writeType: ComposeFlowType,
            dryRun: Boolean,
        ): CodeBlock = asCodeBlock()

        override fun asCodeBlock(): CodeBlock = value.generateCode()
    }

    @Composable
    override fun Editor(
        project: Project,
        node: ComposeNode,
        initialProperty: AssignableProperty?,
        label: String,
        onValidPropertyChanged: (AssignableProperty, lazyListSource: LazyListChildParams?) -> Unit,
        onInitializeProperty: (() -> Unit)?,
        functionScopeProperties: List<FunctionScopeParameterProperty>,
        modifier: Modifier,
        destinationStateId: StateId?,
        validateInput: ((String) -> ValidateResult)?,
        editable: Boolean,
    ) {
        AssignableInstantPropertyEditor(
            project = project,
            node = node,
            label = label,
            initialProperty = initialProperty,
            onValidPropertyChanged = onValidPropertyChanged,
            onInitializeProperty = onInitializeProperty,
            modifier = modifier,
            destinationStateId = destinationStateId,
            editable = editable,
            functionScopeProperties = functionScopeProperties,
        )
    }
}

@Serializable
@SerialName("ValueFromState")
data class ValueFromState(
    val readFromStateId: StateId,
) : AssignablePropertyBase(),
    AssignableProperty {
    override fun valueType(project: Project): ComposeFlowType =
        project.findLocalStateOrNull(readFromStateId)?.valueType(project)
            ?: ComposeFlowType.UnknownType()

    override fun valueExpression(project: Project): String = textFromState(project, readFromStateId)

    override fun displayText(project: Project): String = textFromState(project, readFromStateId)

    private fun textFromState(
        project: Project,
        stateId: StateId,
    ): String =
        project.findLocalStateOrNull(stateId)?.let {
            val prefix =
                if (it is ScreenState<*>) {
                    "state: "
                } else if (it is AppState<*>) {
                    "app state: "
                } else {
                    ""
                }
            "[$prefix${it.name}]"
        } ?: "[Invalid]"

    override fun isDependent(sourceId: String): Boolean = readFromStateId == sourceId || super<AssignableProperty>.isDependent(sourceId)

    override fun isIdentical(other: AssignableProperty): Boolean =
        if (other is ValueFromCompanionState) {
            other.composeNode?.companionStateId == readFromStateId
        } else {
            other == this
        }

    override fun getDependentComposeNodes(project: Project): List<ComposeNode> {
        val companionComposeNode =
            project.findLocalStateOrNull(readFromStateId)?.let { readState ->
                when (readState) {
                    is ScreenState<*> -> {
                        readState.companionNodeId?.let {
                            project.findComposeNodeOrNull(it)
                        }
                    }

                    else -> null
                }
            }
        return companionComposeNode?.let {
            listOf(it)
        } ?: emptyList()
    }

    override fun generateCodeBlock(
        project: Project,
        context: GenerationContext,
        writeType: ComposeFlowType,
        dryRun: Boolean,
    ): CodeBlock {
        val state = project.findLocalStateOrNull(readFromStateId) ?: return CodeBlock.of("")
        val codeBlock =
            when (context.generatedPlace) {
                GeneratedPlace.ComposeScreen ->
                    CodeBlock.of(
                        state.getReadVariableName(
                            project,
                            context,
                        ),
                    )

                GeneratedPlace.ViewModel -> CodeBlock.of("${state.getFlowName(context)}.value")
                GeneratedPlace.Unspecified ->
                    CodeBlock.of(
                        state.getReadVariableName(
                            project,
                            context,
                        ),
                    )
            }

        if (state is WriteableState) {
            state.generateStatePropertiesToViewModel(project, context).forEach {
                context.addProperty(it, dryRun = dryRun)
            }
        }
        return codeBlock
    }

    override fun addReadProperty(
        project: Project,
        context: GenerationContext,
        dryRun: Boolean,
    ) {
        val state = project.findLocalStateOrNull(readFromStateId) ?: return
        if (state is WriteableState) {
            state
                .generateStatePropertiesToViewModel(
                    project,
                    context,
                ).forEach {
                    context.addProperty(it, dryRun = dryRun)
                }
        }
    }

    @Composable
    override fun Editor(
        project: Project,
        node: ComposeNode,
        initialProperty: AssignableProperty?,
        label: String,
        onValidPropertyChanged: (AssignableProperty, lazyListSource: LazyListChildParams?) -> Unit,
        onInitializeProperty: (() -> Unit)?,
        functionScopeProperties: List<FunctionScopeParameterProperty>,
        modifier: Modifier,
        destinationStateId: StateId?,
        validateInput: ((String) -> ValidateResult)?,
        editable: Boolean,
    ) {
        val readState = project.findLocalStateOrNull(readFromStateId)
        AssignableEditableTextPropertyEditor(
            project = project,
            node = node,
            initialProperty = initialProperty,
            label = label,
            onValidPropertyChanged = onValidPropertyChanged,
            modifier = modifier,
            destinationStateId = destinationStateId,
            onInitializeProperty = onInitializeProperty,
            leadingIcon =
                readState?.valueType(project)?.leadingIcon()?.let {
                    {
                        ComposeFlowIcon(
                            it,
                            contentDescription = null,
                        )
                    }
                },
            validateInput = validateInput ?: { ValidateResult.Success },
            editable = editable,
            functionScopeProperties = functionScopeProperties,
        )
    }
}

/**
 * State that represents the value is assigned from a companion state.
 * For example, TextFieldTrait has a default companion state that represents its input value.
 * This class represents that the specific property is assigned from such a companion state.
 * Technically it's same as using [ValueFromState] with the ID representing the state, but this
 * class exits to reduce the implication as much as possible,
 */
@Serializable
@SerialName("ValueFromCompanionState")
data class ValueFromCompanionState(
    /**
     * The reference to the composeNode that has the companion state.
     * Intentionally defining it as MutableState since the initialization of the composeNode
     * may happen after the matching screen is loaded. And the values that depend on this reference
     * needs to be aware of the composition in the UI because they may be used in the UI.
     */
    @Transient
    val composeNode: ComposeNode? = null,
) : AssignablePropertyBase(),
    AssignableProperty {
    @Transient
    private val companionState: ScreenState<*>? =
        composeNode?.let { node ->
            node.trait.value.companionState(node)
        }

    override fun valueType(project: Project): ComposeFlowType =
        companionState?.valueType(project)
            ?: ComposeFlowType.UnknownType()

    override fun valueExpression(project: Project): String = textFromState()

    override fun displayText(project: Project): String = textFromState()

    override fun isIdentical(other: AssignableProperty): Boolean =
        if (other is ValueFromState) {
            other.readFromStateId == composeNode?.companionStateId
        } else {
            other == this
        }

    private fun textFromState(): String =
        companionState?.let {
            // companion state is always screen level
            val prefix = "state: "
            "[$prefix${it.name}]"
        } ?: "[Invalid]"

    override fun isDependent(sourceId: String): Boolean = companionState?.id == sourceId || super<AssignableProperty>.isDependent(sourceId)

    override fun getDependentComposeNodes(project: Project): List<ComposeNode> =
        composeNode?.let {
            listOf(it)
        } ?: emptyList()

    override fun generateCodeBlock(
        project: Project,
        context: GenerationContext,
        writeType: ComposeFlowType,
        dryRun: Boolean,
    ): CodeBlock {
        val state = companionState ?: return CodeBlock.of("")
        val codeBlock =
            when (context.generatedPlace) {
                GeneratedPlace.ComposeScreen ->
                    CodeBlock.of(
                        state.getReadVariableName(
                            project,
                            context,
                        ),
                    )

                GeneratedPlace.ViewModel -> CodeBlock.of("${state.getFlowName(context)}.value")
                GeneratedPlace.Unspecified ->
                    CodeBlock.of(
                        state.getReadVariableName(
                            project,
                            context,
                        ),
                    )
            }

        state.generateStatePropertiesToViewModel(project, context).forEach {
            context.addProperty(it, dryRun = dryRun)
        }
        return codeBlock
    }

    override fun addReadProperty(
        project: Project,
        context: GenerationContext,
        dryRun: Boolean,
    ) {
        val state = companionState ?: return
        state
            .generateStatePropertiesToViewModel(
                project,
                context,
            ).forEach {
                context.addProperty(it, dryRun = dryRun)
            }
    }

    @Composable
    override fun Editor(
        project: Project,
        node: ComposeNode,
        initialProperty: AssignableProperty?,
        label: String,
        onValidPropertyChanged: (AssignableProperty, lazyListSource: LazyListChildParams?) -> Unit,
        onInitializeProperty: (() -> Unit)?,
        functionScopeProperties: List<FunctionScopeParameterProperty>,
        modifier: Modifier,
        destinationStateId: StateId?,
        validateInput: ((String) -> ValidateResult)?,
        editable: Boolean,
    ) {
        val readState = companionState
        AssignableEditableTextPropertyEditor(
            project = project,
            node = node,
            initialProperty = initialProperty,
            label = label,
            onValidPropertyChanged = onValidPropertyChanged,
            modifier = modifier,
            destinationStateId = destinationStateId,
            onInitializeProperty = onInitializeProperty,
            leadingIcon =
                readState?.valueType(project)?.leadingIcon()?.let {
                    {
                        ComposeFlowIcon(
                            it,
                            contentDescription = null,
                        )
                    }
                },
            validateInput = validateInput ?: { ValidateResult.Success },
            editable = editable,
            functionScopeProperties = functionScopeProperties,
        )
    }
}

@Serializable
@SerialName("ValueFromDynamicItem")
data class ValueFromDynamicItem(
    val composeNodeId: String,
    val fieldType: DataFieldType = DataFieldType.Primitive,
) : AssignablePropertyBase(),
    AssignableProperty {
    override fun valueType(project: Project): ComposeFlowType {
        val dynamicItems =
            project.findComposeNodeOrNull(composeNodeId)?.dynamicItems?.value
                ?: return ComposeFlowType.UnknownType()

        val itemType = dynamicItems.transformedValueType(project).copyWith(newIsList = false)
        return when (fieldType) {
            is DataFieldType.FieldInDataType -> {
                val dataType =
                    (itemType as? ComposeFlowType.CustomDataType)?.dataTypeId?.let {
                        project.findDataTypeOrNull(it)
                    }
                val dataField = dataType?.findDataFieldOrNull(fieldType.fieldId)
                dataField?.fieldType?.type() ?: ComposeFlowType.UnknownType()
            }

            is DataFieldType.DataType -> {
                project.findDataTypeOrNull(fieldType.dataTypeId)?.let {
                    ComposeFlowType
                        .CustomDataType(dataTypeId = fieldType.dataTypeId)
                        .copyWith(newIsList = false)
                } ?: ComposeFlowType.UnknownType()
            }

            DataFieldType.Primitive ->
                dynamicItems
                    .transformedValueType(project)
                    .copyWith(newIsList = false)

            is DataFieldType.DocumentId -> {
                ComposeFlowType.DocumentIdType(fieldType.firestoreCollectionId)
            }
        }
    }

    override fun generateParameterSpec(project: Project): ParameterSpec? {
        val type = valueType(project)
        val composeNode = project.findComposeNodeOrNull(composeNodeId)
        return if (type is ComposeFlowType.UnknownType || composeNode == null) {
            null
        } else {
            ParameterSpec
                .builder(
                    name =
                        "${
                            composeNode.trait.value.iconText().replaceSpaces()
                                .replaceFirstChar { it.lowercase() }
                        }Item" +
                            fieldNameAsViewModel(
                                fieldType.fieldName(project),
                            ),
                    type = type.asKotlinPoetTypeName(project),
                ).build()
        }
    }

    override fun generateCodeBlock(
        project: Project,
        context: GenerationContext,
        writeType: ComposeFlowType,
        dryRun: Boolean,
    ): CodeBlock {
        val builder = CodeBlock.builder()
        val composeNode = project.findComposeNodeOrNull(composeNodeId) ?: return builder.build()

        return when (context.generatedPlace) {
            GeneratedPlace.ComposeScreen, GeneratedPlace.Unspecified -> {
                val valueExpression =
                    "${
                        composeNode.trait.value.iconText().replaceSpaces()
                            .replaceFirstChar { it.lowercase() }
                    }Item" +
                        fieldType.fieldName(
                            project,
                        )
                CodeBlock.of(valueExpression)
            }

            GeneratedPlace.ViewModel -> {
                val valueExpression =
                    "${
                        composeNode.trait.value.iconText().replaceSpaces()
                            .replaceFirstChar { it.lowercase() }
                    }Item" +
                        fieldNameAsViewModel(
                            fieldType.fieldName(project),
                        )
                CodeBlock.of(valueExpression)
            }
        }
    }

    override fun valueExpression(project: Project): String = textFromDynamicItem(project)

    override fun displayText(project: Project): String = textFromDynamicItem(project)

    private fun textFromDynamicItem(project: Project): String {
        val unknownItem = "[Unknown dynamic item]"
        val composeNode = project.findComposeNodeOrNull(composeNodeId) ?: return unknownItem
        return "[item: ${composeNode.label.value + fieldType.fieldName(project)}]"
    }

    private fun fieldNameAsViewModel(fieldName: String): String = fieldName.replace(".", "").replaceFirstChar { it.uppercase() }

    @Composable
    override fun Editor(
        project: Project,
        node: ComposeNode,
        initialProperty: AssignableProperty?,
        label: String,
        onValidPropertyChanged: (AssignableProperty, lazyListSource: LazyListChildParams?) -> Unit,
        onInitializeProperty: (() -> Unit)?,
        functionScopeProperties: List<FunctionScopeParameterProperty>,
        modifier: Modifier,
        destinationStateId: StateId?,
        validateInput: ((String) -> ValidateResult)?,
        editable: Boolean,
    ) {
        AssignableEditableTextPropertyEditor(
            project = project,
            node = node,
            initialProperty = initialProperty,
            label = label,
            onValidPropertyChanged = onValidPropertyChanged,
            modifier = modifier,
            destinationStateId = destinationStateId,
            onInitializeProperty = onInitializeProperty,
            functionScopeProperties = functionScopeProperties,
            leadingIcon =
                valueType(project).let {
                    {
                        ComposeFlowIcon(
                            it.leadingIcon(),
                            contentDescription = null,
                        )
                    }
                },
            validateInput = validateInput ?: { ValidateResult.Success },
            editable = editable,
        )
    }
}

@Serializable
@SerialName("ValueFromGlobalProperty")
data class ValueFromGlobalProperty(
    val readableState: ReadableState,
) : AssignablePropertyBase(),
    AssignableProperty {
    override fun valueType(project: Project): ComposeFlowType = readableState.valueType(project)

    override fun valueExpression(project: Project): String = textFromState(readableState)

    override fun displayText(project: Project): String = textFromState(readableState)

    private fun textFromState(readableState: ReadableState): String = "[User: ${readableState.name}]"

    override fun isDependent(sourceId: String): Boolean = readableState.id == sourceId || super<AssignableProperty>.isDependent(sourceId)

    override fun generateCodeBlock(
        project: Project,
        context: GenerationContext,
        writeType: ComposeFlowType,
        dryRun: Boolean,
    ): CodeBlock = readableState.generateReadBlock(project, context, dryRun)
}

@Serializable
sealed interface CustomDataTypeProperty : AssignableProperty {
    override fun valueType(project: Project) = ComposeFlowType.CustomDataType(dataTypeId = dataTypeId)

    val dataTypeId: DataTypeId

    @Serializable
    @SerialName("ValueFromFields")
    data class ValueFromFields(
        override val dataTypeId: DataTypeId,
        val fieldType: DataFieldType = DataFieldType.Primitive,
    ) : AssignablePropertyBase(),
        CustomDataTypeProperty {
        override fun generateCodeBlock(
            project: Project,
            context: GenerationContext,
            writeType: ComposeFlowType,
            dryRun: Boolean,
        ): CodeBlock = throw IllegalStateException("Invalid call")

        override fun valueExpression(project: Project): String = createDisplayText(project)

        override fun displayText(project: Project): String = createDisplayText(project)

        private fun createDisplayText(project: Project): String {
            val dataType = project.findDataTypeOrNull(dataTypeId)
            return when (fieldType) {
                is DataFieldType.DataType -> {
                    "[${dataType?.className ?: "invalid"}]"
                }

                is DataFieldType.FieldInDataType -> {
                    val field = dataType?.findDataFieldOrNull(fieldType.fieldId)
                    "[${dataType?.className ?: "invalid"}.${field?.variableName}]"
                }

                DataFieldType.Primitive -> {
                    "[${dataType?.className ?: "invalid"}]"
                }

                is DataFieldType.DocumentId -> {
                    "[Document ID]"
                }
            }
        }
    }

    @Composable
    override fun Editor(
        project: Project,
        node: ComposeNode,
        initialProperty: AssignableProperty?,
        label: String,
        onValidPropertyChanged: (AssignableProperty, lazyListSource: LazyListChildParams?) -> Unit,
        onInitializeProperty: (() -> Unit)?,
        functionScopeProperties: List<FunctionScopeParameterProperty>,
        modifier: Modifier,
        destinationStateId: StateId?,
        validateInput: ((String) -> ValidateResult)?,
        editable: Boolean,
    ) {
        AssignableDataTypePropertyEditor(
            project = project,
            node = node,
            label = label,
            acceptableType = ComposeFlowType.CustomDataType(dataTypeId = dataTypeId),
            onValidPropertyChanged = onValidPropertyChanged,
            modifier = modifier,
            destinationStateId = destinationStateId,
            initialProperty = initialProperty,
            onInitializeProperty = onInitializeProperty,
            editable = editable,
            functionScopeProperties = functionScopeProperties,
        )
    }
}

@Serializable
@SerialName("EnumProperty")
data class EnumProperty(
    override val value: EnumWrapper,
) : AssignablePropertyBase(),
    AssignableProperty,
    IntrinsicProperty<EnumWrapper> {
    override fun valueType(project: Project) = ComposeFlowType.Enum(enumClass = value.enumValue().declaringJavaClass)

    override fun getEnumValue() = value.enumValue()

    override fun generateCodeBlock(
        project: Project,
        context: GenerationContext,
        writeType: ComposeFlowType,
        dryRun: Boolean,
    ): CodeBlock = asCodeBlock()

    override fun asCodeBlock(): CodeBlock = value.asCodeBlock()

    override fun valueExpression(project: Project): String = value.enumValue().name

    override fun displayText(project: Project): String = value.enumValue().name

    @Composable
    override fun Editor(
        project: Project,
        node: ComposeNode,
        initialProperty: AssignableProperty?,
        label: String,
        onValidPropertyChanged: (AssignableProperty, lazyListSource: LazyListChildParams?) -> Unit,
        onInitializeProperty: (() -> Unit)?,
        functionScopeProperties: List<FunctionScopeParameterProperty>,
        modifier: Modifier,
        destinationStateId: StateId?,
        validateInput: ((String) -> ValidateResult)?,
        editable: Boolean,
    ) {
        check(initialProperty is EnumProperty)
        DropdownProperty(
            project = project,
            items = value.entries(),
            onValueChanged = { _, property ->
                onValidPropertyChanged(EnumProperty(property as EnumWrapper), null)
            },
            selectedIndex = initialProperty.value.enumValue().ordinal,
            modifier = modifier,
            editable = editable,
        )
    }
}

@Serializable
@SerialName("CustomEnumValuesProperty")
data class CustomEnumValuesProperty(
    val customEnumId: CustomEnumId,
) : AssignablePropertyBase(),
    AssignableProperty {
    override fun valueType(project: Project) = ComposeFlowType.StringType(isList = true)

    override fun generateCodeBlock(
        project: Project,
        context: GenerationContext,
        writeType: ComposeFlowType,
        dryRun: Boolean,
    ): CodeBlock {
        val customEnum = project.findCustomEnumOrNull(customEnumId) ?: return CodeBlock.of("")
        return CodeBlock.of(
            """%T.entries.map { it.name }""",
            customEnum.asKotlinPoetClassName(project),
        )
    }

    override fun valueExpression(project: Project): String = asText(project)

    override fun displayText(project: Project): String = asText(project)

    fun rawVale(project: Project): List<String> = project.findCustomEnumOrNull(customEnumId)?.values ?: emptyList()

    private fun asText(project: Project): String {
        val customEnum = project.findCustomEnumOrNull(customEnumId) ?: return ""
        return "${customEnum.enumName}.entries"
    }
}

@Serializable
@SerialName("FirestoreCollectionProperty")
data class FirestoreCollectionProperty(
    val collectionId: CollectionId,
) : AssignablePropertyBase(),
    AssignableProperty {
    override fun isDependent(sourceId: String): Boolean = collectionId == sourceId

    override fun valueType(project: Project): ComposeFlowType {
        val firestoreCollection =
            project.findFirestoreCollectionOrNull(collectionId)
                ?: return ComposeFlowType.UnknownType()
        return firestoreCollection.valueType(project).copyWith(newIsList = true)
    }

    override fun generateCodeBlock(
        project: Project,
        context: GenerationContext,
        writeType: ComposeFlowType,
        dryRun: Boolean,
    ): CodeBlock {
        context
            .getCurrentComposableContext()
            .addDependency(viewModelConstant = ViewModelConstant.firestore)
        val builder = CodeBlock.builder()
        val firestoreCollection =
            project.findFirestoreCollectionOrNull(collectionId) ?: return builder.build()
        // result is wrapped in a data class as `data class Ready<T>(val result: List<T>) : QueryResult<T>`
        return CodeBlock.of("${firestoreCollection.getReadVariableName(project)}.result")
    }

    override fun valueExpression(project: Project): String = asText(project)

    override fun displayText(project: Project): String = asText(project)

    override fun generateWrapWithComposableBlock(
        project: Project,
        insideContent: CodeBlock,
    ): CodeBlock {
        val builder = CodeBlock.builder()
        val firestoreCollection =
            project.findFirestoreCollectionOrNull(collectionId) ?: return builder.build()
        val readVariableName = firestoreCollection.getReadVariableName(project)
        builder.add(
            """when ($readVariableName) {
    %T.Idle -> {}
    %T.Loading -> {
        %M()
    }
    is %T.Error -> {
        %M(text = "Error: ${'$'}{$readVariableName.message}")
    }
    is %T.Success -> {
            """,
            ClassHolder.ComposeFlow.DataResult,
            ClassHolder.ComposeFlow.DataResult,
            MemberHolder.Material3.CircularProgressIndicator,
            ClassHolder.ComposeFlow.DataResult,
            MemberHolder.Material3.Text,
            ClassHolder.ComposeFlow.DataResult,
        )
        builder.add(insideContent)
        builder.add(
            """
    }
}
""",
        )
        return builder.build()
    }

    override fun generateWrapWithViewModelBlock(
        project: Project,
        insideContent: CodeBlock,
    ): CodeBlock {
        val builder = CodeBlock.builder()
        val firestoreCollection =
            project.findFirestoreCollectionOrNull(collectionId) ?: return builder.build()
        val readVariableName = firestoreCollection.getReadVariableName(project)
        builder.add(
            """
when ($readVariableName) {
    is %T.Success -> {
            """,
            ClassHolder.ComposeFlow.DataResult,
        )
        builder.add(insideContent)
        builder.add(
            """
    }
    else -> {}
}
""",
        )
        return builder.build()
    }

    private fun asText(project: Project): String {
        val firestoreCollection = project.findFirestoreCollectionOrNull(collectionId)
        return firestoreCollection?.let { "[Firestore: ${it.name}]" }
            ?: "[Firestore: Unknown collection]"
    }
}

@Serializable
@SerialName("ApiResultProperty")
data class ApiResultProperty(
    val apiId: ApiId?,
    @Transient
    private var apiResultName: String = "apiResult",
) : AssignablePropertyBase(),
    AssignableProperty {
    override fun isDependent(sourceId: String): Boolean = apiId == sourceId

    override fun valueType(project: Project): ComposeFlowType {
        val apiDefinition =
            apiId?.let { project.findApiDefinitionOrNull(apiId) }
                ?: return ComposeFlowType.UnknownType()
        return if (apiDefinition.exampleJsonResponse?.jsonElement?.isList() == true) {
            ComposeFlowType.JsonElementType(isList = true)
        } else {
            ComposeFlowType.JsonElementType()
        }
    }

    override fun generateCodeBlock(
        project: Project,
        context: GenerationContext,
        writeType: ComposeFlowType,
        dryRun: Boolean,
    ): CodeBlock {
        val builder = CodeBlock.builder()
        val apiDefinition =
            apiId?.let { project.findApiDefinitionOrNull(apiId) } ?: return builder.build()

        return when (apiDefinition.exampleJsonResponse?.jsonElement) {
            is JsonArray -> {
                CodeBlock.of(
                    "($apiResultName.result as %T)",
                    ClassHolder.Kotlinx.Serialization.JsonArray,
                )
            }

            is JsonObject -> {
                CodeBlock.of(
                    "($apiResultName.result as %T)",
                    ClassHolder.Kotlinx.Serialization.JsonObject,
                )
            }

            is JsonPrimitive -> {
                CodeBlock.of(
                    "($apiResultName.result as %T)",
                    ClassHolder.Kotlinx.Serialization.JsonPrimitive,
                )
            }

            JsonNull -> {
                CodeBlock.of(apiResultName)
            }

            null -> {
                CodeBlock.of(apiResultName)
            }
        }
    }

    override fun generateWrapWithComposableBlock(
        project: Project,
        insideContent: CodeBlock,
    ): CodeBlock {
        val builder = CodeBlock.builder()
        val apiDefinition =
            apiId?.let { project.findApiDefinitionOrNull(apiId) } ?: return builder.build()

        builder.add(
            """when (val $apiResultName = ${apiDefinition.apiResultName()}) {
    %T.Idle -> {}
    %T.Loading -> {
        %M()
    }
    is %T.Error -> {
        %M(text = "Error: ${'$'}{apiResult.message}")
    }
    is %T.Success -> {
            """,
            ClassHolder.ComposeFlow.DataResult,
            ClassHolder.ComposeFlow.DataResult,
            MemberHolder.Material3.CircularProgressIndicator,
            ClassHolder.ComposeFlow.DataResult,
            MemberHolder.Material3.Text,
            ClassHolder.ComposeFlow.DataResult,
        )
        builder.add(insideContent)
        builder.add(
            """
        }
    }
    """,
        )
        return builder.build()
    }

    override fun generateWrapWithViewModelBlock(
        project: Project,
        insideContent: CodeBlock,
    ): CodeBlock {
        val builder = CodeBlock.builder()
        val apiDefinition =
            apiId?.let { project.findApiDefinitionOrNull(apiId) } ?: return builder.build()

        builder.add(
            """
    when (val $apiResultName = ${apiDefinition.apiResultName()}.value) {
    is %T.Success -> {
            """,
            ClassHolder.ComposeFlow.DataResult,
        )
        builder.add(insideContent)
        builder.add(
            """
        }
    else -> {}
    }
    """,
        )
        return builder.build()
    }

    override fun valueExpression(project: Project): String = asText(project)

    override fun displayText(project: Project): String = asText(project)

    private fun asText(project: Project): String {
        val apiDefinition =
            apiId?.let { project.findApiDefinitionOrNull(apiId) } ?: return "[API: Unknown]"
        return "[API: ${apiDefinition.name}]"
    }
}

@Serializable
@SerialName("ComposableParameterProperty")
data class ComposableParameterProperty(
    val parameterId: ParameterId,
    val dataFieldType: DataFieldType = DataFieldType.Primitive,
) : AssignablePropertyBase(),
    AssignableProperty {
    override fun valueType(project: Project): ComposeFlowType =
        project.findParameterOrNull(parameterId)?.parameterType ?: ComposeFlowType.UnknownType()

    override fun getPropertyAvailableAt(): PropertyAvailableAt = PropertyAvailableAt.ComposeScreen

    override fun valueExpression(project: Project): String = textFromParameter(project, parameterId)

    override fun displayText(project: Project): String = textFromParameter(project, parameterId)

    private fun textFromParameter(
        project: Project,
        parameterId: ParameterId,
    ): String =
        project.findParameterOrNull(parameterId)?.let {
            "[param: ${getParameterFieldName(project)}]"
        } ?: "[Invalid]"

    override fun generateParameterSpec(project: Project): ParameterSpec? {
        val parameter = project.findParameterOrNull(parameterId) ?: return null
        return parameter.generateArgumentParameterSpec(project)
    }

    override fun generateCodeBlock(
        project: Project,
        context: GenerationContext,
        writeType: ComposeFlowType,
        dryRun: Boolean,
    ): CodeBlock {
        val parameter = project.findParameterOrThrow(parameterId)
        val parameterFieldName =
            if (context.generatedPlace == GeneratedPlace.ComposeScreen) {
                ComposeScreenConstant.arguments.name + "." + getParameterFieldName(project)
            } else {
                getParameterFieldName(project)
            }
        val codeBlock =
            if (parameter.parameterType is ComposeFlowType.CustomDataType) {
                val type =
                    when (dataFieldType) {
                        DataFieldType.Primitive -> {
                            parameter.parameterType
                        }

                        is DataFieldType.FieldInDataType -> {
                            val dataType = project.findDataTypeOrNull(dataFieldType.dataTypeId)
                            val dataField = dataType?.findDataFieldOrNull(dataFieldType.fieldId)
                            dataField?.fieldType?.type() ?: ComposeFlowType.UnknownType()
                        }

                        is DataFieldType.DataType -> {
                            val dataType = project.findDataTypeOrThrow(dataFieldType.dataTypeId)
                            ComposeFlowType.CustomDataType(isList = false, dataType.id)
                        }

                        is DataFieldType.DocumentId -> {
                            ComposeFlowType.DocumentIdType(dataFieldType.firestoreCollectionId)
                        }
                    }
                writeType.convertCodeFromType(
                    type,
                    CodeBlock.of(parameterFieldName),
                )
            } else {
                writeType.convertCodeFromType(
                    parameter.parameterType,
                    CodeBlock.of(parameterFieldName),
                )
            }
        return codeBlock
    }

    private fun getParameterFieldName(project: Project): String {
        val parameter = project.findParameterOrThrow(parameterId)
        return if (parameter.parameterType is ComposeFlowType.CustomDataType) {
            "${parameter.variableName}${dataFieldType.fieldName(project)}"
        } else {
            parameter.variableName
        }
    }

    @Composable
    override fun Editor(
        project: Project,
        node: ComposeNode,
        initialProperty: AssignableProperty?,
        label: String,
        onValidPropertyChanged: (AssignableProperty, lazyListSource: LazyListChildParams?) -> Unit,
        onInitializeProperty: (() -> Unit)?,
        functionScopeProperties: List<FunctionScopeParameterProperty>,
        modifier: Modifier,
        destinationStateId: StateId?,
        validateInput: ((String) -> ValidateResult)?,
        editable: Boolean,
    ) {
        AssignableEditableTextPropertyEditor(
            project = project,
            node = node,
            initialProperty = initialProperty,
            label = label,
            onValidPropertyChanged = onValidPropertyChanged,
            modifier = modifier,
            destinationStateId = destinationStateId,
            onInitializeProperty = onInitializeProperty,
            leadingIcon =
                valueType(project).let {
                    {
                        ComposeFlowIcon(
                            Icons.Outlined.ForkLeft,
                            contentDescription = null,
                        )
                    }
                },
            validateInput = validateInput ?: { ValidateResult.Success },
            editable = editable,
            functionScopeProperties = functionScopeProperties,
        )
    }
}

@Serializable
@SerialName("EmptyProperty")
data object EmptyProperty : AssignablePropertyBase() {
    override fun valueType(project: Project): ComposeFlowType = ComposeFlowType.UnknownType()

    override fun valueExpression(project: Project): String = "Not selected"

    override fun displayText(project: Project): String = "Not selected"

    override fun generateCodeBlock(
        project: Project,
        context: GenerationContext,
        writeType: ComposeFlowType,
        dryRun: Boolean,
    ): CodeBlock = CodeBlock.of("")
}

/**
 * Parameter visible in a function scope.
 * E.g. When there is a  following function, this property is created where
 *   - functionName = "filter"
 *   - variableName = "intNumber"
 *
 *   when [FromList.ToList.Filter] transformer is created for the list property.
 * ```
 * listOf(1, 2, 3).filter { intNumber ->
 * }
 * ```
 */
@Serializable
@SerialName("FunctionScopeParameterProperty")
data class FunctionScopeParameterProperty(
    val id: Uuid = Uuid.random(),
    val functionName: String,
    val variableType: ComposeFlowType,
    val variableName: String = "it",
    val dataFieldType: DataFieldType = DataFieldType.Primitive,
) : AssignablePropertyBase(),
    AssignableProperty {
    override fun valueType(project: Project): ComposeFlowType =
        when (dataFieldType) {
            is DataFieldType.DataType -> {
                ComposeFlowType.CustomDataType(dataTypeId = dataFieldType.dataTypeId)
            }

            is DataFieldType.FieldInDataType -> {
                val dataType = project.findDataTypeOrNull(dataFieldType.dataTypeId)
                val dataField = dataType?.findDataFieldOrNull(dataFieldType.fieldId)
                dataField?.fieldType?.type() ?: ComposeFlowType.UnknownType()
            }

            DataFieldType.Primitive -> {
                variableType
            }

            is DataFieldType.DocumentId -> {
                ComposeFlowType.DocumentIdType(dataFieldType.firestoreCollectionId)
            }
        }

    override fun valueExpression(project: Project): String =
        textFromParameter(
            project = project,
            variableName = variableName,
        )

    override fun displayText(project: Project): String =
        textFromParameter(
            project = project,
            variableName = variableName,
        )

    private fun textFromParameter(
        project: Project,
        variableName: String,
    ): String = variableName + dataFieldType.fieldName(project)

    override fun generateCodeBlock(
        project: Project,
        context: GenerationContext,
        writeType: ComposeFlowType,
        dryRun: Boolean,
    ): CodeBlock = CodeBlock.of(variableName + dataFieldType.fieldName(project))
}

@Serializable
@SerialName("ConditionalProperty")
data class ConditionalProperty(
    private val defaultValue: AssignableProperty,
    val ifThen: IfThenBlock = IfThenBlock(thenValue = defaultValue),
    @Serializable(FallbackMutableStateListSerializer::class)
    val elseIfBlocks: MutableList<IfThenBlock> = mutableStateListEqualsOverrideOf(),
    val elseBlock: ElseBlock = ElseBlock(value = defaultValue),
) : AssignablePropertyBase(),
    AssignableProperty {
    fun copyWith(newIfThenBlock: IfThenBlock) = this.copy(ifThen = newIfThenBlock)

    fun copyWith(newElseBlock: ElseBlock) = this.copy(elseBlock = newElseBlock)

    override fun getDependentComposeNodes(project: Project): List<ComposeNode> =
        defaultValue.getDependentComposeNodes(project) +
            ifThen.thenValue.getDependentComposeNodes(project) +
            elseIfBlocks.flatMap { it.thenValue.getDependentComposeNodes(project) } +
            elseBlock.value.getDependentComposeNodes(project)

    override fun generateWrapWithComposableBlock(
        project: Project,
        insideContent: CodeBlock,
    ): CodeBlock? {
        val a = listOf(defaultValue, ifThen.ifExpression, ifThen.thenValue)
        val b =
            elseIfBlocks.map {
                it.ifExpression
            } +
                elseIfBlocks.map {
                    it.thenValue
                }
        val c = elseBlock.value
        val result =
            (a + b + c)
                .filter {
                    it.generateWrapWithComposableBlock(project, insideContent) != null
                }.fold(initial = null as CodeBlock?) { acc, element ->
                    element.generateWrapWithComposableBlock(project, acc ?: insideContent)
                }
        return result
    }

    override fun isDependent(sourceId: String): Boolean =
        defaultValue.isDependent(sourceId) ||
            ifThen.isDependent(sourceId) ||
            elseIfBlocks.any { it.isDependent(sourceId) } ||
            elseBlock.value.isDependent(sourceId)

    fun isValid(): Boolean =
        ifThen.ifExpression != BooleanProperty.Empty &&
            elseIfBlocks.all { it.ifExpression != BooleanProperty.Empty }

    override fun valueType(project: Project): ComposeFlowType = ifThen.thenValue.valueType(project)

    override fun valueExpression(project: Project): String = "[Conditional]"

    override fun displayText(project: Project): String = "[Conditional]"

    override fun getAssignableProperties(includeSelf: Boolean): List<AssignableProperty> {
        val fromTransformers = propertyTransformers.flatMap { it.getAssignableProperties() }
        val fromConditional =
            listOf(defaultValue, ifThen.ifExpression, ifThen.thenValue) +
                elseIfBlocks.flatMap { listOf(it.ifExpression, it.thenValue) } +
                listOf(elseBlock.value)
        return fromTransformers + fromConditional +
            fromConditional.flatMap {
                it.getAssignableProperties()
            } +
            if (includeSelf) {
                listOf(
                    this,
                )
            } else {
                emptyList()
            }
    }

    override fun addReadProperty(
        project: Project,
        context: GenerationContext,
        dryRun: Boolean,
    ) {
        ifThen.ifExpression.addReadProperty(project, context, dryRun = dryRun)
        ifThen.thenValue.addReadProperty(project, context, dryRun = dryRun)
        elseIfBlocks.forEach {
            it.ifExpression.addReadProperty(project, context, dryRun = dryRun)
            it.thenValue.addReadProperty(project, context, dryRun = dryRun)
        }
        elseBlock.value.addReadProperty(project, context, dryRun = dryRun)
    }

    override fun generateCodeBlock(
        project: Project,
        context: GenerationContext,
        writeType: ComposeFlowType,
        dryRun: Boolean,
    ): CodeBlock {
        val builder = CodeBlock.builder()
        builder.add("if (")
        builder.add(
            ifThen.ifExpression.transformedCodeBlock(
                project,
                context,
                ComposeFlowType.BooleanType(),
                dryRun = dryRun,
            ),
        )
        builder.addStatement(") {")
        builder.add(ifThen.thenValue.transformedCodeBlock(project, context, dryRun = dryRun))
        elseIfBlocks.forEach {
            builder.add("} else if (")
            builder.add(
                it.ifExpression.transformedCodeBlock(
                    project,
                    context,
                    ComposeFlowType.BooleanType(),
                    dryRun = dryRun,
                ),
            )
            builder.addStatement(") {")
            builder.add(
                it.thenValue.transformedCodeBlock(
                    project,
                    context,
                    writeType,
                    dryRun = dryRun,
                ),
            )
        }
        builder.addStatement("} else {")
        builder.add(
            elseBlock.value.transformedCodeBlock(
                project,
                context,
                writeType,
                dryRun = dryRun,
            ),
        )
        builder.addStatement("}")
        return builder.build()
    }

    @Serializable
    @SerialName("IfThenBlock")
    data class IfThenBlock(
        val ifExpression: AssignableProperty = BooleanProperty.Empty,
        val metIfTrue: Boolean = true,
        val thenValue: AssignableProperty,
    ) {
        fun isDependent(sourceId: String): Boolean =
            ifExpression.isDependent(sourceId) ||
                thenValue.isDependent(sourceId)
    }

    @Serializable
    @SerialName("ElseBlock")
    data class ElseBlock(
        val value: AssignableProperty,
    )

    @Composable
    override fun Editor(
        project: Project,
        node: ComposeNode,
        initialProperty: AssignableProperty?,
        label: String,
        onValidPropertyChanged: (AssignableProperty, lazyListSource: LazyListChildParams?) -> Unit,
        onInitializeProperty: (() -> Unit)?,
        functionScopeProperties: List<FunctionScopeParameterProperty>,
        modifier: Modifier,
        destinationStateId: StateId?,
        validateInput: ((String) -> ValidateResult)?,
        editable: Boolean,
    ) {
        AssignableEditableTextPropertyEditor(
            project = project,
            node = node,
            initialProperty = initialProperty,
            label = label,
            onValidPropertyChanged = onValidPropertyChanged,
            modifier = modifier,
            destinationStateId = destinationStateId,
            onInitializeProperty = onInitializeProperty,
            leadingIcon =
                valueType(project).let {
                    {
                        ComposeFlowIcon(
                            it.leadingIcon(),
                            contentDescription = null,
                        )
                    }
                },
            validateInput = validateInput ?: { ValidateResult.Success },
            editable = editable,
            functionScopeProperties = functionScopeProperties,
        )
    }
}

@Composable
fun ComposeFlowType.leadingIcon(): ImageVector =
    when (this) {
        is ComposeFlowType.BooleanType -> Icons.Outlined.ToggleOff
        is ComposeFlowType.Color -> Icons.Outlined.ColorLens
        is ComposeFlowType.Brush -> Icons.Filled.Brush
        is ComposeFlowType.CustomDataType -> ComposeFlowIcons.Dbms
        is ComposeFlowType.Enum<*> -> Icons.Outlined.Menu
        is ComposeFlowType.IntType -> Icons.Outlined.Numbers
        is ComposeFlowType.FloatType -> Icons.Outlined.Numbers
        is ComposeFlowType.StringType -> Icons.Outlined.TextFields
        is ComposeFlowType.InstantType -> Icons.Outlined.DateRange
        is ComposeFlowType.JsonElementType -> Icons.Outlined.DataObject
        is ComposeFlowType.AnyType -> Icons.Outlined.QuestionMark
        is ComposeFlowType.DocumentIdType -> ComposeFlowIcons.Firebase
        is ComposeFlowType.UnknownType -> Icons.Outlined.QuestionMark
    }

fun AssignableProperty?.asBooleanValue(): Boolean =
    when (this) {
        is BooleanProperty.BooleanIntrinsicValue -> this.value
        is ValueFromState -> true
        BooleanProperty.Empty -> false
        else -> true
    }

@Composable
fun AssignableProperty.getErrorMessage(
    project: Project,
    acceptableType: ComposeFlowType,
): String? {
    val transformedValueType = transformedValueType(project)
    val errorText =
        if (transformedValueType is ComposeFlowType.UnknownType) {
            stringResource(Res.string.invalid_reference)
        } else if (!acceptableType.isAbleToAssign(transformedValueType)) {
            stringResource(Res.string.invalid_type)
        } else {
            null
        }
    return errorText
}
