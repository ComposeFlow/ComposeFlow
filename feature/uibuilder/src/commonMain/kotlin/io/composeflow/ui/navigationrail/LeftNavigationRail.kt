package io.composeflow.ui.navigationrail

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.VectorGroup
import androidx.compose.ui.graphics.vector.VectorNode
import androidx.compose.ui.graphics.vector.VectorPath
import androidx.compose.ui.graphics.vector.group
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import io.composeflow.BuildConfig
import io.composeflow.NAVIGATION_RAIL_TEST_TAG
import io.composeflow.model.TopLevelDestination
import io.composeflow.ui.Tooltip
import moe.tlaster.precompose.navigation.NavOptions
import moe.tlaster.precompose.navigation.Navigator
import moe.tlaster.precompose.navigation.PopUpTo
import kotlin.math.absoluteValue

@Composable
fun LeftNavigationRail(navigator: Navigator) {
    val currentDestination =
        TopLevelDestination.entries.firstOrNull {
            it.route ==
                navigator.currentEntry
                    .collectAsState(null)
                    .value
                    ?.route
                    ?.route
        }

    var selectedItem by remember(currentDestination) {
        mutableStateOf(
            currentDestination?.ordinal ?: 0,
        )
    }
    NavigationRail(
        modifier =
            Modifier
                .width(40.dp)
                .fillMaxHeight(),
    ) {
        TopLevelDestination.entries.forEachIndexed { index, item ->
            @Suppress("KotlinConstantConditions")
            if (BuildConfig.isRelease) {
                if (item == TopLevelDestination.StringEditor) {
                    return@forEachIndexed
                }
            }
            val isSelected = selectedItem == item.ordinal
            Tooltip(item.label) {
                NavigationRailItem(
                    modifier =
                        Modifier
                            .size(40.dp)
                            .padding(5.dp)
                            .clip(MaterialTheme.shapes.extraSmall)
                            .testTag("$NAVIGATION_RAIL_TEST_TAG/${item.name}"),
                    selected = isSelected,
                    onClick = {
                        navigator.navigate(
                            item.route,
                            options =
                                NavOptions(
                                    popUpTo =
                                        PopUpTo(
                                            route = item.route,
                                        ),
                                ),
                        )
                        selectedItem = index
                    },
                    colors =
                        NavigationRailItemDefaults.colors(
                            indicatorColor = Color(0xFF366ACE),
                        ),
                    icon = {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center,
                        ) {
                            val icon = if (isSelected) item.icon.tint(Color.White) else item.icon
                            Icon(
                                imageVector = icon,
                                contentDescription = "icon",
                                modifier = Modifier.size(20.dp),
                            )
                        }
                    },
                )
            }
        }
    }
}

private fun ImageVector.tint(color: Color): ImageVector {
    val backgroundPalette =
        listOf(
            Color(0xFFFF43454A),
            Color(0xFFEBECF0),
            Color(0xFFE7EFFD),
            Color(0xFFDFF2E0),
            Color(0xFFF2FCF3),
            Color(0xFFFFE8E8),
            Color(0xFFFFF5F5),
            Color(0xFFFFF8E3),
            Color(0xFFFFF4EB),
            Color(0xFFEEE0FF),
        )

    val strokeColors =
        listOf(
            Color(0xFF000000),
            Color(0xFF818594),
            Color(0xFF6C707E),
            Color(0xFF3574F0),
            Color(0xFF5FB865),
            Color(0xFFE35252),
            Color(0xFFEB7171),
            Color(0xFFE3AE4D),
            Color(0xFFFCC75B),
            Color(0xFFF28C35),
            Color(0xFF548AF7),
            Color(0xFFCED0D6),
        )

    fun Color.approxEquals(
        other: Color,
        epsilon: Float = 0.004f,
    ): Boolean =
        (red - other.red).absoluteValue < epsilon &&
            (green - other.green).absoluteValue < epsilon &&
            (blue - other.blue).absoluteValue < epsilon &&
            (alpha - other.alpha).absoluteValue < epsilon

    fun ImageVector.Builder.copyNode(node: VectorNode) {
        when (node) {
            is VectorPath -> {
                val fillValue = (node.fill as? SolidColor)?.value
                val strokeValue = (node.stroke as? SolidColor)?.value

                val palette =
                    backgroundPalette.associateWith { Color.Transparent } + strokeColors.associateWith { color }

                val newFill =
                    if (fillValue != null) {
                        val key = palette.keys.firstOrNull { it.approxEquals(fillValue) }
                        val color = palette[key] ?: fillValue
                        SolidColor(color)
                    } else {
                        node.fill
                    }

                val newStroke =
                    if (strokeValue != null) {
                        val key =
                            palette.keys.firstOrNull {
                                it.approxEquals(strokeValue)
                            }
                        val color = palette[key] ?: Color.Green

                        SolidColor(color)
                    } else {
                        node.stroke
                    }

                addPath(
                    pathData = node.pathData,
                    pathFillType = node.pathFillType,
                    name = node.name,
                    fill = newFill,
                    fillAlpha = node.fillAlpha,
                    stroke = newStroke,
                    strokeAlpha = node.strokeAlpha,
                    strokeLineWidth = node.strokeLineWidth,
                    strokeLineCap = node.strokeLineCap,
                    strokeLineJoin = node.strokeLineJoin,
                    strokeLineMiter = node.strokeLineMiter,
                    trimPathStart = node.trimPathStart,
                    trimPathEnd = node.trimPathEnd,
                    trimPathOffset = node.trimPathOffset,
                )
            }

            is VectorGroup -> {
                group(
                    name = node.name,
                    rotate = node.rotation,
                    pivotX = node.pivotX,
                    pivotY = node.pivotY,
                    scaleX = node.scaleX,
                    scaleY = node.scaleY,
                    translationX = node.translationX,
                    translationY = node.translationY,
                    clipPathData = node.clipPathData,
                ) {
                    node.forEach { child -> copyNode(child) }
                }
            }
        }
    }

    return ImageVector
        .Builder(
            defaultWidth = this.defaultWidth,
            defaultHeight = this.defaultHeight,
            viewportWidth = this.viewportWidth,
            viewportHeight = this.viewportHeight,
        ).apply {
            this@tint.root.forEach { node ->
                copyNode(node)
            }
        }.build()
}
