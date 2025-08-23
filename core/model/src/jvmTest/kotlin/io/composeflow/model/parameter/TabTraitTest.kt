package io.composeflow.model.parameter

import io.composeflow.kotlinpoet.GenerationContext
import io.composeflow.materialicons.Outlined
import io.composeflow.model.project.Project
import io.composeflow.model.project.appscreen.screen.composenode.ComposeNode
import io.composeflow.model.property.StringProperty
import io.composeflow.serializer.decodeFromStringWithFallback
import io.composeflow.serializer.encodeToString
import io.composeflow.trimForCompare
import kotlinx.serialization.encodeToString
import org.junit.Assert
import org.junit.Test
import kotlin.test.assertEquals

class TabTraitTest {
    @Test
    fun toComposeCode_initialState() {
        val tabParams =
            TabTrait(
                text = StringProperty.StringIntrinsicValue("test"),
            )

        val code =
            tabParams.generateCode(
                Project(),
                node = ComposeNode(),
                context = GenerationContext(),
                dryRun = false,
            )
        assertEquals(
            """
            androidx.compose.material3.Tab(
            selected = selectedIndex == 0,
            onClick = { selectedIndex = 0 },
            text = { androidx.compose.material3.Text("test") },
            )
        """.trimForCompare(),
            code.toString().trimForCompare(),
        )
    }

    @Test
    fun toComposeCode_iconIsSet() {
        val tabParams =
            TabTrait(
                text = StringProperty.StringIntrinsicValue("test"),
                icon = Outlined.Abc,
            )

        val code =
            tabParams.generateCode(
                Project(),
                node = ComposeNode(),
                context = GenerationContext(),
                dryRun = false,
            )
        assertEquals(
            """
            androidx.compose.material3.Tab(
            selected = selectedIndex == 0,
            onClick = { selectedIndex = 0 },
            text = { androidx.compose.material3.Text("test") },
            icon = { androidx.compose.material3.Icon(
              imageVector = androidx.compose.material.icons.Icons.Outlined.androidx.compose.material.icons.outlined.Abc,
              contentDescription = null,)
            },
            )
        """.trimForCompare(),
            code.toString().trimForCompare(),
        )
    }

    @Test
    fun serialize_deserialize() {
        val tabParams =
            TabTrait(
                text = StringProperty.StringIntrinsicValue("tab text"),
                enabled = false,
            )

        val encoded = encodeToString(tabParams)
        val decoded = decodeFromStringWithFallback<TabTrait>(encoded)

        Assert.assertEquals(tabParams, decoded)
    }
}
