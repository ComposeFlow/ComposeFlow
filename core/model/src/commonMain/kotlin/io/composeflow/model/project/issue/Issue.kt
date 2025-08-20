package io.composeflow.model.project.issue

import androidx.compose.runtime.Composable
import io.composeflow.Res
import io.composeflow.invalid_api_parameter_reference
import io.composeflow.invalid_api_reference
import io.composeflow.invalid_asset_reference
import io.composeflow.invalid_modifier_wrong_parent_relation
import io.composeflow.invalid_reference
import io.composeflow.invalid_resource_reference
import io.composeflow.invalid_screen_reference
import io.composeflow.model.InspectorTabDestination
import io.composeflow.model.action.Action
import io.composeflow.model.apieditor.ApiId
import io.composeflow.model.project.Project
import io.composeflow.model.project.appscreen.screen.ScreenId
import io.composeflow.model.property.AssignableProperty
import io.composeflow.model.type.ComposeFlowType
import io.composeflow.not_assignable_to
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import moe.tlaster.precompose.navigation.Navigator
import org.jetbrains.compose.resources.stringResource

@Serializable
@SerialName("TrackableIssue")
data class TrackableIssue(
    val destinationContext: DestinationContext,
    val issue: Issue,
) {
    suspend fun onNavigateToTopLevelDestination(navigator: Navigator) {
        navigator.currentEntry.collect {
            if (it?.route?.route != issue.destination.destinationToNavigate().route) {
                navigator.navigate(issue.destination.destinationToNavigate().route)
            }
        }
    }
}

@Serializable
@SerialName("Issue")
sealed interface Issue {
    val destination: NavigatableDestination

    /**
     * Context where this issue is generated. E.g. Action
     */
    val issueContext: Any?

    fun issueContextLabel(): String? =
        when (val context = issueContext) {
            is Action -> {
                "(Action: ${context.name})"
            }

            else -> null
        }

    @Composable
    fun errorMessage(project: Project): String

    data class ResolvedToUnknownType(
        val property: AssignableProperty,
        override val destination: NavigatableDestination =
            NavigatableDestination.UiBuilderScreen(
                inspectorTabDestination = InspectorTabDestination.Inspector,
            ),
        override val issueContext: Any? = null,
    ) : Issue {
        @Composable
        override fun errorMessage(project: Project): String =
            stringResource(Res.string.invalid_reference) + " " +
                property.transformedValueExpression(
                    project,
                )
    }

    data class ResolvedToTypeNotAssignable(
        val property: AssignableProperty,
        val acceptableType: ComposeFlowType,
        override val destination: NavigatableDestination =
            NavigatableDestination.UiBuilderScreen(
                inspectorTabDestination = InspectorTabDestination.Inspector,
            ),
        override val issueContext: Any? = null,
    ) : Issue {
        @Composable
        override fun errorMessage(project: Project): String =
            stringResource(Res.string.not_assignable_to) + " " +
                acceptableType.displayName(
                    project,
                )
    }

    data class InvalidScreenReference(
        val screenId: ScreenId,
        override val destination: NavigatableDestination =
            NavigatableDestination.UiBuilderScreen(
                inspectorTabDestination = InspectorTabDestination.Inspector,
            ),
        override val issueContext: Any? = null,
    ) : Issue {
        @Composable
        override fun errorMessage(project: Project): String = stringResource(Res.string.invalid_screen_reference)
    }

    data class InvalidApiReference(
        val apiId: ApiId,
        override val destination: NavigatableDestination =
            NavigatableDestination.UiBuilderScreen(
                inspectorTabDestination = InspectorTabDestination.Inspector,
            ),
        override val issueContext: Any? = null,
    ) : Issue {
        @Composable
        override fun errorMessage(project: Project): String = stringResource(Res.string.invalid_api_reference)
    }

    data class InvalidAssetReference(
        override val destination: NavigatableDestination =
            NavigatableDestination.UiBuilderScreen(
                inspectorTabDestination = InspectorTabDestination.Inspector,
            ),
        override val issueContext: Any? = null,
    ) : Issue {
        @Composable
        override fun errorMessage(project: Project): String = stringResource(Res.string.invalid_asset_reference)
    }

    data class InvalidResourceReference(
        val resourceType: String = "string",
        override val destination: NavigatableDestination =
            NavigatableDestination.UiBuilderScreen(
                inspectorTabDestination = InspectorTabDestination.Inspector,
            ),
        override val issueContext: Any? = null,
    ) : Issue {
        @Composable
        override fun errorMessage(project: Project): String = stringResource(Res.string.invalid_resource_reference, resourceType)
    }

    data class InvalidApiParameterReference(
        override val destination: NavigatableDestination = NavigatableDestination.ApiEditorScreen,
        override val issueContext: Any? = null,
    ) : Issue {
        @Composable
        override fun errorMessage(project: Project): String = stringResource(Res.string.invalid_api_parameter_reference)
    }

    data class NavigationDrawerIsNotSet(
        override val destination: NavigatableDestination = NavigatableDestination.UiBuilderScreen(),
        override val issueContext: Any? = null,
    ) : Issue {
        @Composable
        override fun errorMessage(project: Project): String = stringResource(Res.string.invalid_api_parameter_reference)
    }

    data class InvalidModifierRelation(
        override val destination: NavigatableDestination = NavigatableDestination.UiBuilderScreen(),
        override val issueContext: Any? = null,
    ) : Issue {
        @Composable
        override fun errorMessage(project: Project): String = stringResource(Res.string.invalid_modifier_wrong_parent_relation)
    }
}
