package io.composeflow.model.modifier

import androidx.compose.ui.unit.dp
import io.composeflow.kotlinpoet.GenerationContext
import io.composeflow.model.parameter.wrapper.ShapeWrapper
import io.composeflow.model.project.Project
import io.composeflow.serializer.decodeFromStringWithFallback
import io.composeflow.serializer.encodeToString
import io.composeflow.trimForCompare
import kotlinx.serialization.encodeToString
import org.junit.Test
import kotlin.test.assertEquals

class ClipTest {
    @Test
    fun toComposeCode_clip_default() {
        val modifierList = listOf(ModifierWrapper.Clip())

        val code =
            modifierList.generateCode(Project(), context = GenerationContext(), dryRun = false)

        assertEquals(
            """modifier = androidx.compose.ui.Modifier.androidx.compose.ui.draw.clip(
                 shape = androidx.compose.ui.graphics.RectangleShape,
               ),""".trimForCompare(),
            code.build().toString().trimForCompare(),
        )
    }

    @Test
    fun toComposeCode_clip_nonDefault() {
        val modifierList =
            listOf(ModifierWrapper.Clip(shapeWrapper = ShapeWrapper.RoundedCorner(16.dp)))

        val code =
            modifierList.generateCode(Project(), context = GenerationContext(), dryRun = false)

        assertEquals(
            """modifier = androidx.compose.ui.Modifier.androidx.compose.ui.draw.clip(
                 shape = androidx.compose.foundation.shape.RoundedCornerShape(16.androidx.compose.ui.unit.dp)
               ,),""".trimForCompare(),
            code.build().toString().trimForCompare(),
        )
    }

    @Test
    fun serialize_verify_restored_instance() {
        val clip = ModifierWrapper.Clip(shapeWrapper = ShapeWrapper.CutCorner(8.dp))

        val encodedString = encodeToString(clip)
        val decoded = decodeFromStringWithFallback<ModifierWrapper.Clip>(encodedString)
        assertEquals(clip, decoded)
    }
}
