package io.composeflow.model.parameter

import io.composeflow.kotlinpoet.GenerationContext
import io.composeflow.model.enumwrapper.FontStyleWrapper
import io.composeflow.model.enumwrapper.TextAlignWrapper
import io.composeflow.model.enumwrapper.TextDecorationWrapper
import io.composeflow.model.enumwrapper.TextOverflowWrapper
import io.composeflow.model.enumwrapper.TextStyleWrapper
import io.composeflow.model.parameter.wrapper.ColorWrapper
import io.composeflow.model.parameter.wrapper.Material3ColorWrapper
import io.composeflow.model.project.Project
import io.composeflow.model.project.appscreen.screen.composenode.ComposeNode
import io.composeflow.model.property.BooleanProperty
import io.composeflow.model.property.ColorProperty
import io.composeflow.model.property.ConditionalProperty
import io.composeflow.model.property.EnumProperty
import io.composeflow.model.property.StringProperty
import io.composeflow.serializer.decodeFromStringWithFallback
import io.composeflow.serializer.encodeToString
import io.composeflow.trimForCompare
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Assert
import org.junit.Test
import kotlin.test.assertEquals

class TextTraitTest {
    @Test
    fun toComposeCode_color() {
        val textParams =
            TextTrait(
                text = StringProperty.StringIntrinsicValue("test"),
                colorWrapper = ColorProperty.ColorIntrinsicValue(ColorWrapper(Material3ColorWrapper.Secondary)),
            )

        val code =
            textParams.generateCode(
                Project(),
                node = ComposeNode(),
                context = GenerationContext(),
                dryRun = false,
            )
        assertEquals(
            """
            androidx.compose.material3.Text(
            text = "test",
            color = androidx.compose.material3.MaterialTheme.colorScheme.secondary,
            )
        """.trimForCompare(),
            code.toString().trimForCompare(),
        )
    }

    @Test
    fun toComposeCode_fontStyle() {
        val textParams =
            TextTrait(
                text = StringProperty.StringIntrinsicValue("test"),
                fontStyle = EnumProperty(FontStyleWrapper.Italic),
            )

        val code =
            textParams.generateCode(
                Project(),
                node = ComposeNode(),
                context = GenerationContext(),
                dryRun = false,
            )
        assertEquals(
            """
            androidx.compose.material3.Text(
            text = "test",
            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
            )
        """.trimForCompare(),
            code.toString().trimForCompare(),
        )
    }

    @Test
    fun toComposeCode_textDecoration() {
        val textParams =
            TextTrait(
                text = StringProperty.StringIntrinsicValue("test"),
                textDecoration = EnumProperty(TextDecorationWrapper.LineThrough),
            )

        val code =
            textParams.generateCode(
                Project(),
                node = ComposeNode(),
                context = GenerationContext(),
                dryRun = false,
            )

        assertEquals(
            """
            androidx.compose.material3.Text(
            text = "test",
            textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough,
            )
        """.trimForCompare(),
            code.toString().trimForCompare(),
        )
    }

    @Test
    fun toComposeCode_textAlign() {
        val textParams =
            TextTrait(
                text = StringProperty.StringIntrinsicValue("test"),
                textAlign = EnumProperty(TextAlignWrapper.Justify),
            )

        val code =
            textParams.generateCode(
                Project(),
                node = ComposeNode(),
                context = GenerationContext(),
                dryRun = false,
            )

        assertEquals(
            """
            androidx.compose.material3.Text(
            text = "test",
            textAlign = androidx.compose.ui.text.style.TextAlign.Justify,
            )
        """.trimForCompare(),
            code.toString().trimForCompare(),
        )
    }

    @Test
    fun toComposeCode_overflow() {
        val textParams =
            TextTrait(
                text = StringProperty.StringIntrinsicValue("test"),
                overflow = EnumProperty(TextOverflowWrapper.Ellipsis),
            )

        val code =
            textParams.generateCode(
                Project(),
                node = ComposeNode(),
                context = GenerationContext(),
                dryRun = false,
            )

        assertEquals(
            """
            androidx.compose.material3.Text(
            text = "test",
            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
            )
        """.trimForCompare(),
            code.toString().trimForCompare(),
        )
    }

    @Test
    fun toComposeCode_softWrap() {
        val textParams =
            TextTrait(
                text = StringProperty.StringIntrinsicValue("test"),
                softWrap = false,
            )

        val code =
            textParams.generateCode(
                Project(),
                node = ComposeNode(),
                context = GenerationContext(),
                dryRun = false,
            )

        assertEquals(
            """
            androidx.compose.material3.Text(
            text = "test",
            softWrap = false,
            )
        """.trimForCompare(),
            code.toString().trimForCompare(),
        )
    }

    @Test
    fun toComposeCode_maxLines() {
        val textParams =
            TextTrait(
                text = StringProperty.StringIntrinsicValue("test"),
                maxLines = 2,
            )

        val code =
            textParams.generateCode(
                Project(),
                node = ComposeNode(),
                context = GenerationContext(),
                dryRun = false,
            )

        assertEquals(
            """
            androidx.compose.material3.Text(
            text = "test",
            maxLines = 2,
            )
        """.trimForCompare(),
            code.toString().trimForCompare(),
        )
    }

    @Test
    fun toComposeCode_style() {
        val textParams =
            TextTrait(
                text = StringProperty.StringIntrinsicValue("test"),
                textStyleWrapper = EnumProperty(TextStyleWrapper.BodyLarge),
            )

        val code =
            textParams.generateCode(
                Project(),
                node = ComposeNode(),
                context = GenerationContext(),
                dryRun = false,
            )

        assertEquals(
            """
            androidx.compose.material3.Text(
            text = "test",
            style = androidx.compose.material3.MaterialTheme.typography.bodyLarge,
            )
        """.trimForCompare(),
            code.toString().trimForCompare(),
        )
    }

    @Test
    fun toComposeCode_longText() {
        val textParams =
            TextTrait(
                text =
                    StringProperty.StringIntrinsicValue(
                        "Great news! Your package has arrived safely and is now waiting for you at your doorstep. Please check it at your earliest convenience and let us know if everything is in order. Enjoy your purchase!",
                    ),
            )

        val code =
            textParams.generateCode(
                Project(),
                node = ComposeNode(),
                context = GenerationContext(),
                dryRun = false,
            )
        val textStr =
            "\"Great news! Your package has arrived safely and is now waiting for you at your doorstep. Please check it at your earliest convenience and let us know if everything is in order. Enjoy your purchase!\""
        assertEquals(
            """
            androidx.compose.material3.Text(
            text = $textStr,
            )
        """.trimForCompare(),
            code.toString().trimForCompare(),
        )
    }

    @Test
    fun toComposeCode_fromJsonElement() {
        val textParams =
            TextTrait(
                text =
                    StringProperty.ValueByJsonPath(
                        "result",
                        jsonElement = Json.parseToJsonElement("""{ "result": "test" }"""),
                    ),
            )

        val code =
            textParams.generateCode(
                Project(),
                node = ComposeNode(),
                context = GenerationContext(),
                dryRun = false,
            )

        assertEquals(
            """
            androidx.compose.material3.Text(
            text = io.composeflow.util.selectString(jsonElement, "result", replaceQuotation = true),
            )
        """.trimForCompare(),
            code.toString().trimForCompare(),
        )
    }

    @Test
    fun serialize_deserialize() {
        val textParams =
            TextTrait(
                text = StringProperty.StringIntrinsicValue("text"),
                fontStyle = EnumProperty(FontStyleWrapper.Italic),
                textDecoration = EnumProperty(TextDecorationWrapper.LineThrough),
                textAlign = EnumProperty(TextAlignWrapper.Justify),
                overflow = EnumProperty(TextOverflowWrapper.Clip),
                textStyleWrapper = EnumProperty(TextStyleWrapper.BodyMedium),
            )

        val encoded = encodeToString(textParams)
        val decoded = decodeFromStringWithFallback<TextTrait>(encoded)

        Assert.assertEquals(textParams, decoded)
    }

    @Test
    fun serialize_deserialize_includingConditionalProperty() {
        val textParams =
            TextTrait(
                text = StringProperty.StringIntrinsicValue("text"),
                fontStyle = EnumProperty(FontStyleWrapper.Italic),
                textDecoration =
                    ConditionalProperty(
                        defaultValue = EnumProperty(TextDecorationWrapper.None),
                        ifThen =
                            ConditionalProperty.IfThenBlock(
                                ifExpression = BooleanProperty.BooleanIntrinsicValue(),
                                thenValue = EnumProperty(value = TextDecorationWrapper.LineThrough),
                            ),
                        elseIfBlocks =
                            mutableListOf(
                                ConditionalProperty.IfThenBlock(
                                    ifExpression = BooleanProperty.BooleanIntrinsicValue(true),
                                    thenValue = EnumProperty(value = TextDecorationWrapper.None),
                                ),
                                ConditionalProperty.IfThenBlock(
                                    ifExpression = BooleanProperty.BooleanIntrinsicValue(false),
                                    thenValue = EnumProperty(value = TextDecorationWrapper.LineThrough),
                                ),
                            ),
                        elseBlock =
                            ConditionalProperty.ElseBlock(
                                value = EnumProperty(value = TextDecorationWrapper.Underline),
                            ),
                    ),
                textAlign = EnumProperty(TextAlignWrapper.Justify),
                overflow = EnumProperty(TextOverflowWrapper.Clip),
                textStyleWrapper = EnumProperty(TextStyleWrapper.BodyMedium),
            )

        val encoded = encodeToString(textParams)
        val decoded = decodeFromStringWithFallback<TextTrait>(encoded)

        Assert.assertEquals(textParams, decoded)
    }
}
