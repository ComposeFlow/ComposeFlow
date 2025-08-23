package io.composeflow.model.parameter

import io.composeflow.kotlinpoet.GenerationContext
import io.composeflow.model.enumwrapper.ContentScaleWrapper
import io.composeflow.model.parameter.wrapper.AlignmentWrapper
import io.composeflow.model.project.Project
import io.composeflow.model.project.appscreen.screen.composenode.ComposeNode
import io.composeflow.model.property.EnumProperty
import io.composeflow.model.property.StringProperty
import io.composeflow.serializer.decodeFromStringWithFallback
import io.composeflow.serializer.encodeToString
import io.composeflow.trimForCompare
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Test
import kotlin.test.assertEquals

class ImageTraitTest {
    @Test
    fun toComposeCode_onlyRequiredParams() {
        val imageParams =
            ImageTrait(
                url = StringProperty.StringIntrinsicValue("https://example.com/image1"),
            )

        val code =
            imageParams.generateCode(
                Project(),
                node = ComposeNode(),
                context = GenerationContext(),
                dryRun = false,
            )
        assertEquals(
            """
            io.composeflow.ui.AsyncImage(
            url = "https://example.com/image1",
            contentDescription = "",
            )
        """.trimForCompare(),
            code.toString().trimForCompare(),
        )
    }

    @Test
    fun toComposeCode_withOptionalParams() {
        val imageParams =
            ImageTrait(
                url = StringProperty.StringIntrinsicValue("https://example.com/image1"),
                alignmentWrapper = AlignmentWrapper.BottomCenter,
                contentScaleWrapper = EnumProperty(ContentScaleWrapper.FillBounds),
                alpha = 0.5f,
            )

        val code =
            imageParams.generateCode(
                Project(),
                node = ComposeNode(),
                context = GenerationContext(),
                dryRun = false,
            )
        assertEquals(
            """
            io.composeflow.ui.AsyncImage(
            url = "https://example.com/image1",
            contentDescription = "",
            alignment = androidx.compose.ui.Alignment.BottomCenter,
            contentScale = androidx.compose.ui.layout.ContentScale.FillBounds,
            alpha = 0.5f,
            )
        """.trimForCompare(),
            code.toString().trimForCompare(),
        )
    }

    @Test
    fun toComposeCode_fromJsonElement() {
        val imageParams =
            ImageTrait(
                url =
                    StringProperty.ValueByJsonPath(
                        "result",
                        jsonElement = Json.parseToJsonElement("""{ "result": "test" }"""),
                    ),
            )

        val code =
            imageParams.generateCode(
                Project(),
                node = ComposeNode(),
                context = GenerationContext(),
                dryRun = false,
            )
        assertEquals(
            """
              io.composeflow.ui.AsyncImage(
              url = io.composeflow.util.selectString(jsonElement, "result", replaceQuotation = true),
              contentDescription="",
              )
        """.trimForCompare(),
            code.toString().trimForCompare(),
        )
    }

    @Test
    fun serialize_deserialize() {
        val imageParams =
            ImageTrait(
                url =
                    StringProperty.ValueByJsonPath(
                        "result",
                        jsonElement = Json.parseToJsonElement("""{ "result": "test" }"""),
                    ),
            )

        val encoded = encodeToString(imageParams)
        val decoded = decodeFromStringWithFallback<ImageTrait>(encoded)

        assertEquals(imageParams, decoded)
    }
}
