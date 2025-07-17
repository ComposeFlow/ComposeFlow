package io.composeflow.model.type

import androidx.compose.runtime.Composable
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.asTypeName
import io.composeflow.model.datatype.DataTypeId
import io.composeflow.model.datatype.EmptyDataType
import io.composeflow.model.enumwrapper.EnumWrapper
import io.composeflow.model.project.Project
import io.composeflow.model.project.findDataTypeOrNull
import io.composeflow.model.project.findDataTypeOrThrow
import io.composeflow.model.project.findFirestoreCollectionOrNull
import io.composeflow.model.project.firebase.CollectionId
import io.composeflow.model.property.ApiResultProperty
import io.composeflow.model.property.AssignableProperty
import io.composeflow.model.property.BooleanProperty
import io.composeflow.model.property.ColorProperty
import io.composeflow.model.property.CustomDataTypeProperty
import io.composeflow.model.property.DocumentIdProperty
import io.composeflow.model.property.EnumProperty
import io.composeflow.model.property.FloatProperty
import io.composeflow.model.property.InstantProperty
import io.composeflow.model.property.IntProperty
import io.composeflow.model.property.StringProperty
import io.composeflow.serializer.ClassSerializer
import io.composeflow.ui.propertyeditor.DropdownTextDisplayable
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.uuid.Uuid

@Serializable
sealed interface ComposeFlowType : DropdownTextDisplayable {
    val isList: Boolean

    /**
     * Returns true if the [type] in the argument is assignable to this type
     *
     * @param exactMatch match without implicit conversion. E.g. String can be represented as
     * Boolean implicitly, but with this flag being false, it's considered as String isn't assignable
     * to Boolean
     */
    fun isAbleToAssign(type: ComposeFlowType, exactMatch: Boolean = false): Boolean

    /**
     * Returns true if this type has any matching type within containing fields for the given
     * [type] in the argument.
     *
     * This recursively checks the containing fields if this type contains sub fields.
     * E.g. Type.CustomDataType
     */
    fun hasTypeThatIsAbleToAssign(
        project: Project,
        type: ComposeFlowType,
        exactMatch: Boolean = false,
    ): Boolean =
        isAbleToAssign(type, exactMatch)

    fun isPrimitive(): Boolean
    fun displayName(project: Project, listAware: Boolean = true): String
    fun defaultValue(): AssignableProperty
    fun asKotlinPoetTypeName(project: Project): TypeName
    fun ComposeFlowType.convertCodeFromType(
        inputType: ComposeFlowType,
        codeBlock: CodeBlock
    ): CodeBlock {
        if (!isAbleToAssign(inputType)) {
            return codeBlock
        }
        return codeBlock
    }

    fun ComposeFlowType.convertExpressionFromType(
        inputType: ComposeFlowType,
        expression: String
    ): String {
        if (!isAbleToAssign(inputType)) {
            return expression
        }
        return expression
    }

    fun copyWith(
        newIsList: Boolean = isList,
    ): ComposeFlowType

    fun copyWith(
        newIsList: Boolean = isList,
        newDataTypeId: DataTypeId,
    ): ComposeFlowType = this

    @Serializable
    @SerialName("StringType")
    data class StringType(override val isList: Boolean = false) : ComposeFlowType {

        // Always returns true except for Unknown since every Kotlin type has the toString() method
        override fun isAbleToAssign(
            type: ComposeFlowType,
            exactMatch: Boolean
        ): Boolean {
            return if (isList) {
                // StringType(isList = false) converts the element as listOf("...")
                type == StringType(isList = true) || type == StringType(isList = false)
            } else {
                if (exactMatch) {
                    type is StringType
                } else {
                    type != UnknownType() && type !is DocumentIdType
                }
            }
        }

        override fun displayName(project: Project, listAware: Boolean): String {
            return if (listAware) {
                if (isList) "List<String>" else "String"
            } else {
                "String"
            }
        }

        override fun copyWith(
            newIsList: Boolean,
        ): ComposeFlowType = this.copy(isList = newIsList)

        override fun defaultValue(): AssignableProperty = StringProperty.StringIntrinsicValue()

        override fun asKotlinPoetTypeName(project: Project): TypeName {
            return if (isList) {
                List::class.asTypeName().parameterizedBy(String::class.asTypeName())
            } else {
                String::class.asTypeName()
            }
        }

        override fun ComposeFlowType.convertCodeFromType(
            inputType: ComposeFlowType,
            codeBlock: CodeBlock,
        ): CodeBlock {
            if (!isAbleToAssign(inputType)) {
                return codeBlock
            }
            return if (inputType is StringType && inputType.isList == this.isList) {
                codeBlock
            } else if (inputType is StringType && isList && !inputType.isList) {
                val builder = CodeBlock.builder()
                builder.add("listOf(")
                builder.add(codeBlock)
                builder.add(")")
                return builder.build()
            } else {
                val builder = CodeBlock.builder()
                builder.add("(")
                builder.add(codeBlock)
                builder.add(")")
                builder.add(".toString()")
                return builder.build()
            }
        }

        override fun ComposeFlowType.convertExpressionFromType(
            inputType: ComposeFlowType,
            expression: String,
        ): String {
            if (!isAbleToAssign(inputType)) {
                return expression
            }
            return if (inputType == StringType(isList = false)) {
                expression
            } else {
                "$expression.toString()"
            }
        }

        override fun isPrimitive() = true

        @Composable
        override fun asDropdownText(): String = "String"
    }

    @Serializable
    @SerialName("BooleanType")
    data class BooleanType(override val isList: Boolean = false) : ComposeFlowType {

        override fun isAbleToAssign(
            type: ComposeFlowType,
            exactMatch: Boolean
        ): Boolean {
            if (isList != type.isList) return false
            return when (type) {
                is BooleanType -> true
                else -> false
            }
        }

        override fun displayName(project: Project, listAware: Boolean): String {
            return if (listAware) {
                if (isList) "List<Boolean>" else "Boolean"
            } else {
                "Boolean"
            }
        }

        override fun copyWith(
            newIsList: Boolean,
        ): ComposeFlowType = this.copy(isList = newIsList)

        override fun isPrimitive() = true
        override fun defaultValue(): AssignableProperty = BooleanProperty.BooleanIntrinsicValue()

        override fun asKotlinPoetTypeName(project: Project): TypeName {
            return if (isList) {
                List::class.asTypeName().parameterizedBy(Boolean::class.asTypeName())
            } else {
                Boolean::class.asTypeName()
            }
        }

        @Composable
        override fun asDropdownText(): String = "Boolean"
    }

    @Serializable
    @SerialName("IntType")
    data class IntType(override val isList: Boolean = false) : ComposeFlowType {

        override fun isAbleToAssign(
            type: ComposeFlowType,
            exactMatch: Boolean
        ): Boolean {
            if (isList != type.isList) return false
            return when (type) {
                is IntType -> true
                is FloatType -> true
                else -> false
            }
        }

        override fun ComposeFlowType.convertCodeFromType(
            inputType: ComposeFlowType,
            codeBlock: CodeBlock,
        ): CodeBlock {
            if (!isAbleToAssign(inputType)) {
                return codeBlock
            }
            return if (inputType == FloatType(isList = false)) {
                CodeBlock.of("${codeBlock}.toFloat()")
            } else {
                codeBlock
            }
        }

        override fun displayName(project: Project, listAware: Boolean): String {
            return if (listAware) {
                if (isList) "List<Int>" else "Int"
            } else {
                "Int"
            }
        }

        override fun copyWith(
            newIsList: Boolean,
        ): ComposeFlowType = this.copy(isList = newIsList)

        override fun defaultValue(): AssignableProperty = IntProperty.IntIntrinsicValue()
        override fun isPrimitive() = true
        override fun asKotlinPoetTypeName(project: Project): TypeName {
            return if (isList) {
                List::class.asTypeName().parameterizedBy(Int::class.asTypeName())
            } else {
                Int::class.asTypeName()
            }
        }

        @Composable
        override fun asDropdownText(): String = "Int"
    }

    @Serializable
    @SerialName("FloatType")
    data class FloatType(override val isList: Boolean = false) : ComposeFlowType {

        override fun isAbleToAssign(
            type: ComposeFlowType,
            exactMatch: Boolean
        ): Boolean {
            if (isList != type.isList) return false
            return when (type) {
                is IntType -> true
                is FloatType -> true
                else -> false
            }
        }

        override fun ComposeFlowType.convertCodeFromType(
            inputType: ComposeFlowType,
            codeBlock: CodeBlock,
        ): CodeBlock {
            if (!isAbleToAssign(inputType)) {
                return codeBlock
            }
            return if (inputType == IntType(isList = false)) {
                CodeBlock.of("${codeBlock}.toInt()")
            } else {
                codeBlock
            }
        }

        override fun displayName(project: Project, listAware: Boolean): String {
            return if (listAware) {
                if (isList) "List<Float>" else "Float"
            } else {
                "Float"
            }
        }

        override fun copyWith(
            newIsList: Boolean,
        ): ComposeFlowType = this.copy(isList = newIsList)

        override fun defaultValue(): AssignableProperty = FloatProperty.FloatIntrinsicValue()
        override fun isPrimitive() = true
        override fun asKotlinPoetTypeName(project: Project): TypeName {
            return if (isList) {
                List::class.asTypeName().parameterizedBy(Float::class.asTypeName())
            } else {
                Float::class.asTypeName()
            }
        }

        @Composable
        override fun asDropdownText(): String = "Float"
    }

    @Serializable
    @SerialName("ColorType")
    data class Color(override val isList: Boolean = false) : ComposeFlowType {

        override fun isAbleToAssign(
            type: ComposeFlowType,
            exactMatch: Boolean
        ): Boolean {
            if (isList != type.isList) return false
            return when (type) {
                is Color -> true
                else -> false
            }
        }

        override fun displayName(project: Project, listAware: Boolean): String {
            return if (listAware) {
                if (isList) "List<Color>" else "Color"
            } else {
                "Color"
            }
        }

        override fun copyWith(
            newIsList: Boolean,
        ): ComposeFlowType = this.copy(isList = newIsList)

        override fun defaultValue(): AssignableProperty = ColorProperty.ColorIntrinsicValue()
        override fun isPrimitive() = true
        override fun asKotlinPoetTypeName(project: Project): TypeName {
            return if (isList) {
                List::class.asTypeName()
                    .parameterizedBy(androidx.compose.ui.graphics.Color::class.asTypeName())
            } else {
                androidx.compose.ui.graphics.Color::class.asTypeName()
            }
        }

        @Composable
        override fun asDropdownText(): String = "Color"
    }

    @Serializable
    @SerialName("InstantType")
    data class InstantType(override val isList: Boolean = false) : ComposeFlowType {

        override fun isAbleToAssign(
            type: ComposeFlowType,
            exactMatch: Boolean
        ): Boolean {
            if (isList != type.isList) return false
            return when (type) {
                is InstantType -> true
                else -> false
            }
        }

        override fun displayName(project: Project, listAware: Boolean): String {
            return if (listAware) {
                if (isList) "List<Instant>" else "Instant"
            } else {
                "Instant (Date)"
            }
        }

        override fun copyWith(
            newIsList: Boolean,
        ): ComposeFlowType = this.copy(isList = newIsList)

        override fun defaultValue(): AssignableProperty = InstantProperty.InstantIntrinsicValue()
        override fun isPrimitive() = true
        override fun asKotlinPoetTypeName(project: Project): TypeName {
            return if (isList) {
                List::class.asTypeName()
                    .parameterizedBy(kotlinx.datetime.Instant::class.asTypeName())
            } else {
                kotlinx.datetime.Instant::class.asTypeName()
            }
        }

        @Composable
        override fun asDropdownText(): String = "Instant"
    }

    @Serializable
    @SerialName("CustomDataType")
    data class CustomDataType(
        override val isList: Boolean = false,
        val dataTypeId: DataTypeId,
    ) : ComposeFlowType {

        override fun isAbleToAssign(
            type: ComposeFlowType,
            exactMatch: Boolean
        ): Boolean {
            if (isList != type.isList) return false
            return when (type) {
                is CustomDataType -> {
                    dataTypeId == type.dataTypeId
                }

                else -> false
            }
        }

        override fun hasTypeThatIsAbleToAssign(
            project: Project,
            type: ComposeFlowType,
            exactMatch: Boolean,
        ): Boolean {
            if (isList != type.isList) return false
            val dataType = project.findDataTypeOrNull(dataTypeId) ?: return false
            return dataType.getDataFields(project).any {
                it.fieldType.type().hasTypeThatIsAbleToAssign(project, type, exactMatch)
            }
        }

        override fun displayName(project: Project, listAware: Boolean): String {
            val className =
                dataTypeId.let { project.findDataTypeOrNull(it)?.className } ?: "DataType"
            return if (listAware) {
                if (isList) "List<$className>" else className
            } else {
                className
            }
        }

        override fun copyWith(
            newIsList: Boolean,
        ): ComposeFlowType = this.copy(isList = newIsList)

        override fun copyWith(
            newIsList: Boolean,
            newDataTypeId: DataTypeId,
        ): ComposeFlowType = this.copy(isList = newIsList, dataTypeId = newDataTypeId)

        override fun defaultValue(): AssignableProperty =
            CustomDataTypeProperty.ValueFromFields(dataTypeId = dataTypeId)

        override fun isPrimitive() = false
        override fun asKotlinPoetTypeName(project: Project): TypeName {
            val className =
                dataTypeId.let { project.findDataTypeOrThrow(it).asKotlinPoetClassName(project) }
            return if (isList) {
                List::class.asTypeName().parameterizedBy(className)
            } else {
                className
            }
        }

        @Composable
        override fun asDropdownText(): String = "DataType"
    }

    data class Enum<E : kotlin.Enum<E>>(
        override val isList: Boolean = false,
        @Serializable(ClassSerializer::class)
        val enumClass: Class<E>,
    ) : ComposeFlowType {

        override fun isAbleToAssign(
            type: ComposeFlowType,
            exactMatch: Boolean
        ): Boolean {
            if (isList != type.isList) return false
            return when (type) {
                is Enum<*> -> {
                    enumClass == type.enumClass
                }

                else -> false
            }
        }

        override fun displayName(project: Project, listAware: Boolean): String {
            val typeName = enumClass.name.split(".").last().replace("Wrapper", "")
            return if (listAware) {
                if (isList) "List<$typeName>" else typeName
            } else {
                typeName
            }
        }

        override fun copyWith(
            newIsList: Boolean,
        ): ComposeFlowType = this.copy(isList = newIsList)

        override fun defaultValue(): AssignableProperty =
            EnumProperty(enumClass.getFirstEnumValue()!! as EnumWrapper)

        override fun isPrimitive() = false
        override fun asKotlinPoetTypeName(project: Project): TypeName {
            return if (isList) {
                List::class.asTypeName().parameterizedBy(Int::class.asTypeName())
            } else {
                Int::class.asTypeName()
            }
        }

        @Composable
        override fun asDropdownText(): String = "Enum"
    }

    @Serializable
    @SerialName("JsonElementType")
    data class JsonElementType(
        override val isList: Boolean = false,
    ) : ComposeFlowType {

        override fun isAbleToAssign(
            type: ComposeFlowType,
            exactMatch: Boolean
        ): Boolean {
            if (isList != type.isList) return false
            return when (type) {
                is JsonElementType -> true
                else -> false
            }
        }

        override fun displayName(project: Project, listAware: Boolean): String {
            return if (listAware) {
                if (isList) "List<JsonElement>" else "JsonElement"
            } else {
                "JsonElement"
            }
        }

        override fun copyWith(
            newIsList: Boolean,
        ): ComposeFlowType = this.copy(isList = newIsList)

        override fun defaultValue(): AssignableProperty =
            ApiResultProperty(apiId = null)

        override fun isPrimitive() = false
        override fun asKotlinPoetTypeName(project: Project): TypeName {
            val className = JsonElementType::class.asTypeName()
            return if (isList) {
                List::class.asTypeName().parameterizedBy(className)
            } else {
                className
            }
        }

        @Composable
        override fun asDropdownText(): String = "JsonElement"
    }

    @Serializable
    @SerialName("AnyType")
    data class AnyType(override val isList: Boolean = false) : ComposeFlowType {

        override fun isAbleToAssign(
            type: ComposeFlowType,
            exactMatch: Boolean
        ): Boolean {
            if (isList != type.isList) return false
            return when (type) {
                is UnknownType -> false
                else -> true
            }
        }

        override fun displayName(project: Project, listAware: Boolean): String {
            return if (listAware) {
                if (isList) "List<Any>" else "Any"
            } else {
                "Any"
            }
        }

        override fun copyWith(
            newIsList: Boolean,
        ): ComposeFlowType = this.copy(isList = newIsList)

        override fun defaultValue(): AssignableProperty = StringProperty.StringIntrinsicValue()
        override fun isPrimitive() = true
        override fun asKotlinPoetTypeName(project: Project): TypeName {
            return if (isList) {
                List::class.asTypeName()
                    .parameterizedBy(AnyType::class.asTypeName())
            } else {
                AnyType::class.asTypeName()
            }
        }

        @Composable
        override fun asDropdownText(): String = "Any"
    }

    /**
     * Special type for representing document id for Firestore.
     *
     * This type is actually String, but to differentiate from other types explicitly, creating
     * a specific type.
     */
    @Serializable
    @SerialName("DocumentIdType")
    data class DocumentIdType(
        val firestoreCollectionId: CollectionId,
        override val isList: Boolean = false,
    ) : ComposeFlowType {

        override fun isAbleToAssign(
            type: ComposeFlowType,
            exactMatch: Boolean
        ): Boolean {
            if (isList != type.isList) return false
            return when (type) {
                is DocumentIdType -> {
                    type.firestoreCollectionId == firestoreCollectionId
                }

                else -> false
            }
        }

        override fun displayName(project: Project, listAware: Boolean): String {
            val firestoreCollection = project.findFirestoreCollectionOrNull(firestoreCollectionId)
                ?: return "DocumentId<Unknown>"
            val documentIdTypeName = "DocumentId<${firestoreCollection.name}>"
            return if (listAware) {
                if (isList) "List<$documentIdTypeName>" else documentIdTypeName
            } else {
                documentIdTypeName
            }
        }

        override fun copyWith(
            newIsList: Boolean,
        ): ComposeFlowType = this.copy(isList = newIsList)

        override fun defaultValue(): AssignableProperty = DocumentIdProperty.EmptyDocumentId
        override fun isPrimitive() = true
        override fun asKotlinPoetTypeName(project: Project): TypeName {
            return if (isList) {
                List::class.asTypeName()
                    .parameterizedBy(String::class.asTypeName())
            } else {
                String::class.asTypeName()
            }
        }

        @Composable
        override fun asDropdownText(): String = "DocumentId"
    }

    @Serializable
    @SerialName("UnknownType")
    data class UnknownType(override val isList: Boolean = false) : ComposeFlowType {

        override fun isAbleToAssign(
            type: ComposeFlowType,
            exactMatch: Boolean
        ): Boolean = false

        override fun displayName(project: Project, listAware: Boolean): String {
            return "Unknown"
        }

        override fun copyWith(
            newIsList: Boolean,
        ): ComposeFlowType = this.copy(isList = newIsList)

        override fun defaultValue(): AssignableProperty = BooleanProperty.Empty
        override fun isPrimitive() = false
        override fun asKotlinPoetTypeName(project: Project): TypeName {
            return Nothing::class.asTypeName()
        }

        @Composable
        override fun asDropdownText(): String = "Unknown"
    }

    companion object {
        fun validSingleEntries(project: Project): List<ComposeFlowType> {
            val firstDataType = if (project.dataTypeHolder.dataTypes.isNotEmpty()) {
                project.dataTypeHolder.dataTypes.first()
            } else {
                EmptyDataType
            }
            return listOf(
                StringType(),
                BooleanType(),
                IntType(),
                FloatType(),
                CustomDataType(dataTypeId = firstDataType.id),
            )
        }
    }
}

private fun <E : Enum<E>> Class<E>.getFirstEnumValue(): E? {
    return enumConstants?.firstOrNull()
}

fun ComposeFlowType.convertCodeFromType(
    inputType: ComposeFlowType,
    codeBlock: CodeBlock
): CodeBlock {
    return this.convertCodeFromType(inputType, codeBlock)
}

val emptyDocumentIdType = ComposeFlowType.DocumentIdType(Uuid.random().toString())