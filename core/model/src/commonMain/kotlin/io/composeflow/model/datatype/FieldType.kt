package io.composeflow.model.datatype

import androidx.compose.runtime.Composable
import com.squareup.kotlinpoet.CodeBlock
import io.composeflow.model.parameter.wrapper.InstantWrapper
import io.composeflow.model.project.Project
import io.composeflow.model.project.findDataTypeOrThrow
import io.composeflow.model.project.firebase.CollectionId
import io.composeflow.model.type.ComposeFlowType
import io.composeflow.ui.propertyeditor.DropdownItem
import kotlinx.datetime.Clock
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
sealed interface FieldType<T> : DropdownItem {

    fun defaultValue(): T
    fun defaultValueAsCodeBlock(project: Project): CodeBlock

    fun type(): ComposeFlowType
    fun isList(): kotlin.Boolean

    @Serializable
    @SerialName("FieldTypeString")
    data class String(
        private val defaultValue: kotlin.String = "",
    ) : FieldType<kotlin.String> {
        override fun isList(): kotlin.Boolean = false
        override fun type() = ComposeFlowType.StringType(isList = false)
        override fun defaultValue(): kotlin.String = defaultValue
        override fun defaultValueAsCodeBlock(project: Project): CodeBlock =
            CodeBlock.of("\"${defaultValue}\"")

        @Composable
        override fun asDropdownText(): kotlin.String = "String"

        override fun isSameItem(item: Any): kotlin.Boolean = item is String
    }

    @Serializable
    @SerialName("FieldTypeInt")
    data class Int(
        private val defaultValue: kotlin.Int = 0,
    ) : FieldType<kotlin.Int> {
        override fun isList(): kotlin.Boolean = false
        override fun type() = ComposeFlowType.IntType(isList = false)
        override fun defaultValue(): kotlin.Int = defaultValue
        override fun defaultValueAsCodeBlock(project: Project): CodeBlock {
            return CodeBlock.of("$defaultValue")
        }

        @Composable
        override fun asDropdownText(): kotlin.String = "Int"

        override fun isSameItem(item: Any): kotlin.Boolean = item is Int
    }

    @Serializable
    @SerialName("FieldTypeFloat")
    data class Float(
        private val defaultValue: kotlin.Float = 0f,
    ) : FieldType<kotlin.Float> {
        override fun isList(): kotlin.Boolean = false
        override fun type() = ComposeFlowType.FloatType(isList = false)
        override fun defaultValue(): kotlin.Float = defaultValue
        override fun defaultValueAsCodeBlock(project: Project): CodeBlock {
            return CodeBlock.of("${defaultValue}f")
        }

        @Composable
        override fun asDropdownText(): kotlin.String = "Float"

        override fun isSameItem(item: Any): kotlin.Boolean = item is Float
    }

    @Serializable
    @SerialName("FieldTypeBoolean")
    data class Boolean(
        private val defaultValue: kotlin.Boolean = false,
    ) : FieldType<kotlin.Boolean> {
        override fun isList(): kotlin.Boolean = false
        override fun type() = ComposeFlowType.BooleanType(isList = false)
        override fun defaultValue(): kotlin.Boolean = defaultValue
        override fun defaultValueAsCodeBlock(project: Project): CodeBlock {
            return CodeBlock.of("$defaultValue")
        }

        @Composable
        override fun asDropdownText(): kotlin.String = "Boolean"

        override fun isSameItem(item: Any): kotlin.Boolean = item is Boolean
    }

    @Serializable
    @SerialName("FieldTypeInstant")
    data class Instant(
        private val defaultValue: InstantWrapper = InstantWrapper(),
    ) : FieldType<kotlinx.datetime.Instant> {
        override fun isList(): kotlin.Boolean = false
        override fun type() = ComposeFlowType.InstantType(isList = false)
        override fun defaultValue(): kotlinx.datetime.Instant =
            defaultValue.instant ?: Clock.System.now()

        override fun defaultValueAsCodeBlock(project: Project): CodeBlock {
            return defaultValue.generateCode()
        }

        @Composable
        override fun asDropdownText(): kotlin.String = "Instant"

        override fun isSameItem(item: Any): kotlin.Boolean = item is Instant
    }

    @Serializable
    @SerialName("FieldTypeCustomDataType")
    data class CustomDataType(
        private val defaultValue: DataType = EmptyDataType,
        val dataTypeId: DataTypeId,
    ) : FieldType<DataType> {
        override fun isList(): kotlin.Boolean = false
        override fun type() = ComposeFlowType.CustomDataType(isList = false, dataTypeId)
        override fun defaultValue(): DataType = defaultValue
        override fun defaultValueAsCodeBlock(project: Project): CodeBlock {
            val dataType = project.findDataTypeOrThrow(dataTypeId)
            return CodeBlock.of("%T()", dataType.asKotlinPoetClassName(project))
        }

        @Composable
        override fun asDropdownText(): kotlin.String = "DataType"

        override fun isSameItem(item: Any): kotlin.Boolean = item is CustomDataType
    }

    @Serializable
    @SerialName("FieldTypeDocumentId")
    data class DocumentId(
        val firestoreCollectionId: CollectionId,
    ) : FieldType<kotlin.String> {
        override fun isList(): kotlin.Boolean = false
        override fun type() =
            ComposeFlowType.DocumentIdType(
                isList = false,
                firestoreCollectionId = firestoreCollectionId
            )

        override fun defaultValue(): kotlin.String = ""
        override fun defaultValueAsCodeBlock(project: Project): CodeBlock {
            return CodeBlock.of("")
        }

        @Composable
        override fun asDropdownText(): kotlin.String = "DocumentId"

        override fun isSameItem(item: Any): kotlin.Boolean = item is DataFieldType.DocumentId
    }

    fun copyWithDefaultValue(defaultValue: Any): FieldType<*> {
        return when (this) {
            is Boolean -> copy(defaultValue = defaultValue as kotlin.Boolean)
            is Int -> copy(defaultValue = defaultValue as kotlin.Int)
            is Float -> copy(defaultValue = defaultValue as kotlin.Float)
            is String -> copy(defaultValue = defaultValue as kotlin.String)
            is Instant -> copy(defaultValue = defaultValue as InstantWrapper)
            is CustomDataType -> copy(defaultValue = defaultValue as DataType)
            is DocumentId -> this
        }
    }

    companion object {
        fun entries(): List<FieldType<*>> = listOf(
            String(),
            Int(),
            Float(),
            Boolean(),
            Instant(),
        )
    }
}
