package io.composeflow.model.apieditor

import io.composeflow.asVariableName
import io.composeflow.kotlinpoet.wrapper.CodeBlockWrapper
import io.composeflow.kotlinpoet.wrapper.ParameterSpecWrapper
import io.composeflow.kotlinpoet.wrapper.asTypeNameWrapper
import io.composeflow.model.project.ParameterId
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlin.uuid.Uuid

@Serializable
sealed interface ApiProperty {
    fun asCodeBlock(): CodeBlockWrapper

    fun asStringValue(): String

    @Serializable
    @SerialName("ApiPropertyIntrinsicValue")
    data class IntrinsicValue(
        val value: String = "",
    ) : ApiProperty {
        override fun asCodeBlock(): CodeBlockWrapper = CodeBlockWrapper.of("\"\"\"$value\"\"\"")

        override fun asStringValue(): String = value
    }

    @Serializable
    @SerialName("ApiPropertyStringParameter")
    data class StringParameter(
        val parameterId: ParameterId = Uuid.random().toString(),
        val name: String,
        val defaultValue: String = "",
    ) : ApiProperty {
        @Transient
        val variableName = name.asVariableName()

        override fun asCodeBlock(): CodeBlockWrapper = CodeBlockWrapper.of(name)

        override fun asStringValue(): String = defaultValue

        fun generateArgumentParameterSpec(): ParameterSpecWrapper =
            ParameterSpecWrapper
                .builder(
                    name = name,
                    type = String::class.asTypeNameWrapper(),
                ).defaultValue("\"$defaultValue\"")
                .build()
    }
}
