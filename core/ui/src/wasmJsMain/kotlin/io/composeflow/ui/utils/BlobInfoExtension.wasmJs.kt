package io.composeflow.ui.utils

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import io.composeflow.cloud.storage.BlobInfoWrapper

@Composable
actual fun BlobInfoWrapper.asIconComposable(
    userId: String,
    projectId: String,
    tint: Color,
    modifier: Modifier,
) {
    // No-op for WASM - image loading not supported
    Box(modifier = modifier)
}

@Composable
actual fun BlobInfoWrapper.asImageComposable(
    userId: String,
    projectId: String,
    modifier: Modifier,
    alignment: Alignment,
    contentScale: ContentScale,
    alpha: Float,
) {
    // No-op for WASM - image loading not supported
    Box(modifier = modifier)
}
