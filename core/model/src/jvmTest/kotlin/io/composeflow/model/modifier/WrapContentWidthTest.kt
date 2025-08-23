package io.composeflow.model.modifier

import io.composeflow.kotlinpoet.GenerationContext
import io.composeflow.model.parameter.wrapper.AlignmentHorizontalWrapper
import io.composeflow.model.project.Project
import io.composeflow.serializer.decodeFromStringWithFallback
import io.composeflow.serializer.encodeToString
import io.composeflow.trimForCompare
import kotlinx.serialization.encodeToString
import org.junit.Test
import kotlin.test.assertEquals

class WrapContentWidthTest {
    @Test
    fun toComposeCode_default() {
        val modifierList = listOf(ModifierWrapper.WrapContentWidth())
        val code =
            modifierList.generateCode(Project(), context = GenerationContext(), dryRun = false)

        assertEquals(
            """modifier = androidx.compose.ui.Modifier.androidx.compose.foundation.layout.wrapContentWidth(),""".trimForCompare(),
            code.build().toString().trimForCompare(),
        )
    }

    @Test
    fun toComposeCode_non_default_args() {
        val modifierList =
            listOf(
                ModifierWrapper.WrapContentWidth(
                    align = AlignmentHorizontalWrapper.End,
                ),
            )
        val code =
            modifierList.generateCode(Project(), context = GenerationContext(), dryRun = false)

        assertEquals(
            """modifier = androidx.compose.ui.Modifier.androidx.compose.foundation.layout.wrapContentWidth(
                 align = androidx.compose.ui.Alignment.End,
               ),""".trimForCompare(),
            code.build().toString().trimForCompare(),
        )
    }

    @Test
    fun serialize_verify_restored_instance() {
        val size = ModifierWrapper.WrapContentWidth()

        val encodedString = encodeToString(size)
        val decoded =
            decodeFromStringWithFallback<ModifierWrapper.WrapContentWidth>(encodedString)
        assertEquals(size, decoded)
    }
}
