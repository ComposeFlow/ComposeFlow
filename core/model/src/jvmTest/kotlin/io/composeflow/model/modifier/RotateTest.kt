package io.composeflow.model.modifier

import io.composeflow.kotlinpoet.GenerationContext
import io.composeflow.model.project.Project
import io.composeflow.serializer.decodeFromStringWithFallback
import io.composeflow.serializer.encodeToString
import io.composeflow.trimForCompare
import kotlinx.serialization.encodeToString
import org.junit.Test
import kotlin.test.assertEquals

class RotateTest {
    @Test
    fun toComposeCode_default_args() {
        val modifierList = listOf(ModifierWrapper.Rotate())

        val code =
            modifierList.generateCode(Project(), context = GenerationContext(), dryRun = false)

        assertEquals(
            """modifier =
                 androidx.compose.ui.Modifier
                 .androidx.compose.ui.draw.rotate(0.0f),""".trimForCompare(),
            code.build().toString().trimForCompare(),
        )
    }

    @Test
    fun toComposeCode_non_default_args() {
        val modifierList = listOf(ModifierWrapper.Rotate(30f))

        val code =
            modifierList.generateCode(Project(), context = GenerationContext(), dryRun = false)

        assertEquals(
            """modifier =
                 androidx.compose.ui.Modifier
                 .androidx.compose.ui.draw.rotate(30.0f),""".trimForCompare(),
            code.build().toString().trimForCompare(),
        )
    }

    @Test
    fun serialize_verify_restored_instance() {
        val rotate = ModifierWrapper.Rotate(60f)

        val encodedString = encodeToString(rotate)
        val decoded = decodeFromStringWithFallback<ModifierWrapper.Rotate>(encodedString)
        assertEquals(rotate, decoded)
    }
}
