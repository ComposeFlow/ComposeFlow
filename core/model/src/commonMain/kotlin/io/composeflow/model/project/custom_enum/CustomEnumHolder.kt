@file:Suppress("ktlint:standard:package-name")

package io.composeflow.model.project.custom_enum

import io.composeflow.kotlinpoet.wrapper.FileSpecWrapper
import io.composeflow.kotlinpoet.wrapper.suppressRedundantVisibilityModifier
import io.composeflow.kotlinpoet.FileSpecWithDirectory
import io.composeflow.model.project.Project
import io.composeflow.override.mutableStateListEqualsOverrideOf
import io.composeflow.serializer.FallbackMutableStateListSerializer
import io.composeflow.util.generateUniqueName
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("CustomEnumHolder")
data class CustomEnumHolder(
    @Serializable(with = FallbackMutableStateListSerializer::class)
    val enumList: MutableList<CustomEnum> = mutableStateListEqualsOverrideOf(),
) {
    fun generateEnumFiles(project: Project): List<FileSpecWithDirectory> =
        enumList
            .mapNotNull { enum ->
                enum.generateCustomEnumSpec()?.let {
                    val fileBuilder =
                        FileSpecWrapper
                            .builder("${project.packageName}.$ENUM_PACKAGE", enum.enumName)
                            .addType(it)
                    fileBuilder.suppressRedundantVisibilityModifier()
                    fileBuilder.build()
                }
            }.map {
                FileSpecWithDirectory(it)
            }

    fun newCustomEnum(name: String): CustomEnum {
        val newName =
            generateUniqueName(initial = name, existing = enumList.map { it.enumName }.toSet())
        return CustomEnum(name = newName)
    }
}
