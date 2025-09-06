package io.composeflow.ui.window.titlebar

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import io.composeflow.ui.window.SystemButtonType
import io.composeflow.ui.window.frame.isWindowFocused
import io.composeflow.ui.window.frame.isWindowMaximized
import io.composeflow.ui.window.windowButton

@Composable
private fun SystemButton(
    onClick: () -> Unit,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    background: Color = Color.Transparent,
    onBackground: Color = MaterialTheme.colorScheme.onBackground,
    hoveredBackgroundColor: Color = onBackground.copy(alpha = 0.1f),
    onHoveredBackgroundColor: Color = MaterialTheme.colorScheme.onBackground,
) {
    val isFocused = isWindowFocused()
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()
    Icon(
        imageVector = icon,
        contentDescription = null,
        tint =
            animateColorAsState(
                when {
                    isHovered -> onHoveredBackgroundColor
                    else -> onBackground
                }.copy(
                    alpha =
                        if (isFocused || isHovered) {
                            1f
                        } else {
                            0.5f
                        },
                ),
            ).value,
        modifier =
            modifier
                .clickable { onClick() }
                .background(
                    animateColorAsState(
                        when {
                            isHovered -> hoveredBackgroundColor
                            else -> background
                        },
                    ).value,
                ).hoverable(interactionSource)
                .windowButton(),
    )
}

@Composable
internal fun CommonSystemButtons(
    onRequestClose: () -> Unit,
    onRequestMinimize: (() -> Unit)?,
    onToggleMaximize: (() -> Unit)?,
    buttons: List<SystemButtonType>,
) {
    Row(
        modifier = Modifier.fillMaxHeight().wrapContentHeight(Alignment.Top),
        verticalAlignment = Alignment.Top,
    ) {
        buttons.forEach {
            when (it) {
                SystemButtonType.Close -> {
                    SystemButton(
                        onClick = onRequestClose,
                        background = Color.Transparent,
                        hoveredBackgroundColor = Color(0xFFc42b1c),
                        icon = Icons.Default.Close,
                    )
                }

                SystemButtonType.Minimize -> {
                    onRequestMinimize?.let {
                        SystemButton(
                            icon = IconMinimize,
                            onClick = onRequestMinimize,
                        )
                    }
                }

                SystemButtonType.Maximize -> {
                    onToggleMaximize?.let {
                        SystemButton(
                            icon =
                                if (isWindowMaximized()) {
                                    IconRestore
                                } else {
                                    IconMaximize
                                },
                            onClick = onToggleMaximize,
                        )
                    }
                }
            }
        }
    }
}

private val IconMaximize: ImageVector
    get() {
        if (_IconMaximize != null) {
            return _IconMaximize!!
        }
        _IconMaximize =
            ImageVector
                .Builder(
                    name = "IconMaximize",
                    defaultWidth = 16.dp,
                    defaultHeight = 16.dp,
                    viewportWidth = 16f,
                    viewportHeight = 16f,
                ).apply {
                    path(
                        stroke = SolidColor(Color(0xFFCED0D6)),
                        strokeLineWidth = 1f,
                    ) {
                        moveTo(3.5f, 3.5f)
                        horizontalLineToRelative(9f)
                        verticalLineToRelative(9f)
                        horizontalLineToRelative(-9f)
                        close()
                    }
                }.build()

        return _IconMaximize!!
    }

@Suppress("ObjectPropertyName")
private var _IconMaximize: ImageVector? = null

private val IconRestore: ImageVector
    get() {
        if (_IconRestore != null) {
            return _IconRestore!!
        }
        _IconRestore =
            ImageVector
                .Builder(
                    name = "IconRestore",
                    defaultWidth = 16.dp,
                    defaultHeight = 16.dp,
                    viewportWidth = 16f,
                    viewportHeight = 16f,
                ).apply {
                    path(
                        fill = SolidColor(Color(0xFFCED0D6)),
                        pathFillType = PathFillType.EvenOdd,
                    ) {
                        moveTo(5f, 3f)
                        horizontalLineTo(13f)
                        verticalLineTo(11f)
                        horizontalLineTo(10f)
                        verticalLineTo(10f)
                        horizontalLineTo(12f)
                        verticalLineTo(4f)
                        horizontalLineTo(6f)
                        verticalLineTo(6f)
                        horizontalLineTo(5f)
                        verticalLineTo(3f)
                        close()
                    }
                    path(
                        fill = SolidColor(Color(0xFFCED0D6)),
                        pathFillType = PathFillType.EvenOdd,
                    ) {
                        moveTo(11f, 5f)
                        horizontalLineTo(3f)
                        verticalLineTo(13f)
                        horizontalLineTo(11f)
                        verticalLineTo(5f)
                        close()
                        moveTo(10f, 6f)
                        horizontalLineTo(4f)
                        verticalLineTo(12f)
                        horizontalLineTo(10f)
                        verticalLineTo(6f)
                        close()
                    }
                }.build()

        return _IconRestore!!
    }

@Suppress("ObjectPropertyName")
private var _IconRestore: ImageVector? = null

private val IconMinimize: ImageVector
    get() {
        if (_IconMinimize != null) {
            return _IconMinimize!!
        }
        _IconMinimize =
            ImageVector
                .Builder(
                    name = "IconMinimize",
                    defaultWidth = 16.dp,
                    defaultHeight = 16.dp,
                    viewportWidth = 16f,
                    viewportHeight = 16f,
                ).apply {
                    path(fill = SolidColor(Color(0xFFCED0D6))) {
                        moveTo(3f, 8f)
                        horizontalLineToRelative(10f)
                        verticalLineToRelative(1f)
                        horizontalLineToRelative(-10f)
                        close()
                    }
                }.build()

        return _IconMinimize!!
    }

@Suppress("ObjectPropertyName")
private var _IconMinimize: ImageVector? = null
