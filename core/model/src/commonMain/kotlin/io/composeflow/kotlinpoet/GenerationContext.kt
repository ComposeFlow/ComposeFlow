package io.composeflow.kotlinpoet

import io.composeflow.kotlinpoet.wrapper.CodeBlockWrapper
import io.composeflow.kotlinpoet.wrapper.FunSpecWrapper
import io.composeflow.kotlinpoet.wrapper.PropertySpecWrapper
import io.composeflow.model.project.CanvasEditable
import io.composeflow.model.project.appscreen.screen.Screen

/**
 * Context passed to each code generator along the way to keep the information that needs to be
 * kept across the each code generator boundary.
 */
data class GenerationContext(
    /**
     * Holds how many times a specific Component is invoked to generate a key as a component's
     * function
     */
    val componentCountMap: MutableMap<String, Int> = mutableMapOf(),
    val generatedPlace: GeneratedPlace = GeneratedPlace.Unspecified,
    val viewModelMap: MutableMap<String, ComposeEditableContext> = mutableMapOf(),
    val currentEditable: CanvasEditable = Screen(name = ""),
) {
    fun addProperty(
        propertySpec: PropertySpecWrapper,
        dryRun: Boolean,
    ) {
        val composableContext = getCurrentComposableContext()
        composableContext.addProperty(propertySpec, dryRun = dryRun)
        viewModelMap[currentEditable.name] = composableContext
    }

    fun addPrioritizedProperty(
        propertySpec: PropertySpecWrapper,
        dryRun: Boolean,
    ) {
        val composableContext = getCurrentComposableContext()
        composableContext.addPrioritizedProperty(propertySpec, dryRun = dryRun)
        viewModelMap[currentEditable.name] = composableContext
    }

    fun addFunction(
        funSpec: FunSpecWrapper,
        dryRun: Boolean,
    ) {
        val composableContext = getCurrentComposableContext()
        composableContext.addFunction(funSpec, dryRun)
        viewModelMap[currentEditable.name] = composableContext
    }

    fun addFunctionInConstructor(
        funSpec: FunSpecWrapper,
        dryRun: Boolean,
    ) {
        val composableContext = getCurrentComposableContext()
        composableContext.addFunctionInConstructor(funSpec, dryRun)
        viewModelMap[currentEditable.name] = composableContext
    }

    fun addLaunchedEffectBlock(
        codeBlock: CodeBlockWrapper,
        dryRun: Boolean,
    ) {
        val composableContext = getCurrentComposableContext()
        composableContext.addLaunchedEffectBlock(codeBlock, dryRun)
        viewModelMap[currentEditable.name] = composableContext
    }

    fun getCurrentComposableContext(): ComposeEditableContext {
        val composableContext = viewModelMap[currentEditable.name]
        return if (composableContext != null) {
            composableContext
        } else {
            val newWrapper = currentEditable.newComposableContext()
            viewModelMap[currentEditable.name] = newWrapper
            newWrapper
        }
    }
}

enum class GeneratedPlace {
    ComposeScreen,
    ViewModel,
    Unspecified,
}
