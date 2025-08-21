package io.composeflow.materialicons

import androidx.compose.ui.graphics.vector.ImageVector
import io.composeflow.kotlinpoet.wrapper.CodeBlockWrapper
import io.composeflow.kotlinpoet.wrapper.MemberNameWrapper
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@SerialName("MaterialIcon")
enum class MaterialIcon {
    Outlined,
    Filled,
    Rounded,
    Sharp,
    TwoTone,
}

@Serializable
@SerialName("ImageVectorHolder")
sealed interface ImageVectorHolder {
    val imageVector: ImageVector
    val name: String
    val packageDescriptor: String
    val memberDescriptor: String
}

fun ImageVectorHolder.asCodeBlock(): CodeBlockWrapper =
    CodeBlockWrapper.of(
        """%M.$memberDescriptor.%M""",
        MemberNameWrapper.get("androidx.compose.material.icons", "Icons"),
        MemberNameWrapper.get("androidx.compose.material.icons.$packageDescriptor", name),
    )
