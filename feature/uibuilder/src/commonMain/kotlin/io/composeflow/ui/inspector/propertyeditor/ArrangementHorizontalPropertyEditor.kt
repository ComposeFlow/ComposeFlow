package io.composeflow.ui.inspector.propertyeditor

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.AlignHorizontalLeft
import androidx.compose.material.icons.automirrored.outlined.AlignHorizontalRight
import androidx.compose.material.icons.outlined.AlignHorizontalCenter
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import io.composeflow.HorizontalAlignJustifySpaceAround
import io.composeflow.HorizontalAlignJustifySpaceBetween
import io.composeflow.HorizontalAlignJustifySpaceEven
import io.composeflow.Res
import io.composeflow.arrangement_horizontal_center
import io.composeflow.arrangement_horizontal_end
import io.composeflow.arrangement_horizontal_start
import io.composeflow.custom.ComposeFlowIcons
import io.composeflow.horizontal_arrangement
import io.composeflow.horizontal_space_around_arrangement
import io.composeflow.horizontal_space_between_arrangement
import io.composeflow.horizontal_space_evenly_arrangement
import io.composeflow.model.parameter.wrapper.ArrangementHorizontalWrapper
import io.composeflow.ui.Tooltip
import io.composeflow.ui.icon.ComposeFlowIcon
import io.composeflow.ui.icon.ComposeFlowIconToggleButton
import io.composeflow.ui.inspector.ParamInspectorHeaderRow
import io.composeflow.ui.modifier.hoverOverlay
import org.jetbrains.compose.resources.stringResource

@Composable
fun ArrangementHorizontalPropertyEditor(
    initialValue: ArrangementHorizontalWrapper?,
    onArrangementSelected: (ArrangementHorizontalWrapper) -> Unit,
) {
    Column(modifier = Modifier.hoverOverlay().padding(vertical = 4.dp)) {
        ParamInspectorHeaderRow(
            label = stringResource(Res.string.horizontal_arrangement),
            modifier = Modifier.padding(vertical = 4.dp),
        )

        @Composable
        fun runIconToggleButton(
            horizontalArrangement: ArrangementHorizontalWrapper,
            imageVector: ImageVector? = null,
            contentDesc: String,
        ) = run {
            val thisItemSelected = initialValue == horizontalArrangement
            Tooltip(contentDesc) {
                ComposeFlowIconToggleButton(
                    checked = thisItemSelected,
                    onCheckedChange = {
                        onArrangementSelected(horizontalArrangement)
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
                    imageVector?.let {
                        ComposeFlowIcon(
                            imageVector = imageVector,
                            contentDescription = contentDesc,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp),
                        )
                    }
                }
            }
        }
        FlowRow {
            runIconToggleButton(
                ArrangementHorizontalWrapper.Start,
                imageVector = Icons.AutoMirrored.Outlined.AlignHorizontalLeft,
                contentDesc = stringResource(Res.string.arrangement_horizontal_start),
            )
            runIconToggleButton(
                ArrangementHorizontalWrapper.Center,
                imageVector = Icons.Outlined.AlignHorizontalCenter,
                contentDesc = stringResource(Res.string.arrangement_horizontal_center),
            )
            runIconToggleButton(
                ArrangementHorizontalWrapper.End,
                imageVector = Icons.AutoMirrored.Outlined.AlignHorizontalRight,
                contentDesc = stringResource(Res.string.arrangement_horizontal_end),
            )

            runIconToggleButton(
                ArrangementHorizontalWrapper.SpaceBetween,
                imageVector = ComposeFlowIcons.HorizontalAlignJustifySpaceBetween,
                contentDesc = stringResource(Res.string.horizontal_space_between_arrangement),
            )
            runIconToggleButton(
                ArrangementHorizontalWrapper.SpaceEvenly,
                imageVector = ComposeFlowIcons.HorizontalAlignJustifySpaceEven,
                contentDesc = stringResource(Res.string.horizontal_space_evenly_arrangement),
            )
            runIconToggleButton(
                ArrangementHorizontalWrapper.SpaceAround,
                imageVector = ComposeFlowIcons.HorizontalAlignJustifySpaceAround,
                contentDesc = stringResource(Res.string.horizontal_space_around_arrangement),
            )
        }
    }
}
