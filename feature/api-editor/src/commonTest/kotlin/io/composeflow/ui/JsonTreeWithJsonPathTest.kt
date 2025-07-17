package io.composeflow.ui

import androidx.compose.ui.test.junit4.createComposeRule
import io.composeflow.model.apieditor.JsonWithJsonPath
import io.composeflow.ui.jsonpath.createJsonTreeWithJsonPath
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import kotlin.test.Ignore
import kotlin.test.Test

// TODO: lateinit property scene has not been initialized
//       kotlin.UninitializedPropertyAccessException: lateinit property scene has not been initialized
@Ignore
class JsonTreeWithJsonPathTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun basicJsonObject() {
        composeTestRule.setContent {
            val json = """
                {"a": 1}
            """
            val jsonElement = Json.parseToJsonElement(json)
            val tree = createJsonTreeWithJsonPath(json)

            assertEquals(
                JsonWithJsonPath(
                    jsonPath = "",
                    jsonElement = jsonElement,
                    displayName = "{object}",
                ),
                tree.roots.first().data,
            )
        }
    }

    @Test
    fun listJsonElement() {
        composeTestRule.setContent {
            val json = """
                [
                  {"a": 1},
                  {"b": 2}
                ]
            """
            val jsonElement = Json.parseToJsonElement(json)
            val tree = createJsonTreeWithJsonPath(json)

            val actual = tree.roots.first().data
            assertEquals(
                JsonWithJsonPath(
                    jsonPath = "",
                    jsonElement = jsonElement,
                    displayName = "[array]",
                ),
                actual,
            )

            assertTrue(actual.jsonElement is JsonArray)
        }
    }
}
