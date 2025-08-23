package io.composeflow.model.modifier

import io.composeflow.kotlinpoet.GenerationContext
import io.composeflow.model.project.Project
import io.composeflow.trimForCompare
import org.junit.Test
import kotlin.test.assertEquals

class AlignHorizontalTest {
    @Test
    fun toComposeCode_align_default() {
        val modifierList = listOf(ModifierWrapper.AlignHorizontal())

        val code =
            modifierList.generateCode(Project(), context = GenerationContext(), dryRun = false)

        assertEquals(
            "modifier = androidx.compose.ui.Modifier.align(androidx.compose.ui.Alignment.Start),".trimForCompare(),
            code.build().toString().trimForCompare(),
        )
    }

    @Test
    fun toModifier_verifyNoCrash() {
        // Verify it doesn't crash in toModifier method since it uses reflection, changing the
        // Compose version may change the signature of the constructor.
        ModifierWrapper.AlignHorizontal().toModifier()
    }
}
