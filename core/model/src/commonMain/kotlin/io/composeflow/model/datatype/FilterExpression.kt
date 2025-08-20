package io.composeflow.model.datatype

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.AnnotatedString
import io.composeflow.kotlinpoet.wrapper.CodeBlockWrapper
import io.composeflow.kotlinpoet.wrapper.MemberNameWrapper
import io.composeflow.kotlinpoet.GenerationContext
import io.composeflow.model.project.Project
import io.composeflow.model.project.findDataTypeOrNull
import io.composeflow.model.project.firebase.CollectionId
import io.composeflow.model.property.AssignableProperty
import io.composeflow.model.property.DocumentIdProperty
import io.composeflow.ui.propertyeditor.DropdownTextDisplayable
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
sealed interface FilterFieldType {
    @Serializable
    @SerialName("FilterFieldTypeDocumentId")
    data class DocumentId(
        val firestoreCollectionId: CollectionId? = null,
    ) : FilterFieldType

    @Serializable
    @SerialName("FilterFieldTypeDataField")
    data class DataField(
        val dataTypeId: DataTypeId,
        val dataFieldId: DataFieldId,
    ) : FilterFieldType
}

enum class FilterOperator : DropdownTextDisplayable {
    EqualTo {
        @Composable
        override fun asDropdownText(): AnnotatedString = AnnotatedString("Equal to (==)")

        override fun asCodeBlock(): CodeBlockWrapper = CodeBlockWrapper.of("equalTo")
    },
    NotEqualTo {
        @Composable
        override fun asDropdownText(): AnnotatedString = AnnotatedString("Not equal to (!=)")

        override fun asCodeBlock(): CodeBlockWrapper = CodeBlockWrapper.of("notEqualTo")
    },
    LessThan {
        @Composable
        override fun asDropdownText(): AnnotatedString = AnnotatedString("Less than (<)")

        override fun asCodeBlock(): CodeBlockWrapper = CodeBlockWrapper.of("lessThan")
    },
    LessThanOrEqualTo {
        @Composable
        override fun asDropdownText(): AnnotatedString = AnnotatedString("Less than or equal to (<=)")

        override fun asCodeBlock(): CodeBlockWrapper = CodeBlockWrapper.of("lessThanOrEqualTo")
    },
    GreaterThan {
        @Composable
        override fun asDropdownText(): AnnotatedString = AnnotatedString("Greater than (>)")

        override fun asCodeBlock(): CodeBlockWrapper = CodeBlockWrapper.of("greaterThan")
    },
    GreaterThanOrEqualTo {
        @Composable
        override fun asDropdownText(): AnnotatedString = AnnotatedString("Greater than or equal to (>=)")

        override fun asCodeBlock(): CodeBlockWrapper = CodeBlockWrapper.of("greaterThanOrEqualTo")
    },
    Contains {
        @Composable
        override fun asDropdownText(): AnnotatedString = AnnotatedString("Contains")

        override fun asCodeBlock(): CodeBlockWrapper = CodeBlockWrapper.of("contains")
    },
    ;

    abstract fun asCodeBlock(): CodeBlockWrapper
}

@Serializable
sealed interface FilterExpression {
    fun generateCodeBlock(
        project: Project,
        context: GenerationContext,
        dryRun: Boolean,
    ): CodeBlockWrapper

    fun getAssignableProperties(): List<AssignableProperty>
}

sealed interface LogicalFilterExpression : FilterExpression {
    val filters: List<FilterExpression>

    fun filterString(): String

    override fun generateCodeBlock(
        project: Project,
        context: GenerationContext,
        dryRun: Boolean,
    ): CodeBlockWrapper {
        val builder = CodeBlockWrapper.builder()
        return if (filters.isEmpty()) {
            builder.build()
        } else if (filters.size == 1) {
            builder.add(filters[0].generateCodeBlock(project, context, dryRun))
            builder.build()
        } else {
            builder.add("(")
            filters.forEachIndexed { i, filter ->
                builder.add(filter.generateCodeBlock(project, context, dryRun))
                if (i != filters.lastIndex) {
                    builder.add(" ${filterString()} ")
                }
            }
            builder.add(")")
            builder.build()
        }
    }

    override fun getAssignableProperties(): List<AssignableProperty> =
        filters.flatMap { it.getAssignableProperties() }.flatMap { it.getAssignableProperties() }
}

@Serializable
@SerialName("SingleFilter")
data class SingleFilter(
    val filterFieldType: FilterFieldType = FilterFieldType.DocumentId(),
    val operator: FilterOperator = FilterOperator.EqualTo,
    val property: AssignableProperty = DocumentIdProperty.EmptyDocumentId,
) : FilterExpression {
    override fun generateCodeBlock(
        project: Project,
        context: GenerationContext,
        dryRun: Boolean,
    ): CodeBlockWrapper {
        val builder = CodeBlockWrapper.builder()
        builder.add("(")
        when (filterFieldType) {
            is FilterFieldType.DocumentId -> {
                builder.add(
                    CodeBlockWrapper.of(
                        "%M.documentId ",
                        MemberNameWrapper.get("dev.gitlive.firebase.firestore", "FieldPath"),
                    ),
                )
            }

            is FilterFieldType.DataField -> {
                project.findDataTypeOrNull(filterFieldType.dataTypeId)?.let {
                    it.findDataFieldOrNull(filterFieldType.dataFieldId)?.let { dataField ->
                        builder.add("\"${dataField.variableName}\" ")
                    }
                }
            }
        }

        builder.add(operator.asCodeBlock())
        builder.add(" ")
        builder.add(property.transformedCodeBlock(project, context, dryRun = dryRun))
        builder.add(")")
        return builder.build()
    }

    override fun getAssignableProperties(): List<AssignableProperty> = listOf(property)
}

@Serializable
@SerialName("AndFilter")
data class AndFilter(
    override val filters: List<FilterExpression> = emptyList(),
) : LogicalFilterExpression {
    override fun filterString(): String = "and"
}

@Serializable
@SerialName("OrFilter")
data class OrFilter(
    override val filters: List<FilterExpression> = emptyList(),
) : FilterExpression,
    LogicalFilterExpression {
    override fun filterString(): String = "or"
}
