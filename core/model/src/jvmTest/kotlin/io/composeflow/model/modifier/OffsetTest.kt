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

class OffsetTest {
    @Test
    fun toComposeCode_default_args() {
        val modifierList = listOf(ModifierWrapper.Offset())

        val code =
            modifierList.generateCode(Project(), context = GenerationContext(), dryRun = false)

        assertEquals(
            """modifier =
                 androidx.compose.ui.Modifier
                 .androidx.compose.foundation.layout.offset(
                   x = 0.androidx.compose.ui.unit.dp,
                   y = 0.androidx.compose.ui.unit.dp),""".trimForCompare(),
            code.build().toString().trimForCompare(),
        )
    }

    @Test
    fun toComposeCode_non_default_args() {
        val modifierList = listOf(ModifierWrapper.Offset(x = 10.dp, y = 20.dp))

        val code =
            modifierList.generateCode(Project(), context = GenerationContext(), dryRun = false)

        assertEquals(
            """modifier = androidx.compose.ui.Modifier
                 .androidx.compose.foundation.layout.offset(
                   x = 10.androidx.compose.ui.unit.dp,
                   y = 20.androidx.compose.ui.unit.dp),""".trimForCompare(),
            code.build().toString().trimForCompare(),
        )
    }

    @Test
    fun serialize_verify_restored_instance() {
        val offset = ModifierWrapper.Offset(x = 8.dp, y = 32.dp)

        val encodedString = encodeToString(offset)
        val decoded = decodeFromStringWithFallback<ModifierWrapper.Offset>(encodedString)
        assertEquals(offset, decoded)
    }
}
