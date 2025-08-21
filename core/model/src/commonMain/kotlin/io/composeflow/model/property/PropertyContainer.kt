package io.composeflow.model.property

import io.composeflow.model.project.Project
import io.composeflow.model.project.appscreen.screen.composenode.ComposeNode
import io.composeflow.model.project.findCanvasEditableHavingNodeOrNull
import io.composeflow.model.project.issue.DestinationContext
import io.composeflow.model.project.issue.Issue
import io.composeflow.model.project.issue.TrackableIssue
import io.composeflow.model.type.ComposeFlowType

data class PropertyContainer(
    val label: String,
    val assignableProperty: AssignableProperty?,
    val acceptableType: ComposeFlowType,
) {
    fun generateTrackableIssues(
        project: Project,
        composeNode: ComposeNode,
    ): List<TrackableIssue> {
        if (assignableProperty == null || assignableProperty is ValueFromCompanionState) {
            return emptyList()
        }
        return buildList {
            project.findCanvasEditableHavingNodeOrNull(composeNode)?.let { canvasEditable ->
                assignableProperty.generateIssues(project, acceptableType).forEach { issue ->
                    add(
                        TrackableIssue(
                            destinationContext =
                                DestinationContext.UiBuilderScreen(
                                    canvasEditableId = canvasEditable.id,
                                    composeNodeId = composeNode.id,
                                ),
                            issue = issue,
                        ),
                    )
                }

                // Adds issues generated from the AssignableProperties derived from the property
                // except for the self  property because self property is considered above block
                assignableProperty
                    .getAssignableProperties(includeSelf = false)
                    .forEach { property ->
                        if (property.transformedValueType(project) is ComposeFlowType.UnknownType) {
                            add(
                                TrackableIssue(
                                    destinationContext =
                                        DestinationContext.UiBuilderScreen(
                                            canvasEditableId = canvasEditable.id,
                                            composeNodeId = composeNode.id,
                                        ),
                                    issue =
                                        Issue.ResolvedToUnknownType(
                                            property = property,
                                        ),
                                ),
                            )
                        }
                    }
            }
        }
    }
}
