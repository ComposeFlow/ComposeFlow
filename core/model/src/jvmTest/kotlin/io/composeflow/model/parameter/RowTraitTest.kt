package io.composeflow.model.parameter

import io.composeflow.model.parameter.wrapper.AlignmentVerticalWrapper
import io.composeflow.model.parameter.wrapper.ArrangementHorizontalWrapper
import io.composeflow.serializer.decodeFromStringWithFallback
import io.composeflow.serializer.encodeToString
import kotlinx.serialization.encodeToString
import org.junit.Assert.assertEquals
import org.junit.Test

class RowTraitTest {
    @Test
    fun serialize_deserialize() {
        val rowParams =
            RowTrait(
                horizontalArrangement = ArrangementHorizontalWrapper.End,
                verticalAlignment = AlignmentVerticalWrapper.CenterVertically,
            )

        val encoded = encodeToString(rowParams)
        val decoded = decodeFromStringWithFallback<RowTrait>(encoded)

        assertEquals(rowParams, decoded)
    }
}
