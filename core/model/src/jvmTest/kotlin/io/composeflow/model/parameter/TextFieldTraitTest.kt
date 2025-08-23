package io.composeflow.model.parameter

import androidx.compose.ui.unit.dp
import io.composeflow.kotlinpoet.GenerationContext
import io.composeflow.materialicons.Outlined
import io.composeflow.model.parameter.wrapper.ShapeWrapper
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

class TextFieldTraitTest {
    @Test
    fun toComposeCode_color() {
        val textFieldParams =
            TextFieldTrait(
                value = StringProperty.StringIntrinsicValue("test"),
            )

        val code =
            textFieldParams.generateCode(
                Project(),
                node = ComposeNode(),
                context = GenerationContext(),
                dryRun = false,
            )
        assertEquals(
            """
            androidx.compose.material3.TextField(
            value = "test",
            onValueChange = {},
            singleLine = true,
            )
        """.trimForCompare(),
            code.toString().trimForCompare(),
        )
    }

    @Test
    fun toComposeCode_shape() {
        val textFieldParams =
            TextFieldTrait(
                value = StringProperty.StringIntrinsicValue("test"),
                shapeWrapper = ShapeWrapper.RoundedCorner(32.dp),
            )

        val code =
            textFieldParams.generateCode(
                Project(),
                node = ComposeNode(),
                context = GenerationContext(),
                dryRun = false,
            )
        assertEquals(
            """
            androidx.compose.material3.TextField(
            value="test",
            onValueChange = {},
            singleLine = true,
            shape = androidx.compose.foundation.shape.RoundedCornerShape(32.androidx.compose.ui.unit.dp),
            )
        """.trimForCompare(),
            code.toString().trimForCompare(),
        )
    }

    @Test
    fun toComposeCode_transparentIndicator() {
        val textFieldParams =
            TextFieldTrait(
                value = StringProperty.StringIntrinsicValue("test"),
                transparentIndicator = true,
            )

        val code =
            textFieldParams.generateCode(
                Project(),
                node = ComposeNode(),
                context = GenerationContext(),
                dryRun = false,
            )
        assertEquals(
            """
            androidx.compose.material3.TextField(
            value="test",
            onValueChange = {},
            singleLine = true,
            colors = androidx.compose.material3.TextFieldDefaults.colors(
              focusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent,
              unfocusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent,
              disabledIndicatorColor = androidx.compose.ui.graphics.Color.Transparent,
              errorIndicatorColor = androidx.compose.ui.graphics.Color.Transparent,
              ),
            )
        """.trimForCompare(),
            code.toString().trimForCompare(),
        )
    }

    @Test
    fun toComposeCode_outlinedTextFieldType() {
        val textFieldParams =
            TextFieldTrait(
                value = StringProperty.StringIntrinsicValue("test"),
                transparentIndicator = true,
                textFieldType = TextFieldType.Outlined,
            )

        val code =
            textFieldParams.generateCode(
                Project(),
                node = ComposeNode(),
                context = GenerationContext(),
                dryRun = false,
            )
        assertEquals(
            """
            androidx.compose.material3.OutlinedTextField(
            value="test",
            onValueChange = {},
            singleLine = true,
            )
        """.trimForCompare(),
            code.toString().trimForCompare(),
        )
    }

    @Test
    fun serialize_deserialize() {
        val textFieldParams =
            TextFieldTrait(
                value = StringProperty.StringIntrinsicValue("textField"),
                trailingIcon = Outlined.Icecream,
                shapeWrapper = ShapeWrapper.Circle,
            )

        val encoded = encodeToString(textFieldParams)
        val decoded = decodeFromStringWithFallback<TextFieldTrait>(encoded)

        Assert.assertEquals(textFieldParams, decoded)
    }
}
