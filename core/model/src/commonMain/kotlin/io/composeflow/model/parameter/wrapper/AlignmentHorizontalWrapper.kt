package io.composeflow.model.parameter.wrapper

import androidx.compose.ui.Alignment
import io.composeflow.serializer.FallbackEnumSerializer
import kotlinx.serialization.Serializable

object AlignmentHorizontalWrapperSerializer :
    FallbackEnumSerializer<AlignmentHorizontalWrapper>(
        AlignmentHorizontalWrapper::class,
        AlignmentHorizontalWrapper.entries.toTypedArray(),
    )

@Serializable(AlignmentHorizontalWrapperSerializer::class)
enum class AlignmentHorizontalWrapper(
    val alignment: Alignment.Horizontal,
) {
    Start(Alignment.Start),
    CenterHorizontally(Alignment.CenterHorizontally),
    End(Alignment.End),
}
