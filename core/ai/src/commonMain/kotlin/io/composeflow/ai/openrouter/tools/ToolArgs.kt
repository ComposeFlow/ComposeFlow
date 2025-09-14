package io.composeflow.ai.openrouter.tools

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Interface for all tool argument types.
 * Each implementation should have a unique SerialName annotation matching the tool_name in API responses.
 */
@Serializable
sealed class ToolArgs {
    /**
     * Indicates the execution status of the tool in ComposeFlow app.
     */
    var status: ToolExecutionStatus = ToolExecutionStatus.NotExecuted

    /**
     * The result of the tool execution. For tools that return data (like list_app_states),
     * this will contain the returned data. For tools that perform actions, this contains
     * a success/error message.
     */
    var result: String = "Successfully executed."

    @Serializable
    @SerialName("add_compose_node_to_container")
    data class AddComposeNodeArgs(
        val containerNodeId: String,
        val composeNodeYaml: String,
        val indexToDrop: Int = 0,
    ) : ToolArgs()

    @Serializable
    @SerialName("remove_compose_node")
    data class RemoveComposeNodeArgs(
        val composeNodeId: String,
    ) : ToolArgs()

    @Serializable
    @SerialName("remove_compose_nodes")
    data class RemoveComposeNodesArgs(
        val composeNodeIds: String,
    ) : ToolArgs()

    @Serializable
    @SerialName("add_modifier")
    data class AddModifierArgs(
        val composeNodeId: String,
        val modifierYaml: String,
    ) : ToolArgs()

    @Serializable
    @SerialName("update_modifier")
    data class UpdateModifierArgs(
        val composeNodeId: String,
        val index: Int,
        val modifierYaml: String,
    ) : ToolArgs()

    @Serializable
    @SerialName("remove_modifier")
    data class RemoveModifierArgs(
        val composeNodeId: String,
        val index: Int,
    ) : ToolArgs()

    @Serializable
    @SerialName("swap_modifiers")
    data class SwapModifiersArgs(
        val composeNodeId: String,
        val fromIndex: Int,
        val toIndex: Int,
    ) : ToolArgs()

    @Serializable
    @SerialName("update_trait")
    data class UpdateTraitArgs(
        val composeNodeId: String,
        val traitYaml: String,
    ) : ToolArgs()

    @Serializable
    @SerialName("move_compose_node_to_container")
    data class MoveComposeNodeToContainerArgs(
        val composeNodeId: String,
        val containerNodeId: String,
        val index: Int,
    ) : ToolArgs()

    @Serializable
    @SerialName("add_app_state")
    data class AddAppStateArgs(
        val appStateYaml: String,
    ) : ToolArgs()

    @Serializable
    @SerialName("delete_app_state")
    data class DeleteAppStateArgs(
        val appStateId: String,
    ) : ToolArgs()

    @Serializable
    @SerialName("update_app_state")
    data class UpdateAppStateArgs(
        val appStateYaml: String,
    ) : ToolArgs()

    @Serializable
    @SerialName("update_custom_data_type_list_default_values")
    data class UpdateCustomDataTypeListDefaultValuesArgs(
        val appStateId: String,
        val defaultValuesYaml: String,
    ) : ToolArgs()

    @Serializable
    @SerialName("list_app_states")
    data class ListAppStatesArgs(
        // Needed because data classes need at least one parameter
        val dummy: String = "",
    ) : ToolArgs()

    @Serializable
    @SerialName("get_app_state")
    data class GetAppStateArgs(
        val appStateId: String,
    ) : ToolArgs()

    @Serializable
    @SerialName("add_data_type")
    data class AddDataTypeArgs(
        val dataTypeYaml: String,
    ) : ToolArgs()

    @Serializable
    @SerialName("delete_data_type")
    data class DeleteDataTypeArgs(
        val dataTypeId: String,
    ) : ToolArgs()

    @Serializable
    @SerialName("update_data_type")
    data class UpdateDataTypeArgs(
        val dataTypeYaml: String,
    ) : ToolArgs()

    @Serializable
    @SerialName("add_data_field")
    data class AddDataFieldArgs(
        val dataTypeId: String,
        val dataFieldYaml: String,
    ) : ToolArgs()

    @Serializable
    @SerialName("delete_data_field")
    data class DeleteDataFieldArgs(
        val dataTypeId: String,
        val dataFieldId: String,
    ) : ToolArgs()

    @Serializable
    @SerialName("add_custom_enum")
    data class AddCustomEnumArgs(
        val customEnumYaml: String,
    ) : ToolArgs()

    @Serializable
    @SerialName("delete_custom_enum")
    data class DeleteCustomEnumArgs(
        val customEnumId: String,
    ) : ToolArgs()

    @Serializable
    @SerialName("update_custom_enum")
    data class UpdateCustomEnumArgs(
        val customEnumYaml: String,
    ) : ToolArgs()

    @Serializable
    @SerialName("list_data_types")
    data class ListDataTypesArgs(
        val dummy: String = "",
    ) : ToolArgs()

    @Serializable
    @SerialName("get_data_type")
    data class GetDataTypeArgs(
        val dataTypeId: String,
    ) : ToolArgs()

    @Serializable
    @SerialName("list_custom_enums")
    data class ListCustomEnumsArgs(
        val dummy: String = "",
    ) : ToolArgs()

    @Serializable
    @SerialName("get_custom_enum")
    data class GetCustomEnumArgs(
        val customEnumId: String,
    ) : ToolArgs()

    @Serializable
    @SerialName("get_project_issues")
    data class GetProjectIssuesArgs(
        val dummy: String = "",
    ) : ToolArgs()

    @Serializable
    @SerialName("list_screens")
    data class ListScreensArgs(
        val dummy: String = "",
    ) : ToolArgs()

    @Serializable
    @SerialName("get_screen_details")
    data class GetScreenDetailsArgs(
        val screenId: String,
    ) : ToolArgs()

    @Serializable
    data class FakeArgs(
        val fakeString: String = "fakeString",
    ) : ToolArgs()
}

enum class ToolExecutionStatus {
    NotExecuted,
    Success,
    Error,
}
