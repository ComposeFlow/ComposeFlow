package io.composeflow.platform

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.loadImageBitmap
import androidx.compose.ui.unit.Density
import org.xml.sax.InputSource
import java.io.File

// Loading from file with java.io API

fun loadImageBitmap(file: File): ImageBitmap = file.inputStream().buffered().use(::loadImageBitmap)

fun loadXmlImageVector(
    file: File,
    density: Density,
): ImageVector =
    file.inputStream().buffered().use {
        androidx.compose.ui.res.loadXmlImageVector(
            InputSource(it),
            density,
        )
    }
