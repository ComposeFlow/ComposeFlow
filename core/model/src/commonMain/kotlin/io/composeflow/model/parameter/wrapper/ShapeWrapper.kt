package io.composeflow.model.parameter.wrapper

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.composeflow.kotlinpoet.wrapper.CodeBlockWrapper
import io.composeflow.kotlinpoet.wrapper.MemberNameWrapper
import io.composeflow.serializer.FallbackSealedSerializer
import io.composeflow.serializer.LocationAwareDpSerializer
import io.composeflow.serializer.withLocationAwareExceptions
import kotlinx.serialization.KSerializer
import kotlinx.serialization.PolymorphicSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic

object ShapeWrapperSerializer : KSerializer<ShapeWrapper> {
    private val delegate =
        FallbackSealedSerializer(
            defaultInstance = ShapeWrapper.Rectangle,
            serializer = PolymorphicSerializer(ShapeWrapper::class),
        )

    override val descriptor: SerialDescriptor
        get() = delegate.descriptor

    override fun deserialize(decoder: Decoder): ShapeWrapper = delegate.deserialize(decoder)

    override fun serialize(
        encoder: Encoder,
        value: ShapeWrapper,
    ) = delegate.serialize(encoder, value)
}

/**
 * Location-aware ShapeWrapperSerializer that provides enhanced error reporting with precise location information
 * when shape parsing fails. This helps with debugging YAML files containing invalid shape definitions.
 */
class LocationAwareShapeWrapperSerializer : KSerializer<ShapeWrapper> {
    private val delegate = ShapeWrapperSerializer.withLocationAwareExceptions()

    override val descriptor: SerialDescriptor = delegate.descriptor

    override fun serialize(
        encoder: Encoder,
        value: ShapeWrapper,
    ) {
        delegate.serialize(encoder, value)
    }

    override fun deserialize(decoder: Decoder): ShapeWrapper = delegate.deserialize(decoder)
}

val shapeWrapperModule =
    SerializersModule {
        polymorphic(ShapeWrapper::class) {
            subclass(ShapeWrapper.Circle::class, ShapeWrapper.Circle.serializer())
            subclass(ShapeWrapper.Rectangle::class, ShapeWrapper.Rectangle.serializer())
            subclass(ShapeWrapper.RoundedCorner::class, ShapeWrapper.RoundedCorner.serializer())
            subclass(ShapeWrapper.CutCorner::class, ShapeWrapper.CutCorner.serializer())
        }
    }

/**
 * The wrapper class for [Shape].
 */
@Serializable(LocationAwareShapeWrapperSerializer::class)
sealed interface ShapeWrapper {
    @Serializable
    @SerialName("Rectangle")
    data object Rectangle : ShapeWrapper {
        override fun toShape(): Shape = RectangleShape

        override fun generateCode(codeBlockBuilder: CodeBlockBuilderWrapper): CodeBlockBuilderWrapper {
            val memberName = MemberNameWrapper.get("androidx.compose.ui.graphics", "RectangleShape")
            codeBlockBuilder.addStatement("shape = %M,", memberName)
            return codeBlockBuilder
        }
    }

    @Serializable
    @SerialName("Circle")
    data object Circle : ShapeWrapper {
        override fun toShape(): Shape = CircleShape

        override fun generateCode(codeBlockBuilder: CodeBlockBuilderWrapper): CodeBlockBuilderWrapper {
            val memberName = MemberNameWrapper.get("androidx.compose.foundation.shape", "CircleShape")
            codeBlockBuilder.addStatement("shape = %M,", memberName)
            return codeBlockBuilder
        }
    }

    @Serializable
    @SerialName("RoundedCorner")
    data class RoundedCorner(
        @Serializable(with = LocationAwareDpSerializer::class)
        override val topStart: Dp = 0.dp,
        @Serializable(with = LocationAwareDpSerializer::class)
        override val topEnd: Dp = 0.dp,
        @Serializable(with = LocationAwareDpSerializer::class)
        override val bottomEnd: Dp = 0.dp,
        @Serializable(with = LocationAwareDpSerializer::class)
        override val bottomStart: Dp = 0.dp,
    ) : ShapeWrapper,
        CornerValueHolder {
        constructor(all: Dp) :
            this(topStart = all, topEnd = all, bottomEnd = all, bottomStart = all)

        override fun toShape(): Shape = RoundedCornerShape(topStart, topEnd, bottomEnd, bottomStart)

        override fun generateCode(codeBlockBuilder: CodeBlockBuilderWrapper): CodeBlockBuilderWrapper {
            val memberName = MemberNameWrapper.get("androidx.compose.foundation.shape", "RoundedCornerShape")
            val dpMemberName = MemberNameWrapper.get("androidx.compose.ui.unit", "dp")
            when (spec()) {
                ShapeCornerSpec.All -> {
                    codeBlockBuilder.addStatement(
                        "shape = %M(${topStart.value.toInt()}.%M),",
                        memberName,
                        dpMemberName,
                    )
                }

                ShapeCornerSpec.Individual -> {
                    codeBlockBuilder.addStatement(
                        """shape = %M(
                            topStart = ${topStart.value.toInt()}.%M,
                            topEnd = ${topEnd.value.toInt()}.%M,
                            bottomEnd = ${bottomEnd.value.toInt()}.%M,
                            bottomStart = ${bottomStart.value.toInt()}.%M,
                        ),""",
                        memberName,
                        dpMemberName,
                        dpMemberName,
                        dpMemberName,
                        dpMemberName,
                    )
                }
            }
            return codeBlockBuilder
        }
    }

    @Serializable
    @SerialName("CutCorner")
    data class CutCorner(
        @Serializable(with = LocationAwareDpSerializer::class)
        override val topStart: Dp = 0.dp,
        @Serializable(with = LocationAwareDpSerializer::class)
        override val topEnd: Dp = 0.dp,
        @Serializable(with = LocationAwareDpSerializer::class)
        override val bottomEnd: Dp = 0.dp,
        @Serializable(with = LocationAwareDpSerializer::class)
        override val bottomStart: Dp = 0.dp,
    ) : ShapeWrapper,
        CornerValueHolder {
        override fun toShape(): Shape = CutCornerShape(topStart, topEnd, bottomEnd, bottomStart)

        override fun generateCode(codeBlockBuilder: CodeBlockBuilderWrapper): CodeBlockBuilderWrapper {
            val memberName = MemberNameWrapper.get("androidx.compose.foundation.shape", "CutCornerShape")
            val dpMemberName = MemberNameWrapper.get("androidx.compose.ui.unit", "dp")
            when (spec()) {
                ShapeCornerSpec.All -> {
                    codeBlockBuilder.addStatement(
                        "shape = %M(${topStart.value.toInt()}.%M),",
                        memberName,
                        dpMemberName,
                    )
                }

                ShapeCornerSpec.Individual -> {
                    codeBlockBuilder.addStatement(
                        """shape = %M(
                            topStart = ${topStart.value.toInt()}.%M,
                            topEnd = ${topEnd.value.toInt()}.%M,
                            bottomEnd = ${bottomEnd.value.toInt()}.%M,
                            bottomStart = ${bottomStart.value.toInt()}.%M,
                        ),""",
                        memberName,
                        dpMemberName,
                        dpMemberName,
                        dpMemberName,
                        dpMemberName,
                    )
                }
            }
            return codeBlockBuilder
        }
    }

    fun toShape(): Shape

    fun generateCode(codeBlockBuilder: CodeBlockBuilderWrapper): CodeBlockBuilderWrapper
}

enum class ShapeCornerSpec {
    All,
    Individual,
}

interface CornerValueHolder {
    val topStart: Dp
    val topEnd: Dp
    val bottomEnd: Dp
    val bottomStart: Dp

    fun spec(): ShapeCornerSpec =
        if (topStart == topEnd &&
            bottomEnd == bottomStart &&
            topStart == bottomEnd
        ) {
            ShapeCornerSpec.All
        } else {
            ShapeCornerSpec.Individual
        }
}
