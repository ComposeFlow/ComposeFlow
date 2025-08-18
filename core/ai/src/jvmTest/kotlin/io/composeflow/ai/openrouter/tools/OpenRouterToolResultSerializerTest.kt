package io.composeflow.ai.openrouter.tools

import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class OpenRouterToolResultSerializerTest {
    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun testAddDataTypeArgsSerialization() {
        val toolArgs = ToolArgs.AddDataTypeArgs("name: TestType\nfields: []")
        val toolResult =
            OpenRouterToolResult.AddDataTypeArgs(
                tool_call_id = "test-call-id",
                tool_args = toolArgs,
            )

        val serialized = json.encodeToString(OpenRouterToolResultSerializer, toolResult)
        val deserialized = json.decodeFromString(OpenRouterToolResultSerializer, serialized)

        assertEquals("add_data_type", deserialized.tool_name)
        assertEquals("test-call-id", deserialized.tool_call_id)
        assertEquals(toolArgs, deserialized.tool_args)
    }

    @Test
    fun testDeleteDataTypeArgsSerialization() {
        val toolArgs = ToolArgs.DeleteDataTypeArgs("data-type-id")
        val toolResult =
            OpenRouterToolResult.DeleteDataTypeArgs(
                tool_call_id = "test-call-id",
                tool_args = toolArgs,
            )

        val serialized = json.encodeToString(OpenRouterToolResultSerializer, toolResult)
        val deserialized = json.decodeFromString(OpenRouterToolResultSerializer, serialized)

        assertEquals("delete_data_type", deserialized.tool_name)
        assertEquals("test-call-id", deserialized.tool_call_id)
        assertEquals(toolArgs, deserialized.tool_args)
    }

    @Test
    fun testUpdateDataTypeArgsSerialization() {
        val toolArgs = ToolArgs.UpdateDataTypeArgs("name: UpdatedType\nfields: []")
        val toolResult =
            OpenRouterToolResult.UpdateDataTypeArgs(
                tool_call_id = "test-call-id",
                tool_args = toolArgs,
            )

        val serialized = json.encodeToString(OpenRouterToolResultSerializer, toolResult)
        val deserialized = json.decodeFromString(OpenRouterToolResultSerializer, serialized)

        assertEquals("update_data_type", deserialized.tool_name)
        assertEquals("test-call-id", deserialized.tool_call_id)
        assertEquals(toolArgs, deserialized.tool_args)
    }

    @Test
    fun testAddDataFieldArgsSerialization() {
        val toolArgs = ToolArgs.AddDataFieldArgs("data-type-id", "name: fieldName\ntype: String")
        val toolResult =
            OpenRouterToolResult.AddDataFieldArgs(
                tool_call_id = "test-call-id",
                tool_args = toolArgs,
            )

        val serialized = json.encodeToString(OpenRouterToolResultSerializer, toolResult)
        val deserialized = json.decodeFromString(OpenRouterToolResultSerializer, serialized)

        assertEquals("add_data_field", deserialized.tool_name)
        assertEquals("test-call-id", deserialized.tool_call_id)
        assertEquals(toolArgs, deserialized.tool_args)
    }

    @Test
    fun testDeleteDataFieldArgsSerialization() {
        val toolArgs = ToolArgs.DeleteDataFieldArgs("data-type-id", "field-id")
        val toolResult =
            OpenRouterToolResult.DeleteDataFieldArgs(
                tool_call_id = "test-call-id",
                tool_args = toolArgs,
            )

        val serialized = json.encodeToString(OpenRouterToolResultSerializer, toolResult)
        val deserialized = json.decodeFromString(OpenRouterToolResultSerializer, serialized)

        assertEquals("delete_data_field", deserialized.tool_name)
        assertEquals("test-call-id", deserialized.tool_call_id)
        assertEquals(toolArgs, deserialized.tool_args)
    }

    @Test
    fun testAddCustomEnumArgsSerialization() {
        val toolArgs = ToolArgs.AddCustomEnumArgs("name: Status\nvalues: [ACTIVE, INACTIVE]")
        val toolResult =
            OpenRouterToolResult.AddCustomEnumArgs(
                tool_call_id = "test-call-id",
                tool_args = toolArgs,
            )

        val serialized = json.encodeToString(OpenRouterToolResultSerializer, toolResult)
        val deserialized = json.decodeFromString(OpenRouterToolResultSerializer, serialized)

        assertEquals("add_custom_enum", deserialized.tool_name)
        assertEquals("test-call-id", deserialized.tool_call_id)
        assertEquals(toolArgs, deserialized.tool_args)
    }

    @Test
    fun testDeleteCustomEnumArgsSerialization() {
        val toolArgs = ToolArgs.DeleteCustomEnumArgs("enum-id")
        val toolResult =
            OpenRouterToolResult.DeleteCustomEnumArgs(
                tool_call_id = "test-call-id",
                tool_args = toolArgs,
            )

        val serialized = json.encodeToString(OpenRouterToolResultSerializer, toolResult)
        val deserialized = json.decodeFromString(OpenRouterToolResultSerializer, serialized)

        assertEquals("delete_custom_enum", deserialized.tool_name)
        assertEquals("test-call-id", deserialized.tool_call_id)
        assertEquals(toolArgs, deserialized.tool_args)
    }

    @Test
    fun testUpdateCustomEnumArgsSerialization() {
        val toolArgs = ToolArgs.UpdateCustomEnumArgs("name: Priority\nvalues: [HIGH, MEDIUM, LOW]")
        val toolResult =
            OpenRouterToolResult.UpdateCustomEnumArgs(
                tool_call_id = "test-call-id",
                tool_args = toolArgs,
            )

        val serialized = json.encodeToString(OpenRouterToolResultSerializer, toolResult)
        val deserialized = json.decodeFromString(OpenRouterToolResultSerializer, serialized)

        assertEquals("update_custom_enum", deserialized.tool_name)
        assertEquals("test-call-id", deserialized.tool_call_id)
        assertEquals(toolArgs, deserialized.tool_args)
    }

    @Test
    fun testListDataTypesArgsSerialization() {
        val toolArgs = ToolArgs.ListDataTypesArgs()
        val toolResult =
            OpenRouterToolResult.ListDataTypesArgs(
                tool_call_id = "test-call-id",
                tool_args = toolArgs,
            )

        val serialized = json.encodeToString(OpenRouterToolResultSerializer, toolResult)
        val deserialized = json.decodeFromString(OpenRouterToolResultSerializer, serialized)

        assertEquals("list_data_types", deserialized.tool_name)
        assertEquals("test-call-id", deserialized.tool_call_id)
        assertEquals(toolArgs, deserialized.tool_args)
    }

    @Test
    fun testGetDataTypeArgsSerialization() {
        val toolArgs = ToolArgs.GetDataTypeArgs("data-type-id")
        val toolResult =
            OpenRouterToolResult.GetDataTypeArgs(
                tool_call_id = "test-call-id",
                tool_args = toolArgs,
            )

        val serialized = json.encodeToString(OpenRouterToolResultSerializer, toolResult)
        val deserialized = json.decodeFromString(OpenRouterToolResultSerializer, serialized)

        assertEquals("get_data_type", deserialized.tool_name)
        assertEquals("test-call-id", deserialized.tool_call_id)
        assertEquals(toolArgs, deserialized.tool_args)
    }

    @Test
    fun testListCustomEnumsArgsSerialization() {
        val toolArgs = ToolArgs.ListCustomEnumsArgs()
        val toolResult =
            OpenRouterToolResult.ListCustomEnumsArgs(
                tool_call_id = "test-call-id",
                tool_args = toolArgs,
            )

        val serialized = json.encodeToString(OpenRouterToolResultSerializer, toolResult)
        val deserialized = json.decodeFromString(OpenRouterToolResultSerializer, serialized)

        assertEquals("list_custom_enums", deserialized.tool_name)
        assertEquals("test-call-id", deserialized.tool_call_id)
        assertEquals(toolArgs, deserialized.tool_args)
    }

    @Test
    fun testGetCustomEnumArgsSerialization() {
        val toolArgs = ToolArgs.GetCustomEnumArgs("enum-id")
        val toolResult =
            OpenRouterToolResult.GetCustomEnumArgs(
                tool_call_id = "test-call-id",
                tool_args = toolArgs,
            )

        val serialized = json.encodeToString(OpenRouterToolResultSerializer, toolResult)
        val deserialized = json.decodeFromString(OpenRouterToolResultSerializer, serialized)

        assertEquals("get_custom_enum", deserialized.tool_name)
        assertEquals("test-call-id", deserialized.tool_call_id)
        assertEquals(toolArgs, deserialized.tool_args)
    }

    @Test
    fun testSerializationWithJsonStringToolArgs() {
        // Test the case where tool_args comes as a JSON string (from API)
        val jsonString =
            """
            {
                "tool_name": "add_data_type",
                "tool_call_id": "test-call-id",
                "tool_args": "{\"dataTypeYaml\": \"name: TestType\\nfields: []\"}"
            }
            """.trimIndent()

        val deserialized = json.decodeFromString(OpenRouterToolResultSerializer, jsonString)

        assertEquals("add_data_type", deserialized.tool_name)
        assertEquals("test-call-id", deserialized.tool_call_id)
        assertNotNull(deserialized.tool_args)
    }

    @Test
    fun testSerializationWithDirectJsonObjectToolArgs() {
        // Test the case where tool_args comes as a direct JSON object
        val jsonString =
            """
            {
                "tool_name": "delete_data_type",
                "tool_call_id": "test-call-id",
                "tool_args": {
                    "dataTypeId": "data-type-id"
                }
            }
            """.trimIndent()

        val deserialized = json.decodeFromString(OpenRouterToolResultSerializer, jsonString)

        assertEquals("delete_data_type", deserialized.tool_name)
        assertEquals("test-call-id", deserialized.tool_call_id)
        assertNotNull(deserialized.tool_args)
    }

    @Test
    fun testAllDataTypeToolNamesAreHandled() {
        // Ensure all DataType-related tool names are properly handled
        val toolTestCases =
            mapOf(
                "add_data_type" to """{"dataTypeYaml": "name: Test"}""",
                "delete_data_type" to """{"dataTypeId": "test-id"}""",
                "update_data_type" to """{"dataTypeYaml": "name: Updated"}""",
                "add_data_field" to """{"dataTypeId": "test-id", "dataFieldYaml": "name: field"}""",
                "delete_data_field" to """{"dataTypeId": "test-id", "dataFieldId": "field-id"}""",
                "add_custom_enum" to """{"customEnumYaml": "name: Status"}""",
                "delete_custom_enum" to """{"customEnumId": "enum-id"}""",
                "update_custom_enum" to """{"customEnumYaml": "name: Priority"}""",
                "list_data_types" to """{"dummy": ""}""",
                "get_data_type" to """{"dataTypeId": "test-id"}""",
                "list_custom_enums" to """{"dummy": ""}""",
                "get_custom_enum" to """{"customEnumId": "enum-id"}""",
            )

        toolTestCases.forEach { (toolName, toolArgs) ->
            val jsonString =
                """
                {
                    "tool_name": "$toolName",
                    "tool_call_id": "test-call-id",
                    "tool_args": $toolArgs
                }
                """.trimIndent()

            val deserialized = json.decodeFromString(OpenRouterToolResultSerializer, jsonString)
            assertEquals(toolName, deserialized.tool_name)
        }
    }

    @Test
    fun testSerializationRoundTripConsistency() {
        // Test that serialization followed by deserialization produces the same result
        val originalResults =
            listOf(
                OpenRouterToolResult.AddDataTypeArgs(
                    tool_call_id = "id1",
                    tool_args = ToolArgs.AddDataTypeArgs("yaml1"),
                ),
                OpenRouterToolResult.DeleteDataTypeArgs(
                    tool_call_id = "id2",
                    tool_args = ToolArgs.DeleteDataTypeArgs("dtId1"),
                ),
                OpenRouterToolResult.AddCustomEnumArgs(
                    tool_call_id = "id3",
                    tool_args = ToolArgs.AddCustomEnumArgs("enumYaml1"),
                ),
                OpenRouterToolResult.ListDataTypesArgs(
                    tool_call_id = "id4",
                    tool_args = ToolArgs.ListDataTypesArgs(),
                ),
            )

        originalResults.forEach { original ->
            val serialized = json.encodeToString(OpenRouterToolResultSerializer, original)
            val deserialized = json.decodeFromString(OpenRouterToolResultSerializer, serialized)

            assertEquals(original.tool_name, deserialized.tool_name)
            assertEquals(original.tool_call_id, deserialized.tool_call_id)
            assertEquals(original.tool_args, deserialized.tool_args)
        }
    }
}
