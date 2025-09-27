package io.composeflow.model.color

import androidx.compose.ui.graphics.Color
import io.composeflow.serializer.LocationAwareColorSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("ExtendedColor")
data class ExtendedColor(
    val name: String,
    @Serializable(LocationAwareColorSerializer::class) val color: Color,
)
