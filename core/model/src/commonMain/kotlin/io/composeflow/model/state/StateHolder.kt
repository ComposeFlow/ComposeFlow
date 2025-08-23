package io.composeflow.model.state

import io.composeflow.model.project.Project
import io.composeflow.model.project.appscreen.screen.composenode.ComposeNode
import io.composeflow.override.mutableStateListEqualsOverrideOf
import io.composeflow.serializer.MutableStateListSerializer
import io.composeflow.util.generateUniqueName
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

typealias StateResult = Pair<StateHolderType, ReadableState>

interface StateHolder {
    fun getStateResults(project: Project): List<StateResult>

    fun getStates(project: Project): List<ReadableState>

    fun addState(readableState: ReadableState)

    fun updateState(readableState: ReadableState)

    /**
     * Create a unique label within the StateHolder
     * This will be used for the value for ComposeNode#label.
     * The value doesn't have to be unique, but to easily distinguish which companion state is
     * used, it's better to keep the label names unique within the StateHolder (the value of the
     * label is used for the companion state name)
     */
    fun createUniqueLabel(
        project: Project,
        composeNode: ComposeNode,
        initial: String,
    ): String

    fun findStateOrNull(
        project: Project,
        stateId: StateId,
    ): ReadableState?

    /**
     * Remove the state from the [StateHolder]  instance.
     * @return true if the state is successfully removed.
     */
    fun removeState(stateId: StateId): Boolean

    fun copyContents(other: StateHolder)
}

@Serializable
@SerialName("StateHolderImpl")
data class StateHolderImpl(
    @Serializable(MutableStateListSerializer::class)
    val states: MutableList<ReadableState> = mutableStateListEqualsOverrideOf(),
) : StateHolder {
    init {
        // Explicitly ignore the companion states if it's created from LLM.
        // It should be explicitly created, but to be on the safe side, filtering out here
        val nonCompanionStates = states.filter { !it.name.contains("-companionState") }
        states.clear()
        states.addAll(nonCompanionStates)
    }

    override fun getStates(project: Project): List<ReadableState> = states

    override fun getStateResults(project: Project): List<StateResult> = states.map { StateHolderType.Global to it }

    override fun addState(readableState: ReadableState) {
        states.add(readableState)
    }

    override fun createUniqueLabel(
        project: Project,
        composeNode: ComposeNode,
        initial: String,
    ): String =
        generateUniqueName(
            initial = initial,
            existing = getStates(project).map { it.name }.toSet(),
        )

    override fun findStateOrNull(
        project: Project,
        stateId: StateId,
    ): ReadableState? = getStates(project).firstOrNull { it.id == stateId }

    override fun removeState(stateId: StateId): Boolean {
        val toRemove = states.find { it.id == stateId }
        return if (toRemove != null) {
            states.remove(toRemove)
        } else {
            false
        }
    }

    override fun updateState(readableState: ReadableState) {
        val index = states.indexOfFirst { it.id == readableState.id }
        if (index != -1) {
            states[index] = readableState
        }
    }

    override fun copyContents(other: StateHolder) {
        states.clear()
        states.addAll((other as? StateHolderImpl)?.states ?: emptyList())
    }
}
