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

class FillMaxWidthTest {
    @Test
    fun toComposeCode_fillMaxWidth() {
        val modifierList = listOf(ModifierWrapper.FillMaxWidth())

        val code =
            modifierList.generateCode(Project(), context = GenerationContext(), dryRun = false)

        assertEquals(
            """modifier =
                 androidx.compose.ui.Modifier
                 .androidx.compose.foundation.layout.fillMaxWidth(),""".trimForCompare(),
            code.build().toString().trimForCompare(),
        )
    }

    @Test
    fun toComposeCode_fillMaxWidth_non_default_arg() {
        val modifierList = listOf(ModifierWrapper.FillMaxWidth(0.5f))

        val code =
            modifierList.generateCode(Project(), context = GenerationContext(), dryRun = false)

        assertEquals(
            """modifier =
                 androidx.compose.ui.Modifier
                 .androidx.compose.foundation.layout.fillMaxWidth(fraction = 0.5f),""".trimForCompare(),
            code.build().toString().trimForCompare(),
        )
    }

    @Test
    fun toComposeCode_withOtherModifier() {
        val modifierList =
            listOf(
                ModifierWrapper.Padding(8.dp),
                ModifierWrapper.FillMaxWidth(),
            )

        val code =
            modifierList.generateCode(Project(), context = GenerationContext(), dryRun = false)

        assertEquals(
            """modifier =
                androidx.compose.ui.Modifier
                .androidx.compose.foundation.layout.padding(
                  all = 8.androidx.compose.ui.unit.dp)
                .androidx.compose.foundation.layout.fillMaxWidth(),""".trimForCompare(),
            code.build().toString().trimForCompare(),
        )
    }

    @Test
    fun serialize_verify_restored_instance() {
        val fillMaxWidth = ModifierWrapper.FillMaxWidth(0.5f)

        val encodedString = encodeToString(fillMaxWidth)
        val decoded =
            decodeFromStringWithFallback<ModifierWrapper.FillMaxWidth>(encodedString)
        assertEquals(fillMaxWidth, decoded)
    }
}
