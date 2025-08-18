package io.composeflow.ui.utils

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import io.composeflow.cloud.storage.BlobInfoWrapper
import io.composeflow.platform.getAssetCacheFileFor
import io.composeflow.platform.loadImageBitmap
import io.composeflow.platform.loadXmlImageVector

@Composable
actual fun BlobInfoWrapper.asIconComposable(
    userId: String,
    projectId: String,
    tint: Color,
    modifier: Modifier,
) {
    val localCache =
        getAssetCacheFileFor(
            userId = userId,
            projectId = projectId,
            this,
        )
    val density = LocalDensity.current
    Column(modifier = modifier) {
        if (localCache.exists()) {
            if (fileName.endsWith(".xml")) {
                val imageVector = loadXmlImageVector(localCache.toFile(), density)
                Icon(
                    imageVector = imageVector,
                    contentDescription = "",
                    tint = tint,
                    modifier = Modifier.heightIn(max = 72.dp),
                )
            } else {
                val bitmap = loadImageBitmap(localCache.toFile())
                Icon(
                    bitmap = bitmap,
                    contentDescription = "",
                    tint = tint,
                    modifier = Modifier.heightIn(max = 72.dp),
                )
            }
        }
    }
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
    val localCache =
        getAssetCacheFileFor(
            userId = userId,
            projectId = projectId,
            this,
        )
    Column(modifier = modifier) {
        if (localCache.exists()) {
            val bitmap = loadImageBitmap(localCache.toFile())
            Image(
                bitmap = bitmap,
                contentDescription = "",
            )
        }
    }
}
