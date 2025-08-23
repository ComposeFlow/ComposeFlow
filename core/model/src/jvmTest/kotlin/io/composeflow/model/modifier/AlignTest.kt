package io.composeflow.model.modifier

import io.composeflow.kotlinpoet.GenerationContext
import io.composeflow.model.project.Project
import io.composeflow.trimForCompare
import org.junit.Test
import kotlin.test.assertEquals

class AlignTest {
    @Test
    fun toComposeCode_align_default() {
        val modifierList = listOf(ModifierWrapper.Align())

        val code =
            modifierList.generateCode(Project(), context = GenerationContext(), dryRun = false)

        assertEquals(
            "modifier = androidx.compose.ui.Modifier.align(androidx.compose.ui.Alignment.TopStart),".trimForCompare(),
            code.build().toString().trimForCompare(),
        )
    }

    @Test
    fun toModifier_verifyNoCrash() {
        // Verify it doesn't crash in toModifier method since it uses reflection, changing the
        // Compose version may change the signature of the constructor.
        ModifierWrapper.Align().toModifier()
    }
}
