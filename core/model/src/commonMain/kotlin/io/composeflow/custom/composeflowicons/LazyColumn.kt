package io.composeflow.custom.composeflowicons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import io.composeflow.custom.ComposeFlowIcons

val ComposeFlowIcons.LazyColumn: ImageVector
    get() {
        if (_LazyColumn != null) {
            return _LazyColumn!!
        }
        _LazyColumn = ImageVector.Builder(
            name = "LazyColumn",
            defaultWidth = 500.dp,
            defaultHeight = 500.dp,
            viewportWidth = 500f,
            viewportHeight = 500f
        ).apply {
            path(
                stroke = SolidColor(Color(0xFF000000)),
                strokeLineWidth = 32f
            ) {
                moveTo(250.16f, 240.91f)
                horizontalLineToRelative(213.71f)
                verticalLineToRelative(174.29f)
                horizontalLineToRelative(-213.71f)
                close()
            }
            path(
                fill = SolidColor(Color(0xFFD8D8D8)),
                stroke = SolidColor(Color(0xFF000000)),
                strokeLineWidth = 32f
            ) {
                moveTo(98.03f, 45.95f)
                lineToRelative(-4.92f, 366.68f)
            }
            path(
                fill = SolidColor(Color(0xFFD8D8D8)),
                stroke = SolidColor(Color(0xFF000000)),
                strokeLineWidth = 32f
            ) {
                moveTo(19.67f, 332.3f)
                lineToRelative(72.86f, 81.11f)
            }
            path(
                fill = SolidColor(Color(0xFFD8D8D8)),
                stroke = SolidColor(Color(0xFF000000)),
                strokeLineWidth = 32f
            ) {
                moveTo(80.08f, 422.46f)
                lineToRelative(78.6f, -84.24f)
            }
            path(
                stroke = SolidColor(Color(0xFF000000)),
                strokeLineWidth = 32f
            ) {
                moveTo(250.32f, 66.96f)
                horizontalLineToRelative(213.71f)
                verticalLineToRelative(174.29f)
                horizontalLineToRelative(-213.71f)
                close()
            }
            path(
                fill = SolidColor(Color(0xFFD8D8D8)),
                stroke = SolidColor(Color(0xFF000000)),
                strokeLineWidth = 32f
            ) {
                moveTo(250.67f, 67.09f)
                lineToRelative(0.23f, -67.14f)
            }
            path(
                fill = SolidColor(Color(0xFFD8D8D8)),
                stroke = SolidColor(Color(0xFF000000)),
                strokeLineWidth = 32f
            ) {
                moveTo(250.02f, 504.29f)
                lineToRelative(0.23f, -75.55f)
            }
            path(
                fill = SolidColor(Color(0xFFD8D8D8)),
                stroke = SolidColor(Color(0xFF000000)),
                strokeLineWidth = 32f
            ) {
                moveTo(463.25f, 67.07f)
                lineToRelative(0.23f, -67.14f)
            }
            path(
                fill = SolidColor(Color(0xFFD8D8D8)),
                stroke = SolidColor(Color(0xFF000000)),
                strokeLineWidth = 32f
            ) {
                moveTo(463.31f, 500.23f)
                lineToRelative(0.23f, -70.57f)
            }
        }.build()

        return _LazyColumn!!
    }

@Suppress("ObjectPropertyName")
private var _LazyColumn: ImageVector? = null
