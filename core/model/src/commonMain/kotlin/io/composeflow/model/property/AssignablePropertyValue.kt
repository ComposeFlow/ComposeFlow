package io.composeflow.model.property

import androidx.compose.runtime.mutableStateMapOf
import io.composeflow.kotlinpoet.wrapper.CodeBlockWrapper
import io.composeflow.kotlinpoet.GenerationContext
import io.composeflow.model.datatype.DataFieldId
import io.composeflow.model.datatype.DataTypeId
import io.composeflow.model.project.Project
import io.composeflow.model.project.findDataTypeOrNull
import io.composeflow.model.project.findDataTypeOrThrow
import io.composeflow.serializer.FallbackMutableStateMapSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
sealed interface AssignablePropertyValue {
    fun isDependent(sourceId: String): Boolean

    fun asCodeBlock(
        project: Project,
        context: GenerationContext,
        dryRun: Boolean,
    ): CodeBlockWrapper

    fun asSimplifiedText(
        project: Project,
        context: GenerationContext,
        dryRun: Boolean,
    ): String

    @Serializable
    @SerialName("ForPrimitive")
    data class ForPrimitive(
        val property: AssignableProperty = StringProperty.StringIntrinsicValue(""),
    ) : AssignablePropertyValue {
        override fun isDependent(sourceId: String): Boolean = property.isDependent(sourceId)

        override fun asCodeBlock(
            project: Project,
            context: GenerationContext,
            dryRun: Boolean,
        ): CodeBlockWrapper = property.transformedCodeBlock(project, context, dryRun = dryRun)

        override fun asSimplifiedText(
            project: Project,
            context: GenerationContext,
            dryRun: Boolean,
        ): String = property.transformedCodeBlock(project, context, dryRun = dryRun).toString()
    }

    @Serializable
    @SerialName("ForDataType")
    data class ForDataType(
        val dataTypeId: DataTypeId,
        @Serializable(FallbackMutableStateMapSerializer::class)
        val properties: MutableMap<DataFieldId, AssignableProperty> =
            mutableStateMapOf(),
    ) : AssignablePropertyValue {
        override fun isDependent(sourceId: String): Boolean =
            properties.any {
                it.value.isDependent(sourceId)
            }

        override fun asCodeBlock(
            project: Project,
            context: GenerationContext,
            dryRun: Boolean,
        ): CodeBlockWrapper {
            val dataType = project.findDataTypeOrThrow(dataTypeId)
            val builder = CodeBlockWrapper.builder()
            builder.add("%T(", dataType.asKotlinPoetClassName(project))
            dataType.fields.forEach { dataField ->
                val fieldValue =
                    properties.entries
                        .firstOrNull { it.key == dataField.id }
                        ?.value
                        ?.transformedCodeBlock(project, context, dryRun = dryRun)
                        ?: dataField.fieldType.defaultValueAsCodeBlock(project)
                builder.add("${dataField.variableName} = $fieldValue,")
            }
            builder.add(")")
            return builder.build()
        }

        override fun asSimplifiedText(
            project: Project,
            context: GenerationContext,
            dryRun: Boolean,
        ): String {
            val dataType = project.findDataTypeOrNull(dataTypeId) ?: return ""

            return buildString {
                append("${dataType.className}(")
                properties.entries.forEachIndexed { i, property ->
                    dataType.fields.find { it.id == property.key }?.let { dataField ->
                        append(
                            "${dataField.variableName} = ${
                                property.value.transformedCodeBlock(
                                    project,
                                    context,
                                    dryRun = dryRun,
                                )
                            }",
                        )
                    }
                    if (i != properties.entries.toList().lastIndex) {
                        append(", ")
                    }
                }
                append(")")
            }
        }
    }
}
