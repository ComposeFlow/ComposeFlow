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

class PaddingTest {
    @Test
    fun constructor_all_to_horizontalVertical() {
        val modifierList =
            listOf(
                ModifierWrapper.Padding(8.dp),
            )
        val modifierList2 =
            listOf(
                ModifierWrapper.Padding(horizontal = 8.dp, vertical = 8.dp),
            )

        assertEquals(modifierList, modifierList2)
    }

    @Test
    fun constructor_all_to_eachPadding() {
        val modifierList =
            listOf(
                ModifierWrapper.Padding(8.dp),
            )
        val modifierList2 =
            listOf(
                ModifierWrapper.Padding(start = 8.dp, top = 8.dp, end = 8.dp, bottom = 8.dp),
            )

        assertEquals(modifierList, modifierList2)
    }

    @Test
    fun findFirst_verifySumOfPadding() {
        val modifierList =
            listOf(
                ModifierWrapper.Padding(8.dp),
                ModifierWrapper.Padding(16.dp),
            )

        val sum = modifierList.sumPadding()

        val expected = ModifierWrapper.Padding(24.dp)
        assertEquals(expected, sum)
    }

    @Test
    fun toComposeCode_singleModifier() {
        val modifierList =
            listOf(
                ModifierWrapper.Padding(8.dp),
            )

        val code =
            modifierList.generateCode(Project(), context = GenerationContext(), dryRun = false)
        assertEquals(
            """modifier =
                 androidx.compose.ui.Modifier
                 .androidx.compose.foundation.layout.padding(
                   all = 8.androidx.compose.ui.unit.dp),""".trimForCompare(),
            code.build().toString().trimForCompare(),
        )
    }

    @Test
    fun toComposeCode_horizontal_vertical_paddings() {
        val modifierList =
            listOf(
                ModifierWrapper.Padding(horizontal = 8.dp, vertical = 4.dp),
            )

        val code =
            modifierList.generateCode(Project(), context = GenerationContext(), dryRun = false)
        assertEquals(
            """modifier =
                 androidx.compose.ui.Modifier
                 .androidx.compose.foundation.layout.padding(
                   horizontal = 8.androidx.compose.ui.unit.dp,
                   vertical = 4.androidx.compose.ui.unit.dp
                   ),""".trimForCompare(),
            code.build().toString().trimForCompare(),
        )
    }

    @Test
    fun toComposeCode_each_different_padding() {
        val modifierList =
            listOf(
                ModifierWrapper.Padding(start = 8.dp, top = 4.dp, end = 2.dp, bottom = 1.dp),
            )

        val code =
            modifierList.generateCode(Project(), context = GenerationContext(), dryRun = false)
        assertEquals(
            """modifier =
                 androidx.compose.ui.Modifier
                 .androidx.compose.foundation.layout.padding(
                   start = 8.androidx.compose.ui.unit.dp,
                   top = 4.androidx.compose.ui.unit.dp,
                   end = 2.androidx.compose.ui.unit.dp,
                   bottom = 1.androidx.compose.ui.unit.dp
                   ),""".trimForCompare(),
            code.build().toString().trimForCompare(),
        )
    }

    @Test
    fun toComposeCode_chainedModifiers() {
        val modifierList =
            listOf(
                ModifierWrapper.Padding(8.dp),
                ModifierWrapper.Padding(16.dp),
            )

        val code =
            modifierList.generateCode(Project(), context = GenerationContext(), dryRun = false)
        assertEquals(
            """modifier =
                 androidx.compose.ui.Modifier
                 .androidx.compose.foundation.layout.padding(
                   all = 8.androidx.compose.ui.unit.dp)
                 .androidx.compose.foundation.layout.padding(
                   all = 16.androidx.compose.ui.unit.dp),""".trimForCompare(),
            code.build().toString().trimForCompare(),
        )
    }

    @Test
    fun serialize_verify_restored_instance() {
        val padding = ModifierWrapper.Padding(all = 16.dp)

        val encodedString = encodeToString(padding)
        val decoded = decodeFromStringWithFallback<ModifierWrapper.Padding>(encodedString)
        assertEquals(padding, decoded)
    }
}
