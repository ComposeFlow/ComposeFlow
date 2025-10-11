package io.composeflow.model.color

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
import com.materialkolor.ktx.toneColor
import com.materialkolor.palettes.CorePalette
import io.composeflow.serializer.LocationAwareColorSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("ExtendedColor")
data class ExtendedColor(
    val name: String,
    @Serializable(LocationAwareColorSerializer::class) val color: Color,
) {
    fun getContentColor(): Color {
        val luminance = color.luminance()
        val palette = CorePalette.contentOf(color.toArgb())
        val baseTone = if (luminance > 0.5f) 20 else 90
        val tone = baseTone.coerceIn(10, 95)
        return palette.a1.toneColor(tone)
    }
}
