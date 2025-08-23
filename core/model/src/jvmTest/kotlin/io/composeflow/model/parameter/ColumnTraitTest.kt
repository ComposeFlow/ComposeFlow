package io.composeflow.model.parameter

import io.composeflow.model.parameter.wrapper.AlignmentHorizontalWrapper
import io.composeflow.model.parameter.wrapper.ArrangementVerticalWrapper
import io.composeflow.serializer.decodeFromStringWithFallback
import io.composeflow.serializer.encodeToString
import kotlinx.serialization.encodeToString
import org.junit.Assert.assertEquals
import org.junit.Test

class ColumnTraitTest {
    @Test
    fun serialize_deserialize() {
        val columnParams =
            ColumnTrait(
                verticalArrangementWrapper = ArrangementVerticalWrapper.Bottom,
                horizontalAlignmentWrapper = AlignmentHorizontalWrapper.CenterHorizontally,
            )

        val encoded = encodeToString(columnParams)
        val decoded = decodeFromStringWithFallback<ColumnTrait>(encoded)

        assertEquals(columnParams, decoded)
    }
}
