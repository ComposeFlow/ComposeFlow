@file:Suppress("ktlint:standard:package-name")

package io.composeflow.model.project.custom_enum

import io.composeflow.trimForCompare
import kotlin.test.Test
import kotlin.test.assertEquals

class CustomEnumTest {
    @Test
    fun testGenerateCustomEnumSpec() {
        val enum =
            CustomEnum(name = "SampleEnum").apply {
                values.addAll(
                    listOf("Entry1", "Entry2"),
                )
            }
        val typeSpec = enum.generateCustomEnumSpec()
        assertEquals(
            """
   public enum class SampleEnum {
    Entry1,
    Entry2,
    }""".trimForCompare(),
            typeSpec.toString().trimForCompare(),
        )
    }
}
