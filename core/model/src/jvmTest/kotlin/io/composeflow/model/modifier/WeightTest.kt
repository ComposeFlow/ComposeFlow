package io.composeflow.model.modifier

import io.composeflow.kotlinpoet.GenerationContext
import io.composeflow.model.project.Project
import io.composeflow.serializer.decodeFromStringWithFallback
import io.composeflow.serializer.encodeToString
import io.composeflow.trimForCompare
import kotlinx.serialization.encodeToString
import org.junit.Test
import kotlin.test.assertEquals

class WeightTest {
    @Test
    fun toComposeCode_weight() {
        val modifierList = listOf(ModifierWrapper.Weight(1f, true))

        val code =
            modifierList.generateCode(Project(), context = GenerationContext(), dryRun = false)

        assertEquals(
            """modifier = androidx.compose.ui.Modifier.weight(weight = 1.0f, fill = true),"""
                .trimForCompare(),
            code.build().toString().trimForCompare(),
        )
    }

    @Test
    fun toModifier_verifyNotCrash() {
        val weight = ModifierWrapper.Weight(weight = 1f, fill = false)
        weight.toModifier()
    }

    @Test
    fun serialize_verify_restored_instance() {
        val weight = ModifierWrapper.Weight(weight = 1f, fill = false)

        val encodedString = encodeToString(weight)
        val decoded = decodeFromStringWithFallback<ModifierWrapper.Weight>(encodedString)
        assertEquals(weight, decoded)
    }
}
