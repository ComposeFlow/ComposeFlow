package io.composeflow.model.parameter.wrapper

import androidx.compose.ui.graphics.TileMode
import io.composeflow.serializer.FallbackEnumSerializer
import kotlinx.serialization.Serializable

object TileModeWrapperSerializer :
    FallbackEnumSerializer<TileModeWrapper>(
        TileModeWrapper::class,
        TileModeWrapper.entries.toTypedArray(),
    )

@Serializable(TileModeWrapperSerializer::class)
enum class TileModeWrapper {
    Clamp,
    Repeated,
    Mirror,
    Decal,
}

fun TileModeWrapper.toTileMode(): TileMode =
    when (this) {
        TileModeWrapper.Clamp -> TileMode.Clamp
        TileModeWrapper.Repeated -> TileMode.Repeated
        TileModeWrapper.Mirror -> TileMode.Mirror
        TileModeWrapper.Decal -> TileMode.Decal
    }

fun TileMode.toTileModeWrapper(): TileModeWrapper =
    when (this) {
        TileMode.Clamp -> TileModeWrapper.Clamp
        TileMode.Repeated -> TileModeWrapper.Repeated
        TileMode.Mirror -> TileModeWrapper.Mirror
        TileMode.Decal -> TileModeWrapper.Decal
        else -> TileModeWrapper.Clamp // fallback for unknown modes
    }
