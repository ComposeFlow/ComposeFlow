package io.composeflow.model.modifier

import androidx.compose.ui.unit.dp
import io.composeflow.kotlinpoet.GenerationContext
import io.composeflow.model.project.Project
import io.composeflow.serializer.decodeFromStringWithFallback
import io.composeflow.serializer.encodeToString
import io.composeflow.trimForCompare
import kotlinx.serialization.encodeToString
import org.junit.Test
import kotlin.test.assertEquals

class WidthTest {
    @Test
    fun toComposeCode_width() {
        val modifierList = listOf(ModifierWrapper.Width(50.dp))

        val code =
            modifierList.generateCode(Project(), context = GenerationContext(), dryRun = false)

        assertEquals(
            """modifier =
                 androidx.compose.ui.Modifier
                 .androidx.compose.foundation.layout.width(
                   50.androidx.compose.ui.unit.dp
                 ),""".trimForCompare(),
            code.build().toString().trimForCompare(),
        )
    }

    @Test
    fun serialize_verify_restored_instance() {
        val width = ModifierWrapper.Width(width = 8.dp)

        val encodedString = encodeToString(width)
        val decoded = decodeFromStringWithFallback<ModifierWrapper.Width>(encodedString)
        assertEquals(width, decoded)
    }
}
