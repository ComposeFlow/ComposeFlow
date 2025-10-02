@file:OptIn(kotlin.time.ExperimentalTime::class)

package io.composeflow.model.state

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.capitalize
import androidx.compose.ui.text.intl.Locale
import io.composeflow.ViewModelConstant
import io.composeflow.asVariableName
import io.composeflow.kotlinpoet.ClassHolder
import io.composeflow.kotlinpoet.GenerationContext
import io.composeflow.kotlinpoet.MemberHolder
import io.composeflow.kotlinpoet.wrapper.ClassNameWrapper
import io.composeflow.kotlinpoet.wrapper.CodeBlockWrapper
import io.composeflow.kotlinpoet.wrapper.FunSpecWrapper
import io.composeflow.kotlinpoet.wrapper.KModifierWrapper
import io.composeflow.kotlinpoet.wrapper.ParameterSpecWrapper
import io.composeflow.kotlinpoet.wrapper.PropertySpecWrapper
import io.composeflow.kotlinpoet.wrapper.asTypeNameWrapper
import io.composeflow.kotlinpoet.wrapper.controlFlow
import io.composeflow.kotlinpoet.wrapper.launchCoroutineIfNeeded
import io.composeflow.kotlinpoet.wrapper.parameterizedBy
import io.composeflow.model.datatype.DataType
import io.composeflow.model.datatype.DataTypeDefaultValue
import io.composeflow.model.datatype.DataTypeId
import io.composeflow.model.datatype.EmptyDataType
import io.composeflow.model.parameter.wrapper.InstantWrapper
import io.composeflow.model.project.CanvasEditable
import io.composeflow.model.project.Project
import io.composeflow.model.project.findComposeNodeOrNull
import io.composeflow.model.project.findDataTypeOrThrow
import io.composeflow.model.project.findLocalStateOrNull
import io.composeflow.model.property.AssignableProperty
import io.composeflow.model.type.ComposeFlowType
import io.composeflow.model.type.convertCodeFromType
import io.composeflow.serializer.FallbackInstantSerializer
import io.composeflow.ui.propertyeditor.DropdownItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.time.Clock
import kotlin.time.Instant
import kotlin.uuid.Uuid

typealias StateId = String

@Serializable
sealed interface ReadableState {
    val id: StateId
    var name: String
    val isList: Boolean

    fun generateVariableInitializationBlock(
        project: Project,
        context: GenerationContext,
        dryRun: Boolean,
    ): CodeBlockWrapper

    fun generateReadBlock(
        project: Project,
        context: GenerationContext,
        dryRun: Boolean,
    ): CodeBlockWrapper

    fun getReadVariableName(
        project: Project,
        context: GenerationContext,
    ): String = getUniqueIdentifier(context).asVariableName()

    fun getFlowName(context: GenerationContext): String = getUniqueIdentifier(context).asVariableName() + "Flow"

    /**
     * [asNonList] If set to true, returns the result type with [ComposeFlowType.isList] = false regardless of the
     * actual [ComposeFlowType.isList] property
     */
    fun valueType(
        project: Project,
        asNonList: Boolean = false,
    ): ComposeFlowType

    fun generateStatePropertiesToViewModel(
        project: Project,
        context: GenerationContext,
    ): List<PropertySpecWrapper> = emptyList()

    fun getUniqueIdentifier(context: GenerationContext): String =
        context.getCurrentComposableContext().getOrAddIdentifier(
            id = id,
            initialIdentifier = name.asVariableName(),
        )
}

@Serializable
sealed interface WriteableState : ReadableState {
    /**
     * Represents if the state is writable in the Set state action.
     * For example, some states are not intended to be writable from the Set state action,
     * instead, it should be changed through a specific action such as "Open date picker" action.
     */
    val userWritable: Boolean

    fun generateWriteBlock(
        project: Project,
        canvasEditable: CanvasEditable,
        context: GenerationContext,
        dryRun: Boolean,
    ): CodeBlockWrapper

    fun getUpdateMethodName(context: GenerationContext): String

    fun generateUpdateStateMethodToViewModel(context: GenerationContext): FunSpecWrapper

    fun generateUpdateStateCodeToViewModel(
        project: Project,
        context: GenerationContext,
        readProperty: AssignableProperty,
        dryRun: Boolean,
    ): CodeBlockWrapper

    fun generateClearStateCodeToViewModel(context: GenerationContext): CodeBlockWrapper

    override fun generateStatePropertiesToViewModel(
        project: Project,
        context: GenerationContext,
    ): List<PropertySpecWrapper>

    fun getValidateResultName(context: GenerationContext): String = getUniqueIdentifier(context).asVariableName() + "ValidateResult"
}

@Serializable
sealed interface State<T> :
    ReadableState,
    WriteableState {
    override val id: StateId
    override var name: String
    override val isList: Boolean

    override fun generateVariableInitializationBlock(
        project: Project,
        context: GenerationContext,
        dryRun: Boolean,
    ): CodeBlockWrapper

    override fun generateReadBlock(
        project: Project,
        context: GenerationContext,
        dryRun: Boolean,
    ): CodeBlockWrapper

    override val userWritable: Boolean

    override fun valueType(
        project: Project,
        asNonList: Boolean,
    ): ComposeFlowType

    override fun generateWriteBlock(
        project: Project,
        canvasEditable: CanvasEditable,
        context: GenerationContext,
        dryRun: Boolean,
    ): CodeBlockWrapper

    override fun getUpdateMethodName(context: GenerationContext): String {
        val uniqueIdentifier = getUniqueIdentifier(context)
        return "on${uniqueIdentifier.asVariableName().capitalize(Locale.current)}Updated"
    }

    override fun generateUpdateStateMethodToViewModel(context: GenerationContext): FunSpecWrapper

    override fun generateUpdateStateCodeToViewModel(
        project: Project,
        context: GenerationContext,
        readProperty: AssignableProperty,
        dryRun: Boolean,
    ): CodeBlockWrapper

    override fun generateClearStateCodeToViewModel(context: GenerationContext): CodeBlockWrapper

    override fun generateStatePropertiesToViewModel(
        project: Project,
        context: GenerationContext,
    ): List<PropertySpecWrapper>

    val defaultValue: T?
}

sealed interface BooleanState : State<Boolean> {
    fun generateToggleStateCodeToViewModel(context: GenerationContext): CodeBlockWrapper
}

sealed interface ListAppState {
    fun generateStateProperty(
        propertyName: String,
        stateName: String,
        typeClassName: ClassNameWrapper,
    ): PropertySpecWrapper =
        PropertySpecWrapper
            .builder(
                propertyName,
                ClassHolder.Coroutines.Flow.StateFlow.parameterizedBy(
                    ClassHolder.Collections.List.parameterizedBy(typeClassName),
                ),
            ).initializer(
                """
                       ${ViewModelConstant.flowSettings.name}.%M(
                         "$stateName",
                         defaultValue = "[]",
                       ).%M {
                            ${ViewModelConstant.jsonSerializer.name}.decodeFromString<%T<%T>>(it)
                       }
                       .%M(
                         scope = %M,
                         started = %M.WhileSubscribed(5_000),
                         initialValue = emptyList(),
                       )
                    """,
                MemberHolder.Settings.getStringFlow,
                MemberHolder.Coroutines.Flow.map,
                ClassHolder.Collections.List,
                typeClassName,
                MemberHolder.Coroutines.Flow.stateIn,
                MemberHolder.PreCompose.viewModelScope,
                MemberHolder.Coroutines.Flow.SharingStarted,
            ).build()

    fun generateClearStateCode(
        context: GenerationContext,
        stateName: String,
    ): CodeBlockWrapper {
        val builder = CodeBlockWrapper.builder()
        builder.launchCoroutineIfNeeded(context) {
            builder.addStatement("${ViewModelConstant.flowSettings.name}.putString(%S, %S)", stateName, "[]")
        }
        return builder.build()
    }

    fun generateAddValueToStateCode(
        project: Project,
        context: GenerationContext,
        readProperty: AssignableProperty,
        dryRun: Boolean,
    ): CodeBlockWrapper

    fun generateRemoveFirstValueCode(
        project: Project,
        context: GenerationContext,
        writeState: WriteableState,
    ): CodeBlockWrapper {
        val builder = CodeBlockWrapper.builder()
        builder.launchCoroutineIfNeeded(context) {
            builder.addStatement("val list = ${writeState.getFlowName(context)}.value.toMutableList()")
            builder.controlFlow("if (list.isNotEmpty())") {
                builder.addStatement("list.removeFirst()")
            }
            builder.addStatement(
                "${ViewModelConstant.flowSettings.name}.putString(%S, ${ViewModelConstant.jsonSerializer.name}.%M(list))",
                writeState.name,
                MemberHolder.Serialization.encodeToString,
            )
        }
        return builder.build()
    }

    fun generateRemoveLastValueCode(
        project: Project,
        context: GenerationContext,
        writeState: WriteableState,
    ): CodeBlockWrapper {
        val builder = CodeBlockWrapper.builder()
        builder.launchCoroutineIfNeeded(context) {
            builder.addStatement("val list = ${writeState.getFlowName(context)}.value.toMutableList()")
            builder.controlFlow("if (list.isNotEmpty())") {
                builder.addStatement("list.removeLast()")
            }
            builder.addStatement(
                "${ViewModelConstant.flowSettings.name}.putString(%S, ${ViewModelConstant.jsonSerializer.name}.%M(list))",
                writeState.name,
                MemberHolder.Serialization.encodeToString,
            )
        }
        return builder.build()
    }

    fun generateRemoveValueAtIndexFun(
        project: Project,
        context: GenerationContext,
        functionName: String,
        writeState: WriteableState,
    ): FunSpecWrapper {
        val indexToRemove = "indexToRemove"
        val builder = CodeBlockWrapper.builder()
        builder.addStatement("val list = ${writeState.getFlowName(context)}.value.toMutableList()")
        builder.addStatement("if ($indexToRemove < 0 || $indexToRemove >= list.size) return")
        builder.launchCoroutineIfNeeded(context) {
            builder.controlFlow("if (list.isNotEmpty())") {
                builder.addStatement("list.removeAt($indexToRemove)")
            }
            builder.addStatement(
                "${ViewModelConstant.flowSettings.name}.putString(%S, ${ViewModelConstant.jsonSerializer.name}.%M(list))",
                writeState.name,
                MemberHolder.Serialization.encodeToString,
            )
        }
        return FunSpecWrapper
            .builder(functionName)
            .addParameter(
                ParameterSpecWrapper
                    .builder(name = indexToRemove, Int::class.asTypeNameWrapper())
                    .build(),
            ).addCode(builder.build())
            .build()
    }

    fun generateUpdateValueAtIndexFunBuilder(
        project: Project,
        context: GenerationContext,
        functionName: String,
        readCodeBlock: CodeBlockWrapper,
        writeState: WriteableState,
    ): FunSpecWrapper {
        val indexToUpdate = "indexToUpdate"
        val builder = CodeBlockWrapper.builder()
        builder.addStatement("val list = ${writeState.getFlowName(context)}.value.toMutableList()")
        builder.addStatement("if ($indexToUpdate < 0 || $indexToUpdate >= list.size) return")
        builder.launchCoroutineIfNeeded(context) {
            builder.addStatement("list.set($indexToUpdate, $readCodeBlock)")
            builder.addStatement(
                "${ViewModelConstant.flowSettings.name}.putString(%S, ${ViewModelConstant.jsonSerializer.name}.%M(list))",
                writeState.name,
                MemberHolder.Serialization.encodeToString,
            )
        }
        return FunSpecWrapper
            .builder(functionName)
            .addParameter(
                ParameterSpecWrapper
                    .builder(name = indexToUpdate, Int::class.asTypeNameWrapper())
                    .build(),
            ).addCode(builder.build())
            .build()
    }
}

@Serializable
sealed interface AppStateWithDataTypeId {
    val dataTypeId: DataTypeId
}

@Serializable
sealed interface ScreenState<T> : State<T> {
    /**
     * Get the Id of the companion node where the states
     */
    val companionNodeId: String?

    override fun generateVariableInitializationBlock(
        project: Project,
        context: GenerationContext,
        dryRun: Boolean,
    ): CodeBlockWrapper {
        val builder = CodeBlockWrapper.builder()
        val variableName = getReadVariableName(project, context)
        builder.addStatement(
            "val $variableName by ${context.currentEditable.viewModelName}.${getFlowName(context)}.%M()",
            MemberHolder.AndroidX.Runtime.collectAsState,
        )
        return builder.build()
    }

    fun generateValidatorInitializationBlock(
        project: Project,
        context: GenerationContext,
        dryRun: Boolean,
    ): CodeBlockWrapper? {
        val companionNode =
            companionNodeId?.let {
                project.findComposeNodeOrNull(it)
            } ?: return null

        val result =
            companionNode.trait.value.getStateValidator()?.let { stateValidator ->
                CodeBlockWrapper.of(
                    """val ${getValidateResultName(context)} by %M(${
                        getReadVariableName(
                            project,
                            context,
                        )
                    }) { %M(
                    ${stateValidator.asCodeBlock(project, context)}.validate(${
                        getReadVariableName(
                            project,
                            context,
                        )
                    })
                ) }""",
                    MemberHolder.AndroidX.Runtime.remember,
                    MemberHolder.AndroidX.Runtime.mutableStateOf,
                )
            }
        return result
    }

    @Serializable
    @SerialName("StringScreenState")
    data class StringScreenState(
        override val id: StateId = Uuid.random().toString(),
        override var name: String,
        override val defaultValue: String = "",
        override val userWritable: Boolean = true,
        override val companionNodeId: String? = null,
    ) : ScreenState<String> {
        override fun generateUpdateStateMethodToViewModel(context: GenerationContext): FunSpecWrapper {
            val flowName = getFlowName(context)
            return FunSpecWrapper
                .builder(getUpdateMethodName(context))
                .addParameter("newValue", String::class.asTypeNameWrapper())
                .addStatement("_$flowName.value = newValue")
                .build()
        }

        override fun generateUpdateStateCodeToViewModel(
            project: Project,
            context: GenerationContext,
            readProperty: AssignableProperty,
            dryRun: Boolean,
        ): CodeBlockWrapper {
            val builder = CodeBlockWrapper.builder()
            readProperty
                .transformedCodeBlock(project, context, dryRun = dryRun)
                .let { readExpression ->
                    val expression =
                        ComposeFlowType.StringType().convertCodeFromType(
                            inputType = readProperty.valueType(project),
                            codeBlock = readExpression,
                        )
                    builder.add("_${getFlowName(context)}.value = ")
                    builder.add(expression)
                }
            return builder.build()
        }

        override fun generateClearStateCodeToViewModel(context: GenerationContext): CodeBlockWrapper =
            CodeBlockWrapper.of(
                """
                    _${getFlowName(context)}.value = "$defaultValue"
                """,
            )

        override fun generateStatePropertiesToViewModel(
            project: Project,
            context: GenerationContext,
        ): List<PropertySpecWrapper> {
            val backingProperty =
                PropertySpecWrapper
                    .builder(
                        "_${getFlowName(context)}",
                        MutableStateFlow::class
                            .asTypeNameWrapper()
                            .parameterizedBy(String::class.asTypeNameWrapper()),
                    ).addModifiers(KModifierWrapper.PRIVATE)
                    .initializer("""MutableStateFlow("$defaultValue")""")
                    .build()
            val property =
                PropertySpecWrapper
                    .builder(
                        getFlowName(context),
                        StateFlow::class
                            .asTypeNameWrapper()
                            .parameterizedBy(String::class.asTypeNameWrapper()),
                    ).initializer("""_${getFlowName(context)}""")
                    .build()
            return listOf(
                backingProperty,
                property,
            )
        }

        override fun generateReadBlock(
            project: Project,
            context: GenerationContext,
            dryRun: Boolean,
        ): CodeBlockWrapper {
            val builder = CodeBlockWrapper.builder()
            builder.addStatement(getReadVariableName(project, context))
            return builder.build()
        }

        override fun generateWriteBlock(
            project: Project,
            canvasEditable: CanvasEditable,
            context: GenerationContext,
            dryRun: Boolean,
        ): CodeBlockWrapper {
            val builder = CodeBlockWrapper.builder()
            builder.addStatement(
                "${canvasEditable.viewModelName}.${getUpdateMethodName(context)}(it)",
            )
            return builder.build()
        }

        override fun valueType(
            project: Project,
            asNonList: Boolean,
        ) = ComposeFlowType.StringType(isList = false)

        override val isList: Boolean = false
    }

    @Serializable
    @SerialName("FloatScreenState")
    data class FloatScreenState(
        override val id: StateId = Uuid.random().toString(),
        override var name: String,
        override val defaultValue: Float = 0f,
        override val userWritable: Boolean = true,
        override val companionNodeId: String? = null,
    ) : ScreenState<Float> {
        override fun generateUpdateStateMethodToViewModel(context: GenerationContext): FunSpecWrapper {
            val flowName = getFlowName(context)
            return FunSpecWrapper
                .builder(getUpdateMethodName(context))
                .addParameter("newValue", Float::class.asTypeNameWrapper())
                .addStatement("_$flowName.value = newValue")
                .build()
        }

        override fun generateUpdateStateCodeToViewModel(
            project: Project,
            context: GenerationContext,
            readProperty: AssignableProperty,
            dryRun: Boolean,
        ): CodeBlockWrapper {
            val builder = CodeBlockWrapper.builder()
            readProperty
                .transformedCodeBlock(project, context, dryRun = dryRun)
                .let { readExpression ->
                    val expression =
                        ComposeFlowType.FloatType().convertCodeFromType(
                            inputType = readProperty.valueType(project),
                            codeBlock = readExpression,
                        )
                    builder.add("_${getFlowName(context)}.value = ")
                    builder.add(expression)
                }
            return builder.build()
        }

        override fun generateClearStateCodeToViewModel(context: GenerationContext): CodeBlockWrapper =
            CodeBlockWrapper.of(
                """
                    _${getFlowName(context)}.value = "${defaultValue}f"
                """,
            )

        override fun generateStatePropertiesToViewModel(
            project: Project,
            context: GenerationContext,
        ): List<PropertySpecWrapper> {
            val backingProperty =
                PropertySpecWrapper
                    .builder(
                        "_${getFlowName(context)}",
                        MutableStateFlow::class
                            .asTypeNameWrapper()
                            .parameterizedBy(Float::class.asTypeNameWrapper()),
                    ).addModifiers(KModifierWrapper.PRIVATE)
                    .initializer("""MutableStateFlow(${defaultValue}f)""")
                    .build()
            val property =
                PropertySpecWrapper
                    .builder(
                        getFlowName(context),
                        StateFlow::class
                            .asTypeNameWrapper()
                            .parameterizedBy(Float::class.asTypeNameWrapper()),
                    ).initializer("""_${getFlowName(context)}""")
                    .build()
            return listOf(
                backingProperty,
                property,
            )
        }

        override fun generateReadBlock(
            project: Project,
            context: GenerationContext,
            dryRun: Boolean,
        ): CodeBlockWrapper {
            val builder = CodeBlockWrapper.builder()
            builder.addStatement(getReadVariableName(project, context))
            return builder.build()
        }

        override fun generateWriteBlock(
            project: Project,
            canvasEditable: CanvasEditable,
            context: GenerationContext,
            dryRun: Boolean,
        ): CodeBlockWrapper {
            val builder = CodeBlockWrapper.builder()
            builder.addStatement(
                "${canvasEditable.viewModelName}.${getUpdateMethodName(context)}(it)",
            )
            return builder.build()
        }

        override fun valueType(
            project: Project,
            asNonList: Boolean,
        ) = ComposeFlowType.FloatType(isList = false)

        override val isList: Boolean = false
    }

    @Serializable
    @SerialName("BooleanScreenState")
    data class BooleanScreenState(
        override val id: StateId = Uuid.random().toString(),
        override var name: String,
        override val defaultValue: Boolean = false,
        override val userWritable: Boolean = true,
        override val companionNodeId: String? = null,
    ) : ScreenState<Boolean>,
        BooleanState {
        override fun generateUpdateStateMethodToViewModel(context: GenerationContext): FunSpecWrapper {
            val flowName = getFlowName(context)
            return FunSpecWrapper
                .builder(getUpdateMethodName(context))
                .addParameter("newValue", Boolean::class.asTypeNameWrapper())
                .addStatement("_$flowName.value = newValue")
                .build()
        }

        override fun generateUpdateStateCodeToViewModel(
            project: Project,
            context: GenerationContext,
            readProperty: AssignableProperty,
            dryRun: Boolean,
        ): CodeBlockWrapper {
            val builder = CodeBlockWrapper.builder()
            readProperty
                .transformedCodeBlock(project, context, dryRun = dryRun)
                .let { readExpression ->
                    val expression =
                        ComposeFlowType.BooleanType().convertCodeFromType(
                            inputType = readProperty.valueType(project),
                            codeBlock = readExpression,
                        )
                    builder.add("_${getFlowName(context)}.value = ")
                    builder.add(expression)
                }
            return builder.build()
        }

        override fun generateClearStateCodeToViewModel(context: GenerationContext): CodeBlockWrapper =
            CodeBlockWrapper.of(
                """
                    _${getFlowName(context)}.value = $defaultValue
                """,
            )

        override fun generateStatePropertiesToViewModel(
            project: Project,
            context: GenerationContext,
        ): List<PropertySpecWrapper> {
            val backingProperty =
                PropertySpecWrapper
                    .builder(
                        "_${getFlowName(context)}",
                        MutableStateFlow::class
                            .asTypeNameWrapper()
                            .parameterizedBy(Boolean::class.asTypeNameWrapper()),
                    ).addModifiers(KModifierWrapper.PRIVATE)
                    .initializer("""MutableStateFlow($defaultValue)""")
                    .build()
            val property =
                PropertySpecWrapper
                    .builder(
                        getFlowName(context),
                        StateFlow::class
                            .asTypeNameWrapper()
                            .parameterizedBy(Boolean::class.asTypeNameWrapper()),
                    ).initializer("""_${getFlowName(context)}""")
                    .build()
            return listOf(
                backingProperty,
                property,
            )
        }

        override fun generateToggleStateCodeToViewModel(context: GenerationContext): CodeBlockWrapper =
            CodeBlockWrapper.of(
                """
                    _${getFlowName(context)}.value = !${getFlowName(context)}.value
                """,
            )

        override fun generateReadBlock(
            project: Project,
            context: GenerationContext,
            dryRun: Boolean,
        ): CodeBlockWrapper {
            val builder = CodeBlockWrapper.builder()
            builder.addStatement(getReadVariableName(project, context))
            return builder.build()
        }

        override fun generateWriteBlock(
            project: Project,
            canvasEditable: CanvasEditable,
            context: GenerationContext,
            dryRun: Boolean,
        ): CodeBlockWrapper {
            val builder = CodeBlockWrapper.builder()
            builder.addStatement(
                "${canvasEditable.viewModelName}.${getUpdateMethodName(context)}(it)",
            )
            return builder.build()
        }

        override fun valueType(
            project: Project,
            asNonList: Boolean,
        ): ComposeFlowType = ComposeFlowType.BooleanType(isList = false)

        override val isList: Boolean = false
    }

    @Serializable
    @SerialName("IntScreenState")
    data class IntScreenState(
        override val id: StateId = Uuid.random().toString(),
        override var name: String,
        override val defaultValue: Int = 0,
        override val userWritable: Boolean = true,
        override val companionNodeId: String? = null,
    ) : ScreenState<Int> {
        override fun generateUpdateStateMethodToViewModel(context: GenerationContext): FunSpecWrapper {
            val flowName = getFlowName(context)
            return FunSpecWrapper
                .builder(getUpdateMethodName(context))
                .addParameter("newValue", Int::class.asTypeNameWrapper())
                .addStatement("_$flowName.value = newValue")
                .build()
        }

        override fun generateUpdateStateCodeToViewModel(
            project: Project,
            context: GenerationContext,
            readProperty: AssignableProperty,
            dryRun: Boolean,
        ): CodeBlockWrapper {
            val builder = CodeBlockWrapper.builder()
            readProperty
                .transformedCodeBlock(project, context, dryRun = dryRun)
                .let { readExpression ->
                    val expression =
                        ComposeFlowType.IntType().convertCodeFromType(
                            inputType = readProperty.valueType(project),
                            codeBlock = readExpression,
                        )
                    builder.add("_${getFlowName(context)}.value = ")
                    builder.add(expression)
                }
            return builder.build()
        }

        override fun generateClearStateCodeToViewModel(context: GenerationContext): CodeBlockWrapper =
            CodeBlockWrapper.of(
                """
                    _${getFlowName(context)}.value = $defaultValue
                """,
            )

        override fun generateStatePropertiesToViewModel(
            project: Project,
            context: GenerationContext,
        ): List<PropertySpecWrapper> {
            val backingProperty =
                PropertySpecWrapper
                    .builder(
                        "_${getFlowName(context)}",
                        MutableStateFlow::class
                            .asTypeNameWrapper()
                            .parameterizedBy(Int::class.asTypeNameWrapper()),
                    ).addModifiers(KModifierWrapper.PRIVATE)
                    .initializer("""MutableStateFlow($defaultValue)""")
                    .build()
            val property =
                PropertySpecWrapper
                    .builder(
                        getFlowName(context),
                        StateFlow::class
                            .asTypeNameWrapper()
                            .parameterizedBy(Int::class.asTypeNameWrapper()),
                    ).initializer("""_${getFlowName(context)}""")
                    .build()
            return listOf(
                backingProperty,
                property,
            )
        }

        override fun generateReadBlock(
            project: Project,
            context: GenerationContext,
            dryRun: Boolean,
        ): CodeBlockWrapper {
            val builder = CodeBlockWrapper.builder()
            builder.addStatement(getReadVariableName(project, context))
            return builder.build()
        }

        override fun generateWriteBlock(
            project: Project,
            canvasEditable: CanvasEditable,
            context: GenerationContext,
            dryRun: Boolean,
        ): CodeBlockWrapper {
            val builder = CodeBlockWrapper.builder()
            builder.addStatement(
                "${canvasEditable.viewModelName}.${getUpdateMethodName(context)}(it)",
            )
            return builder.build()
        }

        override fun valueType(
            project: Project,
            asNonList: Boolean,
        ) = ComposeFlowType.IntType(isList = false)

        override val isList: Boolean = false
    }

    @Serializable
    @SerialName("InstantScreenState")
    data class InstantScreenState(
        override val id: StateId = Uuid.random().toString(),
        override var name: String,
        @Serializable(FallbackInstantSerializer::class)
        override val defaultValue: Instant = Clock.System.now(),
        override val userWritable: Boolean = true,
        override val companionNodeId: String? = null,
    ) : ScreenState<Instant> {
        override fun generateUpdateStateCodeToViewModel(
            project: Project,
            context: GenerationContext,
            readProperty: AssignableProperty,
            dryRun: Boolean,
        ): CodeBlockWrapper {
            val builder = CodeBlockWrapper.builder()
            readProperty
                .transformedCodeBlock(project, context, dryRun = dryRun)
                .let { readExpression ->
                    val expression =
                        ComposeFlowType.InstantType().convertCodeFromType(
                            inputType = readProperty.valueType(project),
                            codeBlock = readExpression,
                        )
                    builder.add("_${getFlowName(context)}.value = ")
                    builder.add(expression)
                }
            return builder.build()
        }

        override fun generateClearStateCodeToViewModel(context: GenerationContext): CodeBlockWrapper =
            CodeBlockWrapper.of(
                """
                    _${getFlowName(context)}.value = $defaultValue
                """,
            )

        override fun generateStatePropertiesToViewModel(
            project: Project,
            context: GenerationContext,
        ): List<PropertySpecWrapper> {
            val backingProperty =
                PropertySpecWrapper
                    .builder(
                        "_${getFlowName(context)}",
                        ClassHolder.Coroutines.Flow.MutableStateFlow
                            .parameterizedBy(Instant::class.asTypeNameWrapper()),
                    ).addModifiers(KModifierWrapper.PRIVATE)
                    .initializer(
                        "%T(%T.System.now())",
                        ClassHolder.Coroutines.Flow.MutableStateFlow,
                        Clock::class.asTypeNameWrapper(),
                    ).build()
            val property =
                PropertySpecWrapper
                    .builder(
                        getFlowName(context),
                        ClassHolder.Coroutines.Flow.StateFlow
                            .parameterizedBy(Instant::class.asTypeNameWrapper()),
                    ).initializer("""_${getFlowName(context)}""")
                    .build()
            return listOf(
                backingProperty,
                property,
            )
        }

        override fun generateReadBlock(
            project: Project,
            context: GenerationContext,
            dryRun: Boolean,
        ): CodeBlockWrapper {
            val builder = CodeBlockWrapper.builder()
            builder.addStatement(getReadVariableName(project, context))
            return builder.build()
        }

        override fun generateWriteBlock(
            project: Project,
            canvasEditable: CanvasEditable,
            context: GenerationContext,
            dryRun: Boolean,
        ): CodeBlockWrapper = CodeBlockWrapper.of("")

        override fun generateUpdateStateMethodToViewModel(context: GenerationContext): FunSpecWrapper {
            val flowName = getFlowName(context)
            return FunSpecWrapper
                .builder(getUpdateMethodName(context))
                .addParameter("newValue", Instant::class.asTypeNameWrapper())
                .addStatement("_$flowName.value = newValue")
                .build()
        }

        override fun valueType(
            project: Project,
            asNonList: Boolean,
        ): ComposeFlowType = ComposeFlowType.InstantType(isList = false)

        override val isList: Boolean = false
    }

    @Serializable
    @SerialName("StringListScreenState")
    data class StringListScreenState(
        override val id: StateId = Uuid.random().toString(),
        override var name: String,
        override val defaultValue: List<String> = listOf(""),
        override val isList: Boolean = true,
        // If set to true, this list only contains a single element.
        // For example, selectedItems in a ChipGroup, this can be set to true when multiselect
        // property is set to false
        val singleValueOnly: Boolean = false,
        override val userWritable: Boolean = true,
        override val companionNodeId: String? = null,
    ) : ScreenState<List<String>> {
        override fun generateUpdateStateCodeToViewModel(
            project: Project,
            context: GenerationContext,
            readProperty: AssignableProperty,
            dryRun: Boolean,
        ): CodeBlockWrapper {
            val builder = CodeBlockWrapper.builder()
            readProperty
                .transformedCodeBlock(project, context, dryRun = dryRun)
                .let { readExpression ->
                    val expression =
                        ComposeFlowType.StringType(isList = true).convertCodeFromType(
                            inputType = readProperty.valueType(project),
                            codeBlock = readExpression,
                        )
                    builder.add("_${getFlowName(context)}.value = ")
                    builder.add(expression)
                }
            return builder.build()
        }

        override fun generateClearStateCodeToViewModel(context: GenerationContext): CodeBlockWrapper =
            CodeBlockWrapper.of(
                """
                    _${getFlowName(context)}.value = emptyList()
                """,
            )

        override fun generateUpdateStateMethodToViewModel(context: GenerationContext): FunSpecWrapper {
            val flowName = getFlowName(context)
            return FunSpecWrapper
                .builder(getUpdateMethodName(context))
                .addParameter(
                    "newValue",
                    List::class
                        .asTypeNameWrapper()
                        .parameterizedBy(String::class.asTypeNameWrapper()),
                ).addStatement("_$flowName.value = newValue")
                .build()
        }

        override fun generateStatePropertiesToViewModel(
            project: Project,
            context: GenerationContext,
        ): List<PropertySpecWrapper> {
            val backingProperty =
                PropertySpecWrapper
                    .builder(
                        "_${getFlowName(context)}",
                        ClassHolder.Coroutines.Flow.MutableStateFlow.parameterizedBy(
                            List::class.asTypeNameWrapper().parameterizedBy(
                                String::class.asTypeNameWrapper(),
                            ),
                        ),
                    ).addModifiers(KModifierWrapper.PRIVATE)
                    .initializer("""MutableStateFlow(List(0) { "" })""")
                    .build()
            val property =
                PropertySpecWrapper
                    .builder(
                        getFlowName(context),
                        ClassHolder.Coroutines.Flow.StateFlow.parameterizedBy(
                            List::class.asTypeNameWrapper().parameterizedBy(
                                String::class.asTypeNameWrapper(),
                            ),
                        ),
                    ).initializer("""_${getFlowName(context)}""")
                    .build()
            return listOf(
                backingProperty,
                property,
            )
        }

        override fun generateReadBlock(
            project: Project,
            context: GenerationContext,
            dryRun: Boolean,
        ): CodeBlockWrapper {
            val builder = CodeBlockWrapper.builder()
            builder.addStatement(getReadVariableName(project, context))
            return builder.build()
        }

        override fun generateWriteBlock(
            project: Project,
            canvasEditable: CanvasEditable,
            context: GenerationContext,
            dryRun: Boolean,
        ): CodeBlockWrapper {
            val builder = CodeBlockWrapper.builder()
            val composeContext = context.getCurrentComposableContext()
            val newItems =
                composeContext.getOrAddIdentifier(
                    id = id,
                    initialIdentifier = "newItems",
                )
            if (singleValueOnly) {
                builder.add(
                    CodeBlockWrapper.of(
                        """
                    val $newItems = ${getReadVariableName(project, context)}.%M()
                    if ($newItems.%M()) {
                        $newItems.clear()
                        $newItems.add(it)
                    } else {
                        $newItems.add(it)
                    }
                    ${canvasEditable.viewModelName}.${getUpdateMethodName(context)}($newItems)
                """,
                        MemberHolder.Kotlin.Collection.toMutableList,
                        MemberHolder.Kotlin.Collection.isNotEmpty,
                    ),
                )
            } else {
                builder.add(
                    CodeBlockWrapper.of(
                        """
                    val $newItems = ${getReadVariableName(project, context)}.%M()
                    if ($newItems.contains(it)) {
                        $newItems.remove(it)
                    } else {
                        $newItems.add(it)
                    }
                    ${canvasEditable.viewModelName}.${getUpdateMethodName(context)}($newItems)
                """,
                        MemberHolder.Kotlin.Collection.toMutableList,
                    ),
                )
            }
            return builder.build()
        }

        override fun valueType(
            project: Project,
            asNonList: Boolean,
        ): ComposeFlowType = ComposeFlowType.StringType(isList = true)
    }
}

@Serializable
sealed interface AppState<T> : State<T> {
    @Serializable
    @SerialName("StringAppState")
    data class StringAppState(
        override val id: StateId = Uuid.random().toString(),
        override var name: String,
        override val defaultValue: String = "",
        override val isList: Boolean = false,
        override val userWritable: Boolean = true,
    ) : AppState<String>,
        DropdownItem {
        override fun generateReadBlock(
            project: Project,
            context: GenerationContext,
            dryRun: Boolean,
        ): CodeBlockWrapper {
            val builder = CodeBlockWrapper.builder()
            builder.addStatement(getReadVariableName(project, context))
            return builder.build()
        }

        override fun generateWriteBlock(
            project: Project,
            canvasEditable: CanvasEditable,
            context: GenerationContext,
            dryRun: Boolean,
        ): CodeBlockWrapper {
            val builder = CodeBlockWrapper.builder()
            builder.addStatement(
                "${canvasEditable.viewModelName}.${getUpdateMethodName(context)}(it)",
            )
            return builder.build()
        }

        override fun generateUpdateStateMethodToViewModel(context: GenerationContext): FunSpecWrapper =
            FunSpecWrapper
                .builder(getUpdateMethodName(context))
                .build()

        override fun valueType(
            project: Project,
            asNonList: Boolean,
        ): ComposeFlowType = ComposeFlowType.StringType(isList = false)

        override fun generateUpdateStateCodeToViewModel(
            project: Project,
            context: GenerationContext,
            readProperty: AssignableProperty,
            dryRun: Boolean,
        ): CodeBlockWrapper {
            val builder = CodeBlockWrapper.builder()
            readProperty
                .transformedCodeBlock(project, context, dryRun = dryRun)
                .let { readExpression ->
                    val expression =
                        ComposeFlowType.StringType().convertCodeFromType(
                            inputType = readProperty.valueType(project),
                            codeBlock = readExpression,
                        )
                    builder.launchCoroutineIfNeeded(context) {
                        builder.add("${ViewModelConstant.flowSettings.name}.putString(%S, ", name)
                        builder.add(expression)
                        builder.add(")\n")
                    }
                }
            return builder.build()
        }

        override fun generateClearStateCodeToViewModel(context: GenerationContext): CodeBlockWrapper {
            val builder = CodeBlockWrapper.builder()
            builder.launchCoroutineIfNeeded(context) {
                builder.addStatement("${ViewModelConstant.flowSettings.name}.putString(%S, %S)", name, defaultValue)
            }
            return builder.build()
        }

        override fun generateStatePropertiesToViewModel(
            project: Project,
            context: GenerationContext,
        ): List<PropertySpecWrapper> =
            listOf(
                PropertySpecWrapper
                    .builder(
                        getFlowName(context),
                        StateFlow::class
                            .asTypeNameWrapper()
                            .parameterizedBy(String::class.asTypeNameWrapper()),
                    ).initializer(
                        """
                       ${ViewModelConstant.flowSettings.name}.%M(
                         "$name",
                         defaultValue = "$defaultValue"
                       )
                       .%M(
                         scope = %M,
                         started = %M.WhileSubscribed(5_000),
                         initialValue = "$defaultValue"
                       )
                    """,
                        MemberHolder.Settings.getStringFlow,
                        MemberHolder.Coroutines.Flow.stateIn,
                        MemberHolder.PreCompose.viewModelScope,
                        MemberHolder.Coroutines.Flow.SharingStarted,
                    ).build(),
            )

        @Composable
        override fun asDropdownText(): AnnotatedString = AnnotatedString("String")

        override fun isSameItem(item: Any): Boolean = item is StringAppState
    }

    @Serializable
    @SerialName("IntAppState")
    data class IntAppState(
        override val id: StateId = Uuid.random().toString(),
        override var name: String,
        override val defaultValue: Int = 0,
        override val isList: Boolean = false,
        override val userWritable: Boolean = true,
    ) : AppState<Int>,
        DropdownItem {
        override fun generateReadBlock(
            project: Project,
            context: GenerationContext,
            dryRun: Boolean,
        ): CodeBlockWrapper = CodeBlockWrapper.of("")

        override fun generateWriteBlock(
            project: Project,
            canvasEditable: CanvasEditable,
            context: GenerationContext,
            dryRun: Boolean,
        ): CodeBlockWrapper = CodeBlockWrapper.of("")

        override fun valueType(
            project: Project,
            asNonList: Boolean,
        ): ComposeFlowType = ComposeFlowType.IntType(isList = false)

        override fun generateUpdateStateCodeToViewModel(
            project: Project,
            context: GenerationContext,
            readProperty: AssignableProperty,
            dryRun: Boolean,
        ): CodeBlockWrapper {
            val builder = CodeBlockWrapper.builder()
            readProperty
                .transformedCodeBlock(project, context, dryRun = dryRun)
                .let { readExpression ->
                    val expression =
                        ComposeFlowType.IntType().convertCodeFromType(
                            inputType = readProperty.valueType(project),
                            codeBlock = readExpression,
                        )
                    builder.launchCoroutineIfNeeded(context) {
                        builder.add("${ViewModelConstant.flowSettings.name}.putInt(%S, ", name)
                        builder.add(expression)
                        builder.add(")\n")
                    }
                }
            return builder.build()
        }

        override fun generateUpdateStateMethodToViewModel(context: GenerationContext): FunSpecWrapper =
            FunSpecWrapper.builder(getUpdateMethodName(context)).build()

        override fun generateClearStateCodeToViewModel(context: GenerationContext): CodeBlockWrapper {
            val builder = CodeBlockWrapper.builder()
            builder.launchCoroutineIfNeeded(context) {
                builder.addStatement("${ViewModelConstant.flowSettings.name}.putInt(%S, %L)", name, defaultValue)
            }
            return builder.build()
        }

        override fun generateStatePropertiesToViewModel(
            project: Project,
            context: GenerationContext,
        ): List<PropertySpecWrapper> =
            listOf(
                PropertySpecWrapper
                    .builder(
                        getFlowName(context),
                        StateFlow::class
                            .asTypeNameWrapper()
                            .parameterizedBy(Int::class.asTypeNameWrapper()),
                    ).initializer(
                        """
                       ${ViewModelConstant.flowSettings.name}.%M(
                         "$name",
                         defaultValue = $defaultValue,
                       )
                       .%M(
                         scope = %M,
                         started = %M.WhileSubscribed(5_000),
                         initialValue = $defaultValue,
                       )
                    """,
                        MemberHolder.Settings.getIntFlow,
                        MemberHolder.Coroutines.Flow.stateIn,
                        MemberHolder.PreCompose.viewModelScope,
                        MemberHolder.Coroutines.Flow.SharingStarted,
                    ).build(),
            )

        @Composable
        override fun asDropdownText(): AnnotatedString = AnnotatedString("Int")

        override fun isSameItem(item: Any): Boolean = item is IntAppState
    }

    @Serializable
    @SerialName("FloatAppState")
    data class FloatAppState(
        override val id: StateId = Uuid.random().toString(),
        override var name: String,
        override val defaultValue: Float = 0f,
        override val isList: Boolean = false,
        override val userWritable: Boolean = true,
    ) : AppState<Float>,
        DropdownItem {
        override fun generateReadBlock(
            project: Project,
            context: GenerationContext,
            dryRun: Boolean,
        ): CodeBlockWrapper = CodeBlockWrapper.of("")

        override fun generateWriteBlock(
            project: Project,
            canvasEditable: CanvasEditable,
            context: GenerationContext,
            dryRun: Boolean,
        ): CodeBlockWrapper = CodeBlockWrapper.of("")

        override fun valueType(
            project: Project,
            asNonList: Boolean,
        ): ComposeFlowType = ComposeFlowType.FloatType(isList = false)

        override fun generateUpdateStateCodeToViewModel(
            project: Project,
            context: GenerationContext,
            readProperty: AssignableProperty,
            dryRun: Boolean,
        ): CodeBlockWrapper {
            val builder = CodeBlockWrapper.builder()
            readProperty
                .transformedCodeBlock(project, context, dryRun = dryRun)
                .let { readExpression ->
                    val expression =
                        ComposeFlowType.FloatType().convertCodeFromType(
                            inputType = readProperty.valueType(project),
                            codeBlock = readExpression,
                        )
                    builder.launchCoroutineIfNeeded(context) {
                        builder.add("${ViewModelConstant.flowSettings.name}.putFloat(%S, ", name)
                        builder.add(expression)
                        builder.add(")\n")
                    }
                }
            return builder.build()
        }

        override fun generateUpdateStateMethodToViewModel(context: GenerationContext): FunSpecWrapper =
            FunSpecWrapper.builder(getUpdateMethodName(context)).build()

        override fun generateClearStateCodeToViewModel(context: GenerationContext): CodeBlockWrapper {
            val builder = CodeBlockWrapper.builder()
            builder.launchCoroutineIfNeeded(context) {
                builder.addStatement("${ViewModelConstant.flowSettings.name}.putFloat(%S, %Lf)", name, defaultValue)
            }
            return builder.build()
        }

        override fun generateStatePropertiesToViewModel(
            project: Project,
            context: GenerationContext,
        ): List<PropertySpecWrapper> =
            listOf(
                PropertySpecWrapper
                    .builder(
                        getFlowName(context),
                        StateFlow::class
                            .asTypeNameWrapper()
                            .parameterizedBy(Float::class.asTypeNameWrapper()),
                    ).initializer(
                        """
                       ${ViewModelConstant.flowSettings.name}.%M(
                         "$name",
                         defaultValue = ${defaultValue}f,
                       )
                       .%M(
                         scope = %M,
                         started = %M.WhileSubscribed(5_000),
                         initialValue = ${defaultValue}f,
                       )
                    """,
                        MemberHolder.Settings.getFloatFlow,
                        MemberHolder.Coroutines.Flow.stateIn,
                        MemberHolder.PreCompose.viewModelScope,
                        MemberHolder.Coroutines.Flow.SharingStarted,
                    ).build(),
            )

        @Composable
        override fun asDropdownText(): AnnotatedString = AnnotatedString("Float")

        override fun isSameItem(item: Any): Boolean = item is FloatAppState
    }

    @Serializable
    @SerialName("BooleanAppState")
    data class BooleanAppState(
        override val id: StateId = Uuid.random().toString(),
        override var name: String,
        override val defaultValue: Boolean = false,
        override val isList: Boolean = false,
        override val userWritable: Boolean = true,
    ) : AppState<Boolean>,
        BooleanState,
        DropdownItem {
        override fun generateReadBlock(
            project: Project,
            context: GenerationContext,
            dryRun: Boolean,
        ): CodeBlockWrapper = CodeBlockWrapper.of("")

        override fun generateWriteBlock(
            project: Project,
            canvasEditable: CanvasEditable,
            context: GenerationContext,
            dryRun: Boolean,
        ): CodeBlockWrapper = CodeBlockWrapper.of("")

        override fun valueType(
            project: Project,
            asNonList: Boolean,
        ): ComposeFlowType = ComposeFlowType.BooleanType(isList = false)

        override fun generateUpdateStateCodeToViewModel(
            project: Project,
            context: GenerationContext,
            readProperty: AssignableProperty,
            dryRun: Boolean,
        ): CodeBlockWrapper {
            val builder = CodeBlockWrapper.builder()
            readProperty
                .transformedCodeBlock(project, context, dryRun = dryRun)
                .let { readExpression ->
                    val expression =
                        ComposeFlowType.BooleanType().convertCodeFromType(
                            inputType = readProperty.valueType(project),
                            codeBlock = readExpression,
                        )
                    builder.launchCoroutineIfNeeded(context) {
                        builder.add("${ViewModelConstant.flowSettings.name}.putBoolean(%S, ", name)
                        builder.add(expression)
                        builder.add(")\n")
                    }
                }
            return builder.build()
        }

        override fun generateUpdateStateMethodToViewModel(context: GenerationContext): FunSpecWrapper =
            FunSpecWrapper.builder(getUpdateMethodName(context)).build()

        override fun generateClearStateCodeToViewModel(context: GenerationContext): CodeBlockWrapper {
            val builder = CodeBlockWrapper.builder()
            builder.launchCoroutineIfNeeded(context) {
                builder.addStatement("${ViewModelConstant.flowSettings.name}.putBoolean(%S, %L)", name, defaultValue)
            }
            return builder.build()
        }

        override fun generateToggleStateCodeToViewModel(context: GenerationContext): CodeBlockWrapper {
            val builder = CodeBlockWrapper.builder()
            builder.launchCoroutineIfNeeded(context) {
                builder.addStatement(
                    "${ViewModelConstant.flowSettings.name}.putBoolean(%S, !${getFlowName(context)}.value)",
                    name,
                )
            }
            return builder.build()
        }

        override fun generateStatePropertiesToViewModel(
            project: Project,
            context: GenerationContext,
        ): List<PropertySpecWrapper> =
            listOf(
                PropertySpecWrapper
                    .builder(
                        getFlowName(context),
                        StateFlow::class
                            .asTypeNameWrapper()
                            .parameterizedBy(Boolean::class.asTypeNameWrapper()),
                    ).initializer(
                        """
                       ${ViewModelConstant.flowSettings.name}.%M(
                         "$name",
                         defaultValue = $defaultValue,
                       )
                       .%M(
                         scope = %M,
                         started = %M.WhileSubscribed(5_000),
                         initialValue = $defaultValue,
                       )
                    """,
                        MemberHolder.Settings.getBooleanFlow,
                        MemberHolder.Coroutines.Flow.stateIn,
                        MemberHolder.PreCompose.viewModelScope,
                        MemberHolder.Coroutines.Flow.SharingStarted,
                    ).build(),
            )

        @Composable
        override fun asDropdownText(): AnnotatedString = AnnotatedString("Boolean")

        override fun isSameItem(item: Any): Boolean = item is BooleanAppState
    }

    @Serializable
    @SerialName("InstantAppState")
    data class InstantAppState(
        override val id: StateId = Uuid.random().toString(),
        override var name: String,
        override val defaultValue: InstantWrapper = InstantWrapper(null),
        override val isList: Boolean = false,
        override val userWritable: Boolean = true,
    ) : AppState<InstantWrapper>,
        DropdownItem {
        override fun generateReadBlock(
            project: Project,
            context: GenerationContext,
            dryRun: Boolean,
        ): CodeBlockWrapper = CodeBlockWrapper.of("")

        override fun generateWriteBlock(
            project: Project,
            canvasEditable: CanvasEditable,
            context: GenerationContext,
            dryRun: Boolean,
        ): CodeBlockWrapper = CodeBlockWrapper.of("")

        override fun valueType(
            project: Project,
            asNonList: Boolean,
        ): ComposeFlowType = ComposeFlowType.InstantType(isList = false)

        override fun generateUpdateStateCodeToViewModel(
            project: Project,
            context: GenerationContext,
            readProperty: AssignableProperty,
            dryRun: Boolean,
        ): CodeBlockWrapper {
            val builder = CodeBlockWrapper.builder()
            readProperty
                .transformedCodeBlock(project, context, dryRun = dryRun)
                .let { readExpression ->
                    val expression =
                        ComposeFlowType.InstantType().convertCodeFromType(
                            inputType = readProperty.valueType(project),
                            codeBlock = readExpression,
                        )
                    builder.launchCoroutineIfNeeded(context) {
                        builder.add("${ViewModelConstant.flowSettings.name}.putLong(%S, ", name)
                        builder.add(expression)
                        builder.add(".toEpochMilliseconds())\n")
                    }
                }
            return builder.build()
        }

        override fun generateUpdateStateMethodToViewModel(context: GenerationContext): FunSpecWrapper =
            FunSpecWrapper.builder(getUpdateMethodName(context)).build()

        override fun generateClearStateCodeToViewModel(context: GenerationContext): CodeBlockWrapper {
            val builder = CodeBlockWrapper.builder()
            builder.launchCoroutineIfNeeded(context) {
                builder.addStatement(
                    "${ViewModelConstant.flowSettings.name}.putLong(%S, ${defaultValue.generateCode()}.toEpochMilliseconds())",
                    name,
                )
            }
            return builder.build()
        }

        override fun generateStatePropertiesToViewModel(
            project: Project,
            context: GenerationContext,
        ): List<PropertySpecWrapper> =
            listOf(
                PropertySpecWrapper
                    .builder(
                        getFlowName(context),
                        StateFlow::class
                            .asTypeNameWrapper()
                            .parameterizedBy(Instant::class.asTypeNameWrapper()),
                    ).initializer(
                        CodeBlockWrapper.of(
                            """
                       ${ViewModelConstant.flowSettings.name}.%M(
                         "$name",
                         defaultValue = ${defaultValue.generateCode()}.toEpochMilliseconds(),
                       )
                       .%M { %M.fromEpochMilliseconds(it) }
                       .%M(
                         scope = %M,
                         started = %M.WhileSubscribed(5_000),
                         initialValue = ${defaultValue.generateCode()},
                       )
                    """,
                            MemberHolder.Settings.getLongFlow,
                            MemberHolder.Coroutines.Flow.map,
                            MemberHolder.DateTime.Instant,
                            MemberHolder.Coroutines.Flow.stateIn,
                            MemberHolder.PreCompose.viewModelScope,
                            MemberHolder.Coroutines.Flow.SharingStarted,
                        ),
                    ).build(),
            )

        @Composable
        override fun asDropdownText(): AnnotatedString = AnnotatedString("Instant")

        override fun isSameItem(item: Any): Boolean = item is InstantAppState
    }

    @Serializable
    @SerialName("StringListAppState")
    data class StringListAppState(
        override val id: StateId = Uuid.random().toString(),
        override var name: String,
        override val defaultValue: List<String> = listOf(""),
        override val isList: Boolean = true,
        override val userWritable: Boolean = true,
    ) : AppState<List<String>>,
        ListAppState {
        override fun generateReadBlock(
            project: Project,
            context: GenerationContext,
            dryRun: Boolean,
        ): CodeBlockWrapper {
            val builder = CodeBlockWrapper.builder()
            builder.addStatement(getReadVariableName(project, context))
            return builder.build()
        }

        override fun generateWriteBlock(
            project: Project,
            canvasEditable: CanvasEditable,
            context: GenerationContext,
            dryRun: Boolean,
        ): CodeBlockWrapper = CodeBlockWrapper.of("")

        override fun valueType(
            project: Project,
            asNonList: Boolean,
        ): ComposeFlowType =
            ComposeFlowType.StringType(
                isList = !asNonList,
            )

        override fun generateUpdateStateCodeToViewModel(
            project: Project,
            context: GenerationContext,
            readProperty: AssignableProperty,
            dryRun: Boolean,
        ): CodeBlockWrapper = CodeBlockWrapper.of("")

        override fun generateUpdateStateMethodToViewModel(context: GenerationContext): FunSpecWrapper =
            FunSpecWrapper.builder(getUpdateMethodName(context)).build()

        override fun generateClearStateCodeToViewModel(context: GenerationContext): CodeBlockWrapper =
            super.generateClearStateCode(context = context, stateName = name)

        override fun generateStatePropertiesToViewModel(
            project: Project,
            context: GenerationContext,
        ): List<PropertySpecWrapper> =
            listOf(
                super.generateStateProperty(
                    propertyName = getFlowName(context),
                    stateName = name,
                    typeClassName = ClassHolder.Kotlin.String,
                ),
            )

        override fun generateAddValueToStateCode(
            project: Project,
            context: GenerationContext,
            readProperty: AssignableProperty,
            dryRun: Boolean,
        ): CodeBlockWrapper {
            val builder = CodeBlockWrapper.builder()
            readProperty
                .transformedCodeBlock(project, context, dryRun = dryRun)
                .let { readExpression ->
                    val state = project.findLocalStateOrNull(id) ?: return builder.build()
                    val writeState = state as? WriteableState ?: return builder.build()
                    val expression =
                        ComposeFlowType.StringType().convertCodeFromType(
                            inputType = readProperty.valueType(project),
                            codeBlock = readExpression,
                        )
                    builder.launchCoroutineIfNeeded(context) {
                        builder.controlFlow("val list = ${writeState.getFlowName(context)}.value.toMutableList().apply {") {
                            builder.add("add(")
                            builder.add(expression)
                            builder.add(")\n")
                        }
                        builder.addStatement(
                            "${ViewModelConstant.flowSettings.name}.putString(%S, ${ViewModelConstant.jsonSerializer.name}.%M(list))",
                            name,
                            MemberHolder.Serialization.encodeToString,
                        )
                    }
                }
            return builder.build()
        }
    }

    @Serializable
    @SerialName("IntListAppState")
    data class IntListAppState(
        override val id: StateId = Uuid.random().toString(),
        override var name: String,
        override val defaultValue: List<Int> = listOf(0),
        override val isList: Boolean = true,
        override val userWritable: Boolean = true,
    ) : AppState<List<Int>>,
        ListAppState {
        override fun generateReadBlock(
            project: Project,
            context: GenerationContext,
            dryRun: Boolean,
        ): CodeBlockWrapper {
            val builder = CodeBlockWrapper.builder()
            builder.addStatement(getReadVariableName(project, context))
            return builder.build()
        }

        override fun generateWriteBlock(
            project: Project,
            canvasEditable: CanvasEditable,
            context: GenerationContext,
            dryRun: Boolean,
        ): CodeBlockWrapper = CodeBlockWrapper.of("")

        override fun valueType(
            project: Project,
            asNonList: Boolean,
        ): ComposeFlowType = ComposeFlowType.IntType(isList = !asNonList)

        override fun generateUpdateStateCodeToViewModel(
            project: Project,
            context: GenerationContext,
            readProperty: AssignableProperty,
            dryRun: Boolean,
        ): CodeBlockWrapper = CodeBlockWrapper.of("")

        override fun generateUpdateStateMethodToViewModel(context: GenerationContext): FunSpecWrapper =
            FunSpecWrapper.builder(getUpdateMethodName(context)).build()

        override fun generateClearStateCodeToViewModel(context: GenerationContext): CodeBlockWrapper =
            super.generateClearStateCode(context = context, stateName = name)

        override fun generateStatePropertiesToViewModel(
            project: Project,
            context: GenerationContext,
        ): List<PropertySpecWrapper> =
            listOf(
                super.generateStateProperty(
                    propertyName = getFlowName(context),
                    stateName = name,
                    typeClassName = ClassHolder.Kotlin.Int,
                ),
            )

        override fun generateAddValueToStateCode(
            project: Project,
            context: GenerationContext,
            readProperty: AssignableProperty,
            dryRun: Boolean,
        ): CodeBlockWrapper {
            val builder = CodeBlockWrapper.builder()
            readProperty
                .transformedCodeBlock(project, context, dryRun = dryRun)
                .let { readExpression ->
                    val writeState = project.findLocalStateOrNull(id)
                    if (writeState != null && writeState is WriteableState) {
                        val expression =
                            ComposeFlowType.IntType().convertCodeFromType(
                                inputType = readProperty.valueType(project),
                                codeBlock = readExpression,
                            )
                        builder.launchCoroutineIfNeeded(context) {
                            builder.controlFlow("val list = ${writeState.getFlowName(context)}.value.toMutableList().apply {") {
                                builder.add("add(")
                                builder.add(expression)
                                builder.add(")\n")
                            }
                            builder.addStatement(
                                "${ViewModelConstant.flowSettings.name}.putString(%S, ${ViewModelConstant.jsonSerializer.name}.%M(list))",
                                name,
                                MemberHolder.Serialization.encodeToString,
                            )
                        }
                    }
                }
            return builder.build()
        }
    }

    @Serializable
    @SerialName("FloatListAppState")
    data class FloatListAppState(
        override val id: StateId = Uuid.random().toString(),
        override var name: String,
        override val defaultValue: List<Float> = listOf(0f),
        override val isList: Boolean = true,
        override val userWritable: Boolean = true,
    ) : AppState<List<Float>>,
        ListAppState {
        override fun generateReadBlock(
            project: Project,
            context: GenerationContext,
            dryRun: Boolean,
        ): CodeBlockWrapper {
            val builder = CodeBlockWrapper.builder()
            builder.addStatement(getReadVariableName(project, context))
            return builder.build()
        }

        override fun generateWriteBlock(
            project: Project,
            canvasEditable: CanvasEditable,
            context: GenerationContext,
            dryRun: Boolean,
        ): CodeBlockWrapper = CodeBlockWrapper.of("")

        override fun valueType(
            project: Project,
            asNonList: Boolean,
        ): ComposeFlowType = ComposeFlowType.FloatType(isList = !asNonList)

        override fun generateUpdateStateCodeToViewModel(
            project: Project,
            context: GenerationContext,
            readProperty: AssignableProperty,
            dryRun: Boolean,
        ): CodeBlockWrapper = CodeBlockWrapper.of("")

        override fun generateClearStateCodeToViewModel(context: GenerationContext): CodeBlockWrapper =
            super.generateClearStateCode(context = context, stateName = name)

        override fun generateUpdateStateMethodToViewModel(context: GenerationContext): FunSpecWrapper =
            FunSpecWrapper.builder(getUpdateMethodName(context)).build()

        override fun generateStatePropertiesToViewModel(
            project: Project,
            context: GenerationContext,
        ): List<PropertySpecWrapper> =
            listOf(
                super.generateStateProperty(
                    propertyName = getFlowName(context),
                    stateName = name,
                    typeClassName = ClassHolder.Kotlin.Float,
                ),
            )

        override fun generateAddValueToStateCode(
            project: Project,
            context: GenerationContext,
            readProperty: AssignableProperty,
            dryRun: Boolean,
        ): CodeBlockWrapper {
            val builder = CodeBlockWrapper.builder()
            readProperty
                .transformedCodeBlock(project, context, dryRun = dryRun)
                .let { readExpression ->
                    val writeState = project.findLocalStateOrNull(id)
                    if (writeState != null && writeState is WriteableState) {
                        val expression =
                            ComposeFlowType.FloatType().convertCodeFromType(
                                inputType = readProperty.valueType(project),
                                codeBlock = readExpression,
                            )
                        builder.launchCoroutineIfNeeded(context) {
                            builder.controlFlow("val list = ${writeState.getFlowName(context)}.value.toMutableList().apply {") {
                                builder.add("add(")
                                builder.add(expression)
                                builder.add(")\n")
                            }
                            builder.addStatement(
                                "${ViewModelConstant.flowSettings.name}.putString(%S, ${ViewModelConstant.jsonSerializer.name}.%M(list))",
                                name,
                                MemberHolder.Serialization.encodeToString,
                            )
                        }
                    }
                }
            return builder.build()
        }
    }

    @Serializable
    @SerialName("BooleanListAppState")
    data class BooleanListAppState(
        override val id: StateId = Uuid.random().toString(),
        override var name: String,
        override val defaultValue: List<Boolean> = listOf(false),
        override val isList: Boolean = true,
        override val userWritable: Boolean = true,
    ) : AppState<List<Boolean>>,
        ListAppState {
        override fun generateReadBlock(
            project: Project,
            context: GenerationContext,
            dryRun: Boolean,
        ): CodeBlockWrapper {
            val builder = CodeBlockWrapper.builder()
            builder.addStatement(getReadVariableName(project, context))
            return builder.build()
        }

        override fun generateWriteBlock(
            project: Project,
            canvasEditable: CanvasEditable,
            context: GenerationContext,
            dryRun: Boolean,
        ): CodeBlockWrapper = CodeBlockWrapper.of("")

        override fun valueType(
            project: Project,
            asNonList: Boolean,
        ): ComposeFlowType = ComposeFlowType.BooleanType(isList = !asNonList)

        override fun generateUpdateStateCodeToViewModel(
            project: Project,
            context: GenerationContext,
            readProperty: AssignableProperty,
            dryRun: Boolean,
        ): CodeBlockWrapper = CodeBlockWrapper.of("")

        override fun generateClearStateCodeToViewModel(context: GenerationContext): CodeBlockWrapper =
            super.generateClearStateCode(context = context, stateName = name)

        override fun generateUpdateStateMethodToViewModel(context: GenerationContext): FunSpecWrapper =
            FunSpecWrapper.builder(getUpdateMethodName(context)).build()

        override fun generateStatePropertiesToViewModel(
            project: Project,
            context: GenerationContext,
        ): List<PropertySpecWrapper> =
            listOf(
                super.generateStateProperty(
                    propertyName = getFlowName(context),
                    stateName = name,
                    typeClassName = ClassHolder.Kotlin.Boolean,
                ),
            )

        override fun generateAddValueToStateCode(
            project: Project,
            context: GenerationContext,
            readProperty: AssignableProperty,
            dryRun: Boolean,
        ): CodeBlockWrapper {
            val builder = CodeBlockWrapper.builder()
            readProperty
                .transformedCodeBlock(project, context, dryRun = dryRun)
                .let { readExpression ->
                    val writeState = project.findLocalStateOrNull(id)
                    if (writeState != null && writeState is WriteableState) {
                        val expression =
                            ComposeFlowType.BooleanType().convertCodeFromType(
                                inputType = readProperty.valueType(project),
                                codeBlock = readExpression,
                            )
                        builder.launchCoroutineIfNeeded(context) {
                            builder.controlFlow("val list = ${writeState.getFlowName(context)}.value.toMutableList().apply {") {
                                builder.add("add(")
                                builder.add(expression)
                                builder.add(")\n")
                            }
                            builder.addStatement(
                                "${ViewModelConstant.flowSettings.name}.putString(%S, ${ViewModelConstant.jsonSerializer.name}.%M(list))",
                                name,
                                MemberHolder.Serialization.encodeToString,
                            )
                        }
                    }
                }
            return builder.build()
        }
    }

    @Serializable
    @SerialName("InstantListAppState")
    data class InstantListAppState(
        override val id: StateId = Uuid.random().toString(),
        override var name: String,
        override val defaultValue: List<InstantWrapper> = listOf(InstantWrapper(null)),
        override val isList: Boolean = true,
        override val userWritable: Boolean = true,
    ) : AppState<List<InstantWrapper>>,
        ListAppState {
        override fun generateReadBlock(
            project: Project,
            context: GenerationContext,
            dryRun: Boolean,
        ): CodeBlockWrapper {
            val builder = CodeBlockWrapper.builder()
            builder.addStatement(getReadVariableName(project, context))
            return builder.build()
        }

        override fun generateWriteBlock(
            project: Project,
            canvasEditable: CanvasEditable,
            context: GenerationContext,
            dryRun: Boolean,
        ): CodeBlockWrapper = CodeBlockWrapper.of("")

        override fun valueType(
            project: Project,
            asNonList: Boolean,
        ): ComposeFlowType =
            ComposeFlowType.StringType(
                isList = !asNonList,
            )

        override fun generateUpdateStateCodeToViewModel(
            project: Project,
            context: GenerationContext,
            readProperty: AssignableProperty,
            dryRun: Boolean,
        ): CodeBlockWrapper = CodeBlockWrapper.of("")

        override fun generateUpdateStateMethodToViewModel(context: GenerationContext): FunSpecWrapper =
            FunSpecWrapper.builder(getUpdateMethodName(context)).build()

        override fun generateClearStateCodeToViewModel(context: GenerationContext): CodeBlockWrapper =
            super.generateClearStateCode(context = context, stateName = name)

        override fun generateStatePropertiesToViewModel(
            project: Project,
            context: GenerationContext,
        ): List<PropertySpecWrapper> =
            listOf(
                super.generateStateProperty(
                    propertyName = getFlowName(context),
                    stateName = name,
                    typeClassName = ClassHolder.Kotlin.Long,
                ),
            )

        override fun generateAddValueToStateCode(
            project: Project,
            context: GenerationContext,
            readProperty: AssignableProperty,
            dryRun: Boolean,
        ): CodeBlockWrapper {
            val builder = CodeBlockWrapper.builder()
            readProperty
                .transformedCodeBlock(project, context, dryRun = dryRun)
                .let { readExpression ->
                    val state = project.findLocalStateOrNull(id) ?: return builder.build()
                    val writeState = state as? WriteableState ?: return builder.build()
                    val expression =
                        ComposeFlowType.StringType().convertCodeFromType(
                            inputType = readProperty.valueType(project),
                            codeBlock = readExpression,
                        )
                    builder.launchCoroutineIfNeeded(context) {
                        builder.controlFlow("val list = ${writeState.getFlowName(context)}.value.toMutableList().apply {") {
                            builder.add("add(")
                            builder.add(expression)
                            builder.add(")\n")
                        }
                        builder.addStatement(
                            "${ViewModelConstant.flowSettings.name}.putString(%S, ${ViewModelConstant.jsonSerializer.name}.%M(list))",
                            name,
                            MemberHolder.Serialization.encodeToString,
                        )
                    }
                }
            return builder.build()
        }
    }

    @Serializable
    @SerialName("CustomDataTypeListAppState")
    data class CustomDataTypeListAppState(
        override val id: StateId = Uuid.random().toString(),
        override var name: String,
        override val defaultValue: List<DataTypeDefaultValue> = emptyList(),
        override val isList: Boolean = true,
        override val dataTypeId: DataTypeId,
        override val userWritable: Boolean = true,
    ) : AppState<List<DataTypeDefaultValue>>,
        ListAppState,
        AppStateWithDataTypeId {
        override fun generateReadBlock(
            project: Project,
            context: GenerationContext,
            dryRun: Boolean,
        ): CodeBlockWrapper {
            val builder = CodeBlockWrapper.builder()
            builder.addStatement(getReadVariableName(project, context))
            return builder.build()
        }

        override fun generateWriteBlock(
            project: Project,
            canvasEditable: CanvasEditable,
            context: GenerationContext,
            dryRun: Boolean,
        ): CodeBlockWrapper = CodeBlockWrapper.of("")

        override fun valueType(
            project: Project,
            asNonList: Boolean,
        ): ComposeFlowType =
            ComposeFlowType.CustomDataType(
                isList = !asNonList,
                dataTypeId = dataTypeId,
            )

        override fun generateUpdateStateCodeToViewModel(
            project: Project,
            context: GenerationContext,
            readProperty: AssignableProperty,
            dryRun: Boolean,
        ): CodeBlockWrapper = CodeBlockWrapper.of("")

        override fun generateClearStateCodeToViewModel(context: GenerationContext): CodeBlockWrapper =
            super.generateClearStateCode(context = context, stateName = name)

        override fun generateUpdateStateMethodToViewModel(context: GenerationContext): FunSpecWrapper =
            FunSpecWrapper.builder(getUpdateMethodName(context)).build()

        override fun generateStatePropertiesToViewModel(
            project: Project,
            context: GenerationContext,
        ): List<PropertySpecWrapper> {
            val dataType = project.findDataTypeOrThrow(dataTypeId)
            return listOf(
                super.generateStateProperty(
                    propertyName = getFlowName(context),
                    stateName = name,
                    typeClassName = dataType.asKotlinPoetClassName(project),
                ),
            )
        }

        override fun generateAddValueToStateCode(
            project: Project,
            context: GenerationContext,
            readProperty: AssignableProperty,
            dryRun: Boolean,
        ): CodeBlockWrapper {
            val builder = CodeBlockWrapper.builder()
            return builder.build()
        }
    }

    @Serializable
    @SerialName("CustomDataTypeAppState")
    data class CustomDataTypeAppState(
        override val id: StateId = Uuid.random().toString(),
        override var name: String,
        override val defaultValue: DataType? = null,
        override val isList: Boolean = false,
        override val dataTypeId: DataTypeId = defaultValue?.id ?: EmptyDataType.id,
        override val userWritable: Boolean = true,
    ) : AppState<DataType>,
        AppStateWithDataTypeId,
        DropdownItem {
        override fun generateReadBlock(
            project: Project,
            context: GenerationContext,
            dryRun: Boolean,
        ): CodeBlockWrapper {
            val builder = CodeBlockWrapper.builder()
            builder.addStatement(getReadVariableName(project, context))
            return builder.build()
        }

        override fun generateWriteBlock(
            project: Project,
            canvasEditable: CanvasEditable,
            context: GenerationContext,
            dryRun: Boolean,
        ): CodeBlockWrapper {
            val builder = CodeBlockWrapper.builder()
            builder.addStatement(
                "${canvasEditable.viewModelName}.${getUpdateMethodName(context)}(it)",
            )
            return builder.build()
        }

        override fun valueType(
            project: Project,
            asNonList: Boolean,
        ): ComposeFlowType =
            ComposeFlowType.CustomDataType(
                isList = false,
                dataTypeId = dataTypeId,
            )

        override fun generateUpdateStateCodeToViewModel(
            project: Project,
            context: GenerationContext,
            readProperty: AssignableProperty,
            dryRun: Boolean,
        ): CodeBlockWrapper {
            val builder = CodeBlockWrapper.builder()
            return builder.build()
        }

        override fun generateUpdateStateMethodToViewModel(context: GenerationContext): FunSpecWrapper =
            FunSpecWrapper.builder(getUpdateMethodName(context)).build()

        override fun generateClearStateCodeToViewModel(context: GenerationContext): CodeBlockWrapper {
            val builder = CodeBlockWrapper.builder()
            builder.launchCoroutineIfNeeded(context) {
                builder.addStatement("${ViewModelConstant.flowSettings.name}.putString(%S, %S)", name, "{}")
            }
            return builder.build()
        }

        override fun generateStatePropertiesToViewModel(
            project: Project,
            context: GenerationContext,
        ): List<PropertySpecWrapper> {
            val dataType = project.findDataTypeOrThrow(dataTypeId)
            return listOf(
                PropertySpecWrapper
                    .builder(
                        getFlowName(context),
                        ClassHolder.Coroutines.Flow.StateFlow.parameterizedBy(
                            dataType.asKotlinPoetClassName(
                                project,
                            ),
                        ),
                    ).initializer(
                        """
                       ${ViewModelConstant.flowSettings.name}.%M(
                         "$name",
                         defaultValue = "{}"
                       ).%M {
                            ${ViewModelConstant.jsonSerializer.name}.decodeFromString<%T>(it)
                       }
                       .%M(
                         scope = %M,
                         started = %M.WhileSubscribed(5_000),
                         initialValue = %T(),
                       )
                    """,
                        MemberHolder.Settings.getStringFlow,
                        MemberHolder.Coroutines.Flow.map,
                        dataType.asKotlinPoetClassName(project),
                        MemberHolder.Coroutines.Flow.stateIn,
                        MemberHolder.PreCompose.viewModelScope,
                        MemberHolder.Coroutines.Flow.SharingStarted,
                        dataType.asKotlinPoetClassName(project),
                    ).build(),
            )
        }

        @Composable
        override fun asDropdownText(): AnnotatedString = AnnotatedString("DataType")

        override fun isSameItem(item: Any): Boolean = item is CustomDataTypeAppState
    }

    override fun generateVariableInitializationBlock(
        project: Project,
        context: GenerationContext,
        dryRun: Boolean,
    ): CodeBlockWrapper {
        val variableName = getReadVariableName(project, context)
        return CodeBlockWrapper.of(
            "val $variableName by ${context.currentEditable.viewModelName}.${getFlowName(context)}.%M()",
            MemberHolder.AndroidX.Runtime.collectAsState,
        )
    }

    companion object {
        fun entries(): List<AppState<*>> =
            listOf(
                StringAppState(name = ""),
                IntAppState(name = ""),
                FloatAppState(name = ""),
                BooleanAppState(name = ""),
                InstantAppState(name = ""),
                CustomDataTypeAppState(name = ""),
            )
    }
}

fun <T> AppState<T>.copy(
    id: String? = null,
    name: String,
    isList: Boolean = this.isList,
    argDataTypeId: DataTypeId? = null,
): AppState<*> {
    val newId = id ?: this.id

    fun copyStringState(
        newId: StateId,
        name: String,
        defaultValue: String,
        isList: Boolean,
    ): AppState<*> =
        if (isList) {
            AppState.StringListAppState(
                id = newId,
                name = name,
                defaultValue = listOf(defaultValue),
            )
        } else {
            AppState.StringAppState(
                id = newId,
                name = name,
                defaultValue = defaultValue,
            )
        }

    fun copyIntState(
        newId: StateId,
        name: String,
        defaultValue: Int,
        isList: Boolean,
    ): AppState<*> =
        if (isList) {
            AppState.IntListAppState(
                id = newId,
                name = name,
                defaultValue = listOf(defaultValue),
            )
        } else {
            AppState.IntAppState(
                id = newId,
                name = name,
                defaultValue = defaultValue,
            )
        }

    fun copyFloatState(
        newId: StateId,
        name: String,
        defaultValue: Float,
        isList: Boolean,
    ): AppState<*> =
        if (isList) {
            AppState.FloatListAppState(
                id = newId,
                name = name,
                defaultValue = listOf(defaultValue),
            )
        } else {
            AppState.FloatAppState(
                id = newId,
                name = name,
                defaultValue = defaultValue,
            )
        }

    fun copyBooleanState(
        newId: StateId,
        name: String,
        defaultValue: Boolean,
        isList: Boolean,
    ): AppState<*> =
        if (isList) {
            AppState.BooleanListAppState(
                id = newId,
                name = name,
                defaultValue = listOf(defaultValue),
            )
        } else {
            AppState.BooleanAppState(
                id = newId,
                name = name,
                defaultValue = defaultValue,
            )
        }

    fun copyInstantState(
        newId: StateId,
        name: String,
        defaultValue: InstantWrapper,
        isList: Boolean,
    ): AppState<*> =
        if (isList) {
            AppState.InstantListAppState(
                id = newId,
                name = name,
                defaultValue = listOf(defaultValue),
            )
        } else {
            AppState.InstantAppState(
                id = newId,
                name = name,
                defaultValue = defaultValue,
            )
        }

    fun copyCustomDataTypeState(
        newId: StateId,
        name: String,
        isList: Boolean,
        dataTypeId: DataTypeId,
    ): AppState<*> =
        if (isList) {
            AppState.CustomDataTypeListAppState(
                id = newId,
                name = name,
                dataTypeId = dataTypeId,
            )
        } else {
            AppState.CustomDataTypeAppState(
                id = newId,
                name = name,
                dataTypeId = dataTypeId,
            )
        }

    return when (this) {
        is AppState.BooleanAppState -> {
            copyBooleanState(
                newId = newId,
                name = name,
                defaultValue = defaultValue,
                isList = isList,
            )
        }

        is AppState.BooleanListAppState -> {
            copyBooleanState(
                newId = newId,
                name = name,
                defaultValue = if (defaultValue.isNotEmpty()) defaultValue[0] else true,
                isList = isList,
            )
        }

        is AppState.IntAppState -> {
            copyIntState(
                newId = newId,
                name = name,
                defaultValue = defaultValue,
                isList = isList,
            )
        }

        is AppState.IntListAppState -> {
            copyIntState(
                newId = newId,
                name = name,
                defaultValue = if (defaultValue.isNotEmpty()) defaultValue[0] else 0,
                isList = isList,
            )
        }

        is AppState.FloatAppState -> {
            copyFloatState(
                newId = newId,
                name = name,
                defaultValue = defaultValue,
                isList = isList,
            )
        }

        is AppState.FloatListAppState -> {
            copyFloatState(
                newId = newId,
                name = name,
                defaultValue = if (defaultValue.isNotEmpty()) defaultValue[0] else 0.0f,
                isList = isList,
            )
        }

        is AppState.StringAppState -> {
            copyStringState(
                newId = newId,
                name = name,
                defaultValue = defaultValue,
                isList = isList,
            )
        }

        is AppState.StringListAppState -> {
            copyStringState(
                newId = newId,
                name = name,
                defaultValue = if (defaultValue.isNotEmpty()) defaultValue[0] else "",
                isList = isList,
            )
        }

        is AppState.CustomDataTypeAppState -> {
            copyCustomDataTypeState(
                newId = newId,
                name = name,
                isList = isList,
                dataTypeId = argDataTypeId ?: this.dataTypeId,
            )
        }

        is AppState.CustomDataTypeListAppState -> {
            copyCustomDataTypeState(
                newId = newId,
                name = name,
                isList = isList,
                dataTypeId = argDataTypeId ?: this.dataTypeId,
            )
        }

        is AppState.InstantAppState -> {
            copyInstantState(
                newId = newId,
                name = name,
                defaultValue = defaultValue,
                isList = isList,
            )
        }

        is AppState.InstantListAppState -> {
            copyInstantState(
                newId = newId,
                name = name,
                defaultValue = if (defaultValue.isNotEmpty()) defaultValue[0] else InstantWrapper(),
                isList = isList,
            )
        }
    }
}

private const val AUTHENTICATED_USER = "authenticatedUser"

@Serializable
sealed interface AuthenticatedUserState : ReadableState {
    override fun generateVariableInitializationBlock(
        project: Project,
        context: GenerationContext,
        dryRun: Boolean,
    ): CodeBlockWrapper {
        val authenticatedUser = AUTHENTICATED_USER
        context.getCurrentComposableContext().addCompositionLocalVariableEntryIfNotPresent(
            id = authenticatedUser,
            authenticatedUser,
            MemberHolder.ComposeFlow.LocalAuthenticatedUser,
        )
        // Initialize the authenticatedUser once in the compose file instead of initializing it in
        // every state that has a reference
        return CodeBlockWrapper.of("")
    }

    @Serializable
    @SerialName("IsSignedIn")
    data object IsSignedIn : AuthenticatedUserState {
        override val id: StateId = Uuid.random().toString()
        override var name: String = "Is signed in"
        override val isList: Boolean = false

        override fun generateReadBlock(
            project: Project,
            context: GenerationContext,
            dryRun: Boolean,
        ): CodeBlockWrapper = CodeBlockWrapper.of("""$AUTHENTICATED_USER != null""")

        override fun valueType(
            project: Project,
            asNonList: Boolean,
        ): ComposeFlowType = ComposeFlowType.BooleanType()
    }

    @Serializable
    @SerialName("DisplayName")
    data object DisplayName : AuthenticatedUserState {
        override val id: StateId = Uuid.random().toString()
        override var name: String = "Display name"
        override val isList: Boolean = false

        override fun generateReadBlock(
            project: Project,
            context: GenerationContext,
            dryRun: Boolean,
        ): CodeBlockWrapper = CodeBlockWrapper.of("""($AUTHENTICATED_USER?.displayName ?: "Invalid display name")""")

        override fun valueType(
            project: Project,
            asNonList: Boolean,
        ): ComposeFlowType = ComposeFlowType.StringType()
    }

    @Serializable
    @SerialName("Email")
    data object Email : AuthenticatedUserState {
        override val id: StateId = Uuid.random().toString()
        override var name: String = "Email"
        override val isList: Boolean = false

        override fun generateReadBlock(
            project: Project,
            context: GenerationContext,
            dryRun: Boolean,
        ): CodeBlockWrapper = CodeBlockWrapper.of("""($AUTHENTICATED_USER?.email ?: "Invalid email")""")

        override fun valueType(
            project: Project,
            asNonList: Boolean,
        ): ComposeFlowType = ComposeFlowType.StringType()
    }

    @Serializable
    @SerialName("PhoneNumber")
    data object PhoneNumber : AuthenticatedUserState {
        override val id: StateId = Uuid.random().toString()
        override var name: String = "Phone number"
        override val isList: Boolean = false

        override fun generateReadBlock(
            project: Project,
            context: GenerationContext,
            dryRun: Boolean,
        ): CodeBlockWrapper = CodeBlockWrapper.of("""($AUTHENTICATED_USER?.phoneNumber ?: "Invalid phone number")""")

        override fun valueType(
            project: Project,
            asNonList: Boolean,
        ): ComposeFlowType = ComposeFlowType.StringType()
    }

    @Serializable
    @SerialName("PhotoUrl")
    data object PhotoUrl : AuthenticatedUserState {
        override val id: StateId = Uuid.random().toString()
        override var name: String = "Photo URL"
        override val isList: Boolean = false

        override fun generateReadBlock(
            project: Project,
            context: GenerationContext,
            dryRun: Boolean,
        ): CodeBlockWrapper = CodeBlockWrapper.of("""($AUTHENTICATED_USER?.photoURL ?: "Invalid photo url")""")

        override fun valueType(
            project: Project,
            asNonList: Boolean,
        ): ComposeFlowType = ComposeFlowType.StringType()
    }

    @Serializable
    @SerialName("IsAnonymous")
    data object IsAnonymous : AuthenticatedUserState {
        override val id: StateId = Uuid.random().toString()
        override var name: String = "Is anonymous"
        override val isList: Boolean = false

        override fun generateReadBlock(
            project: Project,
            context: GenerationContext,
            dryRun: Boolean,
        ): CodeBlockWrapper = CodeBlockWrapper.of("""($AUTHENTICATED_USER?.isAnonymous ?: false)""")

        override fun valueType(
            project: Project,
            asNonList: Boolean,
        ): ComposeFlowType = ComposeFlowType.BooleanType()
    }

    companion object {
        fun entries(): List<AuthenticatedUserState> =
            listOf(
                IsSignedIn,
                DisplayName,
                Email,
                PhoneNumber,
                PhotoUrl,
                IsAnonymous,
            )
    }
}
