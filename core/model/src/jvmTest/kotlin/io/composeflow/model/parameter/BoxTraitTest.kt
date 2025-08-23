package io.composeflow.model.parameter

import io.composeflow.model.parameter.wrapper.AlignmentWrapper
import io.composeflow.serializer.decodeFromStringWithFallback
import io.composeflow.serializer.encodeToString
import kotlinx.serialization.encodeToString
import org.junit.Assert.assertEquals
import org.junit.Test

class BoxTraitTest {
    @Test
    fun serialize_deserialize() {
        val boxParams = BoxTrait(contentAlignment = AlignmentWrapper.Center)

        val encoded = encodeToString(boxParams)
        val decoded = decodeFromStringWithFallback<BoxTrait>(encoded)

        assertEquals(boxParams, decoded)
    }
}
