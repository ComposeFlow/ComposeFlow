package io.composeflow.kotlinpoet

import io.composeflow.kotlinpoet.wrapper.FileSpecWrapper

data class FileSpecWithDirectory(
    val fileSpec: FileSpecWrapper,
    val baseDirectory: BaseDirectory = BaseDirectory.CommonMainKotlin,
)

enum class BaseDirectory(
    val directoryName: String,
) {
    AndroidMainKotlin("composeApp/src/androidMain/kotlin/"),
    CommonMainKotlin("composeApp/src/commonMain/kotlin/"),
}
