package io.composeflow.ui.window.titlebar.mac

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.onClick
import androidx.compose.foundation.shape.CircleShape
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

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun SystemButton(
    onClick: () -> Unit,
    icon: ImageVector,
    isUserInThisArea: Boolean,
    hoveredBackgroundColor: Color,
    modifier: Modifier = Modifier,
    unfocusedBackgroundColor: Color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.2f),
) {
    val isWindowFocused = isWindowFocused()
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()

    Box(
        modifier =
            modifier
                .hoverable(interactionSource)
                .onClick { onClick() }
                .fillMaxHeight()
                .wrapContentHeight()
                .padding(horizontal = 6.dp)
                .background(
                    animateColorAsState(
                        when {
                            !isWindowFocused -> unfocusedBackgroundColor
                            isHovered -> hoveredBackgroundColor.copy(alpha = 0.1f)
                            else -> hoveredBackgroundColor
                        },
                    ).value,
                    CircleShape,
                ).requiredSize(12.dp),
    ) {
        if (isUserInThisArea && isWindowFocused) {
            Icon(
                imageVector = icon,
                tint = Color.Black,
                modifier = Modifier.align(Alignment.Center).size(10.dp),
                contentDescription = null,
            )
        }
    }
}

@Composable
internal fun MacOSSystemButtons(
    onRequestClose: () -> Unit,
    onRequestMinimize: (() -> Unit)?,
    onToggleMaximize: (() -> Unit)?,
    buttons: List<SystemButtonType>,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()

    Row(
        modifier =
            Modifier
                .padding(horizontal = 6.dp)
                .hoverable(interactionSource)
                .fillMaxHeight()
                .wrapContentHeight(Alignment.Top),
        verticalAlignment = Alignment.Top,
    ) {
        buttons.forEach {
            when (it) {
                SystemButtonType.Close -> {
                    SystemButton(
                        onClick = onRequestClose,
                        hoveredBackgroundColor = Color(0xFFFF5F57),
                        icon = Icons.Default.Close,
                        isUserInThisArea = isHovered,
                    )
                }

                SystemButtonType.Minimize -> {
                    onRequestMinimize?.let {
                        SystemButton(
                            icon = IconMinimize,
                            onClick = onRequestMinimize,
                            isUserInThisArea = isHovered,
                            hoveredBackgroundColor = Color(0xFFFFBD2E),
                        )
                    }
                }

                SystemButtonType.Maximize -> {
                    onToggleMaximize?.let {
                        SystemButton(
                            icon = IconMaximize,
                            onClick = onToggleMaximize,
                            isUserInThisArea = isHovered,
                            hoveredBackgroundColor = Color(0xFF28C840),
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
                    defaultWidth = 256.dp,
                    defaultHeight = 256.dp,
                    viewportWidth = 256f,
                    viewportHeight = 256f,
                ).apply {
                    path(
                        fill = SolidColor(Color(0xFF74DA74)),
                        fillAlpha = 0.12f,
                        strokeAlpha = 0.12f,
                        pathFillType = PathFillType.EvenOdd,
                    ) {
                        moveToRelative(20.5f, 100.5f)
                        curveToRelative(-1.41f, 9.61f, -2.41f, 19.27f, -3f, 29f)
                        curveToRelative(1.02f, 11.43f, 2.68f, 22.76f, 5f, 34f)
                        curveToRelative(-6.01f, -17.48f, -7.51f, -35.48f, -4.5f, -54f)
                        curveToRelative(0.41f, -3.24f, 1.24f, -6.24f, 2.5f, -9f)
                        close()
                    }
                    path(
                        fill = SolidColor(Color(0xFF038601)),
                        pathFillType = PathFillType.EvenOdd,
                    ) {
                        moveToRelative(75.5f, 102.5f)
                        curveToRelative(20.69f, 21.69f, 42.02f, 42.69f, 64f, 63f)
                        curveToRelative(5.33f, 5.67f, 10.67f, 11.33f, 16f, 17f)
                        curveToRelative(-22.5f, 1.16f, -45.16f, 1.33f, -68f, 0.5f)
                        curveToRelative(-1.4f, -1.27f, -3.07f, -1.77f, -5f, -1.5f)
                        curveToRelative(-4.09f, -3.76f, -6.59f, -8.43f, -7.5f, -14f)
                        curveToRelative(-0.33f, 0.33f, -0.67f, 0.67f, -1f, 1f)
                        curveToRelative(-0.17f, -11f, -0.33f, -22f, -0.5f, -33f)
                        curveToRelative(0f, -11.35f, 0.67f, -22.35f, 2f, -33f)
                        close()
                    }
                    path(
                        fill = SolidColor(Color(0xFF048702)),
                        pathFillType = PathFillType.EvenOdd,
                    ) {
                        moveToRelative(170.5f, 73.5f)
                        curveToRelative(4.88f, 1.87f, 8.54f, 5.21f, 11f, 10f)
                        curveToRelative(0.5f, 1.98f, 1f, 3.98f, 1.5f, 6f)
                        curveToRelative(0.83f, 22.01f, 0.67f, 44.01f, -0.5f, 66f)
                        curveToRelative(-1.35f, -1.02f, -2.69f, -2.02f, -4f, -3f)
                        curveToRelative(-2.83f, -3f, -5.5f, -6.17f, -8f, -9.5f)
                        curveToRelative(-22.5f, -20.67f, -44.5f, -41.84f, -66f, -63.5f)
                        curveToRelative(-0.33f, -0.67f, -0.67f, -1.33f, -1f, -2f)
                        curveToRelative(-0.79f, -1.29f, -1.79f, -2.46f, -3f, -3.5f)
                        curveToRelative(23.33f, -0.5f, 46.66f, -0.67f, 70f, -0.5f)
                        close()
                    }
                }.build()

        return _IconMaximize!!
    }

@Suppress("ObjectPropertyName")
private var _IconMaximize: ImageVector? = null

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
