package io.composeflow.model.modifier

import io.composeflow.kotlinpoet.GenerationContext
import io.composeflow.model.parameter.wrapper.ColorWrapper
import io.composeflow.model.parameter.wrapper.Material3ColorWrapper
import io.composeflow.model.project.Project
import io.composeflow.model.property.ColorProperty
import io.composeflow.serializer.decodeFromStringWithFallback
import io.composeflow.serializer.encodeToString
import io.composeflow.trimForCompare
import kotlinx.serialization.encodeToString
import org.junit.Test
import kotlin.test.assertEquals

class BackgroundTest {
    @Test
    fun toComposeCode_background_default() {
        val modifierList = listOf(ModifierWrapper.Background())

        val code =
            modifierList.generateCode(Project(), context = GenerationContext(), dryRun = false)

        assertEquals(
            """modifier = androidx.compose.ui.Modifier.androidx.compose.foundation.background(
                 color = androidx.compose.material3.MaterialTheme.colorScheme.secondaryContainer,
                 shape = androidx.compose.ui.graphics.RectangleShape,
               ),""".trimForCompare(),
            code.build().toString().trimForCompare(),
        )
    }

    @Test
    fun serialize_verify_restored_instance() {
        val background =
            ModifierWrapper.Background(
                colorWrapper =
                    ColorProperty.ColorIntrinsicValue(
                        ColorWrapper(themeColor = Material3ColorWrapper.Background),
                    ),
            )

        val encodedString = encodeToString(background)
        val decoded =
            decodeFromStringWithFallback<ModifierWrapper.Background>(encodedString)
        assertEquals(background, decoded)
    }
}
