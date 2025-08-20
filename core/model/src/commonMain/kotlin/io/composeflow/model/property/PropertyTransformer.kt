package io.composeflow.model.property

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.referentialEqualityPolicy
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.composeflow.kotlinpoet.wrapper.CodeBlockWrapper
import io.composeflow.kotlinpoet.wrapper.toWrapper
import io.composeflow.Res
import io.composeflow.add_string_after
import io.composeflow.add_string_before
import io.composeflow.contains
import io.composeflow.datetime.DateTimeFormatter
import io.composeflow.datetime.PaddingHolder
import io.composeflow.datetime.PaddingWrapper
import io.composeflow.datetime.Separator
import io.composeflow.datetime.SeparatorHolder
import io.composeflow.editor.validator.FloatValidator
import io.composeflow.editor.validator.IntValidator
import io.composeflow.editor.validator.NotEmptyNotLessThanZeroIntValidator
import io.composeflow.ends_with
import io.composeflow.equals
import io.composeflow.filter
import io.composeflow.format_as
import io.composeflow.greater_than
import io.composeflow.greater_than_or_equal_to
import io.composeflow.is_empty
import io.composeflow.kotlinpoet.GenerationContext
import io.composeflow.kotlinpoet.MemberHolder
import io.composeflow.length
import io.composeflow.less_than
import io.composeflow.less_than_or_equal_to
import io.composeflow.map
import io.composeflow.mod_equals_to
import io.composeflow.model.datatype.DataFieldId
import io.composeflow.model.project.Project
import io.composeflow.model.project.appscreen.screen.composenode.ComposeNode
import io.composeflow.model.project.findDataTypeOrNull
import io.composeflow.model.type.ComposeFlowType
import io.composeflow.model.type.convertCodeFromType
import io.composeflow.multiplied_by
import io.composeflow.plus
import io.composeflow.plus_days
import io.composeflow.plus_months
import io.composeflow.plus_years
import io.composeflow.serializer.MutableStateSerializer
import io.composeflow.size
import io.composeflow.sorted
import io.composeflow.sorted_by
import io.composeflow.split
import io.composeflow.starts_with
import io.composeflow.substring_after
import io.composeflow.substring_before
import io.composeflow.to_string
import io.composeflow.toggle_value
import io.composeflow.ui.propertyeditor.AssignableBooleanPropertyEditor
import io.composeflow.ui.propertyeditor.AssignableEditableTextPropertyEditor
import io.composeflow.ui.propertyeditor.BasicDropdownPropertyEditor
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.jetbrains.compose.resources.stringResource
import kotlin.uuid.Uuid

fun List<PropertyTransformer>.areValidRelationships(): Boolean {
    val allPropertiesValid = all { it.isValid() }
    val validRelationships =
        zipWithNext { prev, next -> prev.toType() == next.fromType() }
            .all { it }
    return allPropertiesValid && validRelationships
}

@Serializable
sealed interface PropertyTransformer {
    fun fromType(): ComposeFlowType

    fun toType(): ComposeFlowType

    fun transformedValueExpression(
        project: Project,
        input: String,
    ): String

    fun transformedCodeBlock(
        project: Project,
        input: CodeBlockWrapper,
        context: GenerationContext,
        dryRun: Boolean,
    ): CodeBlockWrapper

    fun isValid(): Boolean = true

    fun isDependent(sourceId: String): Boolean

    @Composable
    fun displayName(): String

    @Composable
    fun Editor(
        project: Project,
        node: ComposeNode,
        onTransformerEdited: (transformer: PropertyTransformer) -> Unit,
        modifier: Modifier,
    ) {
    }

    /**
     * Get the assignable properties used in the transformer.
     */
    fun getAssignableProperties(): List<AssignableProperty> = emptyList()

    companion object {
        fun transformers(
            fromType: ComposeFlowType,
            toType: ComposeFlowType? = null,
        ): List<PropertyTransformer> {
            val filterFunc: (PropertyTransformer) -> Boolean = {
                if (toType != null) {
                    it.toType() == toType
                } else {
                    true
                }
            }
            if (fromType.isList) {
                return FromList.transformers(fromType).filter {
                    filterFunc(it)
                }
            } else {
                return when (fromType) {
                    is ComposeFlowType.BooleanType -> {
                        FromBoolean.transformers().filter {
                            filterFunc(it)
                        }
                    }

                    is ComposeFlowType.Color -> emptyList()
                    is ComposeFlowType.Brush -> emptyList()
                    is ComposeFlowType.CustomDataType -> emptyList()
                    is ComposeFlowType.Enum<*> -> emptyList()
                    is ComposeFlowType.IntType -> {
                        FromInt.transformers().filter {
                            filterFunc(it)
                        }
                    }

                    is ComposeFlowType.FloatType -> {
                        FromFloat.transformers().filter {
                            filterFunc(it)
                        }
                    }

                    is ComposeFlowType.StringType -> {
                        FromString.transformers().filter {
                            filterFunc(it)
                        }
                    }

                    is ComposeFlowType.InstantType -> {
                        FromInstant.transformers().filter {
                            filterFunc(it)
                        }
                    }

                    is ComposeFlowType.JsonElementType -> {
                        emptyList()
                    }

                    is ComposeFlowType.AnyType -> emptyList()
                    is ComposeFlowType.DocumentIdType -> emptyList()
                    is ComposeFlowType.UnknownType -> emptyList()
                }
            }
        }
    }
}

@Serializable
sealed interface FromString : PropertyTransformer {
    override fun fromType(): ComposeFlowType = ComposeFlowType.StringType()

    @Serializable
    sealed interface ToString :
        FromString,
        PropertyTransformer {
        @Serializable
        @SerialName("AddBefore")
        data class AddBefore(
            @Serializable(MutableStateSerializer::class)
            val value: MutableState<AssignableProperty> =
                mutableStateOf(StringProperty.StringIntrinsicValue("")),
        ) : ToString {
            override fun getAssignableProperties() = listOf(value.value) + value.value.getAssignableProperties()

            override fun transformedValueExpression(
                project: Project,
                input: String,
            ): String = "${value.value.transformedValueExpression(project)} + $input"

            override fun transformedCodeBlock(
                project: Project,
                input: CodeBlockWrapper,
                context: GenerationContext,
                dryRun: Boolean,
            ): CodeBlockWrapper {
                val builder = CodeBlockWrapper.builder()
                builder.add(value.value.transformedCodeBlock(project, context, dryRun = dryRun))
                builder.add(".plus(")
                builder.add(input)
                builder.add(")")
                value.value.addReadProperty(project, context, dryRun = dryRun)
                return builder.build()
            }

            override fun isDependent(sourceId: String): Boolean = value.value.isDependent(sourceId)

            @Composable
            override fun displayName(): String = stringResource(Res.string.add_string_before)

            @Composable
            override fun Editor(
                project: Project,
                node: ComposeNode,
                onTransformerEdited: (transformer: PropertyTransformer) -> Unit,
                modifier: Modifier,
            ) {
                AssignableEditableTextPropertyEditor(
                    project = project,
                    node = node,
                    acceptableType = ComposeFlowType.StringType(),
                    onValidPropertyChanged = { property, _ ->
                        value.value = property
                        onTransformerEdited(this)
                    },
                    onInitializeProperty = {
                        value.value = ComposeFlowType.StringType().defaultValue()
                        onTransformerEdited(this)
                    },
                    initialProperty = value.value,
                )
            }
        }

        @Serializable
        @SerialName("AddAfter")
        data class AddAfter(
            @Serializable(MutableStateSerializer::class)
            val value: MutableState<AssignableProperty> =
                mutableStateOf(StringProperty.StringIntrinsicValue("")),
        ) : ToString {
            override fun getAssignableProperties() = listOf(value.value) + value.value.getAssignableProperties()

            override fun transformedValueExpression(
                project: Project,
                input: String,
            ): String = "$input + ${value.value.transformedValueExpression(project)}"

            override fun transformedCodeBlock(
                project: Project,
                input: CodeBlockWrapper,
                context: GenerationContext,
                dryRun: Boolean,
            ): CodeBlockWrapper {
                val builder = CodeBlockWrapper.builder()
                builder.add(input)
                builder.add(".plus(")
                builder.add(value.value.transformedCodeBlock(project, context, dryRun = dryRun))
                builder.add(")")
                value.value.addReadProperty(project, context, dryRun = dryRun)
                return builder.build()
            }

            override fun isDependent(sourceId: String): Boolean = value.value.isDependent(sourceId)

            @Composable
            override fun displayName(): String = stringResource(Res.string.add_string_after)

            @Composable
            override fun Editor(
                project: Project,
                node: ComposeNode,
                onTransformerEdited: (transformer: PropertyTransformer) -> Unit,
                modifier: Modifier,
            ) {
                AssignableEditableTextPropertyEditor(
                    project = project,
                    node = node,
                    acceptableType = ComposeFlowType.StringType(),
                    onValidPropertyChanged = { property, _ ->
                        value.value = property
                        onTransformerEdited(this)
                    },
                    onInitializeProperty = {
                        value.value = ComposeFlowType.StringType().defaultValue()
                        onTransformerEdited(this)
                    },
                    initialProperty = value.value,
                )
            }
        }

        @Serializable
        @SerialName("SubstringBefore")
        data class SubstringBefore(
            @Serializable(MutableStateSerializer::class)
            val value: MutableState<AssignableProperty> =
                mutableStateOf(StringProperty.StringIntrinsicValue("")),
        ) : ToString {
            override fun getAssignableProperties() = listOf(value.value) + value.value.getAssignableProperties()

            override fun transformedValueExpression(
                project: Project,
                input: String,
            ): String = "($input).substringBefore(${value.value.transformedValueExpression(project)})"

            override fun transformedCodeBlock(
                project: Project,
                input: CodeBlockWrapper,
                context: GenerationContext,
                dryRun: Boolean,
            ): CodeBlockWrapper {
                val builder = CodeBlockWrapper.builder()
                builder.add(input)
                builder.add(".substringBefore(")
                builder.add(value.value.transformedCodeBlock(project, context, dryRun = dryRun))
                builder.add(")")
                value.value.addReadProperty(project, context, dryRun = dryRun)
                return builder.build()
            }

            override fun isDependent(sourceId: String): Boolean = value.value.isDependent(sourceId)

            @Composable
            override fun displayName(): String = stringResource(Res.string.substring_before)

            @Composable
            override fun Editor(
                project: Project,
                node: ComposeNode,
                onTransformerEdited: (transformer: PropertyTransformer) -> Unit,
                modifier: Modifier,
            ) {
                AssignableEditableTextPropertyEditor(
                    project = project,
                    node = node,
                    acceptableType = ComposeFlowType.StringType(),
                    label = "before",
                    onValidPropertyChanged = { property, _ ->
                        value.value = property
                        onTransformerEdited(this)
                    },
                    onInitializeProperty = {
                        value.value = ComposeFlowType.StringType().defaultValue()
                        onTransformerEdited(this)
                    },
                    initialProperty = value.value,
                )
            }
        }

        @Serializable
        @SerialName("SubstringAfter")
        data class SubstringAfter(
            @Serializable(MutableStateSerializer::class)
            val value: MutableState<AssignableProperty> =
                mutableStateOf(StringProperty.StringIntrinsicValue("")),
        ) : ToString {
            override fun getAssignableProperties() = listOf(value.value) + value.value.getAssignableProperties()

            override fun transformedValueExpression(
                project: Project,
                input: String,
            ): String = "($input).substringAfter(${value.value.transformedValueExpression(project)})"

            override fun transformedCodeBlock(
                project: Project,
                input: CodeBlockWrapper,
                context: GenerationContext,
                dryRun: Boolean,
            ): CodeBlockWrapper {
                val builder = CodeBlockWrapper.builder()
                builder.add(input)
                builder.add(".substringAfter(")
                builder.add(value.value.transformedCodeBlock(project, context, dryRun = dryRun))
                builder.add(")")
                value.value.addReadProperty(project, context, dryRun = dryRun)
                return builder.build()
            }

            override fun isDependent(sourceId: String): Boolean = value.value.isDependent(sourceId)

            @Composable
            override fun displayName(): String = stringResource(Res.string.substring_after)

            @Composable
            override fun Editor(
                project: Project,
                node: ComposeNode,
                onTransformerEdited: (transformer: PropertyTransformer) -> Unit,
                modifier: Modifier,
            ) {
                AssignableEditableTextPropertyEditor(
                    project = project,
                    node = node,
                    acceptableType = ComposeFlowType.StringType(),
                    label = "after",
                    onValidPropertyChanged = { property, _ ->
                        value.value = property
                        onTransformerEdited(this)
                    },
                    onInitializeProperty = {
                        value.value = ComposeFlowType.StringType().defaultValue()
                        onTransformerEdited(this)
                    },
                    initialProperty = value.value,
                )
            }
        }

        override fun toType(): ComposeFlowType = ComposeFlowType.StringType()
    }

    @Serializable
    sealed interface ToBoolean :
        FromString,
        PropertyTransformer {
        @Serializable
        @SerialName("StringContains")
        data class StringContains(
            @Serializable(MutableStateSerializer::class)
            val value: MutableState<AssignableProperty> =
                mutableStateOf(StringProperty.StringIntrinsicValue("")),
        ) : ToBoolean {
            override fun getAssignableProperties() = listOf(value.value) + value.value.getAssignableProperties()

            override fun transformedValueExpression(
                project: Project,
                input: String,
            ): String = "($input).contains(${value.value.transformedValueExpression(project)})"

            override fun transformedCodeBlock(
                project: Project,
                input: CodeBlockWrapper,
                context: GenerationContext,
                dryRun: Boolean,
            ): CodeBlockWrapper {
                val builder = CodeBlockWrapper.builder()
                builder.add(input)
                builder.add(".contains(")
                builder.add(value.value.transformedCodeBlock(project, context, dryRun = dryRun))
                builder.add(")")
                value.value.addReadProperty(project, context, dryRun = dryRun)
                return builder.build()
            }

            override fun isDependent(sourceId: String): Boolean = value.value.isDependent(sourceId)

            @Composable
            override fun displayName(): String = stringResource(Res.string.contains)

            @Composable
            override fun Editor(
                project: Project,
                node: ComposeNode,
                onTransformerEdited: (transformer: PropertyTransformer) -> Unit,
                modifier: Modifier,
            ) {
                AssignableEditableTextPropertyEditor(
                    project = project,
                    node = node,
                    acceptableType = ComposeFlowType.StringType(),
                    onValidPropertyChanged = { property, _ ->
                        value.value = property
                        onTransformerEdited(this)
                    },
                    onInitializeProperty = {
                        value.value = ComposeFlowType.StringType().defaultValue()
                        onTransformerEdited(this)
                    },
                    initialProperty = value.value,
                )
            }
        }

        @Serializable
        @SerialName("StartsWith")
        data class StartsWith(
            @Serializable(MutableStateSerializer::class)
            val value: MutableState<AssignableProperty> =
                mutableStateOf(StringProperty.StringIntrinsicValue("")),
        ) : ToBoolean {
            override fun getAssignableProperties() = listOf(value.value) + value.value.getAssignableProperties()

            override fun transformedValueExpression(
                project: Project,
                input: String,
            ): String = "($input).startsWith(${value.value.transformedValueExpression(project)})"

            override fun transformedCodeBlock(
                project: Project,
                input: CodeBlockWrapper,
                context: GenerationContext,
                dryRun: Boolean,
            ): CodeBlockWrapper {
                val builder = CodeBlockWrapper.builder()
                builder.add(input)
                builder.add(".startsWith(")
                builder.add(value.value.transformedCodeBlock(project, context, dryRun = dryRun))
                builder.add(")")
                value.value.addReadProperty(project, context, dryRun = dryRun)
                return builder.build()
            }

            override fun isDependent(sourceId: String): Boolean = value.value.isDependent(sourceId)

            @Composable
            override fun displayName(): String = stringResource(Res.string.starts_with)

            @Composable
            override fun Editor(
                project: Project,
                node: ComposeNode,
                onTransformerEdited: (transformer: PropertyTransformer) -> Unit,
                modifier: Modifier,
            ) {
                AssignableEditableTextPropertyEditor(
                    project = project,
                    node = node,
                    acceptableType = ComposeFlowType.StringType(),
                    onValidPropertyChanged = { property, _ ->
                        value.value = property
                        onTransformerEdited(this)
                    },
                    onInitializeProperty = {
                        value.value = ComposeFlowType.StringType().defaultValue()
                        onTransformerEdited(this)
                    },
                    initialProperty = value.value,
                )
            }
        }

        @Serializable
        @SerialName("EndsWith")
        data class EndsWith(
            @Serializable(MutableStateSerializer::class)
            val value: MutableState<AssignableProperty> =
                mutableStateOf(StringProperty.StringIntrinsicValue("")),
        ) : ToBoolean {
            override fun getAssignableProperties() = listOf(value.value) + value.value.getAssignableProperties()

            override fun transformedValueExpression(
                project: Project,
                input: String,
            ): String = "($input).endsWith(${value.value.transformedValueExpression(project)})"

            override fun transformedCodeBlock(
                project: Project,
                input: CodeBlockWrapper,
                context: GenerationContext,
                dryRun: Boolean,
            ): CodeBlockWrapper {
                val builder = CodeBlockWrapper.builder()
                builder.add(input)
                builder.add(".endsWith(")
                builder.add(value.value.transformedCodeBlock(project, context, dryRun = dryRun))
                builder.add(")")
                value.value.addReadProperty(project, context, dryRun = dryRun)
                return builder.build()
            }

            override fun isDependent(sourceId: String): Boolean = value.value.isDependent(sourceId)

            @Composable
            override fun displayName(): String = stringResource(Res.string.ends_with)

            @Composable
            override fun Editor(
                project: Project,
                node: ComposeNode,
                onTransformerEdited: (transformer: PropertyTransformer) -> Unit,
                modifier: Modifier,
            ) {
                AssignableEditableTextPropertyEditor(
                    project = project,
                    node = node,
                    acceptableType = ComposeFlowType.StringType(),
                    onValidPropertyChanged = { property, _ ->
                        value.value = property
                        onTransformerEdited(this)
                    },
                    onInitializeProperty = {
                        value.value = ComposeFlowType.StringType().defaultValue()
                        onTransformerEdited(this)
                    },
                    initialProperty = value.value,
                )
            }
        }

        @Serializable
        @SerialName("isEmpty")
        data object IsEmpty : ToBoolean {
            override fun transformedValueExpression(
                project: Project,
                input: String,
            ): String = "($input).isEmpty()"

            override fun transformedCodeBlock(
                project: Project,
                input: CodeBlockWrapper,
                context: GenerationContext,
                dryRun: Boolean,
            ): CodeBlockWrapper {
                val builder = CodeBlockWrapper.builder()
                builder.add(input)
                builder.add(".isEmpty()")
                return builder.build()
            }

            override fun isDependent(sourceId: String): Boolean = false

            @Composable
            override fun displayName(): String = stringResource(Res.string.is_empty)

            @Composable
            override fun Editor(
                project: Project,
                node: ComposeNode,
                onTransformerEdited: (transformer: PropertyTransformer) -> Unit,
                modifier: Modifier,
            ) {
                AssignableEditableTextPropertyEditor(
                    project = project,
                    node = node,
                    acceptableType = ComposeFlowType.StringType(),
                    onValidPropertyChanged = { property, _ ->
                        onTransformerEdited(this)
                    },
                    onInitializeProperty = {
                        onTransformerEdited(this)
                    },
                )
            }
        }

        override fun toType(): ComposeFlowType = ComposeFlowType.BooleanType()
    }

    @Serializable
    sealed interface ToInt :
        FromString,
        PropertyTransformer {
        override fun toType(): ComposeFlowType = ComposeFlowType.IntType()

        @Serializable
        @SerialName("length")
        data object Length : ToInt {
            override fun transformedValueExpression(
                project: Project,
                input: String,
            ): String = "($input).length"

            override fun transformedCodeBlock(
                project: Project,
                input: CodeBlockWrapper,
                context: GenerationContext,
                dryRun: Boolean,
            ): CodeBlockWrapper {
                val builder = CodeBlockWrapper.builder()
                builder.add(input)
                builder.add(".length")
                return builder.build()
            }

            override fun isDependent(sourceId: String): Boolean = false

            @Composable
            override fun displayName(): String = stringResource(Res.string.length)

            @Composable
            override fun Editor(
                project: Project,
                node: ComposeNode,
                onTransformerEdited: (transformer: PropertyTransformer) -> Unit,
                modifier: Modifier,
            ) {
            }
        }
    }

    @Serializable
    sealed interface ToStringList :
        FromString,
        PropertyTransformer {
        override fun toType(): ComposeFlowType = ComposeFlowType.StringType(isList = true)

        @Serializable
        @SerialName("split")
        data class Split(
            @Serializable(MutableStateSerializer::class)
            val value: MutableState<AssignableProperty> =
                mutableStateOf(StringProperty.StringIntrinsicValue(",")),
        ) : ToStringList {
            override fun getAssignableProperties() = listOf(value.value) + value.value.getAssignableProperties()

            override fun transformedValueExpression(
                project: Project,
                input: String,
            ): String = "($input).split(${value.value.transformedValueExpression(project)})"

            override fun transformedCodeBlock(
                project: Project,
                input: CodeBlockWrapper,
                context: GenerationContext,
                dryRun: Boolean,
            ): CodeBlockWrapper {
                val builder = CodeBlockWrapper.builder()
                builder.add("(")
                builder.add(input)
                builder.add(")")
                builder.add(".split(")
                builder.add(value.value.transformedCodeBlock(project, context, dryRun = dryRun))
                builder.add(")")
                return builder.build()
            }

            override fun isDependent(sourceId: String): Boolean = value.value.isDependent(sourceId)

            @Composable
            override fun displayName(): String = stringResource(Res.string.split)

            @Composable
            override fun Editor(
                project: Project,
                node: ComposeNode,
                onTransformerEdited: (transformer: PropertyTransformer) -> Unit,
                modifier: Modifier,
            ) {
                AssignableEditableTextPropertyEditor(
                    project = project,
                    node = node,
                    acceptableType = ComposeFlowType.StringType(),
                    onValidPropertyChanged = { property, _ ->
                        value.value = property
                        onTransformerEdited(this)
                    },
                    onInitializeProperty = {
                        value.value = ComposeFlowType.StringType().defaultValue()
                        onTransformerEdited(this)
                    },
                    initialProperty = value.value,
                )
            }
        }
    }

    companion object {
        fun transformers(): List<PropertyTransformer> =
            listOf(
                ToString.AddBefore(),
                ToString.AddAfter(),
                ToString.SubstringBefore(),
                ToString.SubstringAfter(),
                ToBoolean.StringContains(),
                ToBoolean.StartsWith(),
                ToBoolean.EndsWith(),
                ToBoolean.IsEmpty,
                ToInt.Length,
                ToStringList.Split(),
            )
    }
}

@Serializable
sealed interface FromBoolean : PropertyTransformer {
    override fun fromType(): ComposeFlowType = ComposeFlowType.BooleanType()

    @Serializable
    sealed interface ToStringType :
        FromBoolean,
        PropertyTransformer {
        @Serializable
        @SerialName("FromBooleanToString")
        data object ToString : ToStringType {
            override fun transformedValueExpression(
                project: Project,
                input: String,
            ): String = "$input.toString()"

            override fun transformedCodeBlock(
                project: Project,
                input: CodeBlockWrapper,
                context: GenerationContext,
                dryRun: Boolean,
            ): CodeBlockWrapper {
                val builder = CodeBlockWrapper.builder()
                builder.add(input)
                builder.add(".toString()")
                return builder.build()
            }

            override fun isDependent(sourceId: String): Boolean = false

            @Composable
            override fun displayName(): String = stringResource(Res.string.to_string)
        }

        override fun toType(): ComposeFlowType = ComposeFlowType.StringType()
    }

    @Serializable
    sealed interface ToBoolean :
        FromBoolean,
        PropertyTransformer {
        @Serializable
        @SerialName("ToggleValue")
        data object ToggleValue : ToBoolean {
            override fun transformedValueExpression(
                project: Project,
                input: String,
            ): String = "(!$input)"

            override fun transformedCodeBlock(
                project: Project,
                input: CodeBlockWrapper,
                context: GenerationContext,
                dryRun: Boolean,
            ): CodeBlockWrapper {
                val builder = CodeBlockWrapper.builder()
                builder.add("(!")
                builder.add(input)
                builder.add(")")
                return builder.build()
            }

            override fun isDependent(sourceId: String): Boolean = false

            @Composable
            override fun displayName(): String = stringResource(Res.string.toggle_value)
        }

        override fun toType(): ComposeFlowType = ComposeFlowType.BooleanType()
    }

    companion object {
        fun transformers(): List<PropertyTransformer> =
            listOf(
                ToBoolean.ToggleValue,
                ToStringType.ToString,
            )
    }
}

@Serializable
sealed interface FromInt : PropertyTransformer {
    override fun fromType(): ComposeFlowType = ComposeFlowType.IntType()

    @Serializable
    sealed interface ToInt :
        FromInt,
        PropertyTransformer {
        @Serializable
        @SerialName("IntPlus")
        data class IntPlus(
            @Serializable(MutableStateSerializer::class)
            val value: MutableState<AssignableProperty> =
                mutableStateOf(IntProperty.IntIntrinsicValue(0)),
        ) : ToInt {
            override fun getAssignableProperties() = listOf(value.value) + value.value.getAssignableProperties()

            override fun transformedValueExpression(
                project: Project,
                input: String,
            ): String = "$input + ${value.value.transformedValueExpression(project)}"

            override fun transformedCodeBlock(
                project: Project,
                input: CodeBlockWrapper,
                context: GenerationContext,
                dryRun: Boolean,
            ): CodeBlockWrapper {
                val builder = CodeBlockWrapper.builder()
                builder.add(input)
                builder.add(" + ")
                builder.add(value.value.transformedCodeBlock(project, context, dryRun = dryRun))
                value.value.addReadProperty(project, context, dryRun = dryRun)
                return builder.build()
            }

            override fun isDependent(sourceId: String): Boolean = value.value.isDependent(sourceId)

            @Composable
            override fun displayName(): String = stringResource(Res.string.plus)

            @Composable
            override fun Editor(
                project: Project,
                node: ComposeNode,
                onTransformerEdited: (transformer: PropertyTransformer) -> Unit,
                modifier: Modifier,
            ) {
                AssignableEditableTextPropertyEditor(
                    project = project,
                    node = node,
                    acceptableType = ComposeFlowType.IntType(),
                    onValidPropertyChanged = { property, _ ->
                        value.value = property
                        onTransformerEdited(this)
                    },
                    onInitializeProperty = {
                        value.value = ComposeFlowType.IntType().defaultValue()
                        onTransformerEdited(this)
                    },
                    validateInput = IntValidator()::validate,
                    initialProperty = value.value,
                )
            }
        }

        @Serializable
        @SerialName("IntMultipliedBy")
        data class IntMultipliedBy(
            @Serializable(MutableStateSerializer::class)
            val value: MutableState<AssignableProperty> =
                mutableStateOf(IntProperty.IntIntrinsicValue(0)),
        ) : ToInt {
            override fun getAssignableProperties() = listOf(value.value) + value.value.getAssignableProperties()

            override fun transformedValueExpression(
                project: Project,
                input: String,
            ): String = "$input * ${value.value.transformedValueExpression(project)}"

            override fun transformedCodeBlock(
                project: Project,
                input: CodeBlockWrapper,
                context: GenerationContext,
                dryRun: Boolean,
            ): CodeBlockWrapper {
                val builder = CodeBlockWrapper.builder()
                builder.add(input)
                builder.add(" * ")
                builder.add(value.value.transformedCodeBlock(project, context, dryRun = dryRun))
                value.value.addReadProperty(project, context, dryRun = dryRun)
                return builder.build()
            }

            override fun isDependent(sourceId: String): Boolean = value.value.isDependent(sourceId)

            @Composable
            override fun displayName(): String = stringResource(Res.string.multiplied_by)

            @Composable
            override fun Editor(
                project: Project,
                node: ComposeNode,
                onTransformerEdited: (transformer: PropertyTransformer) -> Unit,
                modifier: Modifier,
            ) {
                AssignableEditableTextPropertyEditor(
                    project = project,
                    node = node,
                    acceptableType = ComposeFlowType.IntType(),
                    onValidPropertyChanged = { property, _ ->
                        value.value = property
                        onTransformerEdited(this)
                    },
                    onInitializeProperty = {
                        value.value = ComposeFlowType.IntType().defaultValue()
                        onTransformerEdited(this)
                    },
                    validateInput = IntValidator()::validate,
                    initialProperty = value.value,
                )
            }
        }

        override fun toType(): ComposeFlowType = ComposeFlowType.IntType()
    }

    @Serializable
    sealed interface ToString :
        FromInt,
        PropertyTransformer {
        override fun toType(): ComposeFlowType = ComposeFlowType.StringType()

        @Serializable
        @SerialName("FromIntToString")
        data object ToStringValue : ToString {
            override fun transformedValueExpression(
                project: Project,
                input: String,
            ): String = "$input.toString()"

            override fun transformedCodeBlock(
                project: Project,
                input: CodeBlockWrapper,
                context: GenerationContext,
                dryRun: Boolean,
            ): CodeBlockWrapper {
                val builder = CodeBlockWrapper.builder()
                builder.add(input)
                builder.add(".toString()")
                return builder.build()
            }

            override fun isDependent(sourceId: String): Boolean = false

            @Composable
            override fun displayName(): String = stringResource(Res.string.to_string)
        }
    }

    @Serializable
    sealed interface ToBoolean :
        FromInt,
        PropertyTransformer {
        @Serializable
        @SerialName("IntLessThan")
        data class IntLessThan(
            @Serializable(MutableStateSerializer::class)
            val value: MutableState<AssignableProperty> =
                mutableStateOf(IntProperty.IntIntrinsicValue(0)),
        ) : ToBoolean {
            override fun getAssignableProperties() = listOf(value.value) + value.value.getAssignableProperties()

            override fun transformedValueExpression(
                project: Project,
                input: String,
            ): String = "$input < ${value.value.transformedValueExpression(project)}"

            override fun transformedCodeBlock(
                project: Project,
                input: CodeBlockWrapper,
                context: GenerationContext,
                dryRun: Boolean,
            ): CodeBlockWrapper {
                val builder = CodeBlockWrapper.builder()
                builder.add(input)
                builder.add(" < ")
                builder.add(value.value.transformedCodeBlock(project, context, dryRun = dryRun))
                value.value.addReadProperty(project, context, dryRun = dryRun)
                return builder.build()
            }

            override fun isDependent(sourceId: String): Boolean = value.value.isDependent(sourceId)

            @Composable
            override fun displayName(): String = stringResource(Res.string.less_than)

            @Composable
            override fun Editor(
                project: Project,
                node: ComposeNode,
                onTransformerEdited: (transformer: PropertyTransformer) -> Unit,
                modifier: Modifier,
            ) {
                AssignableEditableTextPropertyEditor(
                    project = project,
                    node = node,
                    acceptableType = ComposeFlowType.IntType(),
                    onValidPropertyChanged = { property, _ ->
                        value.value = property
                        onTransformerEdited(this)
                    },
                    onInitializeProperty = {
                        value.value = ComposeFlowType.IntType().defaultValue()
                        onTransformerEdited(this)
                    },
                    validateInput = IntValidator()::validate,
                    initialProperty = value.value,
                )
            }
        }

        @Serializable
        @SerialName("IntLessThanOrEqualTo")
        data class IntLessThanOrEqualTo(
            @Serializable(MutableStateSerializer::class)
            val value: MutableState<AssignableProperty> =
                mutableStateOf(IntProperty.IntIntrinsicValue(0)),
        ) : ToBoolean {
            override fun getAssignableProperties() = listOf(value.value) + value.value.getAssignableProperties()

            override fun transformedValueExpression(
                project: Project,
                input: String,
            ): String = "$input <= ${value.value.transformedValueExpression(project)}"

            override fun transformedCodeBlock(
                project: Project,
                input: CodeBlockWrapper,
                context: GenerationContext,
                dryRun: Boolean,
            ): CodeBlockWrapper {
                val builder = CodeBlockWrapper.builder()
                builder.add(input)
                builder.add(" <= ")
                builder.add(value.value.transformedCodeBlock(project, context, dryRun = dryRun))
                value.value.addReadProperty(project, context, dryRun = dryRun)
                return builder.build()
            }

            override fun isDependent(sourceId: String): Boolean = value.value.isDependent(sourceId)

            @Composable
            override fun displayName(): String = stringResource(Res.string.less_than_or_equal_to)

            @Composable
            override fun Editor(
                project: Project,
                node: ComposeNode,
                onTransformerEdited: (transformer: PropertyTransformer) -> Unit,
                modifier: Modifier,
            ) {
                AssignableEditableTextPropertyEditor(
                    project = project,
                    node = node,
                    acceptableType = ComposeFlowType.IntType(),
                    onValidPropertyChanged = { property, _ ->
                        value.value = property
                        onTransformerEdited(this)
                    },
                    onInitializeProperty = {
                        value.value = ComposeFlowType.IntType().defaultValue()
                        onTransformerEdited(this)
                    },
                    validateInput = IntValidator()::validate,
                    initialProperty = value.value,
                )
            }
        }

        @Serializable
        @SerialName("IntEquals")
        data class IntEquals(
            @Serializable(MutableStateSerializer::class)
            val value: MutableState<AssignableProperty> =
                mutableStateOf(IntProperty.IntIntrinsicValue(0)),
        ) : ToBoolean {
            override fun getAssignableProperties() = listOf(value.value) + value.value.getAssignableProperties()

            override fun transformedValueExpression(
                project: Project,
                input: String,
            ): String = "$input == ${value.value.transformedValueExpression(project)}"

            override fun transformedCodeBlock(
                project: Project,
                input: CodeBlockWrapper,
                context: GenerationContext,
                dryRun: Boolean,
            ): CodeBlockWrapper {
                val builder = CodeBlockWrapper.builder()
                builder.add(input)
                builder.add(" == ")
                builder.add(value.value.transformedCodeBlock(project, context, dryRun = dryRun))
                value.value.addReadProperty(project, context, dryRun = dryRun)
                return builder.build()
            }

            override fun isDependent(sourceId: String): Boolean = value.value.isDependent(sourceId)

            @Composable
            override fun displayName(): String = stringResource(Res.string.equals)

            @Composable
            override fun Editor(
                project: Project,
                node: ComposeNode,
                onTransformerEdited: (transformer: PropertyTransformer) -> Unit,
                modifier: Modifier,
            ) {
                AssignableEditableTextPropertyEditor(
                    project = project,
                    node = node,
                    acceptableType = ComposeFlowType.IntType(),
                    onValidPropertyChanged = { property, _ ->
                        value.value = property
                        onTransformerEdited(this)
                    },
                    onInitializeProperty = {
                        value.value = ComposeFlowType.IntType().defaultValue()
                        onTransformerEdited(this)
                    },
                    validateInput = IntValidator()::validate,
                    initialProperty = value.value,
                )
            }
        }

        @Serializable
        @SerialName("IntGreaterThanOrEqualTo")
        data class IntGreaterThanOrEqualTo(
            @Serializable(MutableStateSerializer::class)
            val value: MutableState<AssignableProperty> =
                mutableStateOf(IntProperty.IntIntrinsicValue(0)),
        ) : ToBoolean {
            override fun getAssignableProperties() = listOf(value.value) + value.value.getAssignableProperties()

            override fun transformedValueExpression(
                project: Project,
                input: String,
            ): String = "$input >= ${value.value.transformedValueExpression(project)}"

            override fun transformedCodeBlock(
                project: Project,
                input: CodeBlockWrapper,
                context: GenerationContext,
                dryRun: Boolean,
            ): CodeBlockWrapper {
                val builder = CodeBlockWrapper.builder()
                builder.add(input)
                builder.add(" >= ")
                builder.add(value.value.transformedCodeBlock(project, context, dryRun = dryRun))
                value.value.addReadProperty(project, context, dryRun = dryRun)
                return builder.build()
            }

            override fun isDependent(sourceId: String): Boolean = value.value.isDependent(sourceId)

            @Composable
            override fun displayName(): String = stringResource(Res.string.greater_than_or_equal_to)

            @Composable
            override fun Editor(
                project: Project,
                node: ComposeNode,
                onTransformerEdited: (transformer: PropertyTransformer) -> Unit,
                modifier: Modifier,
            ) {
                AssignableEditableTextPropertyEditor(
                    project = project,
                    node = node,
                    acceptableType = ComposeFlowType.IntType(),
                    onValidPropertyChanged = { property, _ ->
                        value.value = property
                        onTransformerEdited(this)
                    },
                    onInitializeProperty = {
                        value.value = ComposeFlowType.IntType().defaultValue()
                        onTransformerEdited(this)
                    },
                    validateInput = IntValidator()::validate,
                    initialProperty = value.value,
                )
            }
        }

        @Serializable
        @SerialName("IntGreaterThan")
        data class IntGreaterThan(
            @Serializable(MutableStateSerializer::class)
            val value: MutableState<AssignableProperty> =
                mutableStateOf(IntProperty.IntIntrinsicValue(0)),
        ) : ToBoolean {
            override fun getAssignableProperties() = listOf(value.value) + value.value.getAssignableProperties()

            override fun transformedValueExpression(
                project: Project,
                input: String,
            ): String = "$input > ${value.value.transformedValueExpression(project)}"

            override fun transformedCodeBlock(
                project: Project,
                input: CodeBlockWrapper,
                context: GenerationContext,
                dryRun: Boolean,
            ): CodeBlockWrapper {
                val builder = CodeBlockWrapper.builder()
                builder.add(input)
                builder.add(" > ")
                builder.add(value.value.transformedCodeBlock(project, context, dryRun = dryRun))
                value.value.addReadProperty(project, context, dryRun = dryRun)
                return builder.build()
            }

            override fun isDependent(sourceId: String): Boolean = value.value.isDependent(sourceId)

            @Composable
            override fun displayName(): String = stringResource(Res.string.greater_than)

            @Composable
            override fun Editor(
                project: Project,
                node: ComposeNode,
                onTransformerEdited: (transformer: PropertyTransformer) -> Unit,
                modifier: Modifier,
            ) {
                AssignableEditableTextPropertyEditor(
                    project = project,
                    node = node,
                    acceptableType = ComposeFlowType.IntType(),
                    onValidPropertyChanged = { property, _ ->
                        value.value = property
                        onTransformerEdited(this)
                    },
                    onInitializeProperty = {
                        value.value = ComposeFlowType.IntType().defaultValue()
                        onTransformerEdited(this)
                    },
                    validateInput = IntValidator()::validate,
                    initialProperty = value.value,
                )
            }
        }

        @Serializable
        @SerialName("IntModEqualsTo")
        data class IntModEqualsTo(
            @Serializable(MutableStateSerializer::class)
            val mod: MutableState<AssignableProperty> =
                mutableStateOf(IntProperty.IntIntrinsicValue(2)),
            @Serializable(MutableStateSerializer::class)
            val equalsTo: MutableState<AssignableProperty> =
                mutableStateOf(IntProperty.IntIntrinsicValue(0)),
        ) : ToBoolean {
            override fun getAssignableProperties() =
                listOf(mod.value, equalsTo.value) +
                    mod.value.getAssignableProperties() + equalsTo.value.getAssignableProperties()

            override fun transformedValueExpression(
                project: Project,
                input: String,
            ): String =
                "$input mod ${mod.value.transformedValueExpression(project)} == ${
                    equalsTo.value.transformedValueExpression(
                        project,
                    )
                }"

            override fun transformedCodeBlock(
                project: Project,
                input: CodeBlockWrapper,
                context: GenerationContext,
                dryRun: Boolean,
            ): CodeBlockWrapper {
                val builder = CodeBlockWrapper.builder()
                builder.add(input)
                builder.add(".mod(")
                builder.add(mod.value.transformedCodeBlock(project, context, dryRun = dryRun))
                builder.add(")")
                builder.add(" == ")
                builder.add(equalsTo.value.transformedCodeBlock(project, context, dryRun = dryRun))
                mod.value.addReadProperty(project, context, dryRun = dryRun)
                equalsTo.value.addReadProperty(project, context, dryRun = dryRun)
                return builder.build()
            }

            override fun isDependent(sourceId: String): Boolean =
                mod.value.isDependent(sourceId) ||
                    equalsTo.value.isDependent(sourceId)

            @Composable
            override fun displayName(): String = stringResource(Res.string.mod_equals_to)

            @Composable
            override fun Editor(
                project: Project,
                node: ComposeNode,
                onTransformerEdited: (transformer: PropertyTransformer) -> Unit,
                modifier: Modifier,
            ) {
                AssignableEditableTextPropertyEditor(
                    project = project,
                    node = node,
                    acceptableType = ComposeFlowType.IntType(),
                    onValidPropertyChanged = { property, _ ->
                        mod.value = property
                        onTransformerEdited(this)
                    },
                    onInitializeProperty = {
                        mod.value = ComposeFlowType.IntType().defaultValue()
                        onTransformerEdited(this)
                    },
                    validateInput = NotEmptyNotLessThanZeroIntValidator()::validate,
                    initialProperty = mod.value,
                    label = "mod",
                )

                AssignableEditableTextPropertyEditor(
                    project = project,
                    node = node,
                    acceptableType = ComposeFlowType.IntType(),
                    onValidPropertyChanged = { property, _ ->
                        equalsTo.value = property
                        onTransformerEdited(this)
                    },
                    onInitializeProperty = {
                        equalsTo.value = ComposeFlowType.IntType().defaultValue()
                        onTransformerEdited(this)
                    },
                    validateInput = IntValidator()::validate,
                    initialProperty = equalsTo.value,
                    label = "equals to",
                )
            }
        }

        override fun toType(): ComposeFlowType = ComposeFlowType.BooleanType()
    }

    companion object {
        fun transformers(): List<PropertyTransformer> =
            listOf(
                ToInt.IntPlus(),
                ToInt.IntMultipliedBy(),
                ToBoolean.IntLessThan(),
                ToBoolean.IntLessThanOrEqualTo(),
                ToBoolean.IntEquals(),
                ToBoolean.IntGreaterThanOrEqualTo(),
                ToBoolean.IntGreaterThan(),
                ToBoolean.IntModEqualsTo(),
                ToString.ToStringValue,
            )
    }
}

@Serializable
sealed interface FromFloat : PropertyTransformer {
    override fun fromType(): ComposeFlowType = ComposeFlowType.FloatType()

    @Serializable
    sealed interface ToFloat :
        FromFloat,
        PropertyTransformer {
        @Serializable
        @SerialName("FloatPlus")
        data class FloatPlus(
            @Serializable(MutableStateSerializer::class)
            val value: MutableState<AssignableProperty> =
                mutableStateOf(FloatProperty.FloatIntrinsicValue(0f)),
        ) : ToFloat {
            override fun getAssignableProperties() = listOf(value.value) + value.value.getAssignableProperties()

            override fun transformedValueExpression(
                project: Project,
                input: String,
            ): String = "$input + ${value.value.transformedValueExpression(project)}"

            override fun transformedCodeBlock(
                project: Project,
                input: CodeBlockWrapper,
                context: GenerationContext,
                dryRun: Boolean,
            ): CodeBlockWrapper {
                val builder = CodeBlockWrapper.builder()
                builder.add(input)
                builder.add(" + ")
                builder.add(value.value.transformedCodeBlock(project, context, dryRun = dryRun))
                value.value.addReadProperty(project, context, dryRun = dryRun)
                return builder.build()
            }

            override fun isDependent(sourceId: String): Boolean = value.value.isDependent(sourceId)

            @Composable
            override fun displayName(): String = stringResource(Res.string.plus)

            @Composable
            override fun Editor(
                project: Project,
                node: ComposeNode,
                onTransformerEdited: (transformer: PropertyTransformer) -> Unit,
                modifier: Modifier,
            ) {
                AssignableEditableTextPropertyEditor(
                    project = project,
                    node = node,
                    acceptableType = ComposeFlowType.FloatType(),
                    onValidPropertyChanged = { property, _ ->
                        value.value = property
                        onTransformerEdited(this)
                    },
                    onInitializeProperty = {
                        value.value = ComposeFlowType.FloatType().defaultValue()
                        onTransformerEdited(this)
                    },
                    validateInput = FloatValidator()::validate,
                    initialProperty = value.value,
                )
            }
        }

        @Serializable
        @SerialName("FloatMultipliedBy")
        data class FloatMultipliedBy(
            @Serializable(MutableStateSerializer::class)
            val value: MutableState<AssignableProperty> =
                mutableStateOf(FloatProperty.FloatIntrinsicValue(0f)),
        ) : ToFloat {
            override fun getAssignableProperties() = listOf(value.value) + value.value.getAssignableProperties()

            override fun transformedValueExpression(
                project: Project,
                input: String,
            ): String = "$input * ${value.value.transformedValueExpression(project)}"

            override fun transformedCodeBlock(
                project: Project,
                input: CodeBlockWrapper,
                context: GenerationContext,
                dryRun: Boolean,
            ): CodeBlockWrapper {
                val builder = CodeBlockWrapper.builder()
                builder.add(input)
                builder.add(" * ")
                builder.add(value.value.transformedCodeBlock(project, context, dryRun = dryRun))
                value.value.addReadProperty(project, context, dryRun = dryRun)
                return builder.build()
            }

            override fun isDependent(sourceId: String): Boolean = value.value.isDependent(sourceId)

            @Composable
            override fun displayName(): String = stringResource(Res.string.multiplied_by)

            @Composable
            override fun Editor(
                project: Project,
                node: ComposeNode,
                onTransformerEdited: (transformer: PropertyTransformer) -> Unit,
                modifier: Modifier,
            ) {
                AssignableEditableTextPropertyEditor(
                    project = project,
                    node = node,
                    acceptableType = ComposeFlowType.FloatType(),
                    onValidPropertyChanged = { property, _ ->
                        value.value = property
                        onTransformerEdited(this)
                    },
                    onInitializeProperty = {
                        value.value = ComposeFlowType.FloatType().defaultValue()
                        onTransformerEdited(this)
                    },
                    validateInput = FloatValidator()::validate,
                    initialProperty = value.value,
                )
            }
        }

        override fun toType(): ComposeFlowType = ComposeFlowType.FloatType()
    }

    @Serializable
    sealed interface ToString :
        FromFloat,
        PropertyTransformer {
        override fun toType(): ComposeFlowType = ComposeFlowType.StringType()

        @Serializable
        @SerialName("FromFloatToString")
        data object ToStringValue : ToString {
            override fun transformedValueExpression(
                project: Project,
                input: String,
            ): String = "$input.toString()"

            override fun transformedCodeBlock(
                project: Project,
                input: CodeBlockWrapper,
                context: GenerationContext,
                dryRun: Boolean,
            ): CodeBlockWrapper {
                val builder = CodeBlockWrapper.builder()
                builder.add(input)
                builder.add(".toString()")
                return builder.build()
            }

            override fun isDependent(sourceId: String): Boolean = false

            @Composable
            override fun displayName(): String = stringResource(Res.string.to_string)
        }
    }

    @Serializable
    sealed interface ToBoolean :
        FromFloat,
        PropertyTransformer {
        @Serializable
        @SerialName("FloatLessThan")
        data class FloatLessThan(
            @Serializable(MutableStateSerializer::class)
            val value: MutableState<AssignableProperty> =
                mutableStateOf(FloatProperty.FloatIntrinsicValue(0f)),
        ) : ToBoolean {
            override fun getAssignableProperties() = listOf(value.value) + value.value.getAssignableProperties()

            override fun transformedValueExpression(
                project: Project,
                input: String,
            ): String = "$input < ${value.value.transformedValueExpression(project)}"

            override fun transformedCodeBlock(
                project: Project,
                input: CodeBlockWrapper,
                context: GenerationContext,
                dryRun: Boolean,
            ): CodeBlockWrapper {
                val builder = CodeBlockWrapper.builder()
                builder.add(input)
                builder.add(" < ")
                builder.add(value.value.transformedCodeBlock(project, context, dryRun = dryRun))
                value.value.addReadProperty(project, context, dryRun = dryRun)
                return builder.build()
            }

            override fun isDependent(sourceId: String): Boolean = value.value.isDependent(sourceId)

            @Composable
            override fun displayName(): String = stringResource(Res.string.less_than)

            @Composable
            override fun Editor(
                project: Project,
                node: ComposeNode,
                onTransformerEdited: (transformer: PropertyTransformer) -> Unit,
                modifier: Modifier,
            ) {
                AssignableEditableTextPropertyEditor(
                    project = project,
                    node = node,
                    acceptableType = ComposeFlowType.FloatType(),
                    onValidPropertyChanged = { property, _ ->
                        value.value = property
                        onTransformerEdited(this)
                    },
                    onInitializeProperty = {
                        value.value = ComposeFlowType.FloatType().defaultValue()
                        onTransformerEdited(this)
                    },
                    validateInput = FloatValidator()::validate,
                    initialProperty = value.value,
                )
            }
        }

        @Serializable
        @SerialName("FloatLessThanOrEqualTo")
        data class FloatLessThanOrEqualTo(
            @Serializable(MutableStateSerializer::class)
            val value: MutableState<AssignableProperty> =
                mutableStateOf(FloatProperty.FloatIntrinsicValue(0f)),
        ) : ToBoolean {
            override fun getAssignableProperties() = listOf(value.value) + value.value.getAssignableProperties()

            override fun transformedValueExpression(
                project: Project,
                input: String,
            ): String = "$input <= ${value.value.transformedValueExpression(project)}"

            override fun transformedCodeBlock(
                project: Project,
                input: CodeBlockWrapper,
                context: GenerationContext,
                dryRun: Boolean,
            ): CodeBlockWrapper {
                val builder = CodeBlockWrapper.builder()
                builder.add(input)
                builder.add(" <= ")
                builder.add(value.value.transformedCodeBlock(project, context, dryRun = dryRun))
                value.value.addReadProperty(project, context, dryRun = dryRun)
                return builder.build()
            }

            override fun isDependent(sourceId: String): Boolean = value.value.isDependent(sourceId)

            @Composable
            override fun displayName(): String = stringResource(Res.string.less_than_or_equal_to)

            @Composable
            override fun Editor(
                project: Project,
                node: ComposeNode,
                onTransformerEdited: (transformer: PropertyTransformer) -> Unit,
                modifier: Modifier,
            ) {
                AssignableEditableTextPropertyEditor(
                    project = project,
                    node = node,
                    acceptableType = ComposeFlowType.FloatType(),
                    onValidPropertyChanged = { property, _ ->
                        value.value = property
                        onTransformerEdited(this)
                    },
                    onInitializeProperty = {
                        value.value = ComposeFlowType.FloatType().defaultValue()
                        onTransformerEdited(this)
                    },
                    validateInput = FloatValidator()::validate,
                    initialProperty = value.value,
                )
            }
        }

        @Serializable
        @SerialName("FloatEquals")
        data class FloatEquals(
            @Serializable(MutableStateSerializer::class)
            val value: MutableState<AssignableProperty> =
                mutableStateOf(FloatProperty.FloatIntrinsicValue(0f)),
        ) : ToBoolean {
            override fun getAssignableProperties() = listOf(value.value) + value.value.getAssignableProperties()

            override fun transformedValueExpression(
                project: Project,
                input: String,
            ): String = "$input == ${value.value.transformedValueExpression(project)}"

            override fun transformedCodeBlock(
                project: Project,
                input: CodeBlockWrapper,
                context: GenerationContext,
                dryRun: Boolean,
            ): CodeBlockWrapper {
                val builder = CodeBlockWrapper.builder()
                builder.add(input)
                builder.add(" == ")
                builder.add(value.value.transformedCodeBlock(project, context, dryRun = dryRun))
                value.value.addReadProperty(project, context, dryRun = dryRun)
                return builder.build()
            }

            override fun isDependent(sourceId: String): Boolean = value.value.isDependent(sourceId)

            @Composable
            override fun displayName(): String = stringResource(Res.string.equals)

            @Composable
            override fun Editor(
                project: Project,
                node: ComposeNode,
                onTransformerEdited: (transformer: PropertyTransformer) -> Unit,
                modifier: Modifier,
            ) {
                AssignableEditableTextPropertyEditor(
                    project = project,
                    node = node,
                    acceptableType = ComposeFlowType.FloatType(),
                    onValidPropertyChanged = { property, _ ->
                        value.value = property
                        onTransformerEdited(this)
                    },
                    onInitializeProperty = {
                        value.value = ComposeFlowType.FloatType().defaultValue()
                        onTransformerEdited(this)
                    },
                    validateInput = FloatValidator()::validate,
                    initialProperty = value.value,
                )
            }
        }

        @Serializable
        @SerialName("FloatGreaterThanOrEqualTo")
        data class FloatGreaterThanOrEqualTo(
            @Serializable(MutableStateSerializer::class)
            val value: MutableState<AssignableProperty> =
                mutableStateOf(FloatProperty.FloatIntrinsicValue(0f)),
        ) : ToBoolean {
            override fun getAssignableProperties() = listOf(value.value) + value.value.getAssignableProperties()

            override fun transformedValueExpression(
                project: Project,
                input: String,
            ): String = "$input >= ${value.value.transformedValueExpression(project)}"

            override fun transformedCodeBlock(
                project: Project,
                input: CodeBlockWrapper,
                context: GenerationContext,
                dryRun: Boolean,
            ): CodeBlockWrapper {
                val builder = CodeBlockWrapper.builder()
                builder.add(input)
                builder.add(" >= ")
                builder.add(value.value.transformedCodeBlock(project, context, dryRun = dryRun))
                value.value.addReadProperty(project, context, dryRun = dryRun)
                return builder.build()
            }

            override fun isDependent(sourceId: String): Boolean = value.value.isDependent(sourceId)

            @Composable
            override fun displayName(): String = stringResource(Res.string.greater_than_or_equal_to)

            @Composable
            override fun Editor(
                project: Project,
                node: ComposeNode,
                onTransformerEdited: (transformer: PropertyTransformer) -> Unit,
                modifier: Modifier,
            ) {
                AssignableEditableTextPropertyEditor(
                    project = project,
                    node = node,
                    acceptableType = ComposeFlowType.FloatType(),
                    onValidPropertyChanged = { property, _ ->
                        value.value = property
                        onTransformerEdited(this)
                    },
                    onInitializeProperty = {
                        value.value = ComposeFlowType.FloatType().defaultValue()
                        onTransformerEdited(this)
                    },
                    validateInput = FloatValidator()::validate,
                    initialProperty = value.value,
                )
            }
        }

        @Serializable
        @SerialName("FloatGreaterThan")
        data class FloatGreaterThan(
            @Serializable(MutableStateSerializer::class)
            val value: MutableState<AssignableProperty> =
                mutableStateOf(FloatProperty.FloatIntrinsicValue(0f)),
        ) : ToBoolean {
            override fun getAssignableProperties() = listOf(value.value) + value.value.getAssignableProperties()

            override fun transformedValueExpression(
                project: Project,
                input: String,
            ): String = "$input > ${value.value.transformedValueExpression(project)}"

            override fun transformedCodeBlock(
                project: Project,
                input: CodeBlockWrapper,
                context: GenerationContext,
                dryRun: Boolean,
            ): CodeBlockWrapper {
                val builder = CodeBlockWrapper.builder()
                builder.add(input)
                builder.add(" > ")
                builder.add(value.value.transformedCodeBlock(project, context, dryRun = dryRun))
                value.value.addReadProperty(project, context, dryRun = dryRun)
                return builder.build()
            }

            override fun isDependent(sourceId: String): Boolean = value.value.isDependent(sourceId)

            @Composable
            override fun displayName(): String = stringResource(Res.string.greater_than)

            @Composable
            override fun Editor(
                project: Project,
                node: ComposeNode,
                onTransformerEdited: (transformer: PropertyTransformer) -> Unit,
                modifier: Modifier,
            ) {
                AssignableEditableTextPropertyEditor(
                    project = project,
                    node = node,
                    acceptableType = ComposeFlowType.FloatType(),
                    onValidPropertyChanged = { property, _ ->
                        value.value = property
                        onTransformerEdited(this)
                    },
                    onInitializeProperty = {
                        value.value = ComposeFlowType.FloatType().defaultValue()
                        onTransformerEdited(this)
                    },
                    validateInput = FloatValidator()::validate,
                    initialProperty = value.value,
                )
            }
        }

        @Serializable
        @SerialName("FloatModEqualsTo")
        data class FloatModEqualsTo(
            @Serializable(MutableStateSerializer::class)
            val mod: MutableState<AssignableProperty> =
                mutableStateOf(FloatProperty.FloatIntrinsicValue(2f)),
            @Serializable(MutableStateSerializer::class)
            val equalsTo: MutableState<AssignableProperty> =
                mutableStateOf(FloatProperty.FloatIntrinsicValue(0f)),
        ) : ToBoolean {
            override fun getAssignableProperties() =
                listOf(mod.value, equalsTo.value) +
                    mod.value.getAssignableProperties() + equalsTo.value.getAssignableProperties()

            override fun transformedValueExpression(
                project: Project,
                input: String,
            ): String =
                "$input mod ${mod.value.transformedValueExpression(project)} == ${
                    equalsTo.value.transformedValueExpression(
                        project,
                    )
                }"

            override fun transformedCodeBlock(
                project: Project,
                input: CodeBlockWrapper,
                context: GenerationContext,
                dryRun: Boolean,
            ): CodeBlockWrapper {
                val builder = CodeBlockWrapper.builder()
                builder.add(input)
                builder.add(".mod(")
                builder.add(mod.value.transformedCodeBlock(project, context, dryRun = dryRun))
                builder.add(")")
                builder.add(" == ")
                builder.add(equalsTo.value.transformedCodeBlock(project, context, dryRun = dryRun))
                mod.value.addReadProperty(project, context, dryRun = dryRun)
                equalsTo.value.addReadProperty(project, context, dryRun = dryRun)
                return builder.build()
            }

            override fun isDependent(sourceId: String): Boolean =
                mod.value.isDependent(sourceId) ||
                    equalsTo.value.isDependent(sourceId)

            @Composable
            override fun displayName(): String = stringResource(Res.string.mod_equals_to)

            @Composable
            override fun Editor(
                project: Project,
                node: ComposeNode,
                onTransformerEdited: (transformer: PropertyTransformer) -> Unit,
                modifier: Modifier,
            ) {
                AssignableEditableTextPropertyEditor(
                    project = project,
                    node = node,
                    acceptableType = ComposeFlowType.FloatType(),
                    onValidPropertyChanged = { property, _ ->
                        mod.value = property
                        onTransformerEdited(this)
                    },
                    onInitializeProperty = {
                        mod.value = ComposeFlowType.FloatType().defaultValue()
                        onTransformerEdited(this)
                    },
                    validateInput = NotEmptyNotLessThanZeroIntValidator()::validate,
                    initialProperty = mod.value,
                    label = "mod",
                )

                AssignableEditableTextPropertyEditor(
                    project = project,
                    node = node,
                    acceptableType = ComposeFlowType.FloatType(),
                    onValidPropertyChanged = { property, _ ->
                        equalsTo.value = property
                        onTransformerEdited(this)
                    },
                    onInitializeProperty = {
                        equalsTo.value = ComposeFlowType.FloatType().defaultValue()
                        onTransformerEdited(this)
                    },
                    validateInput = FloatValidator()::validate,
                    initialProperty = equalsTo.value,
                    label = "equals to",
                )
            }
        }

        override fun toType(): ComposeFlowType = ComposeFlowType.BooleanType()
    }

    companion object {
        fun transformers(): List<PropertyTransformer> =
            listOf(
                ToFloat.FloatPlus(),
                ToFloat.FloatMultipliedBy(),
                ToBoolean.FloatLessThan(),
                ToBoolean.FloatLessThanOrEqualTo(),
                ToBoolean.FloatEquals(),
                ToBoolean.FloatGreaterThanOrEqualTo(),
                ToBoolean.FloatGreaterThan(),
                ToBoolean.FloatModEqualsTo(),
                ToString.ToStringValue,
            )
    }
}

@Serializable
sealed interface FromInstant : PropertyTransformer {
    override fun fromType(): ComposeFlowType = ComposeFlowType.InstantType()

    @Serializable
    sealed interface ToInstant :
        FromInstant,
        PropertyTransformer {
        @Serializable
        @SerialName("PlusDay")
        data class PlusDay(
            @Serializable(MutableStateSerializer::class)
            val value: MutableState<AssignableProperty> =
                mutableStateOf(IntProperty.IntIntrinsicValue(0)),
        ) : ToInstant {
            override fun getAssignableProperties() = listOf(value.value) + value.value.getAssignableProperties()

            override fun transformedValueExpression(
                project: Project,
                input: String,
            ): String = "$input + ${value.value.transformedValueExpression(project)} day(s) "

            override fun transformedCodeBlock(
                project: Project,
                input: CodeBlockWrapper,
                context: GenerationContext,
                dryRun: Boolean,
            ): CodeBlockWrapper {
                val builder = CodeBlockWrapper.builder()
                builder.add(input)
                builder.add(
                    CodeBlockWrapper.of(
                        ".%M(${
                            value.value.transformedCodeBlock(
                                project,
                                context,
                                dryRun = dryRun,
                            )
                        }, %M.DAY, %M.currentSystemDefault())",
                        MemberHolder.DateTime.plus,
                        MemberHolder.DateTime.DateTimeUnit,
                        MemberHolder.DateTime.TimeZone,
                    ),
                )
                value.value.addReadProperty(project, context, dryRun = dryRun)
                return builder.build()
            }

            override fun isDependent(sourceId: String): Boolean = value.value.isDependent(sourceId)

            @Composable
            override fun displayName(): String = stringResource(Res.string.plus_days)

            @Composable
            override fun Editor(
                project: Project,
                node: ComposeNode,
                onTransformerEdited: (transformer: PropertyTransformer) -> Unit,
                modifier: Modifier,
            ) {
                AssignableEditableTextPropertyEditor(
                    project = project,
                    node = node,
                    acceptableType = ComposeFlowType.IntType(),
                    onValidPropertyChanged = { property, _ ->
                        value.value = property
                        onTransformerEdited(this)
                    },
                    onInitializeProperty = {
                        value.value = ComposeFlowType.IntType().defaultValue()
                        onTransformerEdited(this)
                    },
                    validateInput = IntValidator()::validate,
                    initialProperty = value.value,
                )
            }
        }

        @Serializable
        @SerialName("PlusMonth")
        data class PlusMonth(
            @Serializable(MutableStateSerializer::class)
            val value: MutableState<AssignableProperty> =
                mutableStateOf(IntProperty.IntIntrinsicValue(0)),
        ) : ToInstant {
            override fun getAssignableProperties() = listOf(value.value) + value.value.getAssignableProperties()

            override fun transformedValueExpression(
                project: Project,
                input: String,
            ): String = "$input + ${value.value.transformedValueExpression(project)} month(s) "

            override fun transformedCodeBlock(
                project: Project,
                input: CodeBlockWrapper,
                context: GenerationContext,
                dryRun: Boolean,
            ): CodeBlockWrapper {
                val builder = CodeBlockWrapper.builder()
                builder.add(input)
                builder.add(
                    CodeBlockWrapper.of(
                        ".%M(${
                            value.value.transformedCodeBlock(
                                project,
                                context,
                                dryRun = dryRun,
                            )
                        }, %M.MONTH, %M.currentSystemDefault())",
                        MemberHolder.DateTime.plus,
                        MemberHolder.DateTime.DateTimeUnit,
                        MemberHolder.DateTime.TimeZone,
                    ),
                )
                value.value.addReadProperty(project, context, dryRun = dryRun)
                return builder.build()
            }

            override fun isDependent(sourceId: String): Boolean = value.value.isDependent(sourceId)

            @Composable
            override fun displayName(): String = stringResource(Res.string.plus_months)

            @Composable
            override fun Editor(
                project: Project,
                node: ComposeNode,
                onTransformerEdited: (transformer: PropertyTransformer) -> Unit,
                modifier: Modifier,
            ) {
                AssignableEditableTextPropertyEditor(
                    project = project,
                    node = node,
                    acceptableType = ComposeFlowType.IntType(),
                    onValidPropertyChanged = { property, _ ->
                        value.value = property
                        onTransformerEdited(this)
                    },
                    onInitializeProperty = {
                        value.value = ComposeFlowType.IntType().defaultValue()
                        onTransformerEdited(this)
                    },
                    validateInput = IntValidator()::validate,
                    initialProperty = value.value,
                )
            }
        }

        @Serializable
        @SerialName("PlusYear")
        data class PlusYear(
            @Serializable(MutableStateSerializer::class)
            val value: MutableState<AssignableProperty> =
                mutableStateOf(IntProperty.IntIntrinsicValue(0)),
        ) : ToInstant {
            override fun getAssignableProperties() = listOf(value.value) + value.value.getAssignableProperties()

            override fun transformedValueExpression(
                project: Project,
                input: String,
            ): String = "$input + ${value.value.transformedValueExpression(project)} year(s) "

            override fun transformedCodeBlock(
                project: Project,
                input: CodeBlockWrapper,
                context: GenerationContext,
                dryRun: Boolean,
            ): CodeBlockWrapper {
                val builder = CodeBlockWrapper.builder()
                builder.add(input)
                builder.add(
                    CodeBlockWrapper.of(
                        ".%M(${
                            value.value.transformedCodeBlock(
                                project,
                                context,
                                dryRun = dryRun,
                            )
                        } * 12, %M.MONTH, %M.currentSystemDefault())",
                        MemberHolder.DateTime.plus,
                        MemberHolder.DateTime.DateTimeUnit,
                        MemberHolder.DateTime.TimeZone,
                    ),
                )
                value.value.addReadProperty(project, context, dryRun = dryRun)
                return builder.build()
            }

            override fun isDependent(sourceId: String): Boolean = value.value.isDependent(sourceId)

            @Composable
            override fun displayName(): String = stringResource(Res.string.plus_years)

            @Composable
            override fun Editor(
                project: Project,
                node: ComposeNode,
                onTransformerEdited: (transformer: PropertyTransformer) -> Unit,
                modifier: Modifier,
            ) {
                AssignableEditableTextPropertyEditor(
                    project = project,
                    node = node,
                    acceptableType = ComposeFlowType.IntType(),
                    onValidPropertyChanged = { property, _ ->
                        value.value = property
                        onTransformerEdited(this)
                    },
                    onInitializeProperty = {
                        value.value = ComposeFlowType.IntType().defaultValue()
                        onTransformerEdited(this)
                    },
                    validateInput = IntValidator()::validate,
                    initialProperty = value.value,
                )
            }
        }

        override fun toType(): ComposeFlowType = ComposeFlowType.InstantType()
    }

    @Serializable
    sealed interface ToString :
        FromInstant,
        PropertyTransformer {
        @Serializable
        @SerialName("FormatAs")
        data class FormatAs(
            @Serializable(MutableStateSerializer::class)
            val dateTimeFormatter: MutableState<DateTimeFormatter> =
                mutableStateOf(
                    DateTimeFormatter.YYYY_MM_DD(),
                    policy = referentialEqualityPolicy(),
                ),
        ) : ToString {
            override fun toType(): ComposeFlowType = ComposeFlowType.StringType()

            override fun transformedValueExpression(
                project: Project,
                input: String,
            ): String = "$input.format(${dateTimeFormatter.value.simplifiedFormat()}) "

            override fun transformedCodeBlock(
                project: Project,
                input: CodeBlockWrapper,
                context: GenerationContext,
                dryRun: Boolean,
            ): CodeBlockWrapper {
                val builder = CodeBlockWrapper.builder()
                builder.add(input)
                builder.add(
                    CodeBlockWrapper.of(
                        ".%M(%M.currentSystemDefault(",
                        MemberHolder.DateTime.toLocalDateTime,
                        MemberHolder.DateTime.TimeZone,
                    ),
                )
                builder.add(CodeBlockWrapper.of(")).%M(", MemberHolder.DateTime.format))
                builder.add(dateTimeFormatter.value.asCodeBlock().toWrapper())
                builder.add(")")
                return builder.build()
            }

            override fun isDependent(sourceId: String): Boolean = false

            @Composable
            override fun displayName(): String = stringResource(Res.string.format_as)

            @Composable
            override fun Editor(
                project: Project,
                node: ComposeNode,
                onTransformerEdited: (transformer: PropertyTransformer) -> Unit,
                modifier: Modifier,
            ) {
                BasicDropdownPropertyEditor(
                    project = project,
                    items = DateTimeFormatter.entries(),
                    onValueChanged = { _, item ->
                        dateTimeFormatter.value = item
                        onTransformerEdited(this)
                    },
                    label = "Format",
                    selectedItem = dateTimeFormatter.value,
                )

                val localFormatter = dateTimeFormatter.value
                if (localFormatter is SeparatorHolder) {
                    BasicDropdownPropertyEditor(
                        project = project,
                        items = Separator.entries,
                        onValueChanged = { _, item ->
                            val newFormatter = localFormatter.newInstance()
                            (newFormatter as SeparatorHolder).separator = item
                            if (newFormatter is PaddingHolder) {
                                newFormatter.padding = (localFormatter as PaddingHolder).padding
                            }
                            dateTimeFormatter.value = newFormatter
                            onTransformerEdited(this)
                        },
                        label = "Separator",
                        selectedItem = localFormatter.separator,
                    )
                }
                if (localFormatter is PaddingHolder) {
                    BasicDropdownPropertyEditor(
                        project = project,
                        items = PaddingWrapper.entries,
                        onValueChanged = { _, item ->
                            val newFormatter = localFormatter.newInstance()
                            (newFormatter as PaddingHolder).padding = item
                            if (newFormatter is SeparatorHolder) {
                                newFormatter.separator =
                                    (localFormatter as SeparatorHolder).separator
                            }
                            dateTimeFormatter.value = newFormatter
                            onTransformerEdited(this)
                        },
                        label = "Padding",
                        selectedItem = localFormatter.padding,
                    )
                }
            }
        }
    }

    companion object {
        fun transformers(): List<PropertyTransformer> =
            listOf(
                ToString.FormatAs(),
                ToInstant.PlusDay(),
                ToInstant.PlusMonth(),
                ToInstant.PlusYear(),
            )
    }
}

@Serializable
sealed interface FromList : PropertyTransformer {
    @Serializable
    sealed interface ToList :
        FromList,
        PropertyTransformer {
        @Serializable
        @SerialName("Filter")
        data class Filter(
            private val innerType: ComposeFlowType,
            @Serializable(MutableStateSerializer::class)
            val condition: MutableState<AssignableProperty> = mutableStateOf(BooleanProperty.Empty),
        ) : ToList {
            override fun getAssignableProperties() = listOf(condition.value) + condition.value.getAssignableProperties()

            private val functionScopeParameterProperty =
                FunctionScopeParameterProperty(
                    functionName = "filter",
                    variableType = innerType.copyWith(newIsList = false),
                )

            override fun fromType(): ComposeFlowType = innerType.copyWith(newIsList = true)

            override fun toType(): ComposeFlowType = innerType.copyWith(newIsList = true)

            override fun transformedValueExpression(
                project: Project,
                input: String,
            ): String =
                if (condition.value == BooleanProperty.Empty) {
                    "$input.filter { }"
                } else {
                    "$input.filter { ${
                        condition.value.transformedValueExpression(
                            project,
                        )
                    } }"
                }

            override fun transformedCodeBlock(
                project: Project,
                input: CodeBlockWrapper,
                context: GenerationContext,
                dryRun: Boolean,
            ): CodeBlockWrapper {
                val builder = CodeBlockWrapper.builder()
                builder.add(input)
                builder.add(
                    """.filter { ${
                        condition.value.transformedCodeBlock(
                            project,
                            context,
                            dryRun = dryRun,
                        )
                    } }""",
                )
                condition.value.addReadProperty(project, context, dryRun = dryRun)
                return builder.build()
            }

            override fun isValid(): Boolean = condition.value != BooleanProperty.Empty

            override fun isDependent(sourceId: String): Boolean = condition.value.isDependent(sourceId)

            @Composable
            override fun displayName(): String = stringResource(Res.string.filter)

            @Composable
            override fun Editor(
                project: Project,
                node: ComposeNode,
                onTransformerEdited: (transformer: PropertyTransformer) -> Unit,
                modifier: Modifier,
            ) {
                AssignableBooleanPropertyEditor(
                    project = project,
                    node = node,
                    modifier = modifier,
                    label = "Condition",
                    initialProperty = condition.value,
                    onValidPropertyChanged = { property, _ ->
                        condition.value = property
                        onTransformerEdited(this)
                    },
                    onInitializeProperty = {
                        condition.value = BooleanProperty.Empty
                        onTransformerEdited(this)
                    },
                    functionScopeProperties =
                        listOf(
                            functionScopeParameterProperty,
                        ),
                )
            }
        }

        @Serializable
        @SerialName("Sorted")
        data class Sorted(
            private val innerType: ComposeFlowType,
        ) : ToList {
            override fun fromType(): ComposeFlowType = innerType.copyWith(newIsList = true)

            override fun toType(): ComposeFlowType = innerType.copyWith(newIsList = true)

            override fun isDependent(sourceId: String): Boolean = false

            override fun transformedValueExpression(
                project: Project,
                input: String,
            ): String = "$input.sorted()"

            override fun transformedCodeBlock(
                project: Project,
                input: CodeBlockWrapper,
                context: GenerationContext,
                dryRun: Boolean,
            ): CodeBlockWrapper {
                val builder = CodeBlockWrapper.builder()
                builder.add(input)
                builder.add(
                    CodeBlockWrapper.of(
                        """.%M()""",
                        MemberHolder.Kotlin.Collection.sorted,
                    ),
                )
                return builder.build()
            }

            @Composable
            override fun displayName(): String = stringResource(Res.string.sorted)
        }

        @Serializable
        @SerialName("SortedBy")
        data class SortedBy(
            private val innerType: ComposeFlowType,
            @Serializable(MutableStateSerializer::class)
            private val dataFieldIdToSort: MutableState<DataFieldId> =
                mutableStateOf(
                    Uuid.random().toString(),
                ),
        ) : ToList {
            override fun fromType(): ComposeFlowType = innerType.copyWith(newIsList = true)

            override fun toType(): ComposeFlowType = innerType.copyWith(newIsList = true)

            override fun isDependent(sourceId: String): Boolean = false

            override fun transformedValueExpression(
                project: Project,
                input: String,
            ): String {
                if (innerType !is ComposeFlowType.CustomDataType) return "Invalid"
                val dataType = project.findDataTypeOrNull(innerType.dataTypeId) ?: return "Invalid"

                val dataField =
                    dataType.findDataFieldOrNull(dataFieldIdToSort.value)
                        ?: if (dataType.fields.isNotEmpty()) dataType.fields[0] else null
                return if (dataField == null) {
                    "Invalid"
                } else {
                    "$input.sortedBy { it.${dataField.variableName} }"
                }
            }

            override fun transformedCodeBlock(
                project: Project,
                input: CodeBlockWrapper,
                context: GenerationContext,
                dryRun: Boolean,
            ): CodeBlockWrapper {
                val builder = CodeBlockWrapper.builder()
                if (innerType !is ComposeFlowType.CustomDataType) return builder.build()
                val dataType =
                    project.findDataTypeOrNull(innerType.dataTypeId) ?: return builder.build()
                val dataField =
                    dataType.findDataFieldOrNull(dataFieldIdToSort.value)
                        ?: if (dataType.fields.isNotEmpty()) dataType.fields[0] else null
                builder.add(input)
                builder.add(CodeBlockWrapper.of(".%M", MemberHolder.Kotlin.Collection.sortedBy))
                builder.add(" { it.${dataField?.variableName} }")
                return builder.build()
            }

            @Composable
            override fun displayName(): String = stringResource(Res.string.sorted_by)

            @Composable
            override fun Editor(
                project: Project,
                node: ComposeNode,
                onTransformerEdited: (transformer: PropertyTransformer) -> Unit,
                modifier: Modifier,
            ) {
                if (innerType !is ComposeFlowType.CustomDataType) return
                val dataType = project.findDataTypeOrNull(innerType.dataTypeId) ?: return

                val selectedField =
                    dataType.fields
                        .firstOrNull { it.id == dataFieldIdToSort.value }
                        ?: if (dataType.fields.isNotEmpty()) dataType.fields[0] else null
                BasicDropdownPropertyEditor(
                    project = project,
                    items = dataType.fields,
                    onValueChanged = { _, item ->
                        dataFieldIdToSort.value = item.id
                        onTransformerEdited(this)
                    },
                    label = "Sort field",
                    selectedItem = selectedField,
                )
            }
        }

        @Serializable
        @SerialName("Map")
        data class Map(
            private val innerType: ComposeFlowType,
            @Serializable(MutableStateSerializer::class)
            private val outputType: MutableState<ComposeFlowType> = mutableStateOf(ComposeFlowType.StringType()),
            @Serializable(MutableStateSerializer::class)
            val value: MutableState<AssignablePropertyValue> =
                mutableStateOf(
                    AssignablePropertyValue.ForPrimitive(),
                ),
        ) : ToList {
            // Create the FunctionScopeParameterProperty as a property to avoid a new property
            // is created every time recomposition happens
            private val functionScopeParameterProperty =
                FunctionScopeParameterProperty(
                    functionName = "map",
                    variableType = innerType.copyWith(newIsList = false),
                )

            override fun fromType(): ComposeFlowType = innerType.copyWith(newIsList = true)

            override fun toType(): ComposeFlowType = outputType.value.copyWith(newIsList = true)

            override fun transformedValueExpression(
                project: Project,
                input: String,
            ): String {
                val context = GenerationContext()
                return "$input.map { ${
                    value.value.asSimplifiedText(
                        project,
                        context,
                        dryRun = false,
                    )
                } }"
            }

            override fun transformedCodeBlock(
                project: Project,
                input: CodeBlockWrapper,
                context: GenerationContext,
                dryRun: Boolean,
            ): CodeBlockWrapper {
                val builder = CodeBlockWrapper.builder()
                builder.add(input)
                builder.add(
                    """.map { ${
                        value.value.asCodeBlock(
                            project,
                            context,
                            dryRun = dryRun,
                        )
                    } }""",
                )

                when (val propertyValues = value.value) {
                    is AssignablePropertyValue.ForDataType -> {
                        propertyValues.properties.forEach {
                            it.value.addReadProperty(project, context, dryRun = dryRun)
                        }
                    }

                    is AssignablePropertyValue.ForPrimitive -> {
                        propertyValues.property.addReadProperty(project, context, dryRun = dryRun)
                    }
                }
                return builder.build()
            }

            override fun isDependent(sourceId: String): Boolean = value.value.isDependent(sourceId)

            @Composable
            override fun displayName(): String = stringResource(Res.string.map)

            @Composable
            override fun Editor(
                project: Project,
                node: ComposeNode,
                onTransformerEdited: (transformer: PropertyTransformer) -> Unit,
                modifier: Modifier,
            ) {
                val types = ComposeFlowType.validSingleEntries(project)
                val dataTypes = project.dataTypeHolder.dataTypes

                BasicDropdownPropertyEditor(
                    project = project,
                    items = types,
                    onValueChanged = { _, item ->
                        outputType.value = item
                        onTransformerEdited(this)
                    },
                    label = "Output type",
                    selectedItem = outputType.value,
                )
                if (outputType.value is ComposeFlowType.CustomDataType) {
                    BasicDropdownPropertyEditor(
                        project = project,
                        items = dataTypes,
                        onValueChanged = { _, selectedDataType ->
                            outputType.value =
                                ComposeFlowType.CustomDataType(dataTypeId = selectedDataType.id)
                            onTransformerEdited(this)
                        },
                        label = "Data type",
                        selectedItem =
                            when (val selectedType = outputType.value) {
                                is ComposeFlowType.CustomDataType -> {
                                    project.findDataTypeOrNull(selectedType.dataTypeId)
                                }

                                else -> null
                            },
                    )
                }

                Column(modifier = Modifier.padding(start = 16.dp)) {
                    when (val outputType = outputType.value) {
                        is ComposeFlowType.CustomDataType -> {
                            val dataType = project.findDataTypeOrNull(outputType.dataTypeId)
                            dataType?.let {
                                val existingProperties =
                                    when (val initialProperty = value.value) {
                                        is AssignablePropertyValue.ForDataType -> {
                                            initialProperty.properties
                                        }

                                        is AssignablePropertyValue.ForPrimitive -> {
                                            mutableStateMapOf()
                                        }
                                    }
                                dataType.fields.forEach { dataField ->
                                    dataField.fieldType.type().defaultValue().Editor(
                                        project = project,
                                        node = node,
                                        initialProperty =
                                            existingProperties.entries
                                                .firstOrNull {
                                                    it.key == dataField.id
                                                }?.value ?: dataField.fieldType
                                                .type()
                                                .defaultValue(),
                                        label = dataField.variableName,
                                        onValidPropertyChanged = { property, _ ->
                                            existingProperties[dataField.id] = property
                                            value.value =
                                                AssignablePropertyValue.ForDataType(
                                                    dataTypeId = outputType.dataTypeId,
                                                    properties = existingProperties,
                                                )
                                            onTransformerEdited(this@Map)
                                        },
                                        onInitializeProperty = {
                                            existingProperties[dataField.id] =
                                                dataField.fieldType.type().defaultValue()
                                            value.value =
                                                AssignablePropertyValue.ForDataType(
                                                    dataTypeId = outputType.dataTypeId,
                                                    properties = existingProperties,
                                                )
                                            onTransformerEdited(this@Map)
                                        },
                                        editable = true,
                                        destinationStateId = null,
                                        validateInput = null,
                                        modifier = Modifier,
                                        functionScopeProperties =
                                            listOf(
                                                functionScopeParameterProperty,
                                            ),
                                    )
                                }
                            }
                        }

                        else -> {
                            outputType.defaultValue().Editor(
                                project = project,
                                node = node,
                                initialProperty =
                                    when (val initialProperty = value.value) {
                                        is AssignablePropertyValue.ForDataType -> {
                                            outputType.defaultValue()
                                        }

                                        is AssignablePropertyValue.ForPrimitive -> {
                                            initialProperty.property
                                        }
                                    },
                                label = "value",
                                onValidPropertyChanged = { property, _ ->
                                    value.value = AssignablePropertyValue.ForPrimitive(property)
                                },
                                onInitializeProperty = {},
                                editable = true,
                                destinationStateId = null,
                                validateInput = null,
                                modifier = Modifier,
                                functionScopeProperties =
                                    listOf(
                                        functionScopeParameterProperty,
                                    ),
                            )
                        }
                    }
                }
            }
        }
    }

    @Serializable
    sealed interface JoinToString :
        FromList,
        PropertyTransformer {
        override fun toType(): ComposeFlowType = ComposeFlowType.StringType()

        @Serializable
        @SerialName("FromListJoinToString")
        data class JoinToStringValue(
            private val innerType: ComposeFlowType,
            @Serializable(MutableStateSerializer::class)
            val separator: MutableState<AssignableProperty> =
                mutableStateOf(StringProperty.StringIntrinsicValue(", ")),
        ) : JoinToString {
            override fun getAssignableProperties() = listOf(separator.value) + separator.value.getAssignableProperties()

            override fun fromType(): ComposeFlowType = innerType.copyWith(newIsList = true)

            override fun transformedValueExpression(
                project: Project,
                input: String,
            ): String = "$input.joinToString(${separator.value.transformedValueExpression(project)})"

            override fun transformedCodeBlock(
                project: Project,
                input: CodeBlockWrapper,
                context: GenerationContext,
                dryRun: Boolean,
            ): CodeBlockWrapper {
                val builder = CodeBlockWrapper.builder()
                builder.add(input)
                builder.add(".joinToString(")
                builder.add(separator.value.transformedCodeBlock(project, context, dryRun = dryRun))
                builder.add(")")
                separator.value.addReadProperty(project, context, dryRun = dryRun)
                return builder.build()
            }

            override fun isDependent(sourceId: String): Boolean = separator.value.isDependent(sourceId)

            @Composable
            override fun displayName(): String = "Join to String"

            @Composable
            override fun Editor(
                project: Project,
                node: ComposeNode,
                onTransformerEdited: (transformer: PropertyTransformer) -> Unit,
                modifier: Modifier,
            ) {
                Column(modifier = modifier) {
                    AssignableEditableTextPropertyEditor(
                        project = project,
                        node = node,
                        acceptableType = ComposeFlowType.StringType(),
                        label = "Separator",
                        onValidPropertyChanged = { property, _ ->
                            separator.value = property
                            onTransformerEdited(this@JoinToStringValue)
                        },
                        onInitializeProperty = {
                            separator.value = StringProperty.StringIntrinsicValue(", ")
                            onTransformerEdited(this@JoinToStringValue)
                        },
                        initialProperty = separator.value,
                        modifier = Modifier.padding(vertical = 4.dp),
                    )
                }
            }
        }
    }

    @Serializable
    sealed interface ToString :
        FromList,
        PropertyTransformer {
        override fun toType(): ComposeFlowType = ComposeFlowType.StringType()

        @Serializable
        @SerialName("FromListToString")
        data class ToStringValue(
            private val innerType: ComposeFlowType,
        ) : ToString {
            override fun getAssignableProperties() = emptyList<AssignableProperty>()

            override fun fromType(): ComposeFlowType = innerType.copyWith(newIsList = true)

            override fun transformedValueExpression(
                project: Project,
                input: String,
            ): String = "$input.toString()"

            override fun transformedCodeBlock(
                project: Project,
                input: CodeBlockWrapper,
                context: GenerationContext,
                dryRun: Boolean,
            ): CodeBlockWrapper {
                val builder = CodeBlockWrapper.builder()
                builder.add(input)
                builder.add(".toString()")
                return builder.build()
            }

            override fun isDependent(sourceId: String): Boolean = false

            @Composable
            override fun displayName(): String = stringResource(Res.string.to_string)
        }
    }

    @Serializable
    sealed interface ToBoolean :
        FromList,
        PropertyTransformer {
        override fun toType(): ComposeFlowType = ComposeFlowType.BooleanType()

        @Serializable
        @SerialName("ListContains")
        data class ListContains(
            private val innerType: ComposeFlowType,
            @Serializable(MutableStateSerializer::class)
            val value: MutableState<AssignableProperty> = mutableStateOf(innerType.defaultValue()),
        ) : ToBoolean {
            override fun getAssignableProperties() = listOf(value.value) + value.value.getAssignableProperties()

            private val functionScopeParameterProperty =
                FunctionScopeParameterProperty(
                    functionName = "contains",
                    variableType = innerType.copyWith(newIsList = false),
                )

            override fun fromType(): ComposeFlowType = innerType.copyWith(newIsList = true)

            override fun transformedValueExpression(
                project: Project,
                input: String,
            ): String = "$input.contains(${value.value.transformedValueExpression(project)})"

            override fun transformedCodeBlock(
                project: Project,
                input: CodeBlockWrapper,
                context: GenerationContext,
                dryRun: Boolean,
            ): CodeBlockWrapper {
                val block =
                    innerType.copyWith(newIsList = false).convertCodeFromType(
                        value.value.valueType(project),
                        value.value.transformedCodeBlock(project, context, dryRun = dryRun),
                    )
                val builder = CodeBlockWrapper.builder()
                builder.add(input)
                builder.add(".contains($block)")
                value.value.addReadProperty(project, context, dryRun = dryRun)
                return builder.build()
            }

            override fun isDependent(sourceId: String): Boolean = value.value.isDependent(sourceId)

            @Composable
            override fun displayName(): String = stringResource(Res.string.contains)

            @Composable
            override fun Editor(
                project: Project,
                node: ComposeNode,
                onTransformerEdited: (transformer: PropertyTransformer) -> Unit,
                modifier: Modifier,
            ) {
                value.value.Editor(
                    project,
                    node = node,
                    label = "value",
                    onValidPropertyChanged = { property, _ ->
                        value.value = property
                        onTransformerEdited(this)
                    },
                    onInitializeProperty = {
                        value.value = innerType.defaultValue()
                        onTransformerEdited(this)
                    },
                    validateInput = null,
                    destinationStateId = null,
                    initialProperty = value.value,
                    editable = true,
                    modifier = Modifier,
                    functionScopeProperties =
                        listOf(
                            functionScopeParameterProperty,
                        ),
                )
            }
        }

        @Serializable
        @SerialName("IsEmpty")
        data class IsEmpty(
            private val innerType: ComposeFlowType,
        ) : ToBoolean {
            override fun fromType(): ComposeFlowType = innerType.copyWith(newIsList = true)

            override fun transformedValueExpression(
                project: Project,
                input: String,
            ): String = "$input.isEmpty()"

            override fun transformedCodeBlock(
                project: Project,
                input: CodeBlockWrapper,
                context: GenerationContext,
                dryRun: Boolean,
            ): CodeBlockWrapper {
                val builder = CodeBlockWrapper.builder()
                builder.add(input)
                builder.add(".isEmpty()")
                return builder.build()
            }

            override fun isDependent(sourceId: String): Boolean = false

            @Composable
            override fun displayName(): String = stringResource(Res.string.is_empty)
        }
    }

    @Serializable
    sealed interface ToInt :
        FromList,
        PropertyTransformer {
        override fun toType(): ComposeFlowType = ComposeFlowType.IntType()

        @Serializable
        @SerialName("Size")
        data class Size(
            private val innerType: ComposeFlowType,
        ) : ToInt {
            override fun fromType(): ComposeFlowType = innerType.copyWith(newIsList = true)

            override fun transformedValueExpression(
                project: Project,
                input: String,
            ): String = "$input.size"

            override fun transformedCodeBlock(
                project: Project,
                input: CodeBlockWrapper,
                context: GenerationContext,
                dryRun: Boolean,
            ): CodeBlockWrapper {
                val builder = CodeBlockWrapper.builder()
                builder.add(input)
                builder.add(".size")
                return builder.build()
            }

            override fun isDependent(sourceId: String): Boolean = false

            @Composable
            override fun displayName(): String = stringResource(Res.string.size)
        }
    }

    companion object {
        fun transformers(innerType: ComposeFlowType): List<PropertyTransformer> {
            val sortTransformer =
                if (innerType is ComposeFlowType.CustomDataType) {
                    ToList.SortedBy(innerType)
                } else {
                    ToList.Sorted(innerType)
                }
            return listOf(
                ToList.Filter(innerType.copyWith(newIsList = false)),
                sortTransformer,
                ToList.Map(innerType.copyWith(newIsList = false)),
                ToBoolean.ListContains(innerType.copyWith(newIsList = false)),
                ToBoolean.IsEmpty(innerType.copyWith(newIsList = false)),
                ToInt.Size(innerType.copyWith(newIsList = false)),
                JoinToString.JoinToStringValue(innerType.copyWith(newIsList = false)),
                ToString.ToStringValue(innerType.copyWith(newIsList = false)),
            )
        }
    }
}
