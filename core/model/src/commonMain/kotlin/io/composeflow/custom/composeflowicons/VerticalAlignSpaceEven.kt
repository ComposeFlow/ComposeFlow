package io.composeflow

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import io.composeflow.custom.ComposeFlowIcons

val ComposeFlowIcons.VerticalAlignSpaceEven: ImageVector
    get() {
        if (_VerticalAlignSpaceEven != null) {
            return _VerticalAlignSpaceEven!!
        }
        _VerticalAlignSpaceEven = ImageVector.Builder(
            name = "VerticalAlignSpaceEven",
            defaultWidth = 48.dp,
            defaultHeight = 48.dp,
            viewportWidth = 960f,
            viewportHeight = 960f
        ).apply {
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(320f, 670f)
                quadToRelative(-12.75f, 0f, -21.38f, -8.63f)
                reflectiveQuadTo(290f, 640f)
                verticalLineToRelative(-40f)
                quadToRelative(0f, -12.75f, 8.63f, -21.38f)
                reflectiveQuadTo(320f, 570f)
                horizontalLineToRelative(320f)
                quadToRelative(12.75f, 0f, 21.38f, 8.63f)
                reflectiveQuadTo(670f, 600f)
                verticalLineToRelative(40f)
                quadToRelative(0f, 12.75f, -8.63f, 21.38f)
                reflectiveQuadTo(640f, 670f)
                lineTo(320f, 670f)
                close()
                moveTo(320f, 390f)
                quadToRelative(-12.75f, 0f, -21.38f, -8.63f)
                reflectiveQuadTo(290f, 360f)
                verticalLineToRelative(-40f)
                quadToRelative(0f, -12.75f, 8.63f, -21.38f)
                reflectiveQuadTo(320f, 290f)
                horizontalLineToRelative(320f)
                quadToRelative(12.75f, 0f, 21.38f, 8.63f)
                reflectiveQuadTo(670f, 320f)
                verticalLineToRelative(40f)
                quadToRelative(0f, 12.75f, -8.63f, 21.38f)
                reflectiveQuadTo(640f, 390f)
                lineTo(320f, 390f)
                close()
                moveTo(110f, 880f)
                quadToRelative(-12.75f, 0f, -21.38f, -8.68f)
                quadTo(80f, 862.65f, 80f, 849.83f)
                quadTo(80f, 837f, 88.63f, 828.5f)
                reflectiveQuadTo(110f, 820f)
                horizontalLineToRelative(740f)
                quadToRelative(12.75f, 0f, 21.38f, 8.68f)
                quadToRelative(8.63f, 8.68f, 8.63f, 21.5f)
                quadToRelative(0f, 12.82f, -8.63f, 21.33f)
                reflectiveQuadTo(850f, 880f)
                lineTo(110f, 880f)
                close()
                moveTo(110f, 140f)
                quadToRelative(-12.75f, 0f, -21.38f, -8.68f)
                quadTo(80f, 122.65f, 80f, 109.82f)
                quadTo(80f, 97f, 88.63f, 88.5f)
                reflectiveQuadTo(110f, 80f)
                horizontalLineToRelative(740f)
                quadToRelative(12.75f, 0f, 21.38f, 8.68f)
                quadToRelative(8.63f, 8.68f, 8.63f, 21.5f)
                quadToRelative(0f, 12.82f, -8.63f, 21.33f)
                reflectiveQuadTo(850f, 140f)
                lineTo(110f, 140f)
                close()
            }
        }.build()

        return _VerticalAlignSpaceEven!!
    }

@Suppress("ObjectPropertyName")
private var _VerticalAlignSpaceEven: ImageVector? = null
