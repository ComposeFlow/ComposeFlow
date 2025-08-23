package io.composeflow.model.modifier

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import io.composeflow.kotlinpoet.GenerationContext
import io.composeflow.model.parameter.wrapper.ColorWrapper
import io.composeflow.model.parameter.wrapper.ShapeWrapper
import io.composeflow.model.project.Project
import io.composeflow.model.property.ColorProperty
import io.composeflow.serializer.decodeFromStringWithFallback
import io.composeflow.serializer.encodeToString
import io.composeflow.trimForCompare
import kotlinx.serialization.encodeToString
import org.junit.Test
import kotlin.test.assertEquals

class BorderTest {
    @Test
    fun toComposeCode_border_default() {
        val modifierList = listOf(ModifierWrapper.Border(width = 2.dp))

        val code =
            modifierList.generateCode(Project(), context = GenerationContext(), dryRun = false)

        assertEquals(
            """modifier = androidx.compose.ui.Modifier.androidx.compose.foundation.border(
                 width = 2.androidx.compose.ui.unit.dp,
                 color = androidx.compose.material3.MaterialTheme.colorScheme.outline,
                 shape = androidx.compose.ui.graphics.RectangleShape,
               ),""".trimForCompare(),
            code.build().toString().trimForCompare(),
        )
    }

    @Test
    fun serialize_verify_restored_instance() {
        val border =
            ModifierWrapper.Border(
                width = 2.dp,
                colorWrapper =
                    ColorProperty.ColorIntrinsicValue(
                        ColorWrapper(
                            themeColor = null,
                            color = Color.Blue,
                        ),
                    ),
                shapeWrapper = ShapeWrapper.RoundedCorner(16.dp),
            )

        val encodedString = encodeToString(border)
        val decoded = decodeFromStringWithFallback<ModifierWrapper.Border>(encodedString)
        assertEquals(border, decoded)
    }
}
