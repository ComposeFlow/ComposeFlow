package io.composeflow.model.parameter

import io.composeflow.kotlinpoet.GenerationContext
import io.composeflow.materialicons.Outlined
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

class IconTraitTest {
    @Test
    fun toComposeCode_onlyRequiredParams() {
        val iconParams =
            IconTrait(
                imageVectorHolder = Outlined.ImageSearch,
            )

        val code =
            iconParams.generateCode(
                Project(),
                node = ComposeNode(),
                context = GenerationContext(),
                dryRun = false,
            )
        assertEquals(
            """
            androidx.compose.material3.Icon(
            imageVector = androidx.compose.material.icons.Icons.Outlined.androidx.compose.material.icons.outlined.ImageSearch,
            contentDescription = "Icon for ImageSearch",
            tint = androidx.compose.material3.MaterialTheme.colorScheme.onSurface,
            )
        """.trimForCompare(),
            code.toString().trimForCompare(),
        )
    }

    @Test
    fun toComposeCode_withTint() {
        val iconParams =
            IconTrait(
                imageVectorHolder = Outlined.ImageSearch,
                tint = ColorProperty.ColorIntrinsicValue(ColorWrapper(Material3ColorWrapper.Secondary)),
            )

        val code =
            iconParams.generateCode(
                Project(),
                node = ComposeNode(),
                context = GenerationContext(),
                dryRun = false,
            )
        assertEquals(
            """
            androidx.compose.material3.Icon(
            imageVector = androidx.compose.material.icons.Icons.Outlined.androidx.compose.material.icons.outlined.ImageSearch,
            contentDescription="Icon for ImageSearch",
            tint = androidx.compose.material3.MaterialTheme.colorScheme.secondary,
            )
        """.trimForCompare(),
            code.toString().trimForCompare(),
        )
    }

    @Test
    fun serialize_deserialize() {
        val iconParams = IconTrait(imageVectorHolder = Outlined.ViewArray)

        val encoded = encodeToString(iconParams)
        val decoded = decodeFromStringWithFallback<IconTrait>(encoded)

        Assert.assertEquals(iconParams, decoded)
    }
}
