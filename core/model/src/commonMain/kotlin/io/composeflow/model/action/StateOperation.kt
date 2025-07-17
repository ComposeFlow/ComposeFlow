package io.composeflow.model.action

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.capitalize
import androidx.compose.ui.text.intl.Locale
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.ParameterSpec
import io.composeflow.Res
import io.composeflow.ViewModelConstant
import io.composeflow.add_value
import io.composeflow.clear_value
import io.composeflow.kotlinpoet.GenerationContext
import io.composeflow.kotlinpoet.MemberHolder
import io.composeflow.model.datatype.DataFieldId
import io.composeflow.model.project.Project
import io.composeflow.model.project.appscreen.screen.composenode.ComposeNode
import io.composeflow.model.project.findComposeNodeOrThrow
import io.composeflow.model.project.findDataTypeOrThrow
import io.composeflow.model.project.findLocalStateOrNull
import io.composeflow.model.property.AssignableProperty
import io.composeflow.model.property.BooleanProperty
import io.composeflow.model.property.FloatProperty
import io.composeflow.model.property.IntProperty
import io.composeflow.model.property.StringProperty
import io.composeflow.model.property.ValueFromState
import io.composeflow.model.state.AppState
import io.composeflow.model.state.BooleanState
import io.composeflow.model.state.ListAppState
import io.composeflow.model.state.WriteableState
import io.composeflow.model.type.ComposeFlowType
import io.composeflow.model.type.convertCodeFromType
import io.composeflow.remove_first_value
import io.composeflow.remove_last_value
import io.composeflow.remove_value_at_index
import io.composeflow.set_value
import io.composeflow.toggle_value
import io.composeflow.ui.propertyeditor.DropdownItem
import io.composeflow.update_value_at_index
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.jetbrains.compose.resources.stringResource

@Serializable
sealed interface StateOperation {

    fun isDependent(sourceId: String): Boolean

    fun getUpdateMethodName(project: Project, writeState: WriteableState): String
    fun getUpdateMethodParamsAsString(
        project: Project,
        context: GenerationContext,
        dryRun: Boolean,
    ): String = ""

    fun addUpdateMethodAndReadProperty(
        project: Project,
        context: GenerationContext,
        writeState: WriteableState,
        dryRun: Boolean,
    )

    @Composable
    fun displayName(): String

    fun getDependentComposeNodes(project: Project): List<ComposeNode> = emptyList()

    fun getAssignableProperties(): List<AssignableProperty> = emptyList()

    @Serializable
    @SerialName("SetValue")
    data class SetValue(
        override val readProperty: AssignableProperty,
    ) : StateOperation, StateOperationWithReadProperty, DropdownItem {

        override fun isDependent(sourceId: String): Boolean {
            return readProperty.isDependent(sourceId)
        }

        override fun getUpdateMethodParamsAsString(
            project: Project,
            context: GenerationContext,
            dryRun: Boolean,
        ): String {
            return readProperty.generateParameterSpec(project)?.let {
                "${it.name} = ${readProperty.generateCodeBlock(project, context, dryRun = dryRun)}"
            } ?: ""
        }

        override fun getUpdateMethodName(project: Project, writeState: WriteableState): String {
            return if (readProperty is ValueFromState) {
                val readState = project.findLocalStateOrNull(readProperty.readFromStateId)
                if (readState != null) {
                    "onSet${writeState.name.capitalize(Locale.current)}" +
                            readState.let { "From${readState.name.capitalize(Locale.current)}" }
                } else {
                    "onSet${writeState.name.capitalize(Locale.current)}"
                }
            } else {
                "onSet${writeState.name.capitalize(Locale.current)}"
            }
        }

        override fun addUpdateMethodAndReadProperty(
            project: Project,
            context: GenerationContext,
            writeState: WriteableState,
            dryRun: Boolean,
        ) {
            val funSpecBuilder = FunSpec.builder(
                getUpdateMethodName(
                    project = project,
                    writeState = writeState,
                ),
            )
            readProperty.generateParameterSpec(project)?.let {
                funSpecBuilder.addParameter(it)
            }
            context.addFunction(
                funSpecBuilder.addCode(
                    writeState.generateUpdateStateCodeToViewModel(
                        project,
                        context,
                        readProperty,
                        dryRun = dryRun,
                    ),
                ).build(),
                dryRun = dryRun,
            )
            writeState.generateStatePropertiesToViewModel(project).forEach {
                context.addProperty(it, dryRun = dryRun)
            }
        }

        @Composable
        override fun displayName(): String = stringResource(Res.string.set_value)

        override fun getDependentComposeNodes(project: Project): List<ComposeNode> {
            return readProperty.getDependentComposeNodes(project)
        }

        override fun getAssignableProperties(): List<AssignableProperty> = listOf(readProperty)

        @Composable
        override fun asDropdownText(): String = displayName()

        override fun isSameItem(item: Any): Boolean = item is SetValue
    }

    @Serializable
    @SerialName("ClearValue")
    data object ClearValue : StateOperation, DropdownItem {

        override fun isDependent(sourceId: String): Boolean = false

        override fun getUpdateMethodName(project: Project, writeState: WriteableState): String {
            return "onClear${writeState.name.capitalize(Locale.current)}"
        }

        override fun addUpdateMethodAndReadProperty(
            project: Project,
            context: GenerationContext,
            writeState: WriteableState,
            dryRun: Boolean,
        ) {
            val funSpecBuilder = FunSpec.builder(
                getUpdateMethodName(
                    project = project,
                    writeState = writeState,
                ),
            )
            context.addFunction(
                funSpecBuilder.addCode(writeState.generateClearStateCodeToViewModel())
                    .build(),
                dryRun = dryRun
            )
        }

        @Composable
        override fun displayName(): String = stringResource(Res.string.clear_value)

        @Composable
        override fun asDropdownText(): String = displayName()

        override fun isSameItem(item: Any): Boolean = item == ClearValue
    }

    @Serializable
    @SerialName("ToggleValue")
    data object ToggleValue : StateOperation, DropdownItem {

        override fun isDependent(sourceId: String): Boolean = false

        override fun getUpdateMethodName(project: Project, writeState: WriteableState): String =
            "onToggle${writeState.name.capitalize(Locale.current)}"

        override fun addUpdateMethodAndReadProperty(
            project: Project,
            context: GenerationContext,
            writeState: WriteableState,
            dryRun: Boolean,
        ) {
            val funSpecBuilder = FunSpec.builder(
                getUpdateMethodName(
                    project = project,
                    writeState = writeState,
                ),
            )
            if (writeState is BooleanState) {
                context.addFunction(
                    funSpecBuilder.addCode(
                        writeState.generateToggleStateCodeToViewModel(),
                    ).build(),
                    dryRun = dryRun
                )
                writeState.generateStatePropertiesToViewModel(project).forEach {
                    context.addProperty(it, dryRun)
                }
            }
        }

        @Composable
        override fun displayName(): String = stringResource(Res.string.toggle_value)

        @Composable
        override fun asDropdownText(): String = displayName()

        override fun isSameItem(item: Any): Boolean = item == ToggleValue
    }
}

@Serializable
sealed interface FieldUpdateType {

    @Composable
    fun displayName(): String

    @Serializable
    @SerialName("FieldUpdateTypeSetValue")
    data object SetValue : FieldUpdateType, DropdownItem {
        @Composable
        override fun displayName(): String = stringResource(Res.string.set_value)

        @Composable
        override fun asDropdownText(): String = displayName()

        override fun isSameItem(item: Any): kotlin.Boolean = item == SetValue
    }

    @Serializable
    @SerialName("FieldUpdateTypeClearValue")
    data object ClearValue : FieldUpdateType, DropdownItem {
        @Composable
        override fun displayName(): String = stringResource(Res.string.clear_value)

        @Composable
        override fun asDropdownText(): String = displayName()

        override fun isSameItem(item: Any): kotlin.Boolean = item == ClearValue
    }

    @Serializable
    @SerialName("FieldUpdateTypeToggleValue")
    data object ToggleValue : FieldUpdateType, DropdownItem {
        @Composable
        override fun displayName(): String = stringResource(Res.string.toggle_value)

        @Composable
        override fun asDropdownText(): String = displayName()

        override fun isSameItem(item: Any): kotlin.Boolean = item == ToggleValue
    }

    object Normal {
        fun entries(): List<FieldUpdateType> = listOf(
            SetValue,
            ClearValue,
        )
    }

    object Boolean {
        fun entries(): List<FieldUpdateType> = listOf(
            SetValue,
            ClearValue,
            ToggleValue,
        )
    }
}

@Serializable
@SerialName("DataFieldUpdateProperty")
data class DataFieldUpdateProperty(
    val dataFieldId: DataFieldId,
    val assignableProperty: AssignableProperty,

    /**
     * Update type of the dataField. Only used when the operation is [StateOperationForList.UpdateValueAtIndexForCustomDataType]
     * to express how to update the each field.
     */
    val fieldUpdateType: FieldUpdateType = FieldUpdateType.SetValue,
)

@Serializable
sealed interface StateOperationForDataType : StateOperation {

    val dataFieldUpdateProperties: MutableList<DataFieldUpdateProperty>

    @Serializable
    @SerialName("DataTypeSetValue")
    data class DataTypeSetValue(
        override val dataFieldUpdateProperties: MutableList<DataFieldUpdateProperty> = mutableListOf(),
    ) : StateOperationForDataType, DropdownItem {

        override fun isDependent(sourceId: String): Boolean {
            return dataFieldUpdateProperties.any {
                it.assignableProperty.isDependent(
                    sourceId
                )
            }
        }

        override fun getDependentComposeNodes(project: Project): List<ComposeNode> {
            return dataFieldUpdateProperties.flatMap {
                it.assignableProperty.getDependentComposeNodes(
                    project
                )
            }
        }

        override fun getAssignableProperties(): List<AssignableProperty> =
            dataFieldUpdateProperties.map {
                it.assignableProperty
            }

        override fun getUpdateMethodName(project: Project, writeState: WriteableState): String {
            check(writeState is AppState.CustomDataTypeAppState)
            val dataTypeId = writeState.dataTypeId
            val dataType = project.findDataTypeOrThrow(dataTypeId)
            return "onSet${dataType.className}"
        }

        override fun getUpdateMethodParamsAsString(
            project: Project,
            context: GenerationContext,
            dryRun: Boolean,
        ): String {
            return buildString {
                dataFieldUpdateProperties.forEach { (_, readProperty) ->
                    readProperty.generateParameterSpec(project)?.let {
                        append(
                            "${it.name} = ${
                                readProperty.generateCodeBlock(
                                    project,
                                    context,
                                    dryRun = dryRun
                                )
                            },"
                        )
                    }
                }
            }
        }

        override fun addUpdateMethodAndReadProperty(
            project: Project,
            context: GenerationContext,
            writeState: WriteableState,
            dryRun: Boolean,
        ) {
            check(writeState is AppState.CustomDataTypeAppState)
            val dataTypeId = writeState.dataTypeId
            val dataType = project.findDataTypeOrThrow(dataTypeId)

            val funSpecBuilder = FunSpec.builder(
                getUpdateMethodName(project = project, writeState = writeState),
            )

            val updateString = buildString {
                dataFieldUpdateProperties.forEach { (dataFieldId, readProperty) ->
                    val dataField = dataType.findDataFieldOrNull(dataFieldId)
                    dataField?.let {
                        readProperty.transformedCodeBlock(project, context, dryRun = dryRun)
                            .let { codeBlock ->
                                val expression = dataField.fieldType.type().convertCodeFromType(
                                    inputType = readProperty.valueType(project),
                                    codeBlock = codeBlock,
                                )
                                append("${dataField.variableName} = $expression,\n")
                            }
                    }

                    readProperty.generateParameterSpec(project)?.let {
                        funSpecBuilder.addParameter(it)
                    }
                }
            }

            funSpecBuilder.addCode(
                """%M.%M {
                val updated = ${writeState.getFlowName()}.value.copy($updateString)
                ${ViewModelConstant.flowSettings.name}.putString("${writeState.name}", ${ViewModelConstant.jsonSerializer.name}.%M(updated))
            }""",
                MemberHolder.PreCompose.viewModelScope,
                MemberHolder.Coroutines.launch,
                MemberHolder.Serialization.encodeToString,
            )
            context.addFunction(funSpecBuilder.build(), dryRun)
            writeState.generateStatePropertiesToViewModel(project).forEach {
                context.addProperty(it, dryRun)
            }
        }

        @Composable
        override fun displayName(): String = stringResource(Res.string.set_value)

        @Composable
        override fun asDropdownText(): String = displayName()

        override fun isSameItem(item: Any): Boolean = item is DataTypeSetValue
    }
}

@Serializable
sealed interface StateOperationForList : StateOperation {

    @Serializable
    @SerialName("AddValue")
    data class AddValue(
        override val readProperty: AssignableProperty,
    ) : StateOperationForList, StateOperationWithReadProperty, DropdownItem {

        override fun isDependent(sourceId: String): Boolean {
            return readProperty.isDependent(sourceId)
        }

        override fun getDependentComposeNodes(project: Project): List<ComposeNode> {
            return readProperty.getDependentComposeNodes(project)
        }

        override fun getAssignableProperties(): List<AssignableProperty> = listOf(readProperty)

        override fun getUpdateMethodParamsAsString(
            project: Project,
            context: GenerationContext,
            dryRun: Boolean,
        ): String {
            return readProperty.generateParameterSpec(project)?.let {
                "${it.name} = ${readProperty.generateCodeBlock(project, context, dryRun = dryRun)}"
            } ?: ""
        }

        override fun getUpdateMethodName(project: Project, writeState: WriteableState): String {
            return if (readProperty is ValueFromState) {
                val readState = project.findLocalStateOrNull(readProperty.readFromStateId)
                if (readState != null) {
                    "onAddValueTo${writeState.name.capitalize(Locale.current)}" +
                            readState.let { "From${readState.name.capitalize(Locale.current)}" }
                } else {
                    "onAddValueTo${writeState.name.capitalize(Locale.current)}"
                }
            } else {
                "onAddValueTo${writeState.name.capitalize(Locale.current)}"
            }
        }

        override fun addUpdateMethodAndReadProperty(
            project: Project,
            context: GenerationContext,
            writeState: WriteableState,
            dryRun: Boolean,
        ) {
            val funSpecBuilder = FunSpec.builder(
                getUpdateMethodName(
                    project = project,
                    writeState = writeState,
                ),
            )
            if (writeState !is ListAppState) return
            readProperty.generateParameterSpec(project)?.let {
                funSpecBuilder.addParameter(it)
            }
            context.addFunction(
                funSpecBuilder.addCode(
                    writeState.generateAddValueToStateCode(
                        project,
                        context,
                        readProperty,
                        dryRun = dryRun
                    ),
                ).build(),
                dryRun,
            )
            writeState.generateStatePropertiesToViewModel(project).forEach {
                context.addProperty(it, dryRun)
            }
        }

        @Composable
        override fun displayName(): String = stringResource(Res.string.add_value)

        @Composable
        override fun asDropdownText(): String = displayName()

        override fun isSameItem(item: Any): Boolean = item is AddValue
    }

    @Serializable
    @SerialName("AddValueForCustomDataType")
    data class AddValueForCustomDataType(
        override val dataFieldUpdateProperties: MutableList<DataFieldUpdateProperty> = mutableListOf(),
    ) : StateOperationForDataType, StateOperationForList, DropdownItem {

        override fun isDependent(sourceId: String): Boolean {
            return dataFieldUpdateProperties.any {
                it.assignableProperty.isDependent(
                    sourceId
                )
            }
        }

        override fun getDependentComposeNodes(project: Project): List<ComposeNode> {
            return dataFieldUpdateProperties.flatMap {
                it.assignableProperty.getDependentComposeNodes(
                    project
                )
            }
        }

        override fun getAssignableProperties(): List<AssignableProperty> =
            dataFieldUpdateProperties.map {
                it.assignableProperty
            }

        override fun getUpdateMethodName(project: Project, writeState: WriteableState): String {
            check(writeState is AppState.CustomDataTypeListAppState)
            val dataTypeId = writeState.dataTypeId
            val dataType = project.findDataTypeOrThrow(dataTypeId)
            return "onAddValueTo${dataType.className}"
        }

        override fun getUpdateMethodParamsAsString(
            project: Project,
            context: GenerationContext,
            dryRun: Boolean,
        ): String {
            return buildString {
                dataFieldUpdateProperties.forEach { (_, readProperty) ->
                    readProperty.generateParameterSpec(project)?.let {
                        append(
                            "${it.name} = ${
                                readProperty.generateCodeBlock(
                                    project,
                                    context,
                                    dryRun = dryRun
                                )
                            },"
                        )
                    }
                }
            }
        }

        override fun addUpdateMethodAndReadProperty(
            project: Project,
            context: GenerationContext,
            writeState: WriteableState,
            dryRun: Boolean,
        ) {
            check(writeState is AppState.CustomDataTypeListAppState)
            val dataTypeId = writeState.dataTypeId
            val dataType = project.findDataTypeOrThrow(dataTypeId)

            val funSpecBuilder = FunSpec.builder(
                getUpdateMethodName(project = project, writeState = writeState),
            )

            val updateString = buildString {
                dataFieldUpdateProperties.forEach { (dataFieldId, readProperty) ->
                    val dataField = dataType.findDataFieldOrNull(dataFieldId)
                    dataField?.let {
                        append(
                            "${dataField.variableName} = ${
                                readProperty.transformedCodeBlock(
                                    project,
                                    context,
                                    writeType = dataField.fieldType.type(),
                                    dryRun = dryRun,
                                )
                            },\n",
                        )
                    }

                    readProperty.generateParameterSpec(project)?.let {
                        funSpecBuilder.addParameter(it)
                    }
                }
            }

            funSpecBuilder.addCode(
                """%M.%M {
                    val list = ${writeState.getFlowName()}.value.toMutableList().apply {
                        add(%T().copy($updateString))
                    }
                    ${ViewModelConstant.flowSettings.name}.putString("${writeState.name}", ${ViewModelConstant.jsonSerializer.name}.%M(list))
                }""",
                MemberHolder.PreCompose.viewModelScope,
                MemberHolder.Coroutines.launch,
                dataType.asKotlinPoetClassName(project),
                MemberHolder.Serialization.encodeToString,
            )
            context.addFunction(funSpecBuilder.build(), dryRun)
            writeState.generateStatePropertiesToViewModel(project).forEach {
                context.addProperty(it, dryRun)
            }
        }

        @Composable
        override fun displayName(): String = stringResource(Res.string.add_value)

        @Composable
        override fun asDropdownText(): String = displayName()

        override fun isSameItem(item: Any): Boolean =
            item is AddValueForCustomDataType
    }

    @Serializable
    @SerialName("UpdateValueAtIndex")
    data class UpdateValueAtIndex(
        // Represents the index to update
        override val indexProperty: IntProperty = IntProperty.IntIntrinsicValue(),
        override val readProperty: AssignableProperty,
    ) : StateOperationForList, StateOperationWithReadProperty, StateOperationWithIndexProperty,
        DropdownItem {

        override fun isDependent(sourceId: String): Boolean {
            return readProperty.isDependent(sourceId) ||
                    indexProperty.isDependent(sourceId)
        }

        override fun getDependentComposeNodes(project: Project): List<ComposeNode> {
            return readProperty.getDependentComposeNodes(project)
        }

        override fun getAssignableProperties(): List<AssignableProperty> = listOf(readProperty)

        override fun getUpdateMethodName(project: Project, writeState: WriteableState): String {
            return "onUpdateValueFor${writeState.name.capitalize(Locale.current)}AtIndex"
        }

        override fun getUpdateMethodParamsAsString(
            project: Project,
            context: GenerationContext,
            dryRun: Boolean,
        ): String {
            val indexString = getUpdateIndexParamsAsString(project, context)
            return buildString {
                append(indexString)
                readProperty.generateParameterSpec(project)?.let {
                    ", ${it.name} = ${
                        readProperty.generateCodeBlock(
                            project,
                            context,
                            dryRun = dryRun
                        )
                    }"
                }
            }
        }

        override fun addUpdateMethodAndReadProperty(
            project: Project,
            context: GenerationContext,
            writeState: WriteableState,
            dryRun: Boolean,
        ) {
            if (writeState !is ListAppState) return

            writeState.generateStatePropertiesToViewModel(project).forEach {
                context.addProperty(it, dryRun)
            }

            readProperty.transformedCodeBlock(project, context, dryRun = dryRun).let { codeBlock ->
                val funSpecBuilder = writeState.generateUpdateValueAtIndexFunBuilder(
                    project = project,
                    context = context,
                    functionName = getUpdateMethodName(
                        project = project,
                        writeState = writeState,
                    ),
                    writeState = writeState,
                    readCodeBlock = writeState.valueType(project, asNonList = true)
                        .convertCodeFromType(
                            readProperty.valueType(project),
                            codeBlock = codeBlock,
                        ),
                )
                readProperty.generateParameterSpec(project)?.let {
                    funSpecBuilder.addParameter(it)
                }
                context.addFunction(
                    funSpecBuilder.build(),
                    dryRun
                )
            }
        }

        @Composable
        override fun displayName(): String = stringResource(Res.string.update_value_at_index)

        @Composable
        override fun asDropdownText(): String = displayName()

        override fun isSameItem(item: Any): Boolean =
            item is UpdateValueAtIndex
    }

    @Serializable
    @SerialName("UpdateValueAtIndexForCustomDataType")
    data class UpdateValueAtIndexForCustomDataType(
        // Represents the index to update
        override val indexProperty: IntProperty = IntProperty.IntIntrinsicValue(),
        override val dataFieldUpdateProperties: MutableList<DataFieldUpdateProperty> = mutableListOf(),
    ) : StateOperationForDataType, StateOperationWithIndexProperty, StateOperationForList,
        DropdownItem {

        override fun isDependent(sourceId: String): Boolean {
            return indexProperty.isDependent(sourceId) ||
                    dataFieldUpdateProperties.any {
                        it.assignableProperty.isDependent(
                            sourceId
                        )
                    }
        }

        override fun getDependentComposeNodes(project: Project): List<ComposeNode> {
            return dataFieldUpdateProperties.flatMap {
                it.assignableProperty.getDependentComposeNodes(
                    project
                )
            }
        }

        override fun getAssignableProperties(): List<AssignableProperty> =
            dataFieldUpdateProperties.map {
                it.assignableProperty
            }

        override fun getUpdateMethodName(project: Project, writeState: WriteableState): String {
            check(writeState is AppState.CustomDataTypeListAppState)
            val dataTypeId = writeState.dataTypeId
            val dataType = project.findDataTypeOrThrow(dataTypeId)
            return "onUpdateValueFor${dataType.className}AtIndex"
        }

        override fun getUpdateMethodParamsAsString(
            project: Project,
            context: GenerationContext,
            dryRun: Boolean,
        ): String {
            val indexString = getUpdateIndexParamsAsString(project, context)
            return buildString {
                append("${indexString},")
                dataFieldUpdateProperties.forEach { (_, readProperty) ->
                    readProperty.generateParameterSpec(project)?.let {
                        append(
                            "${it.name} = ${
                                readProperty.generateCodeBlock(
                                    project,
                                    context,
                                    dryRun = dryRun
                                )
                            },"
                        )
                    }
                }
            }
        }

        override fun addUpdateMethodAndReadProperty(
            project: Project,
            context: GenerationContext,
            writeState: WriteableState,
            dryRun: Boolean,
        ) {
            check(writeState is AppState.CustomDataTypeListAppState)
            val dataTypeId = writeState.dataTypeId
            val dataType = project.findDataTypeOrThrow(dataTypeId)

            val indexToUpdate = "indexToUpdate"
            val funSpecBuilder = FunSpec.builder(
                getUpdateMethodName(project = project, writeState = writeState),
            ).addParameter(
                ParameterSpec.builder(name = indexToUpdate, Int::class).build(),
            )

            val item = "item"
            val updateString = buildString {
                dataFieldUpdateProperties.forEach { properties ->
                    val dataField = dataType.findDataFieldOrNull(properties.dataFieldId)

                    dataField?.let {
                        val readCodeBlock: CodeBlock = when (properties.fieldUpdateType) {
                            FieldUpdateType.ClearValue -> dataField.fieldType.defaultValueAsCodeBlock(
                                project,
                            )

                            FieldUpdateType.ToggleValue -> {
                                when (dataField.fieldType.type()) {
                                    is ComposeFlowType.BooleanType -> CodeBlock.of("!$item.${dataField.variableName}")
                                    else -> throw IllegalArgumentException()
                                }
                            }

                            FieldUpdateType.SetValue -> properties.assignableProperty.transformedCodeBlock(
                                project,
                                context,
                                dryRun = dryRun,
                            )
                        }
                        readCodeBlock.let {
                            val expression = dataField.fieldType.type().convertCodeFromType(
                                inputType = properties.assignableProperty.valueType(project),
                                codeBlock = it,
                            )
                            append("${dataField.variableName} = $expression,\n")
                        }
                    }

                    properties.assignableProperty.addReadProperty(
                        project,
                        context,
                        dryRun = dryRun,
                    )
                    properties.assignableProperty.generateParameterSpec(project)?.let {
                        funSpecBuilder.addParameter(it)
                    }
                }
            }

            funSpecBuilder.addCode(
                """
                val list = ${writeState.getFlowName()}.value.toMutableList()
                if ($indexToUpdate < 0 || $indexToUpdate >= list.size) return     
                %M.%M {
                    val $item = list[$indexToUpdate]
                    list.set($indexToUpdate, $item.copy($updateString))
                    ${ViewModelConstant.flowSettings.name}.putString("${writeState.name}", ${ViewModelConstant.jsonSerializer.name}.%M(list))
                }""",
                MemberHolder.PreCompose.viewModelScope,
                MemberHolder.Coroutines.launch,
                MemberHolder.Serialization.encodeToString,
            )
            context.addFunction(funSpecBuilder.build(), dryRun)
            writeState.generateStatePropertiesToViewModel(project).forEach {
                context.addProperty(it, dryRun)
            }
        }

        @Composable
        override fun displayName(): String = stringResource(Res.string.update_value_at_index)

        @Composable
        override fun asDropdownText(): String = displayName()

        override fun isSameItem(item: Any): Boolean = item is UpdateValueAtIndexForCustomDataType
    }

    @Serializable
    @SerialName("RemoveValueAtIndex")
    data class RemoveValueAtIndex(
        // Represents the index to remove
        override val indexProperty: IntProperty = IntProperty.IntIntrinsicValue(),
    ) : StateOperationForList, StateOperationWithIndexProperty, DropdownItem {

        override fun isDependent(sourceId: String): Boolean {
            return indexProperty.isDependent(sourceId)
        }

        override fun getUpdateMethodName(project: Project, writeState: WriteableState): String {
            return "onRemoveValueFrom${writeState.name.capitalize(Locale.current)}AtIndex"
        }

        override fun getUpdateMethodParamsAsString(
            project: Project,
            context: GenerationContext,
            dryRun: Boolean,
        ): String = getUpdateIndexParamsAsString(project, context)

        override fun addUpdateMethodAndReadProperty(
            project: Project,
            context: GenerationContext,
            writeState: WriteableState,
            dryRun: Boolean,
        ) {
            if (writeState !is ListAppState) return

            context.addFunction(
                writeState.generateRemoveValueAtIndexFun(
                    project = project,
                    context = context,
                    functionName = getUpdateMethodName(
                        project = project,
                        writeState = writeState,
                    ),
                    writeState = writeState,
                ),
                dryRun = dryRun
            )
            writeState.generateStatePropertiesToViewModel(project).forEach {
                context.addProperty(it, dryRun)
            }
        }

        @Composable
        override fun displayName(): String = stringResource(Res.string.remove_value_at_index)

        @Composable
        override fun asDropdownText(): String = displayName()

        override fun isSameItem(item: Any): Boolean = item is RemoveValueAtIndex
    }

    @Serializable
    @SerialName("RemoveFirstValue")
    data object RemoveFirstValue : StateOperationForList, DropdownItem {

        override fun isDependent(sourceId: String): Boolean = false

        override fun getUpdateMethodName(project: Project, writeState: WriteableState): String {
            return "onRemoveFirstValueFrom${writeState.name.capitalize(Locale.current)}"
        }

        override fun addUpdateMethodAndReadProperty(
            project: Project,
            context: GenerationContext,
            writeState: WriteableState,
            dryRun: Boolean,
        ) {
            val funSpecBuilder = FunSpec.builder(
                getUpdateMethodName(
                    project = project,
                    writeState = writeState,
                ),
            )
            if (writeState !is ListAppState) return
            context.addFunction(
                funSpecBuilder.addCode(
                    writeState.generateRemoveFirstValueCode(
                        project = project,
                        context = context,
                        writeState = writeState,
                    ),
                ).build(),
                dryRun = dryRun,
            )
            writeState.generateStatePropertiesToViewModel(project).forEach {
                context.addProperty(it, dryRun)
            }
        }

        @Composable
        override fun displayName(): String = stringResource(Res.string.remove_first_value)

        @Composable
        override fun asDropdownText(): String = displayName()

        override fun isSameItem(item: Any): Boolean = item == RemoveFirstValue
    }

    @Serializable
    @SerialName("RemoveLastValue")
    data object RemoveLastValue : StateOperationForList, DropdownItem {

        override fun isDependent(sourceId: String): Boolean = false

        override fun getUpdateMethodName(project: Project, writeState: WriteableState): String {
            return "onRemoveLastValueFrom${writeState.name.capitalize(Locale.current)}"
        }

        override fun addUpdateMethodAndReadProperty(
            project: Project,
            context: GenerationContext,
            writeState: WriteableState,
            dryRun: Boolean,
        ) {
            val funSpecBuilder = FunSpec.builder(
                getUpdateMethodName(
                    project = project,
                    writeState = writeState,
                ),
            )
            if (writeState !is ListAppState) return
            context.addFunction(
                funSpecBuilder.addCode(
                    writeState.generateRemoveLastValueCode(
                        project = project,
                        context = context,
                        writeState = writeState,
                    ),
                ).build(),
                dryRun = dryRun
            )
            writeState.generateStatePropertiesToViewModel(project).forEach {
                context.addProperty(it, dryRun)
            }
        }

        @Composable
        override fun displayName(): String = stringResource(Res.string.remove_last_value)

        @Composable
        override fun asDropdownText(): String = displayName()

        override fun isSameItem(item: Any): Boolean = item == RemoveLastValue
    }
}

sealed interface StateOperationWithReadProperty : StateOperation {
    val readProperty: AssignableProperty

    fun copyWith(readProperty: AssignableProperty): StateOperationWithReadProperty {
        return when (this) {
            is StateOperationForList.AddValue -> {
                this.copy(readProperty = readProperty)
            }

            is StateOperation.SetValue -> {
                this.copy(readProperty = readProperty)
            }

            is StateOperationForList.UpdateValueAtIndex -> {
                this.copy(readProperty = readProperty)
            }
        }
    }
}

sealed interface StateOperationWithIndexProperty : StateOperation {
    val indexProperty: IntProperty

    fun getUpdateIndexParamsAsString(project: Project, context: GenerationContext): String {
        return when (val property = indexProperty) {
            is IntProperty.IntIntrinsicValue -> {
                property.value.toString()
            }

            is IntProperty.ValueFromLazyListIndex -> {
                val lazyList = project.findComposeNodeOrThrow(property.lazyListNodeId)
                lazyList.trait.value.iconText().replaceFirstChar { it.lowercase() } + "Index"
            }
        }
    }

    fun copyWith(indexProperty: IntProperty): StateOperationWithIndexProperty {
        return when (this) {
            is StateOperationForList.RemoveValueAtIndex -> {
                this.copy(indexProperty = indexProperty)
            }

            is StateOperationForList.UpdateValueAtIndex -> {
                this.copy(indexProperty = indexProperty)
            }

            is StateOperationForList.UpdateValueAtIndexForCustomDataType -> {
                this.copy(indexProperty = indexProperty)
            }
        }
    }
}

sealed interface StateOperationProvider {
    fun entries(): List<StateOperation>
}

data object StateOperationForString : StateOperationProvider {
    override fun entries(): List<StateOperation> = listOf(
        StateOperation.SetValue(readProperty = StringProperty.StringIntrinsicValue()),
        StateOperation.ClearValue,
    )
}

data object StateOperationForInt : StateOperationProvider {
    override fun entries(): List<StateOperation> = listOf(
        StateOperation.SetValue(readProperty = IntProperty.IntIntrinsicValue()),
        StateOperation.ClearValue,
    )
}

data object StateOperationForFloat : StateOperationProvider {
    override fun entries(): List<StateOperation> = listOf(
        StateOperation.SetValue(readProperty = FloatProperty.FloatIntrinsicValue()),
        StateOperation.ClearValue,
    )
}

data object StateOperationForBoolean : StateOperationProvider {
    override fun entries(): List<StateOperation> = listOf(
        StateOperation.SetValue(readProperty = BooleanProperty.BooleanIntrinsicValue()),
        StateOperation.ClearValue,
        StateOperation.ToggleValue,
    )
}

data object StateOperationForStringList : StateOperationProvider {
    override fun entries(): List<StateOperation> = listOf(
        StateOperationForList.AddValue(readProperty = StringProperty.StringIntrinsicValue()),
        StateOperationForList.UpdateValueAtIndex(readProperty = StringProperty.StringIntrinsicValue()),
        StateOperationForList.RemoveFirstValue,
        StateOperationForList.RemoveLastValue,
        StateOperationForList.RemoveValueAtIndex(),
        StateOperation.ClearValue,
    )
}

data object StateOperationForIntList : StateOperationProvider {
    override fun entries(): List<StateOperation> = listOf(
        StateOperationForList.AddValue(readProperty = IntProperty.IntIntrinsicValue()),
        StateOperationForList.UpdateValueAtIndex(readProperty = IntProperty.IntIntrinsicValue()),
        StateOperationForList.RemoveFirstValue,
        StateOperationForList.RemoveLastValue,
        StateOperationForList.RemoveValueAtIndex(),
        StateOperation.ClearValue,
    )
}

data object StateOperationForFloatList : StateOperationProvider {
    override fun entries(): List<StateOperation> = listOf(
        StateOperationForList.AddValue(readProperty = FloatProperty.FloatIntrinsicValue()),
        StateOperationForList.UpdateValueAtIndex(readProperty = FloatProperty.FloatIntrinsicValue()),
        StateOperationForList.RemoveFirstValue,
        StateOperationForList.RemoveLastValue,
        StateOperationForList.RemoveValueAtIndex(),
        StateOperation.ClearValue,
    )
}

data object StateOperationForBooleanList : StateOperationProvider {
    override fun entries(): List<StateOperation> = listOf(
        StateOperationForList.AddValue(readProperty = BooleanProperty.BooleanIntrinsicValue()),
        StateOperationForList.UpdateValueAtIndex(readProperty = BooleanProperty.BooleanIntrinsicValue()),
        StateOperationForList.RemoveFirstValue,
        StateOperationForList.RemoveLastValue,
        StateOperationForList.RemoveValueAtIndex(),
        StateOperation.ClearValue,
    )
}

data object StateOperationForCustomDataType : StateOperationProvider {
    override fun entries(): List<StateOperation> = listOf(
        StateOperationForDataType.DataTypeSetValue(),
        StateOperation.ClearValue,
    )
}

data object StateOperationForCustomDataTypeList : StateOperationProvider {
    override fun entries(): List<StateOperation> = listOf(
        StateOperationForList.AddValueForCustomDataType(),
        StateOperationForList.UpdateValueAtIndexForCustomDataType(),
        StateOperationForList.RemoveFirstValue,
        StateOperationForList.RemoveLastValue,
        StateOperationForList.RemoveValueAtIndex(),
        StateOperation.ClearValue,
    )
}
