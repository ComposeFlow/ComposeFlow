package io.composeflow.model.modifier

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.composeflow.kotlinpoet.GenerationContext
import io.composeflow.model.project.Project
import io.composeflow.serializer.decodeFromStringWithFallback
import io.composeflow.serializer.encodeToString
import io.composeflow.trimForCompare
import kotlinx.serialization.encodeToString
import org.junit.Test
import kotlin.test.assertEquals

class SizeTest {
    @Test
    fun toComposeCode_differentConstructor() {
        val modifierList = listOf(ModifierWrapper.Size(50.dp))
        val modifierList2 = listOf(ModifierWrapper.Size(width = 50.dp, height = 50.dp))

        assertEquals(modifierList, modifierList2)
    }

    @Test
    fun toComposeCode_singleModifier() {
        val modifierList = listOf(ModifierWrapper.Size(50.dp))

        val code =
            modifierList.generateCode(Project(), context = GenerationContext(), dryRun = false)

        assertEquals(
            """modifier =
                 androidx.compose.ui.Modifier
                 .androidx.compose.foundation.layout.size(
                   width = 50.androidx.compose.ui.unit.dp,
                   height = 50.androidx.compose.ui.unit.dp,
                 ),""".trimForCompare(),
            code.build().toString().trimForCompare(),
        )
    }

    @Test
    fun toComposeCode_singleModifier_withUnspecified() {
        val modifierList = listOf(ModifierWrapper.Size(width = 50.dp, height = Dp.Unspecified))

        val code =
            modifierList.generateCode(Project(), context = GenerationContext(), dryRun = false)

        assertEquals(
            """modifier = androidx.compose.ui.Modifier
                 .androidx.compose.foundation.layout.size(
                   width = 50.androidx.compose.ui.unit.dp,
                   height = androidx.compose.ui.unit.Dp.Unspecified,
                 ),""".trimForCompare(),
            code.build().toString().trimForCompare(),
        )
    }

    @Test
    fun serialize_verify_restored_instance() {
        val size = ModifierWrapper.Size(height = 8.dp, width = Dp.Unspecified)

        val encodedString = encodeToString(size)
        val decoded = decodeFromStringWithFallback<ModifierWrapper.Size>(encodedString)
        assertEquals(size, decoded)
    }
}
