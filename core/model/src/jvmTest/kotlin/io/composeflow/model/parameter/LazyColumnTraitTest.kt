package io.composeflow.model.parameter

import androidx.compose.ui.unit.dp
import io.composeflow.kotlinpoet.GenerationContext
import io.composeflow.model.parameter.wrapper.AlignmentHorizontalWrapper
import io.composeflow.model.parameter.wrapper.ArrangementVerticalWrapper
import io.composeflow.model.project.Project
import io.composeflow.model.project.appscreen.screen.composenode.ComposeNode
import io.composeflow.serializer.decodeFromStringWithFallback
import io.composeflow.serializer.encodeToString
import io.composeflow.trimForCompare
import kotlinx.serialization.encodeToString
import org.junit.Test
import kotlin.test.assertEquals

class LazyColumnTraitTest {
    @Test
    fun toComposeCode_emptyParams() {
        val lazyColumnParams = LazyColumnTrait()

        val code =
            lazyColumnParams.generateCode(
                Project(),
                node = ComposeNode(),
                context = GenerationContext(),
                dryRun = false,
            )
        assertEquals(
            """
                androidx.compose.foundation.lazy.LazyColumn {}
        """.trimForCompare(),
            code.toString().trimForCompare(),
        )
    }

    @Test
    fun toComposeCode_nonEmptyParams() {
        val lazyColumnParams =
            LazyColumnTrait(
                contentPadding = 8.dp,
                reverseLayout = true,
                verticalArrangement = ArrangementVerticalWrapper.Bottom,
                horizontalAlignment = AlignmentHorizontalWrapper.CenterHorizontally,
                userScrollEnabled = false,
            )

        val code =
            lazyColumnParams.generateCode(
                Project(),
                node = ComposeNode(),
                context = GenerationContext(),
                dryRun = false,
            )
        assertEquals(
            """
                androidx.compose.foundation.lazy.LazyColumn(
                contentPadding = androidx.compose.foundation.layout.PaddingValues(8.androidx.compose.ui.unit.dp),
                reverseLayout = true,
                verticalArrangement = androidx.compose.foundation.layout.Arrangement.Bottom,
                horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
                userScrollEnabled = false,
                ) {}
        """.trimForCompare(),
            code.toString().trimForCompare(),
        )
    }

    @Test
    fun serialize_deserialize() {
        val lazyColumnParams =
            LazyColumnTrait(
                contentPadding = 16.dp,
                verticalArrangement = ArrangementVerticalWrapper.Bottom,
                horizontalAlignment = AlignmentHorizontalWrapper.End,
            )

        val encoded = encodeToString(lazyColumnParams)
        val decoded = decodeFromStringWithFallback<LazyColumnTrait>(encoded)

        assertEquals(lazyColumnParams, decoded)
    }
}
