package io.composeflow.model.project.appscreen.screen.composenode

import io.composeflow.kotlinpoet.GenerationContext
import io.composeflow.kotlinpoet.wrapper.CodeBlockWrapper
import io.composeflow.kotlinpoet.wrapper.MemberNameWrapper
import io.composeflow.model.enumwrapper.NodeVisibility
import io.composeflow.model.project.COMPOSEFLOW_PACKAGE
import io.composeflow.model.project.CanvasEditable
import io.composeflow.model.project.Project
import io.composeflow.model.project.issue.DestinationContext
import io.composeflow.model.project.issue.Issue
import io.composeflow.model.project.issue.TrackableIssue
import io.composeflow.model.property.AssignableProperty
import io.composeflow.model.property.BooleanProperty
import io.composeflow.model.property.EnumProperty
import io.composeflow.model.type.ComposeFlowType
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("VisibilityParams")
data class VisibilityParams(
    val nodeVisibility: AssignableProperty = EnumProperty(value = NodeVisibility.AlwaysVisible),
    val visibilityCondition: AssignableProperty = BooleanProperty.Empty,
    val formFactorVisibility: FormFactorVisibility = FormFactorVisibility(),
    val visibleInUiBuilder: Boolean = true,
) {
    fun nodeVisibilityValue(): NodeVisibility =
        when (nodeVisibility) {
            is EnumProperty -> {
                NodeVisibility.entries[nodeVisibility.value.enumValue().ordinal]
            }

            else -> NodeVisibility.AlwaysVisible
        }

    fun generateTrackableIssues(
        project: Project,
        canvasEditable: CanvasEditable,
        composeNode: ComposeNode,
    ): List<TrackableIssue> {
        if (visibilityCondition is BooleanProperty.Empty) return emptyList()
        return buildList {
            val transformedValueType = visibilityCondition.transformedValueType(project)
            if (!transformedValueType.isAbleToAssign(ComposeFlowType.BooleanType())) {
                add(
                    TrackableIssue(
                        destinationContext =
                            DestinationContext.UiBuilderScreen(
                                canvasEditableId = canvasEditable.id,
                                composeNodeId = composeNode.id,
                            ),
                        issue =
                            Issue.ResolvedToTypeNotAssignable(
                                property = visibilityCondition,
                                acceptableType = ComposeFlowType.BooleanType(),
                            ),
                    ),
                )
            }
            visibilityCondition
                .getAssignableProperties(includeSelf = false)
                .forEach { property ->
                    val transformedType = property.transformedValueType(project)
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
                                        property = property,
                                    ),
                            ),
                        )
                    }
                }
        }
    }

    fun alwaysVisible(): Boolean = isVisibilityConditionAlwaysVisible() && formFactorVisibility.alwaysVisible()

    fun generateVisibilityCondition(
        project: Project,
        context: GenerationContext,
        dryRun: Boolean,
    ): CodeBlockWrapper {
        val builder = CodeBlockWrapper.builder()
        if (!isVisibilityConditionAlwaysVisible()) {
            builder.add(
                visibilityCondition
                    .transformedCodeBlock(
                        project,
                        context,
                        ComposeFlowType.BooleanType(),
                        dryRun = dryRun,
                    ),
            )
        }
        if (!formFactorVisibility.alwaysVisible()) {
            if (!isVisibilityConditionAlwaysVisible()) {
                builder.add(" && ")
            }
            if (formFactorVisibility.visibleFormFactorCount() >= 2) {
                builder.add("(")
            }
            var needsOr = false
            val isCurrentWindowWidthSizeClassMember =
                MemberNameWrapper.get("${COMPOSEFLOW_PACKAGE}.platform", "isCurrentWindowWidthSizeClass")
            val windowWidthSizeClassMember =
                MemberNameWrapper.get("androidx.window.core.layout", "WindowWidthSizeClass")
            if (formFactorVisibility.visibleInCompact) {
                builder.add(
                    CodeBlockWrapper.of(
                        "%M(%M.COMPACT)",
                        isCurrentWindowWidthSizeClassMember,
                        windowWidthSizeClassMember,
                    ),
                )
                needsOr = true
            }

            if (formFactorVisibility.visibleInMedium) {
                if (needsOr) {
                    builder.add(" || ")
                }
                builder.add(
                    CodeBlockWrapper.of(
                        "%M(%M.MEDIUM)",
                        isCurrentWindowWidthSizeClassMember,
                        windowWidthSizeClassMember,
                    ),
                )
                needsOr = true
            }

            if (formFactorVisibility.visibleInExpanded) {
                if (needsOr) {
                    builder.add(" || ")
                }
                builder.add(
                    CodeBlockWrapper.of(
                        "%M(%M.EXPANDED)",
                        isCurrentWindowWidthSizeClassMember,
                        windowWidthSizeClassMember,
                    ),
                )
            }

            if (formFactorVisibility.visibleFormFactorCount() >= 2) {
                builder.add(")")
            }
        }

        return builder.build()
    }

    fun isDependent(sourceUuid: String): Boolean = visibilityCondition.isDependent(sourceUuid)

    private fun isVisibilityConditionAlwaysVisible(): Boolean =
        nodeVisibility == EnumProperty(value = NodeVisibility.AlwaysVisible) ||
            visibilityCondition == BooleanProperty.Empty
}

@Serializable
@SerialName("FormFactorVisibility")
data class FormFactorVisibility(
    val visibleInCompact: Boolean = true,
    val visibleInMedium: Boolean = true,
    val visibleInExpanded: Boolean = true,
) {
    fun visibleFormFactorCount(): Int {
        var count = 0
        if (visibleInCompact) count++
        if (visibleInMedium) count++
        if (visibleInExpanded) count++
        return count
    }

    fun alwaysVisible(): Boolean = visibleInCompact && visibleInMedium && visibleInExpanded
}
