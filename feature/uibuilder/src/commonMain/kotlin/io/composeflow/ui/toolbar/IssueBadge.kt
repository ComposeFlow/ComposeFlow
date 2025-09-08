package io.composeflow.ui.toolbar

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import io.composeflow.ui.dropdown.PlatformCursorDropdownMenu
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Note
import androidx.compose.material.icons.outlined.BugReport
import androidx.compose.material.icons.outlined.Done
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.composeflow.Res
import io.composeflow.custom.ComposeFlowIcons
import io.composeflow.custom.composeflowicons.HttpRequestsFiletype
import io.composeflow.model.TopLevelDestination
import io.composeflow.model.project.Project
import io.composeflow.model.project.appscreen.screen.Screen
import io.composeflow.model.project.findCanvasEditableOrNull
import io.composeflow.model.project.findComposeNodeOrNull
import io.composeflow.model.project.issue.DestinationContext
import io.composeflow.model.project.issue.NavigatableDestination
import io.composeflow.model.project.issue.TrackableIssue
import io.composeflow.no_issues_found_message
import io.composeflow.ui.common.onSurfaceLight
import io.composeflow.ui.icon.ComposeFlowIcon
import io.composeflow.ui.modifier.hoverIconClickable
import kotlinx.coroutines.launch
import moe.tlaster.precompose.navigation.Navigator
import org.jetbrains.compose.resources.stringResource

private val ValidContainerColor = Color(0xFF5B8657)
private val ErrorContainerColor = Color(0xFFA84E4B)
private val ContentColor = Color(0xFFDFE1E5)
private val ContainerColor = Color(0xFFE8E2D2)

@Composable
fun IssuesBadge(
    project: Project,
    issues: List<TrackableIssue>,
    navigator: Navigator,
    onSetPendingFocus: (NavigatableDestination, DestinationContext) -> Unit,
) {
    var openIssuesPanel by remember { mutableStateOf(false) }
    Card(
        colors =
            CardDefaults.cardColors().copy(
                containerColor = if (issues.isEmpty()) ValidContainerColor else ErrorContainerColor,
                contentColor = ContentColor,
            ),
        modifier =
            Modifier
                .hoverIconClickable()
                .clickable {
                    openIssuesPanel = true
                },
    ) {
        if (issues.isEmpty()) {
            ComposeFlowIcon(
                imageVector = Icons.Outlined.Done,
                contentDescription = "",
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
            )
        } else {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
            ) {
                Text(
                    text = issues.size.toString(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = ContentColor,
                    modifier = Modifier.padding(bottom = 2.dp),
                )
                Spacer(Modifier.size(4.dp))
                ComposeFlowIcon(
                    imageVector = Icons.Outlined.BugReport,
                    contentDescription = "Issues",
                )
            }
        }
    }

    if (openIssuesPanel) {
        IssuesPanel(
            project = project,
            issues = issues,
            navigator = navigator,
            onClosePanel = {
                openIssuesPanel = false
            },
            onSetPendingFocus = onSetPendingFocus,
        )
    }
}

@Composable
private fun IssuesPanel(
    project: Project,
    issues: List<TrackableIssue>,
    navigator: Navigator,
    onClosePanel: () -> Unit,
    onSetPendingFocus: (NavigatableDestination, DestinationContext) -> Unit,
    modifier: Modifier = Modifier,
) {
    val coroutineScope = rememberCoroutineScope()
    PlatformCursorDropdownMenu(
        expanded = true,
        onDismissRequest = {
            onClosePanel()
        },
        modifier =
            modifier
                .width(420.dp)
                .background(color = ContainerColor),
    ) {
        if (issues.isEmpty()) {
            DropdownMenuItem(
                text = {
                    Row {
                        ComposeFlowIcon(
                            imageVector = Icons.Outlined.Done,
                            contentDescription = "",
                            tint = ValidContainerColor,
                            modifier = Modifier.size(28.dp),
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            stringResource(Res.string.no_issues_found_message) + "  \uD83D\uDE80",
                            style = MaterialTheme.typography.bodyMedium,
                            color = onSurfaceLight,
                        )
                    }
                },
                onClick = onClosePanel,
            )
        } else {
            issues
                .sortedBy {
                    it.issue.destination
                        .destinationToNavigate()
                        .ordinal
                }.groupBy { it.issue.destination }
                .forEach { destinationEntry ->
                    when (destinationEntry.key.destinationToNavigate()) {
                        TopLevelDestination.UiBuilder -> {
                            destinationEntry.value
                                .groupBy { (it.destinationContext as DestinationContext.UiBuilderScreen).canvasEditableId }
                                .entries
                                .toList()
                                .forEach {
                                    val canvasEditable = project.findCanvasEditableOrNull(it.key)

                                    canvasEditable?.let {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier =
                                                Modifier
                                                    .fillMaxWidth()
                                                    .height(36.dp)
                                                    .padding(start = 8.dp),
                                        ) {
                                            ComposeFlowIcon(
                                                imageVector =
                                                    if (canvasEditable is Screen) {
                                                        Icons.AutoMirrored.Outlined.Note
                                                    } else {
                                                        ComposeFlowIcons.HttpRequestsFiletype
                                                    },
                                                contentDescription = "",
                                            )
                                            Spacer(Modifier.width(8.dp))
                                            Text(
                                                text = canvasEditable.name,
                                                style = MaterialTheme.typography.titleSmall,
                                                color = MaterialTheme.colorScheme.onSurface,
                                            )
                                        }
                                    }

                                    it.value.forEach { trackableIssue ->
                                        val destinationWithContext =
                                            trackableIssue.destinationContext as DestinationContext.UiBuilderScreen

                                        val composeNode =
                                            project.findComposeNodeOrNull(destinationWithContext.composeNodeId)

                                        composeNode?.let {
                                            DropdownMenuItem(text = {
                                                Column(modifier = Modifier.padding(start = 16.dp)) {
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        Icon(
                                                            imageVector = composeNode.trait.value.icon(),
                                                            contentDescription = null,
                                                            modifier = Modifier.size(20.dp),
                                                        )
                                                        Text(
                                                            text = composeNode.label.value,
                                                            style = MaterialTheme.typography.titleSmall,
                                                            color = MaterialTheme.colorScheme.onSurface,
                                                            modifier = Modifier.padding(start = 8.dp),
                                                        )
                                                        Spacer(Modifier.width(8.dp))

                                                        trackableIssue.issue
                                                            .issueContextLabel()
                                                            ?.let { label ->
                                                                Text(
                                                                    text = label,
                                                                    style = MaterialTheme.typography.bodyMedium,
                                                                    color = MaterialTheme.colorScheme.secondary,
                                                                )
                                                                Spacer(Modifier.width(8.dp))
                                                            }
                                                        Text(
                                                            text =
                                                                trackableIssue.issue.errorMessage(
                                                                    project,
                                                                ),
                                                            style = MaterialTheme.typography.titleSmall,
                                                            color = MaterialTheme.colorScheme.error,
                                                            maxLines = 1,
                                                            overflow = TextOverflow.Ellipsis,
                                                            modifier = Modifier.weight(1f),
                                                        )
                                                    }
                                                }
                                            }, onClick = {
                                                onClosePanel()
                                                coroutineScope.launch {
                                                    trackableIssue.onNavigateToTopLevelDestination(
                                                        navigator,
                                                    )
                                                }
                                                onSetPendingFocus(
                                                    destinationEntry.key,
                                                    destinationWithContext,
                                                )
                                            })
                                        }
                                    }
                                }
                        }

                        TopLevelDestination.ApiEditor -> {
                            destinationEntry.value.forEach { trackableIssue ->

                                val api =
                                    project.apiHolder.findApiDefinitionOrNull(
                                        (trackableIssue.destinationContext as DestinationContext.ApiEditorScreen).apiId,
                                    )

                                api?.let {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier =
                                            Modifier
                                                .fillMaxWidth()
                                                .height(36.dp)
                                                .padding(start = 8.dp),
                                    ) {
                                        ComposeFlowIcon(
                                            imageVector = ComposeFlowIcons.HttpRequestsFiletype,
                                            contentDescription = "",
                                        )
                                        Spacer(Modifier.width(8.dp))
                                        Text(
                                            text = api.name,
                                            style = MaterialTheme.typography.titleSmall,
                                            color = MaterialTheme.colorScheme.onSurface,
                                        )
                                    }

                                    val destinationWithContext =
                                        trackableIssue.destinationContext as DestinationContext.ApiEditorScreen
                                    DropdownMenuItem(text = {
                                        Column(modifier = Modifier.padding(start = 16.dp)) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    imageVector = ComposeFlowIcons.HttpRequestsFiletype,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(20.dp),
                                                )
                                                Text(
                                                    text = api.name,
                                                    style = MaterialTheme.typography.titleSmall,
                                                    color = MaterialTheme.colorScheme.onSurface,
                                                    modifier = Modifier.padding(start = 8.dp),
                                                )
                                                Spacer(Modifier.width(8.dp))
                                                trackableIssue.issue
                                                    .issueContextLabel()
                                                    ?.let { label ->
                                                        Text(
                                                            text = label,
                                                            style = MaterialTheme.typography.bodyMedium,
                                                            color = MaterialTheme.colorScheme.secondary,
                                                        )
                                                        Spacer(Modifier.width(8.dp))
                                                    }
                                                Text(
                                                    text = trackableIssue.issue.errorMessage(project),
                                                    style = MaterialTheme.typography.titleSmall,
                                                    color = MaterialTheme.colorScheme.error,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis,
                                                    modifier = Modifier.weight(1f),
                                                )
                                            }
                                        }
                                    }, onClick = {
                                        onClosePanel()
                                        coroutineScope.launch {
                                            trackableIssue.onNavigateToTopLevelDestination(navigator)
                                        }
                                        onSetPendingFocus(
                                            destinationEntry.key,
                                            destinationWithContext,
                                        )
                                    })
                                }
                            }
                        }

                        else -> {}
                    }
                }
        }
    }
}
