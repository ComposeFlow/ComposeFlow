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

class FillMaxSizeTest {
    @Test
    fun toComposeCode_fillMaxSize() {
        val modifierList = listOf(ModifierWrapper.FillMaxSize())

        val code =
            modifierList.generateCode(Project(), context = GenerationContext(), dryRun = false)

        assertEquals(
            """modifier =
                 androidx.compose.ui.Modifier
                 .androidx.compose.foundation.layout.fillMaxSize(),""".trimForCompare(),
            code.build().toString().trimForCompare(),
        )
    }

    @Test
    fun toComposeCode_fillMaxSize_non_default_arg() {
        val modifierList = listOf(ModifierWrapper.FillMaxSize(0.5f))

        val code =
            modifierList.generateCode(Project(), context = GenerationContext(), dryRun = false)

        assertEquals(
            """modifier =
                 androidx.compose.ui.Modifier
                 .androidx.compose.foundation.layout.fillMaxSize(fraction = 0.5f),""".trimForCompare(),
            code.build().toString().trimForCompare(),
        )
    }

    @Test
    fun toComposeCode_withOtherModifier() {
        val modifierList =
            listOf(
                ModifierWrapper.Padding(8.dp),
                ModifierWrapper.FillMaxSize(),
            )

        val code =
            modifierList.generateCode(Project(), context = GenerationContext(), dryRun = false)

        assertEquals(
            """modifier =
                 androidx.compose.ui.Modifier
                 .androidx.compose.foundation.layout.padding(
                   all = 8.androidx.compose.ui.unit.dp)
                 .androidx.compose.foundation.layout.fillMaxSize(),""".trimForCompare(),
            code.build().toString().trimForCompare(),
        )
    }

    @Test
    fun serialize_verify_restored_instance() {
        val fillMaxSize = ModifierWrapper.FillMaxSize(0.5f)

        val encodedString = encodeToString(fillMaxSize)
        val decoded =
            decodeFromStringWithFallback<ModifierWrapper.FillMaxSize>(encodedString)
        assertEquals(fillMaxSize, decoded)
    }
}
