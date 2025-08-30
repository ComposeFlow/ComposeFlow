package io.composeflow.model

import com.charleskorn.kaml.Yaml
import io.composeflow.model.apieditor.ApiDefinition
import io.composeflow.model.apieditor.ApiProperty
import io.composeflow.model.apieditor.JsonWithJsonPath
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Test
import kotlin.test.assertEquals

class ApiDefinitionTest {
    @Test
    fun serialize_deserialize() {
        val apiDefinition =
            ApiDefinition(
                name = "testApi",
                url = "https://example.com/api",
                headers = listOf("a" to ApiProperty.IntrinsicValue("b")),
                queryParameters = listOf("b" to ApiProperty.IntrinsicValue("c")),
                exampleJsonResponse =
                    JsonWithJsonPath(
                        jsonPath = "test",
                        Json.parseToJsonElement("""{ "result": "test" }"""),
                    ),
            )

        val encoded = Yaml.default.encodeToString(apiDefinition)
        val decoded = Yaml.default.decodeFromString<ApiDefinition>(encoded)

        assertEquals(apiDefinition, decoded)
    }
}
