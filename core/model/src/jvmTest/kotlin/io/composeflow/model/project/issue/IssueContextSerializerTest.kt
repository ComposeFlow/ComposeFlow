package io.composeflow.model.project.issue

import io.composeflow.model.action.Navigation
import io.composeflow.model.property.StringProperty
import io.composeflow.serializer.decodeFromStringWithFallback
import io.composeflow.serializer.encodeToString
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class IssueContextSerializerTest {
    @Test
    fun testSerializeTrackableIssueWithNullContext() {
        val issue =
            Issue.InvalidScreenReference(
                screenId = "screen-123",
                issueContext = null,
            )

        val trackableIssue =
            TrackableIssue(
                destinationContext =
                    DestinationContext.UiBuilderScreen(
                        canvasEditableId = "canvas-1",
                        composeNodeId = "node-1",
                    ),
                issue = issue,
            )

        val serialized = encodeToString(trackableIssue)
        val deserialized = decodeFromStringWithFallback<TrackableIssue>(serialized)

        assertTrue(deserialized.issue is Issue.InvalidScreenReference)
        assertEquals("screen-123", deserialized.issue.screenId)
        assertNull(deserialized.issue.issueContext)
    }

    @Test
    fun testSerializeTrackableIssueWithActionContext() {
        val action =
            Navigation.NavigateTo(
                id = "action-123",
                screenId = "target-screen",
            )

        val issue =
            Issue.InvalidScreenReference(
                screenId = "screen-456",
                issueContext = action,
            )

        val trackableIssue =
            TrackableIssue(
                destinationContext =
                    DestinationContext.UiBuilderScreen(
                        canvasEditableId = "canvas-1",
                        composeNodeId = "node-1",
                    ),
                issue = issue,
            )

        val serialized = encodeToString(trackableIssue)
        val deserialized = decodeFromStringWithFallback<TrackableIssue>(serialized)

        assertTrue(deserialized.issue is Issue.InvalidScreenReference)
        assertEquals("screen-456", deserialized.issue.screenId)

        // issueContext should be deserialized as Action
        assertTrue(deserialized.issue.issueContext is Navigation.NavigateTo)
        assertEquals("action-123", deserialized.issue.issueContext.id)
        assertEquals("target-screen", deserialized.issue.issueContext.screenId)
    }

    @Test
    fun testSerializeMultipleIssueTypesWithinTrackableIssue() {
        val destinationContext =
            DestinationContext.UiBuilderScreen(
                canvasEditableId = "canvas-1",
                composeNodeId = "node-1",
            )

        // Test ResolvedToUnknownType
        val issue1 =
            TrackableIssue(
                destinationContext = destinationContext,
                issue =
                    Issue.ResolvedToUnknownType(
                        property = StringProperty.StringIntrinsicValue("test"),
                        issueContext = null,
                    ),
            )
        val serialized1 = encodeToString(issue1)
        val deserialized1 = decodeFromStringWithFallback<TrackableIssue>(serialized1)
        assertTrue(deserialized1.issue is Issue.ResolvedToUnknownType)
        assertNull(deserialized1.issue.issueContext)

        // Test InvalidApiReference
        val issue2 =
            TrackableIssue(
                destinationContext = destinationContext,
                issue =
                    Issue.InvalidApiReference(
                        apiId = "api-789",
                        issueContext = null,
                    ),
            )
        val serialized2 = encodeToString(issue2)
        val deserialized2 = decodeFromStringWithFallback<TrackableIssue>(serialized2)
        assertTrue(deserialized2.issue is Issue.InvalidApiReference)
        assertEquals("api-789", deserialized2.issue.apiId)
        assertNull(deserialized2.issue.issueContext)

        // Test InvalidResourceReference
        val issue3 =
            TrackableIssue(
                destinationContext = destinationContext,
                issue =
                    Issue.InvalidResourceReference(
                        resourceType = "drawable",
                        issueContext = null,
                    ),
            )
        val serialized3 = encodeToString(issue3)
        val deserialized3 = decodeFromStringWithFallback<TrackableIssue>(serialized3)
        assertTrue(deserialized3.issue is Issue.InvalidResourceReference)
        assertEquals("drawable", deserialized3.issue.resourceType)
        assertNull(deserialized3.issue.issueContext)
    }

    @Test
    fun testSerializeTrackableIssueWithMultipleFields() {
        val action =
            Navigation.NavigateTo(
                id = "nav-action",
                screenId = "destination",
            )

        val issue =
            Issue.InvalidApiReference(
                apiId = "api-missing",
                issueContext = action,
            )

        val trackableIssue =
            TrackableIssue(
                destinationContext =
                    DestinationContext.UiBuilderScreen(
                        canvasEditableId = "screen-1",
                        composeNodeId = "node-1",
                    ),
                issue = issue,
            )

        val serialized = encodeToString(trackableIssue)
        val deserialized = decodeFromStringWithFallback<TrackableIssue>(serialized)

        assertTrue(deserialized.destinationContext is DestinationContext.UiBuilderScreen)
        assertTrue(deserialized.issue is Issue.InvalidApiReference)
        assertEquals("api-missing", deserialized.issue.apiId)

        // Verify issueContext was serialized and deserialized
        val deserializedIssue = deserialized.issue
        assertTrue(deserializedIssue.issueContext is Navigation.NavigateTo)
        assertEquals("nav-action", deserializedIssue.issueContext.id)
        assertEquals("destination", deserializedIssue.issueContext.screenId)
    }

    @Test
    fun testAllIssueSubclassesAreSerializableWithinTrackableIssue() {
        val action = Navigation.NavigateTo(id = "test-action", screenId = "test-screen")
        val destinationContext =
            DestinationContext.UiBuilderScreen(
                canvasEditableId = "canvas-1",
                composeNodeId = "node-1",
            )

        // Test all Issue subclasses can be serialized with Action context when wrapped in TrackableIssue
        val trackableIssues =
            listOf(
                TrackableIssue(
                    destinationContext = destinationContext,
                    issue =
                        Issue.ResolvedToUnknownType(
                            property = StringProperty.StringIntrinsicValue("value"),
                            issueContext = action,
                        ),
                ),
                TrackableIssue(
                    destinationContext = destinationContext,
                    issue =
                        Issue.InvalidScreenReference(
                            screenId = "screen-1",
                            issueContext = action,
                        ),
                ),
                TrackableIssue(
                    destinationContext = destinationContext,
                    issue =
                        Issue.InvalidApiReference(
                            apiId = "api-1",
                            issueContext = action,
                        ),
                ),
                TrackableIssue(
                    destinationContext = destinationContext,
                    issue =
                        Issue.InvalidAssetReference(
                            issueContext = action,
                        ),
                ),
                TrackableIssue(
                    destinationContext = destinationContext,
                    issue =
                        Issue.InvalidResourceReference(
                            resourceType = "string",
                            issueContext = action,
                        ),
                ),
                TrackableIssue(
                    destinationContext = destinationContext,
                    issue =
                        Issue.InvalidApiParameterReference(
                            issueContext = action,
                        ),
                ),
                TrackableIssue(
                    destinationContext = destinationContext,
                    issue =
                        Issue.NavigationDrawerIsNotSet(
                            issueContext = action,
                        ),
                ),
                TrackableIssue(
                    destinationContext = destinationContext,
                    issue =
                        Issue.InvalidModifierRelation(
                            issueContext = action,
                        ),
                ),
            )

        trackableIssues.forEach { trackableIssue ->
            val serialized = encodeToString(trackableIssue)
            val deserialized = decodeFromStringWithFallback<TrackableIssue>(serialized)

            // Verify the trackable issue was deserialized correctly
            assertEquals(trackableIssue.issue::class, deserialized.issue::class)
            // Verify issueContext was deserialized
            assertTrue(deserialized.issue.issueContext is Navigation.NavigateTo)
            assertEquals("test-action", (deserialized.issue.issueContext as Navigation.NavigateTo).id)
        }
    }
}
