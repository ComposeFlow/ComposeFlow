package io.composeflow.model.parameter

import androidx.compose.ui.unit.dp
import io.composeflow.kotlinpoet.GenerationContext
import io.composeflow.model.parameter.wrapper.ColorWrapper
import io.composeflow.model.parameter.wrapper.Material3ColorWrapper
import io.composeflow.model.project.Project
import io.composeflow.model.project.appscreen.screen.composenode.ComposeNode
import io.composeflow.model.property.ColorProperty
import io.composeflow.serializer.decodeFromStringWithFallback
import io.composeflow.serializer.encodeToString
import io.composeflow.trimForCompare
import kotlinx.serialization.encodeToString
import org.junit.Assert
import org.junit.Test
import kotlin.test.assertEquals

class HorizontalDividerTraitTest {
    @Test
    fun toComposeCode_default() {
        val dividerParams = HorizontalDividerTrait()

        val code =
            dividerParams.generateCode(
                Project(),
                node = ComposeNode(),
                context = GenerationContext(),
                dryRun = false,
            )
        assertEquals(
            "androidx.compose.material3.HorizontalDivider()",
            code.toString().trimForCompare(),
        )
    }

    @Test
    fun toComposeCode_withOptionalArguments() {
        val dividerParams =
            HorizontalDividerTrait(
                thickness = 4.dp,
                color = ColorProperty.ColorIntrinsicValue(ColorWrapper(themeColor = Material3ColorWrapper.Outline)),
            )

        val code =
            dividerParams.generateCode(
                Project(),
                node = ComposeNode(),
                context = GenerationContext(),
                dryRun = false,
            )
        assertEquals(
            """
            androidx.compose.material3.HorizontalDivider(
            thickness = 4.androidx.compose.ui.unit.dp,
            color = androidx.compose.material3.MaterialTheme.colorScheme.outline,
            )
        """.trimForCompare(),
            code.toString().trimForCompare(),
        )
    }

    @Test
    fun serialize_deserialize() {
        val dividerParams =
            HorizontalDividerTrait(
                thickness = 4.dp,
                color = ColorProperty.ColorIntrinsicValue(ColorWrapper(themeColor = Material3ColorWrapper.Outline)),
            )
        val encoded = encodeToString(dividerParams)
        val decoded = decodeFromStringWithFallback<HorizontalDividerTrait>(encoded)

        Assert.assertEquals(dividerParams, decoded)
    }
}
