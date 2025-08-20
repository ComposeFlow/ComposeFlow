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
        if (assignableProperty is ValueFromCompanionState) return emptyList()
        val transformedType =
            assignableProperty?.transformedValueType(project) ?: return emptyList()
        return buildList {
            project.findCanvasEditableHavingNodeOrNull(composeNode)?.let { canvasEditable ->
                if (transformedType is ComposeFlowType.UnknownType) {
                    add(
                        TrackableIssue(
                            destinationContext =
                                DestinationContext.UiBuilderScreen(
                                    canvasEditableId = canvasEditable.id,
                                    composeNodeId = composeNode.id,
                                ),
                            issue =
                                Issue.ResolvedToUnknownType(
                                    property = assignableProperty,
                                ),
                        ),
                    )
                } else if (!acceptableType.isAbleToAssign(transformedType)) {
                    add(
                        TrackableIssue(
                            destinationContext =
                                DestinationContext.UiBuilderScreen(
                                    canvasEditableId = canvasEditable.id,
                                    composeNodeId = composeNode.id,
                                ),
                            issue =
                                Issue.ResolvedToTypeNotAssignable(
                                    property = assignableProperty,
                                    acceptableType = acceptableType,
                                ),
                        ),
                    )
                }

                assignableProperty.checkResourceReference(project)?.let { issue ->
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

private fun AssignableProperty.checkResourceReference(project: Project): Issue? {
    if (this is StringProperty.ValueFromStringResource) {
        if (project.stringResourceHolder.stringResources.none { it.id == stringResourceId }) {
            return Issue.InvalidResourceReference(
                resourceType = "string",
            )
        }
    }
    return null
}
