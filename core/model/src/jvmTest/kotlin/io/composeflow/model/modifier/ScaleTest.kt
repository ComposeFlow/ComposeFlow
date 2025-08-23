package io.composeflow.model.modifier

import io.composeflow.kotlinpoet.GenerationContext
import io.composeflow.model.project.Project
import io.composeflow.serializer.decodeFromStringWithFallback
import io.composeflow.serializer.encodeToString
import io.composeflow.trimForCompare
import kotlinx.serialization.encodeToString
import org.junit.Test
import kotlin.test.assertEquals

class ScaleTest {
    @Test
    fun toComposeCode_default_args() {
        val modifierList = listOf(ModifierWrapper.Scale())

        val code =
            modifierList.generateCode(Project(), context = GenerationContext(), dryRun = false)

        assertEquals(
            """modifier =
                 androidx.compose.ui.Modifier
                 .androidx.compose.ui.draw.scale(
                   1.0f
                 ),""".trimForCompare(),
            code.build().toString().trimForCompare(),
        )
    }

    @Test
    fun toComposeCode_non_default_args() {
        val modifierList = listOf(ModifierWrapper.Scale(scaleX = -1f, scaleY = 1f))

        val code =
            modifierList.generateCode(Project(), context = GenerationContext(), dryRun = false)

        assertEquals(
            """modifier = androidx.compose.ui.Modifier
                 .androidx.compose.ui.draw.scale(
                   scaleX = -1.0f,
                   scaleY = 1.0f
                 ),""".trimForCompare(),
            code.build().toString().trimForCompare(),
        )
    }

    @Test
    fun toComposeCode_overloaded_constructor() {
        val modifierList = listOf(ModifierWrapper.Scale(2f))

        val code =
            modifierList.generateCode(Project(), context = GenerationContext(), dryRun = false)

        assertEquals(
            """modifier = androidx.compose.ui.Modifier
                 .androidx.compose.ui.draw.scale(
                   2.0f
                 ),""".trimForCompare(),
            code.build().toString().trimForCompare(),
        )
    }

    @Test
    fun serialize_verify_restored_instance() {
        val scale = ModifierWrapper.Scale(scaleX = 2f, scaleY = 3f)

        val encodedString = encodeToString(scale)
        val decoded = decodeFromStringWithFallback<ModifierWrapper.Scale>(encodedString)
        assertEquals(scale, decoded)
    }
}
