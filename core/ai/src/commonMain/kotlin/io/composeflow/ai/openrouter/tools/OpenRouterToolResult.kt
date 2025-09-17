package io.composeflow.ai.openrouter.tools

import kotlinx.serialization.Serializable

/**
 * Interface for all tool result types.
 * Each implementation should have a unique SerialName annotation matching the tool_name in API responses.
 *
 * Suppress the naming lint warning for naming because the name needs to match with the result
 * with OpenRouter.
 */
@Serializable(with = OpenRouterToolResultSerializer::class)
@Suppress("ktlint:standard:property-naming")
sealed interface OpenRouterToolResult {
    val tool_name: String
    val tool_call_id: String
    val tool_args: ToolArgs

    @Serializable
    data class AddComposeNodeArgs(
        override val tool_name: String = "add_compose_node_to_container",
        override val tool_call_id: String,
        override val tool_args: ToolArgs.AddComposeNodeArgs,
    ) : OpenRouterToolResult

    @Serializable
    data class RemoveComposeNodeArgs(
        override val tool_name: String = "remove_compose_node",
        override val tool_call_id: String,
        override val tool_args: ToolArgs.RemoveComposeNodeArgs,
    ) : OpenRouterToolResult

    @Serializable
    data class RemoveComposeNodesArgs(
        override val tool_name: String = "remove_compose_nodes",
        override val tool_call_id: String,
        override val tool_args: ToolArgs.RemoveComposeNodesArgs,
    ) : OpenRouterToolResult

    @Serializable
    data class AddModifierArgs(
        override val tool_name: String = "add_modifier",
        override val tool_call_id: String,
        override val tool_args: ToolArgs.AddModifierArgs,
    ) : OpenRouterToolResult

    @Serializable
    data class UpdateModifierArgs(
        override val tool_name: String = "update_modifier",
        override val tool_call_id: String,
        override val tool_args: ToolArgs.UpdateModifierArgs,
    ) : OpenRouterToolResult

    @Serializable
    data class RemoveModifierArgs(
        override val tool_name: String = "remove_modifier",
        override val tool_call_id: String,
        override val tool_args: ToolArgs.RemoveModifierArgs,
    ) : OpenRouterToolResult

    @Serializable
    data class SwapModifiersArgs(
        override val tool_name: String = "swap_modifiers",
        override val tool_call_id: String,
        override val tool_args: ToolArgs.SwapModifiersArgs,
    ) : OpenRouterToolResult

    @Serializable
    data class UpdateTraitArgs(
        override val tool_name: String = "update_trait",
        override val tool_call_id: String,
        override val tool_args: ToolArgs.UpdateTraitArgs,
    ) : OpenRouterToolResult

    @Serializable
    data class MoveComposeNodeToContainerArgs(
        override val tool_name: String = "move_compose_node_to_container",
        override val tool_call_id: String,
        override val tool_args: ToolArgs.MoveComposeNodeToContainerArgs,
    ) : OpenRouterToolResult

    @Serializable
    data class AddAppStateArgs(
        override val tool_name: String = "add_app_state",
        override val tool_call_id: String,
        override val tool_args: ToolArgs.AddAppStateArgs,
    ) : OpenRouterToolResult

    @Serializable
    data class DeleteAppStateArgs(
        override val tool_name: String = "delete_app_state",
        override val tool_call_id: String,
        override val tool_args: ToolArgs.DeleteAppStateArgs,
    ) : OpenRouterToolResult

    @Serializable
    data class UpdateAppStateArgs(
        override val tool_name: String = "update_app_state",
        override val tool_call_id: String,
        override val tool_args: ToolArgs.UpdateAppStateArgs,
    ) : OpenRouterToolResult

    @Serializable
    data class UpdateCustomDataTypeListDefaultValuesArgs(
        override val tool_name: String = "update_custom_data_type_list_default_values",
        override val tool_call_id: String,
        override val tool_args: ToolArgs.UpdateCustomDataTypeListDefaultValuesArgs,
    ) : OpenRouterToolResult

    @Serializable
    data class ListAppStatesArgs(
        override val tool_name: String = "list_app_states",
        override val tool_call_id: String,
        override val tool_args: ToolArgs.ListAppStatesArgs,
    ) : OpenRouterToolResult

    @Serializable
    data class GetAppStateArgs(
        override val tool_name: String = "get_app_state",
        override val tool_call_id: String,
        override val tool_args: ToolArgs.GetAppStateArgs,
    ) : OpenRouterToolResult

    @Serializable
    data class AddDataTypeArgs(
        override val tool_name: String = "add_data_type",
        override val tool_call_id: String,
        override val tool_args: ToolArgs.AddDataTypeArgs,
    ) : OpenRouterToolResult

    @Serializable
    data class DeleteDataTypeArgs(
        override val tool_name: String = "delete_data_type",
        override val tool_call_id: String,
        override val tool_args: ToolArgs.DeleteDataTypeArgs,
    ) : OpenRouterToolResult

    @Serializable
    data class UpdateDataTypeArgs(
        override val tool_name: String = "update_data_type",
        override val tool_call_id: String,
        override val tool_args: ToolArgs.UpdateDataTypeArgs,
    ) : OpenRouterToolResult

    @Serializable
    data class AddDataFieldArgs(
        override val tool_name: String = "add_data_field",
        override val tool_call_id: String,
        override val tool_args: ToolArgs.AddDataFieldArgs,
    ) : OpenRouterToolResult

    @Serializable
    data class DeleteDataFieldArgs(
        override val tool_name: String = "delete_data_field",
        override val tool_call_id: String,
        override val tool_args: ToolArgs.DeleteDataFieldArgs,
    ) : OpenRouterToolResult

    @Serializable
    data class AddCustomEnumArgs(
        override val tool_name: String = "add_custom_enum",
        override val tool_call_id: String,
        override val tool_args: ToolArgs.AddCustomEnumArgs,
    ) : OpenRouterToolResult

    @Serializable
    data class DeleteCustomEnumArgs(
        override val tool_name: String = "delete_custom_enum",
        override val tool_call_id: String,
        override val tool_args: ToolArgs.DeleteCustomEnumArgs,
    ) : OpenRouterToolResult

    @Serializable
    data class UpdateCustomEnumArgs(
        override val tool_name: String = "update_custom_enum",
        override val tool_call_id: String,
        override val tool_args: ToolArgs.UpdateCustomEnumArgs,
    ) : OpenRouterToolResult

    @Serializable
    data class ListDataTypesArgs(
        override val tool_name: String = "list_data_types",
        override val tool_call_id: String,
        override val tool_args: ToolArgs.ListDataTypesArgs,
    ) : OpenRouterToolResult

    @Serializable
    data class GetDataTypeArgs(
        override val tool_name: String = "get_data_type",
        override val tool_call_id: String,
        override val tool_args: ToolArgs.GetDataTypeArgs,
    ) : OpenRouterToolResult

    @Serializable
    data class ListCustomEnumsArgs(
        override val tool_name: String = "list_custom_enums",
        override val tool_call_id: String,
        override val tool_args: ToolArgs.ListCustomEnumsArgs,
    ) : OpenRouterToolResult

    @Serializable
    data class GetCustomEnumArgs(
        override val tool_name: String = "get_custom_enum",
        override val tool_call_id: String,
        override val tool_args: ToolArgs.GetCustomEnumArgs,
    ) : OpenRouterToolResult

    @Serializable
    data class GetProjectIssuesArgs(
        override val tool_name: String = "get_project_issues",
        override val tool_call_id: String,
        override val tool_args: ToolArgs.GetProjectIssuesArgs,
    ) : OpenRouterToolResult

    @Serializable
    data class ListScreensArgs(
        override val tool_name: String = "list_screens",
        override val tool_call_id: String,
        override val tool_args: ToolArgs.ListScreensArgs,
    ) : OpenRouterToolResult

    @Serializable
    data class GetScreenDetailsArgs(
        override val tool_name: String = "get_screen_details",
        override val tool_call_id: String,
        override val tool_args: ToolArgs.GetScreenDetailsArgs,
    ) : OpenRouterToolResult

    @Serializable
    data class AddStringResourcesArgs(
        override val tool_name: String = "add_string_resources",
        override val tool_call_id: String,
        override val tool_args: ToolArgs.AddStringResourcesArgs,
    ) : OpenRouterToolResult

    @Serializable
    data class DeleteStringResourcesArgs(
        override val tool_name: String = "delete_string_resources",
        override val tool_call_id: String,
        override val tool_args: ToolArgs.DeleteStringResourcesArgs,
    ) : OpenRouterToolResult

    @Serializable
    data class UpdateStringResourcesArgs(
        override val tool_name: String = "update_string_resources",
        override val tool_call_id: String,
        override val tool_args: ToolArgs.UpdateStringResourcesArgs,
    ) : OpenRouterToolResult

    @Serializable
    data class UpdateSupportedLocalesArgs(
        override val tool_name: String = "update_supported_locales",
        override val tool_call_id: String,
        override val tool_args: ToolArgs.UpdateSupportedLocalesArgs,
    ) : OpenRouterToolResult

    @Serializable
    data class SetDefaultLocaleArgs(
        override val tool_name: String = "set_default_locale",
        override val tool_call_id: String,
        override val tool_args: ToolArgs.SetDefaultLocaleArgs,
    ) : OpenRouterToolResult

    @Serializable
    data class ListStringResourcesArgs(
        override val tool_name: String = "list_string_resources",
        override val tool_call_id: String,
        override val tool_args: ToolArgs.ListStringResourcesArgs,
    ) : OpenRouterToolResult

    @Serializable
    data class GetStringResourceArgs(
        override val tool_name: String = "get_string_resource",
        override val tool_call_id: String,
        override val tool_args: ToolArgs.GetStringResourceArgs,
    ) : OpenRouterToolResult

    @Serializable
    data class GetSupportedLocalesArgs(
        override val tool_name: String = "get_supported_locales",
        override val tool_call_id: String,
        override val tool_args: ToolArgs.GetSupportedLocalesArgs,
    ) : OpenRouterToolResult
}
