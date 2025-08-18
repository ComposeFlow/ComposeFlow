package io.composeflow.ui.utils

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import io.composeflow.cloud.storage.BlobInfoWrapper
import io.composeflow.platform.getAssetCacheFileFor

@Composable
expect fun BlobInfoWrapper.asIconComposable(
    userId: String,
    projectId: String,
    tint: Color = LocalContentColor.current,
    modifier: Modifier = Modifier,
)

@Composable
expect fun BlobInfoWrapper.asImageComposable(
    userId: String,
    projectId: String,
    modifier: Modifier = Modifier,
    alignment: Alignment = Alignment.Center,
    contentScale: ContentScale = ContentScale.None,
    alpha: Float = 1f,
)
