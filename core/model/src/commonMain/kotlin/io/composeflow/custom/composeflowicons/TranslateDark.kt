package io.composeflow.custom.composeflowicons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import io.composeflow.custom.ComposeFlowIcons

val ComposeFlowIcons.TranslateDark: ImageVector
    get() {
        if (_TranslateDark != null) {
            return _TranslateDark!!
        }
        _TranslateDark =
            ImageVector
                .Builder(
                    name = "TranslateDark",
                    defaultWidth = 20.dp,
                    defaultHeight = 20.dp,
                    viewportWidth = 960f,
                    viewportHeight = 960f,
                ).apply {
                    path(fill = SolidColor(Color(0xFFCED0D6))) {
                        moveToRelative(488f, 864f)
                        lineToRelative(171f, -456f)
                        horizontalLineToRelative(82f)
                        lineTo(912f, 864f)
                        horizontalLineToRelative(-79f)
                        lineToRelative(-41f, -117f)
                        lineTo(608f, 747f)
                        lineTo(567f, 864f)
                        horizontalLineToRelative(-79f)
                        close()
                        moveTo(169f, 744f)
                        lineToRelative(-50f, -51f)
                        lineToRelative(192f, -190f)
                        quadToRelative(-36f, -38f, -67f, -79f)
                        reflectiveQuadToRelative(-54f, -89f)
                        horizontalLineToRelative(82f)
                        quadToRelative(18f, 32f, 36f, 54.5f)
                        reflectiveQuadToRelative(52f, 60.5f)
                        quadToRelative(38f, -42f, 70f, -87.5f)
                        reflectiveQuadToRelative(52f, -98.5f)
                        lineTo(48f, 264f)
                        verticalLineToRelative(-72f)
                        horizontalLineToRelative(276f)
                        verticalLineToRelative(-96f)
                        horizontalLineToRelative(72f)
                        verticalLineToRelative(96f)
                        horizontalLineToRelative(276f)
                        verticalLineToRelative(72f)
                        lineTo(558f, 264f)
                        quadToRelative(-21f, 69f, -61f, 127.5f)
                        reflectiveQuadTo(409f, 503f)
                        lineToRelative(91f, 90f)
                        lineToRelative(-28f, 74f)
                        lineToRelative(-112f, -112f)
                        lineToRelative(-191f, 189f)
                        close()
                        moveTo(632f, 681f)
                        horizontalLineToRelative(136f)
                        lineToRelative(-66f, -189f)
                        lineToRelative(-70f, 189f)
                        close()
                    }
                }.build()

        return _TranslateDark!!
    }

@Suppress("ObjectPropertyName")
private var _TranslateDark: ImageVector? = null
