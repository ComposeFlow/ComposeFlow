package io.composeflow.model.modifier

import io.composeflow.kotlinpoet.GenerationContext
import io.composeflow.model.project.Project
import io.composeflow.serializer.decodeFromStringWithFallback
import io.composeflow.serializer.encodeToString
import io.composeflow.trimForCompare
import kotlinx.serialization.encodeToString
import org.junit.Test
import kotlin.test.assertEquals

class AlphaTest {
    @Test
    fun toComposeCode_alpha_default() {
        val modifierList = listOf(ModifierWrapper.Alpha())

        val code =
            modifierList.generateCode(Project(), context = GenerationContext(), dryRun = false)

        assertEquals(
            "modifier = androidx.compose.ui.Modifier.androidx.compose.ui.draw.alpha(),".trimForCompare(),
            code.build().toString().trimForCompare(),
        )
    }

    @Test
    fun toComposeCode_alpha_non_default() {
        val modifierList = listOf(ModifierWrapper.Alpha(0.5f))

        val code =
            modifierList.generateCode(Project(), context = GenerationContext(), dryRun = false)

        assertEquals(
            "modifier = androidx.compose.ui.Modifier.androidx.compose.ui.draw.alpha(alpha = 0.5f),".trimForCompare(),
            code.build().toString().trimForCompare(),
        )
    }

    @Test
    fun serialize_verify_restored_instance() {
        val alpha = ModifierWrapper.Alpha(0.5f)
        alpha.visible.value = false

        val encodedString = encodeToString(alpha)
        val decoded = decodeFromStringWithFallback<ModifierWrapper.Alpha>(encodedString)
        assertEquals(alpha, decoded)
    }
}
