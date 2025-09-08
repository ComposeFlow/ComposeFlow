package io.composeflow.platform

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.loadImageBitmap
import androidx.compose.ui.unit.Density
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.xml.sax.InputSource
import java.io.File
import java.io.IOException
import java.net.URL

@Composable
actual fun <T> AsyncImage(
    load: suspend () -> T,
    painterFor: @Composable (T) -> Painter,
    contentDescription: String,
    colorFilter: ColorFilter?,
    modifier: Modifier,
    contentScale: ContentScale,
) {
    val image: T? by produceState<T?>(null) {
        value =
            withContext(Dispatchers.IO) {
                try {
                    load()
                } catch (e: IOException) {
                    e.printStackTrace()
                    null
                }
            }
    }

    image?.let {
        Image(
            painter = painterFor(it),
            contentDescription = contentDescription,
            contentScale = contentScale,
            colorFilter = colorFilter,
            modifier = modifier,
        )
    }
}
