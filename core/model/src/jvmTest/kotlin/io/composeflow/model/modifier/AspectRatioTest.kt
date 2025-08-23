package io.composeflow.model.modifier

import io.composeflow.kotlinpoet.GenerationContext
import io.composeflow.model.project.Project
import io.composeflow.serializer.decodeFromStringWithFallback
import io.composeflow.serializer.encodeToString
import io.composeflow.trimForCompare
import kotlinx.serialization.encodeToString
import org.junit.Test
import kotlin.test.assertEquals

class AspectRatioTest {
    @Test
    fun toComposeCode_alpha_ratioOnly() {
        val modifierList = listOf(ModifierWrapper.AspectRatio(1f))

        val code =
            modifierList.generateCode(Project(), context = GenerationContext(), dryRun = false)

        assertEquals(
            """modifier =
                androidx.compose.ui.Modifier.androidx.compose.foundation.layout.aspectRatio(ratio=1.0f,),""".trimForCompare(),
            code.build().toString().trimForCompare(),
        )
    }

    @Test
    fun toComposeCode_alpha_ratioAndHeightFirst() {
        val modifierList = listOf(ModifierWrapper.AspectRatio(1f, true))

        val code =
            modifierList.generateCode(Project(), context = GenerationContext(), dryRun = false)

        assertEquals(
            """modifier =
                 androidx.compose.ui.Modifier.androidx.compose.foundation.layout.aspectRatio(ratio = 1.0f,
                 matchHeightConstraintsFirst = true,),""".trimForCompare(),
            code.build().toString().trimForCompare(),
        )
    }

    @Test
    fun serialize_verify_restored_instance() {
        val aspectRatio = ModifierWrapper.AspectRatio(0.5f)

        val encodedString = encodeToString(aspectRatio)
        val decoded =
            decodeFromStringWithFallback<ModifierWrapper.AspectRatio>(encodedString)
        assertEquals(aspectRatio, decoded)
    }
}
