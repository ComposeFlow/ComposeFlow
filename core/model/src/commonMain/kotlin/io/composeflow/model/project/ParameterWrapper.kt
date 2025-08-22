package io.composeflow.model.project

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.composeflow.kotlinpoet.wrapper.CodeBlockWrapper
import io.composeflow.kotlinpoet.wrapper.ParameterSpecWrapper
import io.composeflow.asVariableName
import io.composeflow.editor.validator.FloatValidator
import io.composeflow.editor.validator.IntValidator
import io.composeflow.kotlinpoet.KOTLINPOET_COLUMN_LIMIT
import io.composeflow.model.parameter.lazylist.LazyListChildParams
import io.composeflow.model.project.appscreen.screen.composenode.ComposeNode
import io.composeflow.model.property.AssignableProperty
import io.composeflow.model.property.BooleanProperty
import io.composeflow.model.property.FloatProperty
import io.composeflow.model.property.IntProperty
import io.composeflow.model.property.StringProperty
import io.composeflow.model.type.ComposeFlowType
import io.composeflow.ui.modifier.hoverOverlay
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlin.uuid.Uuid

typealias ParameterId = String

@Serializable
@SerialName("ParameterWrapper")
sealed interface ParameterWrapper<T> {
    val id: ParameterId
    val parameterType: ComposeFlowType
    val defaultValue: T
    val defaultValueAsAssignableProperty: AssignableProperty?

    val variableName: String

    fun defaultValueAsCodeBlock(project: Project): CodeBlockWrapper

    @Serializable
    @SerialName("StringParameter")
    data class StringParameter(
        override val id: ParameterId = Uuid.random().toString(),
        private val name: String,
        override val parameterType: ComposeFlowType = ComposeFlowType.StringType(),
        override val defaultValue: String = "",
        @Transient
        override val defaultValueAsAssignableProperty: AssignableProperty =
            StringProperty.StringIntrinsicValue(
                defaultValue,
            ),
    ) : ParameterWrapper<String> {
        @Transient
        override val variableName: String = name.asVariableName()

        override fun defaultValueAsCodeBlock(project: Project): CodeBlockWrapper =
            if (defaultValue.contains("\n") ||
                defaultValue.contains("\r") ||
                defaultValue.length >= KOTLINPOET_COLUMN_LIMIT
            ) {
                // It looks like Kotlinpoet's column limit is hard-coded as 100.
                // That means a string more than 100 characters can be translated as multiline
                // string unintentionally.
                CodeBlockWrapper.of("\"\"\"${defaultValue}\"\"\"")
            } else {
                CodeBlockWrapper.of("\"$defaultValue\"")
            }
    }

    @Serializable
    @SerialName("IntParameter")
    data class IntParameter(
        override val id: ParameterId = Uuid.random().toString(),
        private val name: String,
        override val defaultValue: Int = 0,
        @Transient
        override val defaultValueAsAssignableProperty: AssignableProperty =
            IntProperty.IntIntrinsicValue(
                defaultValue,
            ),
        override val parameterType: ComposeFlowType = ComposeFlowType.IntType(),
    ) : ParameterWrapper<Int> {
        @Transient
        override val variableName: String = name.asVariableName()

        override fun defaultValueAsCodeBlock(project: Project): CodeBlockWrapper = CodeBlockWrapper.of("$defaultValue")
    }

    @Serializable
    @SerialName("FloatParameter")
    data class FloatParameter(
        override val id: ParameterId = Uuid.random().toString(),
        private val name: String,
        override val defaultValue: Float = 0f,
        @Transient
        override val defaultValueAsAssignableProperty: AssignableProperty =
            FloatProperty.FloatIntrinsicValue(
                defaultValue,
            ),
        override val parameterType: ComposeFlowType = ComposeFlowType.FloatType(),
    ) : ParameterWrapper<Float> {
        @Transient
        override val variableName: String = name.asVariableName()

        override fun defaultValueAsCodeBlock(project: Project): CodeBlockWrapper = CodeBlockWrapper.of("${defaultValue}f")
    }

    @Serializable
    @SerialName("BooleanParameter")
    data class BooleanParameter(
        override val id: ParameterId = Uuid.random().toString(),
        private val name: String,
        override val defaultValue: Boolean = false,
        @Transient
        override val defaultValueAsAssignableProperty: AssignableProperty =
            BooleanProperty.BooleanIntrinsicValue(
                defaultValue,
            ),
        override val parameterType: ComposeFlowType = ComposeFlowType.BooleanType(),
    ) : ParameterWrapper<Boolean> {
        @Transient
        override val variableName: String = name.asVariableName()

        override fun defaultValueAsCodeBlock(project: Project): CodeBlockWrapper = CodeBlockWrapper.of("$defaultValue")
    }

    fun generateArgumentParameterSpec(project: Project): ParameterSpecWrapper =
        ParameterSpecWrapper
            .builder(
                variableName,
                parameterType.asKotlinPoetTypeName(project),
            ).defaultValue(defaultValueAsCodeBlock(project))
            .build()

    companion object {
        fun entries(): List<ParameterWrapper<*>> =
            listOf(
                StringParameter(name = ""),
                IntParameter(name = ""),
                FloatParameter(name = ""),
                BooleanParameter(name = ""),
            )
    }
}

fun <T> ParameterWrapper<T>.copy(
    newId: ParameterId = id,
    newName: String = variableName,
    newType: ComposeFlowType = parameterType,
    newDefaultValue: T = defaultValue,
): ParameterWrapper<*> =
    when (newType) {
        is ComposeFlowType.BooleanType -> {
            ParameterWrapper.BooleanParameter(
                id = newId,
                name = newName,
                defaultValue = newDefaultValue as? Boolean ?: false,
                parameterType = newType,
            )
        }

        is ComposeFlowType.StringType -> {
            ParameterWrapper.StringParameter(
                id = newId,
                name = newName,
                defaultValue = newDefaultValue as? String ?: "",
                parameterType = newType,
            )
        }

        is ComposeFlowType.IntType -> {
            ParameterWrapper.IntParameter(
                id = newId,
                name = newName,
                defaultValue = newDefaultValue as? Int ?: 0,
                parameterType = newType,
            )
        }

        is ComposeFlowType.FloatType -> {
            ParameterWrapper.FloatParameter(
                id = newId,
                name = newName,
                defaultValue = newDefaultValue as? Float ?: 0f,
                parameterType = newType,
            )
        }

        is ComposeFlowType.InstantType -> throw IllegalArgumentException("Instant type isn't supported for parameter")
        is ComposeFlowType.Color -> throw IllegalArgumentException("Color type isn't supported for parameter")
        is ComposeFlowType.Brush -> throw IllegalArgumentException("Brush type isn't supported for parameter")
        is ComposeFlowType.Enum<*> -> throw IllegalArgumentException("Enum type isn't supported for parameter")
        is ComposeFlowType.CustomDataType -> throw IllegalArgumentException("Custom data type isn't supported for parameter")
        is ComposeFlowType.JsonElementType -> throw IllegalArgumentException("JsonElement type isn't supported for parameter")
        is ComposeFlowType.AnyType -> throw IllegalArgumentException("Any type isn't supported for parameter")
        is ComposeFlowType.DocumentIdType -> throw IllegalArgumentException("DocumentId type isn't supported for parameter")
        is ComposeFlowType.UnknownType -> throw IllegalArgumentException("")
    }

@Composable
fun ParameterEditor(
    project: Project,
    node: ComposeNode,
    parameter: ParameterWrapper<*>,
    initialProperty: AssignableProperty?,
    onValidPropertyChanged: (AssignableProperty, lazyListSource: LazyListChildParams?) -> Unit,
    onInitializeProperty: () -> Unit,
) {
    when (parameter) {
        is ParameterWrapper.StringParameter -> {
            parameter.parameterType.defaultValue().Editor(
                project = project,
                node = node,
                initialProperty = initialProperty,
                label = parameter.variableName,
                onValidPropertyChanged = onValidPropertyChanged,
                modifier = Modifier.hoverOverlay(),
                destinationStateId = null,
                onInitializeProperty = onInitializeProperty,
                validateInput = null,
                editable = true,
                functionScopeProperties = emptyList(),
            )
        }

        is ParameterWrapper.IntParameter -> {
            parameter.parameterType.defaultValue().Editor(
                project = project,
                node = node,
                initialProperty = initialProperty,
                label = parameter.variableName,
                onValidPropertyChanged = onValidPropertyChanged,
                modifier = Modifier.hoverOverlay(),
                destinationStateId = null,
                onInitializeProperty = onInitializeProperty,
                validateInput = IntValidator()::validate,
                editable = true,
                functionScopeProperties = emptyList(),
            )
        }

        is ParameterWrapper.FloatParameter -> {
            parameter.parameterType.defaultValue().Editor(
                project = project,
                node = node,
                initialProperty = initialProperty,
                label = parameter.variableName,
                onValidPropertyChanged = onValidPropertyChanged,
                modifier = Modifier.hoverOverlay(),
                destinationStateId = null,
                onInitializeProperty = onInitializeProperty,
                validateInput = FloatValidator()::validate,
                editable = true,
                functionScopeProperties = emptyList(),
            )
        }

        is ParameterWrapper.BooleanParameter -> {
            parameter.parameterType.defaultValue().Editor(
                project = project,
                node = node,
                initialProperty = initialProperty,
                label = parameter.variableName,
                onValidPropertyChanged = onValidPropertyChanged,
                modifier = Modifier.hoverOverlay(),
                destinationStateId = null,
                onInitializeProperty = onInitializeProperty,
                validateInput = null,
                editable = true,
                functionScopeProperties = emptyList(),
            )
        }
    }
}
