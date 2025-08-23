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

class HeightTest {
    @Test
    fun toComposeCode_height() {
        val modifierList = listOf(ModifierWrapper.Height(50.dp))

        val code =
            modifierList.generateCode(Project(), context = GenerationContext(), dryRun = false)

        assertEquals(
            """modifier =
                 androidx.compose.ui.Modifier
                 .androidx.compose.foundation.layout.height(
                   50.androidx.compose.ui.unit.dp),""".trimForCompare(),
            code.build().toString().trimForCompare(),
        )
    }

    @Test
    fun serialize_verify_restored_instance() {
        val height = ModifierWrapper.Height(height = 8.dp)

        val encodedString = encodeToString(height)
        val decoded = decodeFromStringWithFallback<ModifierWrapper.Height>(encodedString)
        assertEquals(height, decoded)
    }
}
