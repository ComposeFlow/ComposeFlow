package io.composeflow.model.parameter

import io.composeflow.model.property.StringProperty
import io.composeflow.serializer.decodeFromStringWithFallback
import io.composeflow.serializer.encodeToString
import kotlinx.serialization.encodeToString
import org.junit.Assert
import org.junit.Test

class TopAppBarTraitTest {
    @Test
    fun serialize_deserialize() {
        val topAppBarParams =
            TopAppBarTrait(
                title = StringProperty.StringIntrinsicValue("test title"),
                topAppBarType = TopAppBarTypeWrapper.CenterAligned,
                scrollBehaviorWrapper = ScrollBehaviorWrapper.EnterAlways,
            )

        val encoded = encodeToString(topAppBarParams)
        val decoded = decodeFromStringWithFallback<TopAppBarTrait>(encoded)

        Assert.assertTrue(topAppBarParams.contentEquals(decoded))
    }
}
