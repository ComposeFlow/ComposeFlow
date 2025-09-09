package io.composeflow.model.parameter.wrapper

import androidx.compose.ui.Alignment
import io.composeflow.serializer.FallbackEnumSerializer
import kotlinx.serialization.Serializable

object AlignmentVerticalWrapperSerializer :
    FallbackEnumSerializer<AlignmentVerticalWrapper>(
        AlignmentVerticalWrapper::class,
        AlignmentVerticalWrapper.entries.toTypedArray(),
    )

@Serializable(AlignmentVerticalWrapperSerializer::class)
enum class AlignmentVerticalWrapper(
    val alignment: Alignment.Vertical,
) {
    Top(Alignment.Top),
    CenterVertically(Alignment.CenterVertically),
    Bottom(Alignment.Bottom),
}
