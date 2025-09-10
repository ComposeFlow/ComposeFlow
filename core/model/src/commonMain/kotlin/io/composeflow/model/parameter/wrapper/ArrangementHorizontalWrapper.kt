package io.composeflow.model.parameter.wrapper

import androidx.compose.foundation.layout.Arrangement
import io.composeflow.serializer.FallbackEnumSerializer
import kotlinx.serialization.Serializable

object ArrangementHorizontalWrapperSerializer :
    FallbackEnumSerializer<ArrangementHorizontalWrapper>(
        ArrangementHorizontalWrapper::class,
        ArrangementHorizontalWrapper.entries.toTypedArray(),
    )

@Serializable(ArrangementHorizontalWrapperSerializer::class)
enum class ArrangementHorizontalWrapper(
    val arrangement: Arrangement.Horizontal,
) {
    Start(Arrangement.Start),
    Center(Arrangement.Center),
    End(Arrangement.End),
    SpaceEvenly(Arrangement.SpaceEvenly),
    SpaceBetween(Arrangement.SpaceBetween),
    SpaceAround(Arrangement.SpaceAround),
}
