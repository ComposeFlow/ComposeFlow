package io.composeflow.ui.inspector.propertyeditor

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.VerticalAlignBottom
import androidx.compose.material.icons.outlined.VerticalAlignCenter
import androidx.compose.material.icons.outlined.VerticalAlignTop
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import io.composeflow.Res
import io.composeflow.VerticalAlignSpaceAround
import io.composeflow.VerticalAlignSpaceBetween
import io.composeflow.VerticalAlignSpaceEven
import io.composeflow.custom.ComposeFlowIcons
import io.composeflow.model.parameter.wrapper.ArrangementVerticalWrapper
import io.composeflow.ui.Tooltip
import io.composeflow.ui.icon.ComposeFlowIcon
import io.composeflow.ui.icon.ComposeFlowIconToggleButton
import io.composeflow.ui.inspector.ParamInspectorHeaderRow
import io.composeflow.ui.modifier.hoverOverlay
import io.composeflow.vertical_arrangement
import io.composeflow.vertical_bottom_arrangement
import io.composeflow.vertical_center_arrangement
import io.composeflow.vertical_space_around_arrangement
import io.composeflow.vertical_space_between_arrangement
import io.composeflow.vertical_space_evenly_arrangement
import io.composeflow.vertical_top_arrangement
import org.jetbrains.compose.resources.stringResource

@Composable
fun ArrangementVerticalPropertyEditor(
    initialValue: ArrangementVerticalWrapper?,
    onArrangementSelected: (ArrangementVerticalWrapper) -> Unit,
) {
    Column(modifier = Modifier.hoverOverlay().padding(vertical = 4.dp)) {
        ParamInspectorHeaderRow(
            label = stringResource(Res.string.vertical_arrangement),
            modifier = Modifier.padding(vertical = 4.dp),
        )

        @Composable
        fun runIconToggleButton(
            verticalArrangement: ArrangementVerticalWrapper,
            imageVector: ImageVector,
            contentDesc: String,
        ) = run {
            val thisItemSelected = initialValue == verticalArrangement
            Tooltip(contentDesc) {
                ComposeFlowIconToggleButton(
                    checked = thisItemSelected,
                    onCheckedChange = {
                        onArrangementSelected(verticalArrangement)
                    },
                    modifier =
                        Modifier.then(
                            if (thisItemSelected) {
                                Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        MaterialTheme.colorScheme.onPrimaryContainer.copy(
                                            alpha = 0.3f,
                                        ),
                                    )
                            } else {
                                Modifier
                            },
                        ),
                ) {
                    ComposeFlowIcon(
                        imageVector = imageVector,
                        contentDescription = contentDesc,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp),
                    )
                }
            }
        }
        FlowRow {
            runIconToggleButton(
                ArrangementVerticalWrapper.Top,
                imageVector = Icons.Outlined.VerticalAlignTop,
                contentDesc = stringResource(Res.string.vertical_top_arrangement),
            )
            runIconToggleButton(
                ArrangementVerticalWrapper.Center,
                imageVector = Icons.Outlined.VerticalAlignCenter,
                contentDesc = stringResource(Res.string.vertical_center_arrangement),
            )
            runIconToggleButton(
                ArrangementVerticalWrapper.Bottom,
                imageVector = Icons.Outlined.VerticalAlignBottom,
                contentDesc = stringResource(Res.string.vertical_bottom_arrangement),
            )

            runIconToggleButton(
                ArrangementVerticalWrapper.SpaceBetween,
                imageVector = ComposeFlowIcons.VerticalAlignSpaceBetween,
                contentDesc = stringResource(Res.string.vertical_space_between_arrangement),
            )
            runIconToggleButton(
                ArrangementVerticalWrapper.SpaceEvenly,
                imageVector = ComposeFlowIcons.VerticalAlignSpaceEven,
                contentDesc = stringResource(Res.string.vertical_space_evenly_arrangement),
            )
            runIconToggleButton(
                ArrangementVerticalWrapper.SpaceAround,
                imageVector = ComposeFlowIcons.VerticalAlignSpaceAround,
                contentDesc = stringResource(Res.string.vertical_space_around_arrangement),
            )
        }
    }
}
