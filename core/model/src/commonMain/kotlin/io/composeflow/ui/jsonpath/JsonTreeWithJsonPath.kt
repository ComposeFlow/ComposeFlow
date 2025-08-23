package io.composeflow.ui.jsonpath

import androidx.compose.runtime.Composable
import io.composeflow.model.apieditor.JsonWithJsonPath
import io.composeflow.ui.treeview.node.Branch
import io.composeflow.ui.treeview.node.Leaf
import io.composeflow.ui.treeview.tree.Tree
import io.composeflow.ui.treeview.tree.TreeScope
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull

@Composable
fun createJsonTreeWithJsonPath(json: String): Tree<JsonWithJsonPath> =
    Tree {
        jsonNode(
            key = null,
            jsonElement = JsonWithJsonPath("", Json.parseToJsonElement(json)),
        )
    }

@Composable
private fun TreeScope.jsonNode(
    key: String?,
    jsonElement: JsonWithJsonPath,
) {
    when (jsonElement.jsonElement) {
        is JsonNull -> jsonPrimitiveNode(key, jsonElement)
        is JsonPrimitive -> jsonPrimitiveNode(key, jsonElement)
        is JsonObject -> jsonObjectNode(key, jsonElement)
        is JsonArray -> jsonArrayNode(key, jsonElement)
    }
}

@Composable
private fun TreeScope.jsonPrimitiveNode(
    key: String?,
    jsonPrimitive: JsonWithJsonPath,
) {
    Leaf(
        content =
            jsonPrimitive
                .copy(displayName = "${getFormattedKey(key)}${getFormattedValue(jsonPrimitive.jsonElement as JsonPrimitive)}"),
        key = jsonPrimitive.jsonPath.ifEmpty { "root" } + "_leaf_" + key.orEmpty(),
        customName = { node ->
            androidx.compose.material3.Text(
                text = node.content.displayName ?: "",
                style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
            )
        },
    )
}

@Composable
private fun TreeScope.jsonObjectNode(
    key: String?,
    jsonObject: JsonWithJsonPath,
) {
    Branch(
        content = jsonObject.copy(displayName = "${getFormattedKey(key)}{object}"),
        key = jsonObject.jsonPath.ifEmpty { "root" } + "_object_" + key.orEmpty(),
        customName = { node ->
            androidx.compose.material3.Text(
                text = node.content.displayName ?: "",
                style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
            )
        },
    ) {
        (jsonObject.jsonElement as JsonObject).entries.forEach { (key, jsonElement) ->
            val parentPath = if (jsonObject.jsonPath.isEmpty()) "" else "${jsonObject.jsonPath}."
            val path = parentPath + key
            this.jsonNode(key, JsonWithJsonPath(path, jsonElement))
        }
    }
}

@Composable
private fun TreeScope.jsonArrayNode(
    key: String?,
    jsonArray: JsonWithJsonPath,
) {
    Branch(
        content = jsonArray.copy(displayName = "${getFormattedKey(key)}[array]"),
        key = jsonArray.jsonPath.ifEmpty { "root" } + "_array_" + key.orEmpty(),
        customName = { node ->
            androidx.compose.material3.Text(
                text = node.content.displayName ?: "",
                style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
            )
        },
    ) {
        (jsonArray.jsonElement as JsonArray).forEachIndexed { index, jsonElement ->
            val path = "${jsonArray.jsonPath}[$index]"
            this.jsonNode(index.toString(), JsonWithJsonPath(path, jsonElement))
        }
    }
}

private fun getFormattedKey(key: String?) =
    if (key.isNullOrBlank()) {
        ""
    } else {
        "$key: "
    }

private fun getFormattedValue(jsonPrimitive: JsonPrimitive) =
    if (jsonPrimitive.isString) {
        "\"${jsonPrimitive.contentOrNull}\""
    } else {
        jsonPrimitive.contentOrNull
    }
