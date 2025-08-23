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

class FillMaxHeightTest {
    @Test
    fun toComposeCode_fillMaxHeight() {
        val modifierList = listOf(ModifierWrapper.FillMaxHeight())

        val code =
            modifierList.generateCode(Project(), context = GenerationContext(), dryRun = false)

        assertEquals(
            """modifier =
                 androidx.compose.ui.Modifier.androidx.compose.foundation.layout.fillMaxHeight(),
            """.trimForCompare(),
            code.build().toString().trimForCompare(),
        )
    }

    @Test
    fun toComposeCode_fillMaxHeight_non_default_arg() {
        val modifierList = listOf(ModifierWrapper.FillMaxHeight(0.5f))

        val code =
            modifierList.generateCode(Project(), context = GenerationContext(), dryRun = false)

        assertEquals(
            """modifier =
                 androidx.compose.ui.Modifier.androidx.compose.foundation.layout.fillMaxHeight
                   (fraction = 0.5f),""".trimForCompare(),
            code.build().toString().trimForCompare(),
        )
    }

    @Test
    fun toComposeCode_withOtherModifier() {
        val modifierList =
            listOf(
                ModifierWrapper.Padding(8.dp),
                ModifierWrapper.FillMaxHeight(),
            )

        val code =
            modifierList.generateCode(Project(), context = GenerationContext(), dryRun = false)

        assertEquals(
            """modifier =
                 androidx.compose.ui.Modifier
                 .androidx.compose.foundation.layout.padding(
                     all = 8.androidx.compose.ui.unit.dp)
                 .androidx.compose.foundation.layout.fillMaxHeight(),""".trimForCompare(),
            code.build().toString().trimForCompare(),
        )
    }

    @Test
    fun serialize_verify_restored_instance() {
        val fillMaxHeight = ModifierWrapper.FillMaxHeight(0.5f)

        val encodedString = encodeToString(fillMaxHeight)
        val decoded =
            decodeFromStringWithFallback<ModifierWrapper.FillMaxHeight>(encodedString)
        assertEquals(fillMaxHeight, decoded)
    }
}
