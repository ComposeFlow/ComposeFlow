package io.composeflow.kotlinpoet

import io.composeflow.kotlinpoet.wrapper.ClassNameWrapper
import io.composeflow.model.project.COMPOSEFLOW_PACKAGE

object ClassHolder {
    object AndroidX {
        object Lazy {
            val GridCells = ClassNameWrapper.get("androidx.compose.foundation.lazy.grid", "GridCells")
        }

        object Ui {
            val Modifier = ClassNameWrapper.get("androidx.compose.ui", "Modifier")
            val Color = ClassNameWrapper.get("androidx.compose.ui.graphics", "Color")
        }
    }

    object Kotlin {
        val Boolean = ClassNameWrapper.get("kotlin", "Boolean")
        val Int = ClassNameWrapper.get("kotlin", "Int")
        val Float = ClassNameWrapper.get("kotlin", "Float")
        val Long = ClassNameWrapper.get("kotlin", "Long")
        val String = ClassNameWrapper.get("kotlin", "String")
    }

    object Kotlinx {
        object DateTime {
            val Clock = ClassNameWrapper.get("kotlinx.datetime", "Clock")
        }

        object Serialization {
            val JsonElement = ClassNameWrapper.get("kotlinx.serialization.json", "JsonElement")
            val JsonArray = ClassNameWrapper.get("kotlinx.serialization.json", "JsonArray")
            val JsonObject = ClassNameWrapper.get("kotlinx.serialization.json", "JsonObject")
            val JsonPrimitive = ClassNameWrapper.get("kotlinx.serialization.json", "JsonPrimitive")
            val Serializable = ClassNameWrapper.get("kotlinx.serialization", "Serializable")
        }
    }

    object ComposeFlow {
        val DataResult = ClassNameWrapper.get("$COMPOSEFLOW_PACKAGE.model", "DataResult")
        val EventResultState = ClassNameWrapper.get("$COMPOSEFLOW_PACKAGE.model", "EventResultState")
    }

    object Collections {
        val List = ClassNameWrapper.get("kotlin.collections", "List")
    }

    object Coroutines {
        object Flow {
            val StateFlow = ClassNameWrapper.get("kotlinx.coroutines.flow", "StateFlow")
            val MutableStateFlow = ClassNameWrapper.get("kotlinx.coroutines.flow", "MutableStateFlow")
        }
    }
}
