package io.composeflow.model.parameter

import io.composeflow.model.property.StringProperty
import io.composeflow.serializer.decodeFromStringWithFallback
import io.composeflow.serializer.encodeToString
import kotlinx.serialization.encodeToString
import org.junit.Assert.assertEquals
import org.junit.Test

class ButtonTraitTest {
    @Test
    fun serialize_deserialize() {
        val buttonParams =
            ButtonTrait(
                textProperty = StringProperty.StringIntrinsicValue("button"),
            )

        val encoded = encodeToString(buttonParams)
        val decoded = decodeFromStringWithFallback<ButtonTrait>(encoded)

        assertEquals(buttonParams, decoded)
    }
}
