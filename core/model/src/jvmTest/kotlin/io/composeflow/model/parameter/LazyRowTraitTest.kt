package io.composeflow.model.parameter

import androidx.compose.ui.unit.dp
import io.composeflow.kotlinpoet.GenerationContext
import io.composeflow.model.parameter.wrapper.AlignmentVerticalWrapper
import io.composeflow.model.parameter.wrapper.ArrangementHorizontalWrapper
import io.composeflow.model.project.Project
import io.composeflow.model.project.appscreen.screen.composenode.ComposeNode
import io.composeflow.serializer.decodeFromStringWithFallback
import io.composeflow.serializer.encodeToString
import io.composeflow.trimForCompare
import kotlinx.serialization.encodeToString
import org.junit.Test
import kotlin.test.assertEquals

class LazyRowTraitTest {
    @Test
    fun toComposeCode_emptyParams() {
        val lazyRowParams = LazyRowTrait()

        val code =
            lazyRowParams.generateCode(
                Project(),
                node = ComposeNode(),
                context = GenerationContext(),
                dryRun = false,
            )
        assertEquals(
            """
                androidx.compose.foundation.lazy.LazyRow {}
        """.trimForCompare(),
            code.toString().trimForCompare(),
        )
    }

    @Test
    fun toComposeCode_nonEmptyParams() {
        val lazyRowParams =
            LazyRowTrait(
                contentPadding = 8.dp,
                reverseLayout = true,
                horizontalArrangement = ArrangementHorizontalWrapper.End,
                verticalAlignment = AlignmentVerticalWrapper.CenterVertically,
                userScrollEnabled = false,
            )

        val code =
            lazyRowParams.generateCode(
                Project(),
                node = ComposeNode(),
                context = GenerationContext(),
                dryRun = false,
            )
        assertEquals(
            """
                androidx.compose.foundation.lazy.LazyRow(
                contentPadding = androidx.compose.foundation.layout.PaddingValues(8.androidx.compose.ui.unit.dp),
                reverseLayout = true,
                horizontalArrangement = androidx.compose.foundation.layout.Arrangement.End,
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                userScrollEnabled = false,
                ) {}
        """.trimForCompare(),
            code.toString().trimForCompare(),
        )
    }

    @Test
    fun serialize_deserialize() {
        val lazyRowParams =
            LazyRowTrait(
                contentPadding = 16.dp,
                horizontalArrangement = ArrangementHorizontalWrapper.Center,
                verticalAlignment = AlignmentVerticalWrapper.Bottom,
            )

        val encoded = encodeToString(lazyRowParams)
        val decoded = decodeFromStringWithFallback<LazyRowTrait>(encoded)

        assertEquals(lazyRowParams, decoded)
    }
}
