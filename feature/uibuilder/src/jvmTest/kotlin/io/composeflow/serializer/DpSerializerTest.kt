package io.composeflow.serializer

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.junit.Assert.assertEquals
import org.junit.Test

class DpSerializerTest {
    @Test
    fun normalValue() {
        verifySerializeDeserialize(8.dp)
    }

    @Test
    fun unspecified() {
        verifySerializeDeserialize(Dp.Unspecified)
    }

    @Test
    fun hairline() {
        verifySerializeDeserialize(Dp.Hairline)
    }

    @Test
    fun infinity() {
        verifySerializeDeserialize(Dp.Infinity)
    }

    @Test
    fun negativeValue() {
        val dp = Dp(-2f)
        val encoded = encodeToString(serializer = DpNonNegativeSerializer, dp)
        val decoded =
            decodeFromStringWithFallback(
                deserializer = DpNonNegativeSerializer,
                encoded,
            )
        assertEquals(Dp(0f), decoded)
    }

    private fun verifySerializeDeserialize(dp: Dp) {
        val encoded = encodeToString(serializer = DpSerializer, dp)
        val decoded = decodeFromStringWithFallback(deserializer = DpSerializer, encoded)

        assertEquals(dp, decoded)
    }
}
