package io.composeflow.ai

import co.touchlab.kermit.Logger
import io.composeflow.ai.openrouter.tools.ToolArgs
import io.composeflow.model.project.Project
import io.composeflow.model.project.appscreen.screen.Screen
import io.composeflow.serializer.encodeToString
import io.composeflow.ui.EventResult
import io.composeflow.ui.appstate.AppStateEditorOperator
import io.composeflow.ui.datatype.DataTypeEditorOperator
import io.composeflow.ui.uibuilder.UiBuilderOperator
import io.composeflow.ui.uibuilder.toScreenSummary

/**
 * Handles dispatching tool execution requests to appropriate operators.
 */
class ToolDispatcher(
    private val uiBuilderOperator: UiBuilderOperator = UiBuilderOperator(),
    private val appStateEditorOperator: AppStateEditorOperator = AppStateEditorOperator(),
    private val dataTypeEditorOperator: DataTypeEditorOperator = DataTypeEditorOperator(),
) {
    /**
     * Dispatches a tool execution request to the appropriate operator.
     *
     * @param project The project to operate on
     * @param toolArgs The tool arguments containing the operation details
     * @return EventResult indicating success or failure with error messages
     */
    suspend fun dispatchToolResponse(
        project: Project,
        toolArgs: ToolArgs,
    ): EventResult {
        Logger.i("dispatchToolResponse: $toolArgs")

        return when (toolArgs) {
            is ToolArgs.AddComposeNodeArgs -> {
                uiBuilderOperator.onAddComposeNodeToContainerNode(
                    project,
                    toolArgs.containerNodeId,
                    toolArgs.composeNodeYaml,
                    toolArgs.indexToDrop,
                )
            }

            is ToolArgs.AddModifierArgs -> {
                uiBuilderOperator.onAddModifier(
                    project,
                    toolArgs.composeNodeId,
                    toolArgs.modifierYaml,
                )
            }

            is ToolArgs.MoveComposeNodeToContainerArgs -> {
                uiBuilderOperator.onMoveComposeNodeToContainer(
                    project,
                    toolArgs.composeNodeId,
                    toolArgs.containerNodeId,
                    toolArgs.index,
                )
            }

            is ToolArgs.RemoveComposeNodeArgs -> {
                uiBuilderOperator.onRemoveComposeNode(
                    project,
                    toolArgs.composeNodeId,
                )
            }

            is ToolArgs.RemoveModifierArgs -> {
                uiBuilderOperator.onRemoveModifier(
                    project,
                    toolArgs.composeNodeId,
                    toolArgs.index,
                )
            }

            is ToolArgs.SwapModifiersArgs -> {
                uiBuilderOperator.onSwapModifiers(
                    project,
                    toolArgs.composeNodeId,
                    toolArgs.fromIndex,
                    toolArgs.toIndex,
                )
            }

            is ToolArgs.UpdateModifierArgs -> {
                uiBuilderOperator.onUpdateModifier(
                    project,
                    toolArgs.composeNodeId,
                    toolArgs.index,
                    toolArgs.modifierYaml,
                )
            }

            is ToolArgs.AddAppStateArgs -> {
                val result =
                    appStateEditorOperator.onAddAppState(
                        project,
                        toolArgs.appStateYaml,
                    )
                if (result.errorMessages.isNotEmpty()) {
                    toolArgs.result = result.errorMessages.joinToString("; ")
                }
                result
            }

            is ToolArgs.DeleteAppStateArgs -> {
                val result =
                    appStateEditorOperator.onDeleteAppState(
                        project,
                        toolArgs.appStateId,
                    )
                if (result.errorMessages.isNotEmpty()) {
                    toolArgs.result = result.errorMessages.joinToString("; ")
                }
                result
            }

            is ToolArgs.UpdateAppStateArgs -> {
                val result =
                    appStateEditorOperator.onUpdateAppState(
                        project,
                        toolArgs.appStateYaml,
                    )
                if (result.errorMessages.isNotEmpty()) {
                    toolArgs.result = result.errorMessages.joinToString("; ")
                }
                result
            }

            is ToolArgs.UpdateCustomDataTypeListDefaultValuesArgs -> {
                val result =
                    appStateEditorOperator.onUpdateCustomDataTypeListDefaultValues(
                        project,
                        toolArgs.appStateId,
                        toolArgs.defaultValuesYaml,
                    )
                if (result.errorMessages.isNotEmpty()) {
                    toolArgs.result = result.errorMessages.joinToString("; ")
                }
                result
            }

            is ToolArgs.ListAppStatesArgs -> {
                try {
                    val appStatesResult = appStateEditorOperator.onListAppStates(project)
                    toolArgs.result = appStatesResult
                    EventResult() // Success
                } catch (e: Exception) {
                    Logger.e(e) { "Error listing app states" }
                    toolArgs.result = "Error listing app states: ${e.message}"
                    EventResult().apply { errorMessages.add("Failed to list app states: ${e.message}") }
                }
            }

            is ToolArgs.GetAppStateArgs -> {
                try {
                    val appStateResult =
                        appStateEditorOperator.onGetAppState(project, toolArgs.appStateId)
                    toolArgs.result = appStateResult
                    EventResult() // Success
                } catch (e: Exception) {
                    Logger.e(e) { "Error getting app state" }
                    toolArgs.result = "Error getting app state: ${e.message}"
                    EventResult().apply { errorMessages.add("Failed to get app state: ${e.message}") }
                }
            }

            is ToolArgs.AddDataTypeArgs -> {
                val result =
                    dataTypeEditorOperator.onAddDataType(
                        project,
                        toolArgs.dataTypeYaml,
                    )
                if (result.errorMessages.isNotEmpty()) {
                    toolArgs.result = result.errorMessages.joinToString("; ")
                }
                result
            }

            is ToolArgs.DeleteDataTypeArgs -> {
                val result =
                    dataTypeEditorOperator.onDeleteDataType(
                        project,
                        toolArgs.dataTypeId,
                    )
                if (result.errorMessages.isNotEmpty()) {
                    toolArgs.result = result.errorMessages.joinToString("; ")
                }
                result
            }

            is ToolArgs.UpdateDataTypeArgs -> {
                val result =
                    dataTypeEditorOperator.onUpdateDataType(
                        project,
                        toolArgs.dataTypeYaml,
                    )
                if (result.errorMessages.isNotEmpty()) {
                    toolArgs.result = result.errorMessages.joinToString("; ")
                }
                result
            }

            is ToolArgs.AddDataFieldArgs -> {
                val result =
                    dataTypeEditorOperator.onAddDataField(
                        project,
                        toolArgs.dataTypeId,
                        toolArgs.dataFieldYaml,
                    )
                if (result.errorMessages.isNotEmpty()) {
                    toolArgs.result = result.errorMessages.joinToString("; ")
                }
                result
            }

            is ToolArgs.DeleteDataFieldArgs -> {
                val result =
                    dataTypeEditorOperator.onDeleteDataField(
                        project,
                        toolArgs.dataTypeId,
                        toolArgs.dataFieldId,
                    )
                if (result.errorMessages.isNotEmpty()) {
                    toolArgs.result = result.errorMessages.joinToString("; ")
                }
                result
            }

            is ToolArgs.AddCustomEnumArgs -> {
                val result =
                    dataTypeEditorOperator.onAddCustomEnum(
                        project,
                        toolArgs.customEnumYaml,
                    )
                if (result.errorMessages.isNotEmpty()) {
                    toolArgs.result = result.errorMessages.joinToString("; ")
                }
                result
            }

            is ToolArgs.DeleteCustomEnumArgs -> {
                val result =
                    dataTypeEditorOperator.onDeleteCustomEnum(
                        project,
                        toolArgs.customEnumId,
                    )
                if (result.errorMessages.isNotEmpty()) {
                    toolArgs.result = result.errorMessages.joinToString("; ")
                }
                result
            }

            is ToolArgs.UpdateCustomEnumArgs -> {
                val result =
                    dataTypeEditorOperator.onUpdateCustomEnum(
                        project,
                        toolArgs.customEnumYaml,
                    )
                if (result.errorMessages.isNotEmpty()) {
                    toolArgs.result = result.errorMessages.joinToString("; ")
                }
                result
            }

            is ToolArgs.ListDataTypesArgs -> {
                try {
                    val dataTypesResult = dataTypeEditorOperator.onListDataTypes(project)
                    toolArgs.result = dataTypesResult
                    EventResult() // Success
                } catch (e: Exception) {
                    Logger.e(e) { "Error listing data types" }
                    toolArgs.result = "Error listing data types: ${e.message}"
                    EventResult().apply { errorMessages.add("Failed to list data types: ${e.message}") }
                }
            }

            is ToolArgs.GetDataTypeArgs -> {
                try {
                    val dataTypeResult =
                        dataTypeEditorOperator.onGetDataType(project, toolArgs.dataTypeId)
                    toolArgs.result = dataTypeResult
                    EventResult() // Success
                } catch (e: Exception) {
                    Logger.e(e) { "Error getting data type" }
                    toolArgs.result = "Error getting data type: ${e.message}"
                    EventResult().apply { errorMessages.add("Failed to get data type: ${e.message}") }
                }
            }

            is ToolArgs.ListCustomEnumsArgs -> {
                try {
                    val customEnumsResult = dataTypeEditorOperator.onListCustomEnums(project)
                    toolArgs.result = customEnumsResult
                    EventResult() // Success
                } catch (e: Exception) {
                    Logger.e(e) { "Error listing custom enums" }
                    toolArgs.result = "Error listing custom enums: ${e.message}"
                    EventResult().apply { errorMessages.add("Failed to list custom enums: ${e.message}") }
                }
            }

            is ToolArgs.GetCustomEnumArgs -> {
                try {
                    val customEnumResult =
                        dataTypeEditorOperator.onGetCustomEnum(project, toolArgs.customEnumId)
                    toolArgs.result = customEnumResult
                    EventResult() // Success
                } catch (e: Exception) {
                    Logger.e(e) { "Error getting custom enum" }
                    toolArgs.result = "Error getting custom enum: ${e.message}"
                    EventResult().apply { errorMessages.add("Failed to get custom enum: ${e.message}") }
                }
            }

            is ToolArgs.ListScreensArgs -> {
                try {
                    val result = uiBuilderOperator.onListScreens(project)
                    if (result.errorMessages.isEmpty()) {
                        val screens = project.screenHolder.screens
                        val screenList = screens.map { it.toScreenSummary() }
                        toolArgs.result = encodeToString(screenList)
                    } else {
                        toolArgs.result = result.errorMessages.joinToString("; ")
                    }
                    result
                } catch (e: Exception) {
                    Logger.e(e) { "Error listing screens" }
                    toolArgs.result = "Error listing screens: ${e.message}"
                    EventResult().apply { errorMessages.add("Failed to list screens: ${e.message}") }
                }
            }

            is ToolArgs.GetScreenDetailsArgs -> {
                try {
                    val result = uiBuilderOperator.onGetScreenDetails(project, toolArgs.screenId)
                    if (result.errorMessages.isEmpty()) {
                        val screen = project.screenHolder.findScreen(toolArgs.screenId)
                        if (screen != null) {
                            toolArgs.result = encodeToString(Screen.serializer(), screen)
                        } else {
                            toolArgs.result = "Screen with ID '${toolArgs.screenId}' not found"
                            result.errorMessages.add("Screen with ID '${toolArgs.screenId}' not found")
                        }
                    } else {
                        toolArgs.result = result.errorMessages.joinToString("; ")
                    }
                    result
                } catch (e: Exception) {
                    Logger.e(e) { "Error getting screen details" }
                    toolArgs.result = "Error getting screen details: ${e.message}"
                    EventResult().apply { errorMessages.add("Failed to get screen details: ${e.message}") }
                }
            }

            is ToolArgs.FakeArgs -> {
                EventResult()
            }
        }
    }
}
