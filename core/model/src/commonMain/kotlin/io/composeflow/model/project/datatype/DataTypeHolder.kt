package io.composeflow.model.project.datatype

import io.composeflow.kotlinpoet.FileSpecWithDirectory
import io.composeflow.kotlinpoet.wrapper.FileSpecWrapper
import io.composeflow.model.datatype.DATA_TYPE_PACKAGE
import io.composeflow.model.datatype.DataType
import io.composeflow.model.project.Project
import io.composeflow.override.mutableStateListEqualsOverrideOf
import io.composeflow.serializer.FallbackMutableStateListSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("DataTypeHolder")
data class DataTypeHolder(
    @Serializable(with = FallbackMutableStateListSerializer::class)
    val dataTypes: MutableList<DataType> = mutableStateListEqualsOverrideOf(),
) {
    fun generateDataTypeFiles(project: Project): List<FileSpecWithDirectory> =
        dataTypes.mapNotNull { dataType ->
            dataType.generateDataClassSpec(project)?.let {
                FileSpecWrapper
                    .builder("${project.packageName}.$DATA_TYPE_PACKAGE", dataType.className)
                    .addType(it)
                    .build()
            }
        }
            .map {
                FileSpecWithDirectory(it)
            }
}
