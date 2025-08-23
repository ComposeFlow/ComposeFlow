package io.composeflow.model.action

import io.composeflow.kotlinpoet.GenerationContext
import io.composeflow.model.project.Project
import io.composeflow.model.project.appscreen.screen.Screen
import io.composeflow.model.property.BooleanProperty
import io.composeflow.trimForCompare
import junit.framework.TestCase.assertEquals
import kotlin.test.Test
import kotlin.test.assertTrue
import kotlin.uuid.Uuid

class ActionNodeTest {
    @Test
    fun testAllActions_simpleNode() {
        val emptyAction = ActionNode.Simple()

        assertEquals(emptyList<Action>(), emptyAction.allActions())

        val navigateBackAction = ActionNode.Simple(action = Navigation.NavigateBack)
        assertEquals(listOf(Navigation.NavigateBack), navigateBackAction.allActions())
    }

    @Test
    fun testAllActions_conditionalNode() {
        val navigateBack1 = ActionNode.Simple(action = Navigation.NavigateBack)
        val navigateTo1 =
            ActionNode.Simple(action = Navigation.NavigateTo(screenId = Uuid.random().toString()))
        val navigateBack2 = ActionNode.Simple(action = Navigation.NavigateBack)
        val navigateTo2 =
            ActionNode.Simple(
                action =
                    Navigation.NavigateTo(
                        screenId = Uuid.random().toString(),
                    ),
            )
        val setAppStateValue =
            ActionNode.Simple(
                action =
                    StateAction.SetAppStateValue(
                        setValueToStates =
                            mutableListOf(
                                SetValueToState(
                                    writeToStateId = Uuid.random().toString(),
                                    operation = StateOperation.ClearValue,
                                ),
                            ),
                    ),
            )
        val navigateTo3 =
            ActionNode.Simple(
                action = Navigation.NavigateTo(screenId = Uuid.random().toString()),
            )
        val conditionalNode =
            ActionNode.Conditional(
                trueNodes =
                    mutableListOf(
                        navigateBack1,
                        navigateTo1,
                    ),
                falseNodes =
                    mutableListOf(
                        navigateBack2,
                        navigateTo2,
                        ActionNode.Conditional(
                            trueNodes =
                                mutableListOf(
                                    setAppStateValue,
                                ),
                            falseNodes =
                                mutableListOf(
                                    navigateTo3,
                                ),
                        ),
                    ),
            )
        assertEquals(6, conditionalNode.allActions().size)
        assertTrue(
            conditionalNode.allActions().containsAll(
                listOf(
                    navigateBack1.getFocusedAction(),
                    navigateTo1.getFocusedAction(),
                    navigateBack2.getFocusedAction(),
                    navigateTo2.getFocusedAction(),
                    setAppStateValue.getFocusedAction(),
                    navigateTo3.getFocusedAction(),
                ),
            ),
        )
    }

    @Test
    fun testGenerateCodeBlockWithSimpleNode() {
        val navigateBack1 = ActionNode.Simple(action = Navigation.NavigateBack)
        val codeBlock =
            navigateBack1.generateCodeBlock(
                project = Project(),
                GenerationContext(),
                dryRun = false,
            )

        assertEquals("onNavigateBack()".trimForCompare(), codeBlock.toString().trimForCompare())
    }

    @Test
    fun testGenerateCodeBlockWithConditionalNode() {
        val project = Project()
        val screen1 = Screen(name = "screen1")
        val screen2 = Screen(name = "screen2")
        project.screenHolder.addScreen("screen1", screen1)
        project.screenHolder.addScreen("screen2", screen2)

        val navigateBack1 = ActionNode.Simple(action = Navigation.NavigateBack)
        val navigateBack2 = ActionNode.Simple(action = Navigation.NavigateBack)
        val conditionalNode =
            ActionNode.Conditional(
                ifCondition = BooleanProperty.BooleanIntrinsicValue(true),
                trueNodes =
                    mutableListOf(
                        navigateBack1,
                    ),
                falseNodes =
                    mutableListOf(
                        navigateBack2,
                        ActionNode.Conditional(
                            ifCondition = BooleanProperty.BooleanIntrinsicValue(false),
                            trueNodes =
                                mutableListOf(
                                    navigateBack2,
                                ),
                        ),
                    ),
            )
        val codeBlock =
            conditionalNode.generateCodeBlock(
                project = Project(),
                GenerationContext(),
                dryRun = false,
            )
        assertEquals(
            """
            if (true) {
                onNavigateBack()
            } else {
                onNavigateBack()
                if (false) {
                    onNavigateBack()
                }
            }
        """.trimForCompare(),
            codeBlock.toString().trimForCompare(),
        )
    }
}
